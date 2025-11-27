package com.ims;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.zaxxer.hikari.HikariDataSource;

//  Import our "toolbox"
import static com.ims._00_Utils.*;
import static com.ims._20_ValidBusinessRules.*;

/**
 *  This is the "User Specialist" (a Data Access Object - DAO).
 *  This is a "Server-Side Only" class.
 *  This is the "bouncer" and "accountant" for the 'user' table.
 *  It handles all the 'real' security logic (checking passwords, locking accounts) and all the database CRUD commands.

 *  WEBSITE: Jenkov.com - Java DAO Pattern: https://jenkov.com/tutorials/java-persistence/DAO-design-pattern.html
 *  It explains the "Department Specialist" concept (DAO) perfectly—why we want a specific class just for DB operations.
 */
public class _14_ModelUserDB {

    //  CONNECTION

    //  This is the "master ledger" (DB Connection) given by the Server.
    //  To get the MySQL database credentials, we embed the connection logic from the _10_ModelConnectToDB file
    //     as a Dependency Injection, making the data access layer cleaner and easier to test...
    //  The _10_ModelConnectToDB class handles the configuration and connection creation.
    //private final Connection connection;
    //  Switched to Hikari bc it creates a pool of multiple connections, instead of creating, closing, creating, closing,
    //     etc., which is very resource intensive.



    //  CONNECTION POOL
    //  Instead of holding one connection, we hold the "DataSource" (a toolbox w many wrenches).
    private final HikariDataSource dataSource;

    /**
     *  CONSTRUCTOR
     *  The _01_InventoryServer "hires" this specialist and gives it access to the connection pool.
     *  @param dataSource The shared connection pool.
     */
    public _14_ModelUserDB(HikariDataSource dataSource) {  //  We propagate the SQLException out of the constructor,
        //  bc the calling code (_04_Main.java) is already set up to handle it.

        //  The constructor now receives the Connection, and assign the passed-in connection to the final field.
        //this.connection = externalConnection;  // Connection setup is now handled entirely by _13_ModelConnectToDB

        this.dataSource = dataSource;  // Connection setup is now handled entirely by the Hikari imported class
        // We only perform configuration/logging that uses the valid connection:
        println("User Specialist (DAO) is ready with User Model connection pool initialized.");
    }




