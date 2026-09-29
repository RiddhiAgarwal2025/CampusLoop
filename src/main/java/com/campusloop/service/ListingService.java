package com.campusloop.service;

import com.campusloop.dao.CartDao;
import com.campusloop.dao.CategoryDao;
import com.campusloop.dao.ListingDao;
import com.campusloop.dao.TransactionDao;
import com.campusloop.model.Category;
import com.campusloop.model.ItemCondition;
import com.campusloop.model.Listing;
import com.campusloop.model.ListingForm;
import com.campusloop.model.ListingStatus;
import com.campusloop.util.DbUtil;
import com.campusloop.util.ImageStorage;
import com.campusloop.util.Params;
import com.campusloop.util.Validator;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.Part;

/**
 * Seller-side listing rules: create (photo mandatory), edit, remove, "My Listings".
 *
 * Ownership is always checked against the signed-in student's id from the session;
 * ids arriving in URLs or hidden fields are only used to look records up.
 */
public class ListingService {

    private final ListingDao listingDao = new ListingDao();
    private final CategoryDao categoryDao = new CategoryDao();
    private final TransactionDao transactionDao = new TransactionDao();
    private final CartDao cartDao = new CartDao();

    /** Parsed, validated values of a listing form. */
    private static final class Valid {
        int categoryId;
        String title;
        String description;
        BigDecimal price;
        ItemCondition condition;
    }

    // ------------------------------------------------------------------ create

    /** Creates an AVAILABLE listing. The photo is mandatory. Returns the new listing id. */
    public int create(int sellerId, ListingForm form, Part image) throws ServiceException, SQLException, IOException {
        Map<String, String> errors = new LinkedHashMap<>();
        Valid v;
        try (Connection conn = DbUtil.getConnection()) {
            v = validate(conn, form, errors);
        }
        if (!ImageStorage.hasFile(image)) {
            errors.put("image", "A photo is required. Please add a clear picture of your item.");
        } else if (!errors.isEmpty()) {
            errors.put("image", "Please choose your photo again. For security, browsers don't keep a selected "
                    + "file after a form error.");
        }
        ValidationException.throwIfAny(errors);

        String imagePath = ImageStorage.store(image, ImageStorage.Kind.LISTINGS, "image");
        try (Connection conn = DbUtil.getConnection()) {
            return listingDao.insert(conn, sellerId, v.categoryId, v.title, v.description, v.price,
                    v.condition, imagePath);
        } catch (SQLException e) {
            ImageStorage.deleteQuietly(imagePath);
            throw e;
        }
    }

    // ------------------------------------------------------------------ edit

    /** The listing if (and only if) it belongs to {@code sellerId} and can still be edited. */
    public Listing getForEdit(int sellerId, int listingId) throws ServiceException, SQLException {
        Listing listing;
        try (Connection conn = DbUtil.getConnection()) {
            listing = listingDao.findById(conn, listingId);
        }
        requireOwner(listing, sellerId);
        if (!listing.isEditable()) {
            throw new ServiceException(notEditableMessage(listing.getStatus()));
        }
        return listing;
    }

    /**
     * Updates an owned AVAILABLE listing. The photo is optional here; if a new one is
     * supplied it replaces the old file, which is deleted after the update commits.
     */
    public void update(int sellerId, int listingId, ListingForm form, Part image)
            throws ServiceException, SQLException, IOException {
        Listing current = getForEdit(sellerId, listingId);

        Map<String, String> errors = new LinkedHashMap<>();
        Valid v;
        try (Connection conn = DbUtil.getConnection()) {
            v = validate(conn, form, errors);
        }
        boolean replacing = ImageStorage.hasFile(image);
        if (replacing && !errors.isEmpty()) {
            errors.put("image", "Please choose your new photo again. Your current photo has been kept.");
        }
        ValidationException.throwIfAny(errors);

        final String newImage = replacing ? ImageStorage.store(image, ImageStorage.Kind.LISTINGS, "image") : null;
        final Valid valid = v;
        try {
            DbUtil.inTransaction(conn -> {
                Listing locked = listingDao.lockById(conn, listingId);
                requireOwner(locked, sellerId);
                if (locked.getStatus() != ListingStatus.AVAILABLE) {
                    throw new ServiceException(notEditableMessage(locked.getStatus()));
                }
                int rows = listingDao.update(conn, listingId, sellerId, valid.categoryId, valid.title,
                        valid.description, valid.price, valid.condition,
                        newImage != null ? newImage : locked.getImageUrl());
                if (rows != 1) {
                    throw new ServiceException("This listing could not be updated. Please try again.");
                }
                return null;
            });
        } catch (ServiceException | SQLException | RuntimeException e) {
            ImageStorage.deleteQuietly(newImage);
            throw e;
        }
        if (newImage != null) {
            ImageStorage.deleteQuietly(current.getImageUrl());
        }
    }

