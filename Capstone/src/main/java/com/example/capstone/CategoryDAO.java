package com.example.capstone.database;

import com.example.capstone.model.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

    private final Connection connection;

    public CategoryDAO() {
        connection = DBConnection.getInstance().getConnection();
    }


    /* INSERT */
    public boolean insert(Category category) {

        String sql = "INSERT INTO categories (name, description) VALUES (?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, category.getName());
            ps.setString(2, category.getDescription());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    /* UPDATE */
    public boolean update(Category category) {

        String sql = "UPDATE categories SET name=?, description=? WHERE category_id=?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, category.getName());
            ps.setString(2, category.getDescription());
            ps.setInt(3, category.getCategoryId());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    /* DELETE */
    public boolean delete(int categoryId) {

        String sql = "DELETE FROM categories WHERE category_id=?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, categoryId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    /* SELECT ALL */
    public List<Category> getAll() {

        List<Category> list = new ArrayList<>();
        String sql = "SELECT * FROM categories ORDER BY name";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Category(
                        rs.getInt("category_id"),
                        rs.getString("name"),
                        rs.getString("description")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
}