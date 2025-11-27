package com.ims;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.*;




/**
 *  Integration Tests covering TC-01 to TC-09.
 *  We cover the Client <-> Server <-> Database integration, using the real SSL connection and protocol.
 *  ASSUMPTIONS:
 *      1. Main GUI class is named 'MainApp'.
 *      2. FXML IDs are: #skuField, #nameField, #qtyField, #productTable, #addButton, #updateButton, #removeButton, #refreshButton.
 *      3. Database is MySQL running on localhost.

 *  IMPORTANT: Inventory Server is running before this test starts.
 */


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class _43_IntegrationTest {

    private static _03_ClientServerLine clientLine;

    @BeforeAll
    static void setUpClient() throws Exception {
        // Create the client "phone line" and connect to the server.
        // This will use your client-config.json (host, port, truststore, etc.)
        clientLine = new _03_ClientServerLine();

        // If _03a_connect throws, the whole test class will fail, which is what we want.
        clientLine._03a_connect();
    }

    @AfterAll
    static void tearDownClient() {
        if (clientLine != null) {
            try {
                clientLine._03c_disconnect();
            } catch (Exception ex) {
                // Best-effort: we don't want teardown to crash the test run.
                System.err.println("Warning: error while disconnecting client: " + ex.getMessage());
            }
        }
    }


    /**
     *   TC-01:
     *   Trying to log in with a clearly invalid user and password.
     *   We expect some kind of FAILURE / INVALID / ERROR response from the server.
     */
    @Test
    @Order(1)
    void tc01_loginWithInvalidCredentials_returnsFailureStyleMessage() {
        String email = "no_such_user_12345@example.com";
        String password = "WrongPassword!123";

        String response = clientLine._03d_attemptToLogin(email, password);

        assertNotNull(response, "Server login response must not be null.");

        String upper = response.toUpperCase();

        assertTrue(
                upper.contains("FAILURE") ||
                        upper.contains("INVALID") ||
                        upper.contains("ERROR"),
                () -> "Expected a failure-style message for bad credentials, but got: " + response
        );
    }


    /**
     *   TC-02:
     *   Asking the server for all products.
     *   The protocol says: "SUCCESS|product1;product2;...;productN"
     *   We assert that the response starts with "SUCCESS|".
     */
    @Test
    @Order(2)
    void tc02_getAllProducts_returnsSuccessPrefix() {
        String response = clientLine._03f_getAllProducts();

        assertNotNull(response, "getAllProducts() response must not be null.");
        assertFalse(response.isBlank(), "getAllProducts() response must not be blank.");

        assertTrue(
                response.startsWith("SUCCESS|"),
                () -> "Expected response starting with 'SUCCESS|' for products, but got: " + response
        );
    }


    /**
     *   TC-03:
     *   Asking the server for all users.
     *   We expect a similar "SUCCESS|..." pattern.
     */
    @Test
    @Order(3)
    void tc03_getAllUsers_returnsSuccessPrefix() {
        String response = clientLine._03l_getAllUsers();

        assertNotNull(response, "getAllUsers() response must not be null.");
        assertFalse(response.isBlank(), "getAllUsers() response must not be blank.");

        assertTrue(
                response.startsWith("SUCCESS|"),
                () -> "Expected response starting with 'SUCCESS|' for users, but got: " + response
        );
    }


    /**
     *   TC-04:
     *   Asking the server for all suppliers.
     *   Again, we expect a "SUCCESS|..." pattern.
     */
    @Test
    @Order(4)
    void tc04_getAllSuppliers_returnsSuccessPrefix() {
        String response = clientLine._03o_getAllSuppliers();

        assertNotNull(response, "getAllSuppliers() response must not be null.");
        assertFalse(response.isBlank(), "getAllSuppliers() response must not be blank.");

        assertTrue(
                response.startsWith("SUCCESS|"),
                () -> "Expected response starting with 'SUCCESS|' for suppliers, but got: " + response
        );
    }
}
