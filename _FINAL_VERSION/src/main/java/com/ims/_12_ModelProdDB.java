package com.ims;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.zaxxer.hikari.HikariDataSource;

//  Import our "toolbox"
import static com.ims._00_Utils.*;
import static com.ims._20_ValidBusinessRules.*;


/**
 *  This is the "Product Specialist" (a Data Access Object - DAO).
 *  This is a "Server-Side Only" class.
 *  Its only job is to handle all database operations for the 'product' table.
 *  It's the only class that knows how to "speak SQL" for products.
 */
public class _12_ModelProdDB {

    //  CONNECTION (Old)

    //  This is the "master ledger" (DB Connection) given by the Server.
    //  To get the MySQL database credentials, we embed the connection logic from the _10_ModelConnectToDB file
    //     as a Dependency Injection, making the data access layer cleaner and easier to test...
    //  The _10_ModelConnectToDB class handles the configuration and connection creation.
    //private final Connection connection;

    //  SWITCHED TO HIKARI BC OF MULTIPLE CONNECTIONS

    //  Amigoscode: Connection Pooling with HikariCP (https://www.youtube.com/watch?v=CJjHdchLY9Y)

    //  Baeldung Article: Introduction to HikariCP. (https://www.baeldung.com/hikaricp)

    //  Also:

    //  Derek Banas: Visual walkthrough of the DAO pattern. (https://www.youtube.com/watch?v=D25CNL6aGb0)

    //  FreeCodeCamp - Java Full Course (Database Section)
    //     (https://www.freecodecamp.org/news/object-oriented-programming-in-java/)
    //     Timestamp: Around 8:30:00 covers JDBC and shows: Connection, PreparedStatement, ResultSet

    /**
     *  CONNECTION POOL (New)

     *  WEBSITE: HikariCP - Connection Pooling with Java - A fantastic, hands-on tutorial that walks through the exact
     *     HikariConfig setup you implemented. (https://www.baeldung.com/hikaricp)

     *  IMPORTANT: This class uses HikariCP connection pooling.
     *  Instead of holding one connection (a single wrench w many impatient users),
     *     we hold the "DataSource" (a toolbox with many wrenches, and for many users).

     *  Each method that needs a database connection does this:
     *      try (Connection connect = dataSource.getConnection()) { ... }

     *  This is SAFE for multi-threading because:
     *     1. Each method call gets its OWN connection from the pool
     *     2. The connection is automatically returned when the try block ends
     *     3. Multiple threads can call these methods simultaneously without interfering

     *  Think of it like a book lending library: each student borrows a copy of the book when they need it,
     *     uses it, and returns it when they're done. The next student can then borrow that same copy.
     */
    //  TODO: Add an image to every view of the product. DONE!
    private final HikariDataSource dataSource;

    /**
     *  CONSTRUCTOR
     *  The _01_InventoryServer "hires" this specialist and gives it access to the connection pool.
     *  @param dataSource The shared connection pool.
     */

    //  Old:
    //public _12_ModelProdDB(Connection externalConnection) {  //  We propagate the SQLException out of the constructor,
        //  bc the calling code (_04_Main.java) is already set up to handle it.
    //  New: Converted this to  a datasource as parameter:
    public _12_ModelProdDB(HikariDataSource dataSource) {

        //  The constructor now receives the Connection, and assign the passed-in connection to the final field.
        //this.connection = externalConnection;  // Connection setup is now handled entirely by _13_ModelConnectToDB

        this.dataSource = dataSource;  // Connection setup is now handled entirely by the Hikari imported class
        // We only perform configuration/logging that uses the valid connection:
        println("Product Specialist (DAO) initialized with connection pool.");
    }


    //------------------------------------------------------------------------------------------------------------------
    //   VALIDATION
    //------------------------------------------------------------------------------------------------------------------

