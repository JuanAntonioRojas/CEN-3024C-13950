package com.ims;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

//  This class is DEPRECATED now, because I'm using "HikariDataSource" instead, for multiple connections.
//  I'm keeping it for backward compatibility or testing. This proj. is my springboard for bigger ones.


/**
 *  This is the "Master Key" class for the database.

 *  This is a special utility class that has only 'one' job: to connect to the MySQL database.
 *  It uses the 'config.properties' file (via the _05_ConfigLoader) to get the secret password and address.

 *  The _01_InventoryServer calls this once to get the 'master' connection, and then passes that connection to all the
 *     'department specialists' (the DB classes).
 *  This centralized class is ideal for the products, users and suppliers data models.
 */
public class _10_ModelConnectToDB {

    //  A private constructor so nobody can make an 'instance' of this.
    private _10_ModelConnectToDB() {}

    /**
     *  The one and only public method. It gets the database connection.
     *     @return The 'Connection' object (the "master ledger").
     *     @throws SQLException If it can't connect for any reason.
     */
    public static Connection getConnection_10a() throws SQLException {
        //  Step 1: Get the "little black book" (the config file).
        Properties config = _05_ConfigLoader.load();
        if (config == null) {
            //  If there's no config file, we can't connect.
            throw new SQLException("FATAL: Could not load config.properties file.");
        }

        //  Step 2: Get the database address, user, and password from the config file.
        String url = config.getProperty("db.url");
        String user = config.getProperty("db.user");
        String pass = config.getProperty("db.pass");

        //  Step 2.1:  VALIDATE THE KEYS
        //     This is our 'second' check (the "missing cheese").
        //     We demand that the keys we 'need' actually 'exist'.
        //     It would be fatal if we pass the first non-null (config does exist), but it's empty or corrupted.
        //     Without this "validation" the very next line of code, DriverManager.getConnection(url, user, pass),
        //        will crash with a "NullPointerException" because the url, user or pass are null.
        if (url == null || url.isBlank() ||  user == null || user.isBlank() || pass == null) {// A password 'can' be blank, but not 'null'
            //  If any key is missing, we "throw" an error with a 'helpful' message.
            _00_Utils.error("FATAL: 'db.url', 'db.user', or 'db.pass' is missing from config.properties.");
            throw new SQLException("FATAL: Database configuration is incomplete.");
        }

        //  Step 3: CORE. Try to connect to the database using the credentials.
        try {
            Connection connection = DriverManager.getConnection(url, user, pass);
            //  If we get here, we're connected!
            _00_Utils.println("✅ Database connection to '" + url + "' established.");
            return connection;

        } catch (SQLException e) {
            //  If we're here, something went wrong (bad password, DB is turned off, etc.)
            _00_Utils.error("FATAL: Database connection failed!");
            _00_Utils.error(e.getMessage());
            //  We "throw" the error up to the _01_InventoryServer, so it knows to shut down.
            throw e;
        }
    }
}