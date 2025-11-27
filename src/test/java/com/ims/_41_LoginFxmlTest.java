package com.ims;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class _41_LoginFxmlTest {

    private static boolean jfxStarted = false;
    private static Map<String, Object> namespace;

    @BeforeAll
    static void loadFxml() throws Exception {
        // 1) Make sure JavaFX toolkit is initialized (needed because login.fxml loads an <Image>)
        if (!jfxStarted) {
            Platform.startup(() -> {});
            jfxStarted = true;
        }

        // 2) Load the FXML
        URL fxml = _41_LoginFxmlTest.class.getResource("/login.fxml");
        assertNotNull(fxml, "Could not find /login.fxml on the classpath");

        FXMLLoader loader = new FXMLLoader(fxml);
        loader.load();                    // We don't actually need the Parent here
        namespace = loader.getNamespace(); // fx:id -> object map
        assertNotNull(namespace, "FXML namespace should not be null");
    }

    @Test
    void emailField_present_and_isTextField() {
        Object node = namespace.get("emailField");
        assertNotNull(node, "fx:id=\"emailField\" should exist in login.fxml");
        assertTrue(node instanceof TextField, "emailField should be a TextField");
    }

    @Test
    void passwordField_present_and_isPasswordField() {
        Object node = namespace.get("passwordField");
        assertNotNull(node, "fx:id=\"passwordField\" should exist in login.fxml");
        assertTrue(node instanceof PasswordField, "passwordField should be a PasswordField");
    }

    @Test
    void loginButton_present_and_isButton() {
        Object node = namespace.get("loginButton");
        assertNotNull(node, "fx:id=\"loginButton\" should exist in login.fxml");
        assertTrue(node instanceof Button, "loginButton should be a Button");
    }

    @Test
    void signUpButton_present_and_isButton() {
        Object node = namespace.get("signUpButton");
        assertNotNull(node, "fx:id=\"signUpButton\" should exist in login.fxml");
        assertTrue(node instanceof Button, "signUpButton should be a Button");
    }
}
