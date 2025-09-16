package com.example.ims;


/**
*  Our "DatabaseManager" is our dedicated data wrangler. Back in the day, we'd just open a file and start reading
*    and writing data, but now with a database, things are a little different (faster and better and more secure).
*  This class's whole job is to be the go-between for our program and the MySQL database.
*  It's the only one that knows how to talk to the database, so if we ever need to change from MySQL to
*    something else, we only have to change the code in this one place.
*  It handles all the nitty-gritty stuff, like establishing the connection, sending the SQL queries, and grabbing the results.
*  It's also responsible for making sure our data is safe from bad code by using prepared statements—a solid way to
*    stop SQL injection attacks.
*  In short, this class keeps our data safe and organized without the rest of the program having to worry about how it's done.
**/





import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;

public class DatabaseManager {

    //  MySQL database credentials
    private final String URL ="jdbc:mysql://localhost:3306/inventorymgmt";
    private final String WHO = "Admin";
    private final String PWD = "6093007";

    private Connection connection;

    public DatabaseManager() {
        try {
            this.connection = DriverManager.getConnection(URL, WHO, PWD);
            connection.setAutoCommit(true);
            System.out.println("Database connection established successfully.");
        } catch (SQLException e) {
            System.err.println("Error connecting to the database: " + e.getMessage());
        }
    }



    public ObservableList<Product> getAllProducts() {
        ObservableList<Product> products = FXCollections.observableArrayList();
        String sql = "SELECT * FROM products";
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                String sku = rs.getString("sku");
                String name = rs.getString("name");
                String description = rs.getString("description");
                int quantity = rs.getInt("quantity");
                double price = rs.getDouble("price");
                products.add(new Product(sku, name, description, quantity, price));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching products from database: " + e.getMessage());
        }
        return products;
    }



    public void addProduct(Product product) {
        String sql = "INSERT INTO products (sku, name, description, quantity, price) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, product.getSku());
            pstmt.setString(2, product.getName());
            pstmt.setString(3, product.getDescription());
            pstmt.setInt(4, product.getQuantity());
            pstmt.setDouble(5, product.getPrice());
            pstmt.executeUpdate();
            System.out.println("Product added successfully.");
        } catch (SQLException e) {
            System.err.println("Error adding product: " + e.getMessage());
        }
    }



    public void removeProduct(String sku) {
        String sql = "DELETE FROM products WHERE sku = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, sku);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Product removed successfully.");
            } else {
                System.out.println("No product found with that SKU.");
            }
        } catch (SQLException e) {
            System.err.println("Error removing product: " + e.getMessage());
        }
    }



    public void updateProduct(Product product) {
        String sql = "UPDATE products SET name = ?, description = ?, quantity = ?, price = ? WHERE sku = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, product.getName());
            pstmt.setString(2, product.getDescription());
            pstmt.setInt(3, product.getQuantity());
            pstmt.setDouble(4, product.getPrice());
            pstmt.setString(5, product.getSku());
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Product updated successfully.");
            } else {
                System.out.println("No product found with that SKU.");
            }
        } catch (SQLException e) {
            System.err.println("Error updating product: " + e.getMessage());
        }
    }



    public boolean doesProductExist(String sku) {
        String sql = "SELECT 1 FROM products WHERE sku = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, sku);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // Returns true if a row is found
            }
        } catch (SQLException e) {
            System.err.println("Error checking for product existence: " + e.getMessage());
            return false;
        }
    }



    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
}
