package com.ims;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.zaxxer.hikari.HikariDataSource;

import static com.ims._00_Utils.*;
import static com.ims._20_ValidBusinessRules.*;

//  Import our "toolbox"


/**
 *  This is the "Supplier Database Specialist" (a Data Access Object - DAO).
 *  This is a "Server-Side Only" class.
 *  Its only job is to handle all database operations for the 'supplier' table. It's the only class that knows how to
 *     "speak SQL" for suppliers.

 *  WEBSITE: Baeldung - Preventing SQL Injection: https://www.baeldung.com/sql-injection-prevention-jpa-hibernate
 *  (Section on "Using Prepared Statements")
 *  It explains why we use ? placeholders instead of adding strings together. This is a critical security concept.
 */
public class _16_ModelSupplierDB {

    //  CONNECTION

    //  This is the "master ledger" (DB Connection) given by the Server.
    //private final Connection connection;
    //  Switched to Hikari bc of multiple connections



    //  CONNECTION POOL
    //  Instead of holding one connection, we hold the "DataSource" (a toolbox w many wrenches).
    private final HikariDataSource dataSource;

    /**
     *  CONSTRUCTOR
     *  The _01_InventoryServer "hires" this specialist and gives it access to the connection pool.
     *  @param dataSource The shared connection pool.
     */
    public _16_ModelSupplierDB(HikariDataSource dataSource) {
        this.dataSource = dataSource;  //  Connection setup is now handled entirely by the Hikari imported class
        println("Supplier Specialist (DAO) is ready with connection pool.");
    }







    //------------------------------------------------------------------------------------------------------------------
    //   VALIDATION
    //------------------------------------------------------------------------------------------------------------------
    //  getCompany, getContactName, getPhone, getEmail, getAddress, getNotes
    /**
     *  Validates a product using our centralized _20_ValidBusinessRules class.
     *  This ensures all validation rules are consistent across the application.
     *     @param sup The product to validate
     */
    private static void _16a_ValidateSupplier(_15_ModelSupplier sup) {

        // STEP 1: COMPANY NAME
        _20a_validAlphaNumeric("Name", sup.getCompanyName(), 1, 100);

        // STEP 2: CONTACT NAME
         _20a_validAlphaNumeric("Name", sup.getContactName(), 1, 100);

        // STEP 3: PHONE
        _20a_validAlphaNumeric("Phone", sup.getPhone(), 1, 15);

        // STEP 4: EMAIL
        _20a_validAlphaNumeric("Email", sup.getEmail(), 0, 100);

        // STEP 5: ADDRESS
        _20a_validAlphaNumeric("Address", sup.getAddress(), 0, 255);

        // STEP 6: NOTES
        _20a_validAlphaNumeric("Description", sup.getNotes(), 0, 10000);
    }








    // =========================================================================
    //  CRUD OPERATIONS
    // =========================================================================


