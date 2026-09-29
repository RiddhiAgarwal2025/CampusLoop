package com.campusloop.dao;

import com.campusloop.model.Category;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** JDBC access to the categories table. */
public class CategoryDao {

    /** All categories in display order, each with its number of AVAILABLE listings. */
    public List<Category> listWithAvailableCounts(Connection conn) throws SQLException {
        String sql = "SELECT c.category_id, c.name, c.slug, c.description, c.display_order, "
                + "COUNT(l.listing_id) AS available_count "
                + "FROM categories c "
                + "LEFT JOIN listings l ON l.category_id = c.category_id AND l.status = 'AVAILABLE' "
                + "GROUP BY c.category_id, c.name, c.slug, c.description, c.display_order "
                + "ORDER BY c.display_order, c.name";
        List<Category> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Category c = map(rs);
                c.setAvailableCount(rs.getInt("available_count"));
                list.add(c);
            }
        }
        return list;
    }

    public Category findById(Connection conn, int id) throws SQLException {
        String sql = "SELECT category_id, name, slug, description, display_order FROM categories WHERE category_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    private Category map(ResultSet rs) throws SQLException {
        Category c = new Category();
        c.setId(rs.getInt("category_id"));
        c.setName(rs.getString("name"));
        c.setSlug(rs.getString("slug"));
        c.setDescription(rs.getString("description"));
        c.setDisplayOrder(rs.getInt("display_order"));
        return c;
    }
}
