package com.example.capstone.database;

import com.example.capstone.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    private final Connection connection;

    public ProductDAO() {
        connection = DBConnection.getInstance().getConnection();
    }


    /* ── Helper: map ResultSet row → Product ── */
    private Product mapRow(ResultSet rs) throws SQLException {
        return new Product(
                rs.getInt("product_id"),
                rs.getInt("category_id"),
                rs.getInt("supplier_id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getInt("quantity"),
                rs.getDouble("price"),
                rs.getInt("low_stock_threshold"),
                rs.getString("category_name"),
                rs.getString("supplier_name")
        );
    }

    /* Base SELECT with JOINs so category/supplier names come along */
    private static final String BASE_SELECT =
            "SELECT p.*, " +
                    "  COALESCE(c.name, '—') AS category_name, " +
                    "  COALESCE(s.name, '—') AS supplier_name  " +
                    "FROM products p " +
                    "LEFT JOIN categories c ON p.category_id = c.category_id " +
                    "LEFT JOIN suppliers  s ON p.supplier_id  = s.supplier_id ";


    /* INSERT */
    public boolean insertProduct(Product product) {

        String sql = "INSERT INTO products " +
                "(category_id, supplier_id, name, description, quantity, price, low_stock_threshold) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, product.getCategoryId());
            ps.setInt(2, product.getSupplierId());
            ps.setString(3, product.getName());
            ps.setString(4, product.getDescription());
            ps.setInt(5, product.getQuantity());
            ps.setDouble(6, product.getPrice());
            ps.setInt(7, product.getLowStockThreshold());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    /* UPDATE */
    public boolean updateProduct(Product product) {

        String sql = "UPDATE products SET " +
                "category_id=?, supplier_id=?, name=?, description=?, " +
                "quantity=?, price=?, low_stock_threshold=? " +
                "WHERE product_id=?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, product.getCategoryId());
            ps.setInt(2, product.getSupplierId());
            ps.setString(3, product.getName());
            ps.setString(4, product.getDescription());
            ps.setInt(5, product.getQuantity());
            ps.setDouble(6, product.getPrice());
            ps.setInt(7, product.getLowStockThreshold());
            ps.setInt(8, product.getProductId());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    /* DELETE */
    public boolean deleteProduct(int productId) {

        String sql = "DELETE FROM products WHERE product_id=?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, productId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    /* SELECT ALL */
    public List<Product> getAllProducts() {

        List<Product> list = new ArrayList<>();

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(BASE_SELECT + "ORDER BY p.name")) {

            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }


    /* SEARCH by name */
    public List<Product> searchProducts(String keyword) {

        List<Product> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE p.name LIKE ? ORDER BY p.name";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }


    /* FILTER by category */
    public List<Product> getByCategory(int categoryId) {

        List<Product> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE p.category_id = ? ORDER BY p.name";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, categoryId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }


    /* LOW STOCK — uses per-product threshold from DB */
    public List<Product> getLowStockProducts() {

        List<Product> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE p.quantity < p.low_stock_threshold ORDER BY p.quantity";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }


    /* INVENTORY SUMMARY for stat cards */
    public int[]    getSummary() {
        // returns [totalSkus, lowStockCount]
        int[] result = {0, 0};
        String sql = "SELECT COUNT(*) AS skus, " +
                "SUM(quantity < low_stock_threshold) AS low_count FROM products";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) {
                result[0] = rs.getInt("skus");
                result[1] = rs.getInt("low_count");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public double getTotalValue() {
        String sql = "SELECT COALESCE(SUM(quantity * price), 0) AS total FROM products";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble("total");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}