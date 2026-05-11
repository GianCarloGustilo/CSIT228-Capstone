package com.example.capstone.database;

import com.example.capstone.model.Supplier;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupplierDAO {

    private final Connection connection;

    public SupplierDAO() {
        connection = DBConnection.getInstance().getConnection();
    }


    /* INSERT */
    public boolean insert(Supplier supplier) {

        String sql = "INSERT INTO suppliers (name, contact_name, phone, email, address) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, supplier.getName());
            ps.setString(2, supplier.getContactName());
            ps.setString(3, supplier.getPhone());
            ps.setString(4, supplier.getEmail());
            ps.setString(5, supplier.getAddress());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    /* UPDATE */
    public boolean update(Supplier supplier) {

        String sql = "UPDATE suppliers SET name=?, contact_name=?, phone=?, email=?, address=? " +
                "WHERE supplier_id=?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, supplier.getName());
            ps.setString(2, supplier.getContactName());
            ps.setString(3, supplier.getPhone());
            ps.setString(4, supplier.getEmail());
            ps.setString(5, supplier.getAddress());
            ps.setInt(6, supplier.getSupplierId());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    /* DELETE */
    public boolean delete(int supplierId) {

        String sql = "DELETE FROM suppliers WHERE supplier_id=?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, supplierId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    /* SELECT ALL */
    public List<Supplier> getAll() {

        List<Supplier> list = new ArrayList<>();
        String sql = "SELECT * FROM suppliers ORDER BY name";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Supplier(
                        rs.getInt("supplier_id"),
                        rs.getString("name"),
                        rs.getString("contact_name"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("address")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
}