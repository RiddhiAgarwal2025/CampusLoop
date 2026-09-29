package com.campusloop.service;

import com.campusloop.dao.CategoryDao;
import com.campusloop.dao.ListingDao;
import com.campusloop.dao.ReviewDao;
import com.campusloop.dao.StatsDao;
import com.campusloop.dao.StudentDao;
import com.campusloop.model.Category;
import com.campusloop.model.HomeSummary;
import com.campusloop.model.Listing;
import com.campusloop.model.ListingFilter;
import com.campusloop.model.ListingStatus;
import com.campusloop.model.PagedResult;
import com.campusloop.model.ProductView;
import com.campusloop.model.SellerProfile;
import com.campusloop.model.SellerRating;
import com.campusloop.model.Student;
import com.campusloop.util.DbUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** Read side of the marketplace: home, browse/search, product detail, public seller page. */
public class MarketService {

    public static final int TRENDING_SIZE = 8;

    private final ListingDao listingDao = new ListingDao();
    private final CategoryDao categoryDao = new CategoryDao();
    private final ReviewDao reviewDao = new ReviewDao();
    private final StatsDao statsDao = new StatsDao();
    private final StudentDao studentDao = new StudentDao();

    public HomeSummary home() throws SQLException {
        try (Connection conn = DbUtil.getConnection()) {
            return new HomeSummary(
                    listingDao.trending(conn, TRENDING_SIZE),
                    categoryDao.listWithAvailableCounts(conn),
                    statsDao.itemsRecirculated(conn),
                    statsDao.availableListings(conn),
                    statsDao.registeredStudents(conn));
        }
    }

    public int itemsRecirculated() throws SQLException {
        try (Connection conn = DbUtil.getConnection()) {
            return statsDao.itemsRecirculated(conn);
        }
    }

    public List<Category> categories() throws SQLException {
        try (Connection conn = DbUtil.getConnection()) {
            return categoryDao.listWithAvailableCounts(conn);
        }
    }

    public PagedResult<Listing> search(ListingFilter filter) throws SQLException {
        try (Connection conn = DbUtil.getConnection()) {
            return listingDao.search(conn, filter);
        }
    }

    /**
     * Product detail. REMOVED listings are visible only to their seller; everyone
     * else gets "not found". SOLD and RESERVED items stay visible with their status.
     */
    public ProductView product(int listingId, Integer viewerId) throws SQLException, NotFoundException {
        try (Connection conn = DbUtil.getConnection()) {
            Listing listing = listingDao.findById(conn, listingId);
            boolean owner = listing != null && viewerId != null && listing.getSellerId() == viewerId;
            if (listing == null || (listing.getStatus() == ListingStatus.REMOVED && !owner)) {
                throw new NotFoundException("This listing doesn't exist or has been removed by its seller.");
            }
            SellerRating rating = reviewDao.ratingForSeller(conn, listing.getSellerId());
            return new ProductView(listing, rating,
                    reviewDao.listForSeller(conn, listing.getSellerId(), 3),
                    listingDao.listAvailableBySeller(conn, listing.getSellerId(), listing.getId(), 4),
                    owner);
        }
    }

    public SellerProfile seller(int sellerId) throws SQLException, NotFoundException {
        try (Connection conn = DbUtil.getConnection()) {
            Student s = studentDao.findById(conn, sellerId);
            if (s == null) {
                throw new NotFoundException("We couldn't find that student.");
            }
            return new SellerProfile(s.getId(), s.getName(), s.getProfileImage(), s.getCreatedAt(),
                    reviewDao.ratingForSeller(conn, sellerId),
                    statsDao.completedSales(conn, sellerId),
                    listingDao.listAvailableBySeller(conn, sellerId, 0, 48),
                    reviewDao.listForSeller(conn, sellerId, 20));
        }
    }
}
