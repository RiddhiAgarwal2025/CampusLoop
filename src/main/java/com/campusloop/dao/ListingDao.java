package com.campusloop.dao;

import com.campusloop.model.ItemCondition;
import com.campusloop.model.Listing;
import com.campusloop.model.ListingFilter;
import com.campusloop.model.ListingStatus;
import com.campusloop.model.PagedResult;
import com.campusloop.util.DbUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC access to the listings table.
 *
 * Read queries join the seller, the category and the seller_rating_summary view,
 * so every card shows a rating computed from real seller_reviews rows.
 */
public class ListingDao {

    private static final String SELECT_FULL =
            "SELECT l.listing_id, l.seller_id, l.category_id, l.title, l.description, l.price, "
            + "l.item_condition, l.image_url, l.status, l.created_at, l.updated_at, "
            + "s.name AS seller_name, s.profile_image AS seller_image, "
            + "c.name AS category_name, c.slug AS category_slug, "
            + "COALESCE(r.avg_rating, 0) AS seller_rating, COALESCE(r.review_count, 0) AS seller_review_count "
            + "FROM listings l "
            + "JOIN students s ON s.student_id = l.seller_id "
            + "JOIN categories c ON c.category_id = l.category_id "
            + "LEFT JOIN seller_rating_summary r ON r.seller_id = l.seller_id ";

    private static final String SELECT_BASIC =
            "SELECT listing_id, seller_id, category_id, title, description, price, item_condition, "
            + "image_url, status, created_at, updated_at FROM listings ";

    private static final int MAX_SEARCH_TERMS = 5;

    // ------------------------------------------------------------------ reads

