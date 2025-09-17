package com.example.ims;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;







/**
*  Our "DatabaseManager" is our dedicated data wrangler. Back in the day, we'd just open a file and start reading
*    and writing data, but now with a database, things are a little better (faster and more secure).
*  This class's whole job is to be the intermediary between our program and the MySQL database.
*  It's the only one that knows how to talk to the database, so if we ever need to change from MySQL to
*    something else, we only have to change the code in this class.
*  It handles all the nitty-gritty stuff, like establishing the connection, sending the queries, and grabbing the results.
*  It's also responsible for making sure our data is safe from bad code by using "prepared statements" that are
*    a solid way to stop SQL injection attacks.
*  In short, this class keeps our data safe and organized, without the rest of the program having to know how it's done.
**/




public class DatabaseManager {

    //  CONNECTION
    //  to the MySQL database credentials
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


    /**
     *  Note: The best and most reliable way to prevent an SQL injection is to use a pre-compiled PREPARED STATEMENT
     *        This SQL code uses parameterized queries, with placeholders (?) as values to SELECT, INSERT, UPDATE, or DELETE.
     *        We shan't use String+Concatenation and avoid building the SQL query by joining input strings
     *        (e.g., String sql = "INSERT INTO ... VALUES ('" + sku + "', '" + category + "', ...").
     *        This removes the risk of a user inserting (injecting) SQL code into a text field, to manipulate the DB.
     *
     *        That's the common newbie mistake that leaves any DB application vulnerable to injection attacks, such
     *        as when an attacker enters the following malicious string into the search box:
     *        ' UNION SELECT username, password FROM users --
     *
     *        The application then builds the complete, vulnerable query:
     *        SELECT name, description FROM products WHERE category = '' UNION SELECT username, password FROM users --';
     *        The first single quote (') closes the original category value, allowing the attacker to end the original SQL statement.
     *        The UNION operator combines the results of the two queries, thus selecting (bringing up) the usernames and
     *        passwords from a different table (users), which the attacker can then view in the search results.
     *        The "--" comments out the rest of the original query, including the final ' character, so it doesn't cause a syntax error.
     *
     *        By using this technique, the attacker has successfully bypassed the intended function of the query,
     *        and stolen sensitive users and passwords data, or anything else a DB might contain.
     *        This is why using "Prepared Statements" is crucial. They prevent the user input from being executed as part of the SQL command
    **/



    //  "C" RUD: CREATE

    //  Before we ADD a product the Actions class is calling us to "check" if such SKU (id) item exists or not.
    public boolean doesProductExist(String sku) {
        String sql = "SELECT 1 FROM products WHERE sku = ?";
        try (PreparedStatement prepStatmt = connection.prepareStatement(sql)) {
            prepStatmt.setString(1, sku);
            try (ResultSet rs = prepStatmt.executeQuery()) {
                return rs.next(); // Returns true if a row is found
            }
        } catch (SQLException e) {
            System.err.println("Error checking for product existence: " + e.getMessage());
            return false;
        }
    }

    //  Then Action calls us to ADD the product
    public void addProduct(Product product) {
        String sql = "INSERT INTO products (category, sku, name, description, quantity, price) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement prepStatmt = connection.prepareStatement(sql)) {
            prepStatmt.setString(1, product.getSku());
            prepStatmt.setString(2, product.getCategory());
            prepStatmt.setString(3, product.getName());
            prepStatmt.setString(4, product.getDescription());
            prepStatmt.setInt(5, product.getQuantity());
            prepStatmt.setDouble(6, product.getPrice());
            prepStatmt.executeUpdate();                                     //  IMPLICIT COMMIT
            System.out.println("Product added successfully.");
        } catch (SQLException e) {
            System.err.println("Error adding product: " + e.getMessage());
        }
    }





    //  C "R" UD: READ
    public ObservableList<Product> getAllProducts() {
        ObservableList<Product> products = FXCollections.observableArrayList();
        String sql = "SELECT * FROM products";
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet resultSet = pstmt.executeQuery()) {
            while (resultSet.next()) {
                String category = resultSet.getString("category");
                String sku = resultSet.getString("sku");
                String name = resultSet.getString("name");
                String description = resultSet.getString("description");
                int quantity = resultSet.getInt("quantity");
                double price = resultSet.getDouble("price");
                products.add(new Product(category, sku, name, description, quantity, price));  //  IMPLICIT COMMIT
            }
        } catch (SQLException e) {
            System.err.println("Error fetching products from database: " + e.getMessage());
        }
        return products;
    }




    //  CR "U" D: UPDATE
    public void updateProduct(Product product, String originalSku) {
        String sql = "UPDATE products SET sku = ?, category = ?, name = ?, description = ?, quantity = ?, price = ? WHERE sku = ?";
        try (PreparedStatement prepStatmt = connection.prepareStatement(sql)) {
            prepStatmt.setString(1, product.getSku());
            prepStatmt.setString(2, product.getCategory());
            prepStatmt.setString(3, product.getName());
            prepStatmt.setString(4, product.getDescription());
            prepStatmt.setInt(5, product.getQuantity());
            prepStatmt.setDouble(6, product.getPrice());
            prepStatmt.setString(7, originalSku);         // <— ORIGINAL SKU

            int rowsAffected = prepStatmt.executeUpdate();  //  IMPLICIT COMMIT:
            if (rowsAffected > 0) {
                System.out.println("Product updated successfully.");
            } else {
                System.out.println("No product found with that SKU.");
            }
        } catch (SQLException e) {
            System.err.println("Error updating product: " + e.getMessage());
        }
    }



    //  CRU "D": DELETE
    public void removeProduct(String sku) {
        String sql = "DELETE FROM products WHERE sku = ?";
        try (PreparedStatement prepStatmt = connection.prepareStatement(sql)) {
            prepStatmt.setString(1, sku);
            int rowsAffected = prepStatmt.executeUpdate();  //  IMPLICIT COMMIT
            if (rowsAffected > 0) {
                System.out.println("Product removed successfully.");
            } else {
                System.out.println("No product found with that SKU.");
            }
        } catch (SQLException e) {
            System.err.println("Error removing product: " + e.getMessage());
        }
    }



    //  END TRANS
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