    /**
     *  THE "SECURITY GUARD" - SERVER SIDE

     *  This fella works deep inside the secure back office (the Server). He's the only one with the keys to the vault (DB).
     *  His only job is to:
     *     1. Get the login request (email and plain password) from the Server Operator (_02_ServerClientHandler).
     *     2. Go into the vault (DB) and look up the customer's file (getUserFromDB).
     *     3. Check if the account is already locked.
     *     4. Take the Secret "plainTextPassword" the Teller sent and compare it to the Super Secret Hashed Password,
     *        ("user._13d_getPwdHash()") stored in the file utilizing "_00_Utils.verify".
     *     5. If it matches, reset the failed attempts and tell the Operator "SUCCESS" (plus the customer's name/role).
     *     6. If it doesn't match, add a failed attempt and tell the Operator "FAILURE" or "LOCKED".

     *  What it DOESN'T do: This manager never talks to the user, never sees the UI, never builds the main app screen.
     *  He just works with the data and the security rules.

     *  Handles a login attempt from the client. This is the core server-side security "brain."
     *     @param email The plain-text email from the client.
     *     @param plainTextPassword The plain-text password from the client.
     *     @return A protocol string for the client (e.g., "SUCCESS|...", "LOCKED|...").
     */
    public String _14a_svrHandleLogin(String email, String plainTextPassword) {
        //  STEP 1: Go find this user in the database.
        //     We use a private helper 'getUserFromDB' to do this.
        _13_ModelUser user = _14f_getUserFromDB(email);

        // STEP 2: We always hash the password, even if user is null. This makes timing consistent and prevents timing attacks.
        String hashedPassword = (user != null) ? user.getPwdHash() : "dummYH4sh70Pr3v3n7T1m1nG4774ck5"; // leet for
        // "dummyHashToPreventTimingAttacks". Now every login attempt takes roughly the same amount of time, whether the email exists or not.

        // STEP 3: Always verify, even with a dummy hash, using the _00_Utils class.
        boolean passwordMatches = verify(plainTextPassword, hashedPassword);


        //  STEP 4: Check if the user even exists.
        if (user == null || !passwordMatches) {
            //  We send a generic message. We don't want to tell the hacker "that email is real but the password was wrong."
            return "FAILURE: Invalid email or password.";
        }

        //  STEP 5: Check if the account is already locked.
        if (user.getLoginAttempts() >= 5) {
            return "LOCKED|Account locked. Please contact your admin.";
        }

        //  STEP 6: Verify the password.
        //  We use our '_00_Utils' tool to compare the 'plainTextPassword' from the client against the 'pwdHash' we just got from the database.
        if (passwordMatches) {
            //  case 1: SUCCESS!

            //  STEP 6a: If they had failed attempts, reset them.
            if (user.getLoginAttempts() > 0) {
                //  We use a helper to set their attempts back to 0.
                _14h_updateUserAttempts(user.getId(), 0);
            }

            //  STEP 6b: Send the "SUCCESS" response back, including the user's name and role for the main app screen.
            return "SUCCESS|" + user.getName() + "|" + user.getRole();

        } else {
            //  case 2: FAILURE!

            //  STEP 7a: Increment the failed attempt counter.
            int newAttempts = user.getLoginAttempts() + 1;

            //  STEP 7b: Save this new count to the database.
            boolean updated = _14h_updateUserAttempts(user.getId(), newAttempts);
            if (!updated) return "FAILURE: Server error. Please try again.";

            //  STEP 7c: Send back the bad news.
            if (newAttempts >= 5) {
                return "LOCKED|Account locked. Too many failed attempts.";
            } else {
                return "FAILURE: Invalid email or password. " + (5 - newAttempts) + " attempts remaining.";
            }
        }
    }







    //------------------------------------------------------------------------------------------------------------------
    //   VALIDATION
    //------------------------------------------------------------------------------------------------------------------

    /**
     *  Validates a product using our centralized _20_ValidBusinessRules class.
     *  This ensures all validation rules are consistent across the application.
     *     @param usr The product to validate
     */
    private static void _14a_ValidateUser(_13_ModelUser usr) {

        // STEP 1: NAME
        _20a_validAlphaNumeric("Name", usr.getName(), 1, 100);

        // STEP 5: EMAIL
        _20a_validAlphaNumeric("Email", usr.getEmail(), 0, 100);

        // STEP 3: PHONE
        _20a_validAlphaNumeric("Phone", usr.getPhone(), 1, 15);
    }

    // =========================================================================
    //  CRUD OPERATIONS
    // =========================================================================