    // ------------------------------------------------------------------ remove

    /**
     * Soft-deletes an owned AVAILABLE listing: status becomes REMOVED, pending
     * exchange requests for it are cancelled and it is taken out of every cart, all
     * in one database transaction. Sold history is never deleted.
     *
     * @return the number of pending requests that were cancelled
     */
    public int remove(int sellerId, int listingId) throws ServiceException, SQLException {
        return DbUtil.inTransaction(conn -> {
            Listing locked = listingDao.lockById(conn, listingId);
            requireOwner(locked, sellerId);
            switch (locked.getStatus()) {
                case AVAILABLE:
                    break;
                case RESERVED:
                    throw new ServiceException("You've confirmed an exchange for this item. Release the "
                            + "reservation from Sales before removing the listing.");
                case SOLD:
                    throw new ServiceException("Sold items stay in your history and can't be removed.");
                default:
                    throw new ServiceException("This listing has already been removed.");
            }
            listingDao.updateStatus(conn, listingId, ListingStatus.REMOVED);
            int cancelled = transactionDao.cancelOtherActiveRequests(conn, listingId, 0);
            cartDao.deleteByListing(conn, listingId);
            return cancelled;
        });
    }

    // ------------------------------------------------------------------ my listings

    public List<Listing> myListings(int sellerId) throws SQLException {
        try (Connection conn = DbUtil.getConnection()) {
            return listingDao.listBySeller(conn, sellerId);
        }
    }

    public Map<Integer, Integer> pendingRequestCounts(int sellerId) throws SQLException {
        try (Connection conn = DbUtil.getConnection()) {
            return transactionDao.awaitingSellerCountsByListing(conn, sellerId);
        }
    }

    // ------------------------------------------------------------------ helpers

    private static void requireOwner(Listing listing, int sellerId) throws ServiceException {
        if (listing == null || (listing.getStatus() == ListingStatus.REMOVED && listing.getSellerId() != sellerId)) {
            throw new NotFoundException("This listing doesn't exist.");
        }
        if (listing.getSellerId() != sellerId) {
            throw new ForbiddenException("You can only manage your own listings.");
        }
    }

    private static String notEditableMessage(ListingStatus status) {
        switch (status) {
            case RESERVED:
                return "This item is reserved for a confirmed exchange, so it can't be edited right now.";
            case SOLD:
                return "Sold items can't be edited.";
            default:
                return "Removed listings can't be edited.";
        }
    }

    private Valid validate(Connection conn, ListingForm form, Map<String, String> errors) throws SQLException {
        Valid v = new Valid();

        v.title = Validator.clean(form.getTitle()).replaceAll("\\s+", " ");
        if (v.title.isEmpty()) {
            errors.put("title", "Please give your item a title.");
        } else if (!Validator.lengthBetween(v.title, Validator.TITLE_MIN, Validator.TITLE_MAX)) {
            errors.put("title", "The title must be " + Validator.TITLE_MIN + "-" + Validator.TITLE_MAX + " characters.");
        }

        v.description = Validator.clean(form.getDescription());
        if (v.description.isEmpty()) {
            errors.put("description", "Please describe your item.");
        } else if (!Validator.lengthBetween(v.description, Validator.DESCRIPTION_MIN, Validator.DESCRIPTION_MAX)) {
            errors.put("description", "The description must be " + Validator.DESCRIPTION_MIN + "-"
                    + Validator.DESCRIPTION_MAX + " characters.");
        }

        Integer categoryId = Params.positiveInt(form.getCategoryId());
        Category category = categoryId == null ? null : categoryDao.findById(conn, categoryId);
        if (category == null) {
            errors.put("categoryId", "Please choose a category from the list.");
        } else {
            v.categoryId = category.getId();
        }

        String priceText = Validator.clean(form.getPrice());
        v.price = Validator.parseMoney(priceText);
        if (priceText.isEmpty()) {
            errors.put("price", "Please enter a price.");
        } else if (v.price == null) {
            errors.put("price", "Enter the price in rupees, e.g. 450 or 450.50.");
        } else if (!Validator.isValidPrice(v.price)) {
            errors.put("price", "The price must be between \u20B910 and \u20B91,00,000.");
        }

        v.condition = ItemCondition.parse(form.getCondition());
        if (v.condition == null) {
            errors.put("condition", "Please choose the item's condition.");
        }
        return v;
    }
}