    /**
     *  Validates a product using our centralized _20_ValidBusinessRules class.
     *  This ensures all validation rules are consistent across the application.
     *     @param prod The product to validate
     */
    private static void _12_ValidateProduct(_11_ModelProd prod) {

        // STEP 1: SKU (required, 1-32 characters)
        _20a_validAlphaNumeric("Sku", prod.getSku(), 1, 32);

        // STEP 2: CATEGORY (required, 1-60 characters)
        _20a_validAlphaNumeric("Brand", prod.getBrand(), 1, 60);

        // STEP 3: NAME (required, 1-100 characters)
        _20a_validAlphaNumeric("Name", prod.getProdName(), 1, 100);

        // STEP 4: DESCRIPTION (optional, but if present, check format and length)
        _20a_validAlphaNumeric("Description", prod.getDescription(), 0, 10000);

        // STEP 5: QUANTITY (must be non-negative)
        _20b_ValidQuantity(String.valueOf(prod.getQuantity()));

        // STEP 6: PRICE (must be non-negative)
        _20c_ValidPrice(String.valueOf(prod.getPrice()));

        // STEP 7: IMAGE (required, 1-100 characters)
        _20a_validAlphaNumeric("Image", prod.getImageUrl(), 1, 100);
    }












    //==================================================================================================================
    //
    //  CRUD OPERATIONS
    //
    //==================================================================================================================



    /**
     *  "C" RUD: CREATE:  ADD 1 PRODUCT

     *  Adds a single new product to the database.
     *     @param product The product object to add.
     *     @throws SQLException If the database insert fails.

     *   Oracle Java Documentation
     *   Citation: "A SQL statement is precompiled and stored in a PreparedStatement object. This object can then be used
     *      to efficiently execute this statement multiple times."
     *   Link: https://docs.oracle.com/javase/8/docs/api/java/sql/PreparedStatement.html
     */
    public void _12a_addProduct(_11_ModelProd product) throws SQLException {

        //  Validation first
        _12_ValidateProduct(product);

        //  STEP 1: Define the SQL query. We use '?' as
        //     placeholders to prevent SQL Injection attacks.
        String sql = "INSERT INTO products (sku, brand, name, description, quantity, price, imageUrl) VALUES (?, ?, ?, ?, ?, ?, ?)";

        //  STEP 2: Use a 'try-with-resources' to create the
        //     'PreparedStatement' (the "safe" query).
        try (Connection connect = dataSource.getConnection();
             PreparedStatement prepStatmt = connect.prepareStatement(sql)) {
            //  STEP 3: "Fill in the blanks" for each '?'
            prepStatmt.setString(1, product.getSku());
            prepStatmt.setString(2, product.getBrand());
            prepStatmt.setString(3, product.getProdName());
            prepStatmt.setString(4, product.getDescription());
            prepStatmt.setInt(5, product.getQuantity());
            prepStatmt.setDouble(6, product.getPrice());
            prepStatmt.setString(7, product.getImageUrl());

            //  STEP 4: Execute the command.
            prepStatmt.executeUpdate();
        }
    }