    /**
     *  "C" RUD: CREATE:  ADD USER ON SIGN-UP

     *  Adds a new user to the database (from the SIGNUP command).
     *     @param user The '_13_ModelUser' object from the client. (This object *already* contains the *hashed* password).
     *     @return A "SUCCESS" or "FAILURE" protocol string.
     */
    public String _14b_addNewUser(_13_ModelUser user) {

        //  Validation first
        _14a_ValidateUser(user);

        //  STEP 1: Check if a user with this email *already* exists.
        if (_14g_doesUserExist(user.getEmail())) {
            return "FAILURE: An account with this email already exists.";
        }
        //  testing:
        System.out.printf("DAO INSERT -> role=[%s], name=[%s], hash=[%s], email=[%s], phone=[%s]%n",
                user.getRole(), user.getName(), user.getPwdHash(), user.getEmail(), user.getPhone());


        //  STEP 2: Define the "safe" SQL query.
        //     We set 'loginAttempts' to 0 for a new user.
        String sql = "INSERT INTO `user` (role, name, pwdHash, email, phone, loginAttempts) VALUES (?, ?, ?, ?, ?, 0)";

        try (Connection connect = dataSource.getConnection();
             PreparedStatement prepStmt = connect.prepareStatement(sql)) {
            //  STEP 3: Fill in the blanks.
            prepStmt.setString(1, user.getRole());
            prepStmt.setString(2, user.getName());
            prepStmt.setString(3, user.getPwdHash()); //  The secure hash
            prepStmt.setString(4, user.getEmail());
            prepStmt.setString(5, user.getPhone());

            //  STEP 4: Execute the command.
            prepStmt.executeUpdate();

            //  STEP 5: Send back the good news.
            return "SUCCESS";

        } catch (SQLException e) {
            error("Error adding user: " + e.getMessage());
            return "FAILURE: Server database error: " + e.getMessage();
        }
    }





    /**
     *  C "R" UD: READ

     *  Gets all users from the DB and "serializes" them into a single string for the client.
     *     @return A protocol string, e.g., "SUCCESS|user1;user2;..."
     */
    public String _14c_getAllUsersAsString() {
        List<_13_ModelUser> userList = new ArrayList<>();
        String sql = "SELECT * FROM user";

        try (Connection connect = dataSource.getConnection();
             PreparedStatement prepStmt = connect.prepareStatement(sql);
             ResultSet resSet = prepStmt.executeQuery()) {

            //  STEP 1: Loop through every row in the result.
            while (resSet.next()) {
                //  STEP 2: Create a 'user' object for each row.
                userList.add(new _13_ModelUser(
                        resSet.getString("id"),
                        resSet.getString("role"),
                        resSet.getString("name"),
                        resSet.getString("pwdHash"),
                        resSet.getString("email"),
                        resSet.getString("phone"),
                        resSet.getInt("loginAttempts")
                ));
            }

            //  STEP 3: "Serialize" the list into our protocol string.
            String data = userList.stream()
                    .map(this::_14i_serializeUser) //  Use our private helper
                    .collect(Collectors.joining(";")); //  Join with ";"

            //  STEP 4: Send the data back.
            return "SUCCESS|" + data;

        } catch (SQLException e) {
            error("Error fetching users: " + e.getMessage());
            return "FAILURE: Could not fetch user list from database.";
        }
    }




    /**
     *  CR "U" D: UPDATE

     *  Updates an existing user's information in the database.
     *     @param user The '_13_ModelUser' object with the new data.
     *     @throws SQLException If the update fails.
     */
    public void _14d_updateUser(_13_ModelUser user) throws SQLException {

        //  Validation first
        _14a_ValidateUser(user);

        String sql = "UPDATE user SET role = ?, name = ?, pwdHash = ?, email = ?, phone = ?, loginAttempts = ? WHERE id = ?";
        try (Connection connect = dataSource.getConnection();
             PreparedStatement prepStmt = connect.prepareStatement(sql)) {
            //  STEP 1: Fill in all the blanks.
            prepStmt.setString(1, user.getRole());
            prepStmt.setString(2, user.getName());
            prepStmt.setString(3, user.getPwdHash()); //  The new or old hash
            prepStmt.setString(4, user.getEmail());
            prepStmt.setString(5, user.getPhone());
            prepStmt.setInt(6, user.getLoginAttempts());
            prepStmt.setString(7, user.getId()); //  This is for the 'WHERE'

            //  STEP 2: Run the command.
            prepStmt.executeUpdate();
        }
    }





