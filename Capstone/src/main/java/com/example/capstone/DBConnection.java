package com.example.capstone.database;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    private static DBConnection instance;

    private Connection connection;

    // ── updated host to 127.0.0.1 for XAMPP ──
    private final String url =
            "jdbc:mysql://127.0.0.1:3306/inventory_db" +
                    "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Manila";

    private final String username = "root";
    private final String password = "";       // default XAMPP has no password


    private DBConnection() {

        try {

            Class.forName("com.mysql.cj.jdbc.Driver");

            connection = DriverManager.getConnection(url, username, password);

            System.out.println("✔ Database connected: inventory_db");

        } catch (Exception e) {

            System.err.println("✘ DB connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public static DBConnection getInstance() {

        if (instance == null) {
            instance = new DBConnection();
        }

        return instance;
    }


    public Connection getConnection() {
        return connection;
    }
}