    /**
     *  "C" RUD: CREATE - ADD SUPPLIER

     *  Adds a single new supplier to the database.
     *     @param supplier The '_15_ModelSupplier' object to add.
     *     @throws SQLException If the database insert fails.
     */
    public void _16b_addSupplier(_15_ModelSupplier supplier) throws SQLException {

        //  Validation first
        _16a_ValidateSupplier(supplier);

        //  Step 1: Define the "safe" SQL query using '?'.
        String sql = "INSERT INTO supplier (supplierName, contactPerson, phone, email, address, notes) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connect = dataSource.getConnection();
             PreparedStatement prepStmt = connect.prepareStatement(sql)) {
            //  Step 2: Fill in the blanks.
            prepStmt.setString(1, supplier.getCompanyName());
            prepStmt.setString(2, supplier.getContactName());
            prepStmt.setString(3, supplier.getPhone());
            prepStmt.setString(4, supplier.getEmail());
            prepStmt.setString(5, supplier.getAddress());
            prepStmt.setString(6, supplier.getNotes());

            //  Step 3: Execute the command.
            prepStmt.executeUpdate();
        }
    }




    /**
     *  C "R" UD: READ - GET ALL SUPPLIERS

     *  Gets all suppliers from the DB and "serializes" them into a single string for the client.
     *     @return A protocol string, e.g., "SUCCESS|supplier1;supplier2;..."
     */
    public String _16c_getAllSuppliersAsString() {
        //  Step 1: Create an empty list to hold the suppliers.
        List<_15_ModelSupplier> supplierList = new ArrayList<>();

        //  Step 2: Define the SQL query.
        String sql = "SELECT * FROM supplier"; // (Assuming table name is 'supplier')

        //  Step 3: Use a 'try-with-resources' to safely run the query.
        try (Connection connect = dataSource.getConnection();
             PreparedStatement prepStmt = connect.prepareStatement(sql);
             ResultSet resSet = prepStmt.executeQuery()) {

            //  Step 4: Loop through every 'row' in the results.
            while (resSet.next()) {
                //  Step 4a: Create a new '_15_ModelSupplier'
                //     "data bucket" and fill it with data.
                supplierList.add(new _15_ModelSupplier(
                        resSet.getString("supplierID"),      // Read supplierID column
                        resSet.getString("supplierName"),    // Read supplierName column
                        resSet.getString("contactPerson"),   // Read contactPerson column
                        resSet.getString("phone"),
                        resSet.getString("email"),
                        resSet.getString("address"),
                        resSet.getString("notes")
                ));
            }

            //  Step 5: "Serialize" the list into our protocol string.
            String data = supplierList.stream()
                    .map(this::_16f_serializeSupplier) //  Use our private helper
                    .collect(Collectors.joining(";")); //  Join with ";"

            //  Step 6: Return the "SUCCESS" message.
            return "SUCCESS|" + data;

        } catch (SQLException e) {
            error("Error fetching suppliers: " + e.getMessage());
            return "FAILURE|Could not fetch supplier list from database.";
        }
    }




    /**
     *  CR "U" D: UPDATE - UPDATE SUPPLIER

     *  Updates an existing supplier in the database.
     *     @param supplier The supplier object with the new data.
     *     @throws SQLException If the update fails.
     */
    public void _16d_updateSupplier(_15_ModelSupplier supplier) throws SQLException {

        //  Validation first
        _16a_ValidateSupplier(supplier);

        //  Step 1: Define the "safe" SQL query. We update
        //     all fields WHERE the 'id' matches.
        String sql = "UPDATE supplier SET supplierName = ?, contactPerson = ?, phone = ?, email = ?, address = ?, notes = ? WHERE supplierID = ?";

        try (Connection connect = dataSource.getConnection();
             PreparedStatement prepStmt = connect.prepareStatement(sql)) {
            //  Step 2: Fill in the blanks.
            prepStmt.setString(1, supplier.getCompanyName());
            prepStmt.setString(2, supplier.getContactName());
            prepStmt.setString(3, supplier.getPhone());
            prepStmt.setString(4, supplier.getEmail());
            prepStmt.setString(5, supplier.getAddress());
            prepStmt.setString(6, supplier.getNotes());  //  Just in case...
            //  The last '?' is the ID for the 'WHERE' part.
            prepStmt.setString(7, supplier.getId());

            //  Step 3: Execute the command.
            prepStmt.executeUpdate();
        }
    }




    /**
     *  CRU "D": DELETE - REMOVE SUPPLIERS BY ID

     *  Removes a list of suppliers from the database based on their IDs.
     *     @param supplierIds A List of supplier ID strings to delete.
     *     @throws SQLException If the delete fails.
     */
    public void _16e_removeSuppliersById(List<String> supplierIds) throws SQLException {
        //  Step 1: Define the "safe" delete query.
        String sql = "DELETE FROM supplier WHERE supplierID = ?";

        //  Step 2: Use a 'try-with-resources' for the statement.
        try (Connection connect = dataSource.getConnection();
             PreparedStatement prepStmt = connect.prepareStatement(sql)) {
            //  Step 3: This is a "batch" operation.
            for (String id : supplierIds) {
                //  Step 3a: Fill in the '?' with an ID.
                prepStmt.setString(1, id);
                //  Step 3b: "Add it to the batch".
                prepStmt.addBatch();
            }
            //  Step 4: Execute all commands in the batch at once.
            prepStmt.executeBatch();
        }
    }





    // =========================================================================
    //  PRIVATE HELPER "TOOLS"
    // =========================================================================

    /**
     *  Private "helper" tool to turn a supplier object into a string.
     *  Format: id|company|contactName|address|email|phone
     */
    private String _16f_serializeSupplier(_15_ModelSupplier supplier) {
        return String.join("|",
                supplier.getId(),
                supplier.getCompanyName(),
                supplier.getContactName(),
                supplier.getPhone(),
                supplier.getEmail(),
                supplier.getAddress(),
                supplier.getNotes()
        );
    }
}