    /**
     *  CRU "D": DELETE

     *  Removes a list of users from the database by their IDs.
     *     @param userIds A List of user ID strings to delete.
     *     @throws SQLException If the delete fails.
     */
    public void _14e_removeUsersById(List<String> userIds) throws SQLException {
        String sql = "DELETE FROM user WHERE id = ?";
        try (Connection connect = dataSource.getConnection();
             PreparedStatement prepStmt = connect.prepareStatement(sql)) {
            //  STEP 1: This is a "batch" delete. We loop through all the IDs...
            for (String id : userIds) {
                //  STEP 2: ...fill in the '?' for this ID...
                prepStmt.setString(1, id);
                //  STEP 3: ...and 'add it to the batch' (don't run it yet).
                prepStmt.addBatch();
            }
            //  STEP 4: Execute all commands in the batch at once.
            prepStmt.executeBatch();
        }
    }






    // =========================================================================
    //  PRIVATE HELPER "TOOLS" FOR THIS CLASS
    // =========================================================================

    /**
     * A private tool to get a single user's data from the DB.
     *     @param email The email to search for.
     *     @return A '_13_ModelUser' object, or 'null' if not found.
     */
    private _13_ModelUser _14f_getUserFromDB(String email) {
        String query = "SELECT * FROM user WHERE email = ?";
        try (Connection connect = dataSource.getConnection();
             PreparedStatement prepStmt = connect.prepareStatement(query)) {
            //  STEP 1: Fill in the '?' with the email.
            prepStmt.setString(1, email);

            //  STEP 2: Run the query.
            try (ResultSet rs = prepStmt.executeQuery()) {
                //  STEP 3: Check if the 'ResultSet' has *any* rows.
                if (rs.next()) {
                    //  If 'yes', build the user object and return it.
                    return new _13_ModelUser(
                            rs.getString("id"),
                            rs.getString("role"),
                            rs.getString("name"),
                            rs.getString("pwdHash"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getInt("loginAttempts")
                    );
                }
            }
        } catch (SQLException e) {
            error("Error getting user by email: " + e.getMessage());
        }
        //  STEP 4: If we get here, no user was found.
        return null;
    }





    /**
     * A private tool to check if an email already exists (for sign-up).
     *     @param email The email to check.
     *     @return 'true' if the email is taken, 'false' if not.
     */
    private boolean _14g_doesUserExist(String email) {
        //  This query is faster. It just checks for '1' (existence).
        String sql = "SELECT 1 FROM user WHERE email = ?";
        try (Connection connect = dataSource.getConnection();
             PreparedStatement prepStmt = connect.prepareStatement(sql)) {
            prepStmt.setString(1, email);
            try (ResultSet rs = prepStmt.executeQuery()) {
                //  If 'rs.next()' is true, it means we found a row.
                return rs.next();
            }
        } catch (SQLException e) {
            error("Error checking for user existence: " + e.getMessage());
            return false; //  Be safe and say 'false' if the DB errors.
        }
    }





    /**
     * A private tool to update *only* the login attempt count.
     *
     * @param userId   The ID of the user to update.
     * @param attempts The new attempt count (e.g., 0 or 3).
     */
    private boolean _14h_updateUserAttempts(String userId, int attempts) {
        String sql = "UPDATE user SET loginAttempts = ? WHERE id = ?";
        try (Connection connect = dataSource.getConnection();
             PreparedStatement prepStmt = connect.prepareStatement(sql)) {
            prepStmt.setInt(1, attempts);
            prepStmt.setString(2, userId);
            int rowsAffected = prepStmt.executeUpdate();
            return rowsAffected > 0; // true if it worked
        } catch (SQLException e) {
            error("Database error updating attempts: " + e.getMessage());
        }
        return false;
    }





    /**
     * Private "helper" tool to turn a user object into a string.
     * Format: id|role|name|pwdHash|email|phone|loginAttempts
     */
    private String _14i_serializeUser(_13_ModelUser user) {
        return String.join("|",
                user.getId(),
                user.getRole(),
                user.getName(),
                user.getPwdHash(),
                user.getEmail(),
                user.getPhone(),
                String.valueOf(user.getLoginAttempts())
        );
    }
}