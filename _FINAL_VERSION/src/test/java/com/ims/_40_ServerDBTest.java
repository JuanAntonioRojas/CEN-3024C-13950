
package com.ims;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;




/**
 * Simple integration tests (no JavaFX, no TestFX):
 * - Can we load config.properties?
 * - Can we connect to the database and run a simple query?
 *
 * If these pass, it means:
 *  - Your config file is visible on the classpath.
 *  - Your DB URL / user / password are correct.
 *  - MySQL server is running and reachable.
 */
public class _40_ServerDBTest {

    @Test
    @DisplayName("Config file loads and has DB settings")
    void configLoadsAndHasDbSettings() {
        // Use the same loader the server uses
        Properties props = _05_ConfigLoader.load();

        assertNotNull(props, "config.properties should be loadable (check it is under src/main/resources).");

        String url  = props.getProperty("db.url");
        String user = props.getProperty("db.user");
        String pass = props.getProperty("db.password");

        assertNotNull(url,  "db.url must be set in config.properties");
        assertNotNull(user, "db.user must be set in config.properties");
        assertNotNull(pass, "db.password must be set in config.properties");
    }

    @Test
    @DisplayName("Database is reachable and responds to SELECT 1")
    void databaseIsReachable() throws Exception {
        // 1) Load config like the server
        Properties props = _05_ConfigLoader.load();
        assertNotNull(props, "config.properties should be loadable.");

        String url  = props.getProperty("db.url");
        String user = props.getProperty("db.user");
        String pass = props.getProperty("db.password");

        assertNotNull(url,  "db.url must be set.");
        assertNotNull(user, "db.user must be set.");
        assertNotNull(pass, "db.password must be set.");

        // 2) Try to open a plain JDBC connection and run a simple query.
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {

            assertFalse(conn.isClosed(), "Connection should be open if DB is reachable.");

            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT 1")) {

                assertTrue(rs.next(), "SELECT 1 should return at least one row.");
                int one = rs.getInt(1);
                assertEquals(1, one, "DB should return 1 for SELECT 1.");
            }
        }
    }
}

/**
 * This test passes with exit code 0, that means:
      1. config.properties is loading correctly from the classpath.
      2. The db.url, db.user, db.password are all good.
 */