    /**
     *  "C" RUD: CREATE:  ADD > 1 PRODUCTS

     *  Adds a whole list of products at once (from a CSV).
     *     @param products The List of products to add.
     *     @return The number of products successfully added.
     *     @throws SQLException If the batch insert fails.
     */
    public int _12b_bulkAddProducts(List<_11_ModelProd> products) throws SQLException {

        //  STEP 1: Define the "safe" query.
        String sql = "INSERT INTO products (sku, brand, name, description, quantity, price, imageUrl) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connect = dataSource.getConnection();
             PreparedStatement prepStatmt = connect.prepareStatement(sql)) {

            //  STEP 2: For each loop: Go through all products in the list.
            for (_11_ModelProd product : products) {

                //  Validation first
                _12_ValidateProduct(product);

                //  STEP 3: Fill in the blanks for this one products.
                prepStatmt.setString(1, product.getSku());
                prepStatmt.setString(2, product.getBrand());
                prepStatmt.setString(3, product.getProdName());
                prepStatmt.setString(4, product.getDescription());
                prepStatmt.setInt(5, product.getQuantity());
                prepStatmt.setDouble(6, product.getPrice());
                prepStatmt.setString(7, product.getImageUrl());

                //  STEP 4: Add this products to the "batch."
                prepStatmt.addBatch();
            }
            //  STEP 5: Execute the entire batch.
            //  'executeBatch()' returns an array of ints, one for each command. We just return the length to show how many were added.
            return prepStatmt.executeBatch().length;
        }
    }




    /**
     *  C "R" UD: READ

     *  Gets all products from the DB and "serializes" them into a single string for the client.
     *     @return A protocol string, e.g., "SUCCESS|prod1;prod2;..."
     */
    public String _12c_getAllProductsAsString() {
        //  STEP 1: Create an empty list to hold the products we find in the DB.
        List<_11_ModelProd> productList = new ArrayList<>();

        //  STEP 2: Define the SQL query.
        String sql = "SELECT * FROM products";

        //  STEP 3: Use a 'try-with-resources' to safely prepare and execute the query.
        try (Connection connect = dataSource.getConnection();
             PreparedStatement prepStatmt = connect.prepareStatement(sql);
             ResultSet resSet = prepStatmt.executeQuery()) {

            //  STEP 4: Loop through every 'row' in the 'ResultSet' (the results).
            while (resSet.next()) {
                //  STEP 4a: For each row, create a new '_11_ModelProd' (a "data bucket") and fill it with the data.
                productList.add(new _11_ModelProd(
                        resSet.getString("sku"),
                        resSet.getString("brand"),
                        resSet.getString("name"),
                        resSet.getString("description"),
                        resSet.getInt("quantity"),
                        resSet.getDouble("price"),
                        resSet.getString("imageUrl")
                ));
            }

            //  STEP 5: Now, "serialize" that List into our protocol string, that we send to the Client. We use the 'stream()' tool for this.
            String data = productList.stream()
                    .map(this::_12f_serializeProduct) //  Turn each products into a string
                    .collect(Collectors.joining(";")); //  Join all strings with a ";"

            //  STEP 6: Return the final "SUCCESS" message with the data.
            return "SUCCESS|" + data;

        } catch (SQLException e) {
            error("Error fetching products: " + e.getMessage());
            return "FAILURE|Could not fetch products list from database.";
        }
    }





    /**
     *  CR "U" D: UPDATE

     *  Updates an existing products in the database.
     *     @param product The product object with the new data.
     *     @throws SQLException If the update fails.
     */
    public void _12d_updateProduct(_11_ModelProd product) throws SQLException {

        //  Validation first
        _12_ValidateProduct(product);

        //  STEP 1: Define the "safe" SQL query.
        //     We update all fields WHERE  the 'sku' matches.
        String sql = "UPDATE products SET brand=?, name=?, description=?, quantity=?, price=?, imageUrl=?  WHERE sku=?";

        try (Connection connect = dataSource.getConnection();
             PreparedStatement prepStatmt = connect.prepareStatement(sql)) {
            //  STEP 2: Fill in the blanks.
            prepStatmt.setString(1, product.getBrand());
            prepStatmt.setString(2, product.getProdName());
            prepStatmt.setString(3, product.getDescription());
            prepStatmt.setInt(4, product.getQuantity());
            prepStatmt.setDouble(5, product.getPrice());
            prepStatmt.setString(6, product.getImageUrl());
            //  The last '?' is the SKU for the 'WHERE' part.
            prepStatmt.setString(7, product.getSku());

            //  STEP 3: Execute the command.
            prepStatmt.executeUpdate();
        }
    }




    /**
     *  CRU "D": DELETE

     *  Removes a list of products from the database based on their SKUs.
     *     @param skus A List of SKU strings to delete.
     *     @throws SQLException If the delete fails.
     */
    public void _12e_removeProductsBySku(List<String> skus) throws SQLException {
        //  STEP 1: Define the "safe" delete query.
        String sql = "DELETE FROM products WHERE sku = ?";

        //  STEP 2: Use a 'try-with-resources' for the statement.
        try (Connection connect = dataSource.getConnection();
             PreparedStatement prepStatmt = connect.prepareStatement(sql)) {
            //  STEP 3: This is a "batch" operation. We'll
            //     run the same command for many different SKUs.
            for (String sku : skus) {
                //  STEP 3a: Fill in the '?' with a SKU.
                prepStatmt.setString(1, sku);
                //  STEP 3b: "Add it to the batch" (don't run it yet).
                prepStatmt.addBatch();
            }
            //  STEP 4: Execute all commands in the batch at once.
            //     This is 'much' faster than running them one by one.
            prepStatmt.executeBatch();
        }
    }




    /**
     *  Private "helper" tool to turn a product object into a string.
     *  Format: sku|brand|name|description|quantity|price
     *  This helper method formats the data as a pipe-delimited string, which matches my client-server protocol.
     */
    private String _12f_serializeProduct(_11_ModelProd product) {
        return String.join("|",
                product.getSku(),
                product.getBrand(),
                product.getProdName(),
                product.getDescription(),
                String.valueOf(product.getQuantity()),
                String.valueOf(product.getPrice()),
                product.getImageUrl()
        );
    }
}