    /** Marketplace search: AVAILABLE listings only, with filters, sort and paging. */
    public PagedResult<Listing> search(Connection conn, ListingFilter f) throws SQLException {
        StringBuilder where = new StringBuilder("WHERE l.status = 'AVAILABLE' ");
        List<Object> params = new ArrayList<>();

        String[] terms = f.getQuery().trim().isEmpty() ? new String[0] : f.getQuery().trim().split("\\s+");
        for (int i = 0; i < terms.length && i < MAX_SEARCH_TERMS; i++) {
            where.append("AND (l.title LIKE ? OR l.description LIKE ? OR c.name LIKE ?) ");
            String like = DbUtil.likeContains(terms[i]);
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (!f.getCategorySlug().isEmpty()) {
            where.append("AND c.slug = ? ");
            params.add(f.getCategorySlug());
        }
        if (f.getMinPrice() != null) {
            where.append("AND l.price >= ? ");
            params.add(f.getMinPrice());
        }
        if (f.getMaxPrice() != null) {
            where.append("AND l.price <= ? ");
            params.add(f.getMaxPrice());
        }
        if (f.getCondition() != null) {
            where.append("AND l.item_condition = ? ");
            params.add(f.getCondition().name());
        }

        int total;
        String countSql = "SELECT COUNT(*) FROM listings l JOIN categories c ON c.category_id = l.category_id " + where;
        try (PreparedStatement ps = conn.prepareStatement(countSql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                total = rs.getInt(1);
            }
        }

        String order;
        if (ListingFilter.SORT_PRICE_ASC.equals(f.getSort())) {
            order = "ORDER BY l.price ASC, l.listing_id DESC ";
        } else if (ListingFilter.SORT_PRICE_DESC.equals(f.getSort())) {
            order = "ORDER BY l.price DESC, l.listing_id DESC ";
        } else {
            order = "ORDER BY l.created_at DESC, l.listing_id DESC ";
        }

        List<Listing> items = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_FULL + where + order + "LIMIT ? OFFSET ?")) {
            List<Object> all = new ArrayList<>(params);
            all.add(f.getPageSize());
            all.add(f.getOffset());
            bind(ps, all);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(mapFull(rs));
                }
            }
        }
        return new PagedResult<>(items, f.getPage(), f.getPageSize(), total);
    }

    public Listing findById(Connection conn, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_FULL + "WHERE l.listing_id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapFull(rs) : null;
            }
        }
    }

    /**
     * "Trending on Campus": AVAILABLE listings that the most students currently have
     * in their carts, newest first as a tie-breaker. Derived entirely from real rows.
     */
    public List<Listing> trending(Connection conn, int limit) throws SQLException {
        String sql = SELECT_FULL + "WHERE l.status = 'AVAILABLE' "
                + "ORDER BY (SELECT COUNT(*) FROM cart_items ci WHERE ci.listing_id = l.listing_id) DESC, "
                + "l.created_at DESC, l.listing_id DESC LIMIT ?";
        return list(conn, sql, limit);
    }

    /** AVAILABLE listings of one seller (public seller page, "more from this seller"). */
    public List<Listing> listAvailableBySeller(Connection conn, int sellerId, int excludeListingId, int limit)
            throws SQLException {
        String sql = SELECT_FULL + "WHERE l.seller_id = ? AND l.status = 'AVAILABLE' AND l.listing_id <> ? "
                + "ORDER BY l.created_at DESC, l.listing_id DESC LIMIT ?";
        return list(conn, sql, sellerId, excludeListingId, limit);
    }

    /** Every non-removed listing of a seller, for "My Listings" (owner only). */
    public List<Listing> listBySeller(Connection conn, int sellerId) throws SQLException {
        String sql = SELECT_FULL + "WHERE l.seller_id = ? AND l.status <> 'REMOVED' "
                + "ORDER BY FIELD(l.status, 'RESERVED', 'AVAILABLE', 'SOLD'), l.created_at DESC, l.listing_id DESC";
        return list(conn, sql, sellerId);
    }

    // ------------------------------------------------------------------ writes

    public int insert(Connection conn, int sellerId, int categoryId, String title, String description,
                      BigDecimal price, ItemCondition condition, String imageUrl) throws SQLException {
        String sql = "INSERT INTO listings (seller_id, category_id, title, description, price, item_condition, "
                + "image_url, status) VALUES (?, ?, ?, ?, ?, ?, ?, 'AVAILABLE')";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, sellerId);
            ps.setInt(2, categoryId);
            ps.setString(3, title);
            ps.setString(4, description);
            ps.setBigDecimal(5, price);
            ps.setString(6, condition.name());
            ps.setString(7, imageUrl);
            ps.executeUpdate();
            return DbUtil.generatedId(ps);
        }
    }

    /**
     * Updates an AVAILABLE listing owned by {@code sellerId}. The ownership and status
     * conditions are part of the WHERE clause, so a forged id can never update
     * another student's listing. Returns the number of rows changed (0 or 1).
     */
    public int update(Connection conn, int listingId, int sellerId, int categoryId, String title,
                      String description, BigDecimal price, ItemCondition condition, String imageUrl)
            throws SQLException {
        String sql = "UPDATE listings SET category_id = ?, title = ?, description = ?, price = ?, "
                + "item_condition = ?, image_url = ? "
                + "WHERE listing_id = ? AND seller_id = ? AND status = 'AVAILABLE'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            ps.setString(2, title);
            ps.setString(3, description);
            ps.setBigDecimal(4, price);
            ps.setString(5, condition.name());
            ps.setString(6, imageUrl);
            ps.setInt(7, listingId);
            ps.setInt(8, sellerId);
            return ps.executeUpdate();
        }
    }

    /** Locks one listing row (SELECT ... FOR UPDATE) inside the caller's transaction. */
    public Listing lockById(Connection conn, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BASIC + "WHERE listing_id = ? FOR UPDATE")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapBasic(rs) : null;
            }
        }
    }

    public void updateStatus(Connection conn, int id, ListingStatus status) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE listings SET status = ? WHERE listing_id = ?")) {
            ps.setString(1, status.name());
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    // ------------------------------------------------------------------ helpers

    private List<Listing> list(Connection conn, String sql, Object... params) throws SQLException {
        List<Listing> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            List<Object> p = new ArrayList<>();
            for (Object o : params) {
                p.add(o);
            }
            bind(ps, p);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(mapFull(rs));
                }
            }
        }
        return out;
    }

    private static void bind(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object v = params.get(i);
            if (v instanceof Integer) {
                ps.setInt(i + 1, (Integer) v);
            } else if (v instanceof BigDecimal) {
                ps.setBigDecimal(i + 1, (BigDecimal) v);
            } else {
                ps.setString(i + 1, String.valueOf(v));
            }
        }
    }

    private Listing mapBasic(ResultSet rs) throws SQLException {
        Listing l = new Listing();
        l.setId(rs.getInt("listing_id"));
        l.setSellerId(rs.getInt("seller_id"));
        l.setCategoryId(rs.getInt("category_id"));
        l.setTitle(rs.getString("title"));
        l.setDescription(rs.getString("description"));
        l.setPrice(rs.getBigDecimal("price"));
        l.setCondition(ItemCondition.valueOf(rs.getString("item_condition")));
        l.setImageUrl(rs.getString("image_url"));
        l.setStatus(ListingStatus.valueOf(rs.getString("status")));
        l.setCreatedAt(DbUtil.getDateTime(rs, "created_at"));
        l.setUpdatedAt(DbUtil.getDateTime(rs, "updated_at"));
        return l;
    }

    private Listing mapFull(ResultSet rs) throws SQLException {
        Listing l = mapBasic(rs);
        l.setSellerName(rs.getString("seller_name"));
        l.setSellerImage(rs.getString("seller_image"));
        l.setCategoryName(rs.getString("category_name"));
        l.setCategorySlug(rs.getString("category_slug"));
        l.setSellerRating(rs.getDouble("seller_rating"));
        l.setSellerReviewCount(rs.getInt("seller_review_count"));
        return l;
    }
}
