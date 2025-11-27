package com.ims;


import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;



public class _42_ValidationsTest {

    // -------------- SKU -----------------

    @Test
    void validSku_passes() {
        assertDoesNotThrow(() ->
                _20_ValidBusinessRules._20a_validAlphaNumeric("Sku", "ABC123", 3, 10)
        );
    }

    @Test
    void emptySku_throws() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> _20_ValidBusinessRules._20a_validAlphaNumeric("Sku", "   ", 3, 10)
        );
        System.out.println("Message = " + ex.getMessage());
    }


    // -------------- NAME -----------------

    @Test
    void validName_passes() {
        assertDoesNotThrow(() ->
                _20_ValidBusinessRules._20a_validAlphaNumeric(
                        "Name",           // must match your fieldType string
                        "Road Runner",
                        2,
                        40
                )
        );
    }

    @Test
    void emptyName_throws() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> _20_ValidBusinessRules._20a_validAlphaNumeric(
                        "Name",
                        "",
                        2,
                        40
                )
        );
        assertTrue(ex.getMessage().contains("must not be empty"));
    }

    @Test
    void nameTooShort_throws() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> _20_ValidBusinessRules._20a_validAlphaNumeric(
                        "Name",
                        "A",   // too short
                        2,
                        40
                )
        );
        assertTrue(ex.getMessage().contains("too short"));
    }


    // -------------- CATEGORY -----------------

    @Test
    void validCategory_passes() {
        assertDoesNotThrow(() ->
                _20_ValidBusinessRules._20a_validAlphaNumeric(
                        "Category",
                        "Electronics",
                        3,
                        30
                )
        );
    }

    @Test
    void emptyCategory_throws() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> _20_ValidBusinessRules._20a_validAlphaNumeric(
                        "Category",
                        "",
                        3,
                        30
                )
        );
        assertTrue(ex.getMessage().contains("must not be empty"));
    }

    @Test
    void categoryTooLong_throws() {
        String longCat = "ThisCategoryNameIsWayTooLongForTheField";
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> _20_ValidBusinessRules._20a_validAlphaNumeric(
                        "Category",
                        longCat,
                        3,
                        30
                )
        );
        assertTrue(ex.getMessage().contains("too long"));
    }


    // -------------- DESCRIPTION -----------------

    @Test
    void validDescription_passes() {
        assertDoesNotThrow(() ->
                _20_ValidBusinessRules._20a_validAlphaNumeric(
                        "Description",
                        "A nice, durable product for daily use.",
                        5,
                        255
                )
        );
    }

    @Test
    void emptyDescription_throws() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> _20_ValidBusinessRules._20a_validAlphaNumeric(
                        "Description",
                        "",
                        5,
                        255
                )
        );
        assertTrue(ex.getMessage().contains("must not be empty"));
    }


    // -------------- ADDRESS -----------------

    @Test
    void validAddress_passes() {
        assertDoesNotThrow(() ->
                _20_ValidBusinessRules._20a_validAlphaNumeric(
                        "Address",
                        "123 Main St, Apt #4B",
                        5,
                        100
                )
        );
    }

    @Test
    void emptyAddress_throws() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> _20_ValidBusinessRules._20a_validAlphaNumeric(
                        "Address",
                        "",
                        5,
                        100
                )
        );
        assertTrue(ex.getMessage().contains("must not be empty"));
    }


    // -------------- EMAIL -----------------

    @Test
    void validEmail_passes() {
        assertDoesNotThrow(() ->
                _20_ValidBusinessRules._20a_validAlphaNumeric(
                        "Email",
                        "wile.coyote@example.com",
                        5,
                        100
                )
        );
    }

    @Test
    void emptyEmail_throws() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> _20_ValidBusinessRules._20a_validAlphaNumeric(
                        "Email",
                        "",
                        5,
                        100
                )
        );
        assertTrue(ex.getMessage().contains("must not be empty"));
    }

    @Test
    void badlyFormattedEmail_throws() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> _20_ValidBusinessRules._20a_validAlphaNumeric(
                        "Email",
                        "not-an-email",
                        5,
                        100
                )
        );
        assertTrue(ex.getMessage().contains("email"));
    }


    // -------------- PHONE -----------------

    @Test
    void validPhone_passes() {
        assertDoesNotThrow(() ->
                _20_ValidBusinessRules._20a_validAlphaNumeric(
                        "Phone",
                        "4075551212",
                        10,
                        15
                )
        );
    }

    @Test
    void emptyPhone_throws() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> _20_ValidBusinessRules._20a_validAlphaNumeric(
                        "Phone",
                        "",
                        10,
                        15
                )
        );
        assertTrue(ex.getMessage().contains("must not be empty"));
    }

    @Test
    void badlyFormattedPhone_throws() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> _20_ValidBusinessRules._20a_validAlphaNumeric(
                        "Phone",
                        "407-555-1212",
                        10,
                        15
                )
        );
        assertTrue(ex.getMessage().contains("Phone number (no dashes nor parenthesis)"));
    }


    // -------------- PASSWORD -----------------

    @Test
    void validPassword_passes() {
        assertDoesNotThrow(() ->
                _20_ValidBusinessRules._20a_validAlphaNumeric(
                        "Password",
                        "Abc@123!",
                        4,
                        64
                )
        );
    }

    @Test
    void emptyPassword_throws() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> _20_ValidBusinessRules._20a_validAlphaNumeric(
                        "Password",
                        "",
                        4,
                        64
                )
        );
        assertTrue(ex.getMessage().contains("must not be empty"));
    }

    @Test
    void weakPassword_throws() {   // too short / weak
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> _20_ValidBusinessRules._20a_validAlphaNumeric(
                        "Password",
                        "abc",
                        4,
                        64
                )
        );
        // adjust the substring to match whatever message you throw
        assertTrue(ex.getMessage().toLowerCase().contains("password"));
    }



    // -------------- QUANTITY -----------------

    @Test
    void badQuantity_throws() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> _20_ValidBusinessRules._20b_ValidQuantity("-5")
        );
        assertTrue(ex.getMessage().contains("non-negative"));
    }



    // -------------- PRICE -----------------

    @Test
    void validPrice_passes() {
        // Happy-path: reasonable price with 2 decimals
        assertDoesNotThrow(() ->
                _20_ValidBusinessRules._20c_ValidPrice("19.99")
        );
    }

    @Test
    void zeroPrice_passes_or_throwsDependingOnRule() {
        // Decide your rule:
        //  - If price=0 is allowed, keep assertDoesNotThrow
        //  - If price must be > 0, change this test to expect an exception
        assertDoesNotThrow(() ->
                _20_ValidBusinessRules._20c_ValidPrice("0.00")
        );
    }

    @Test
    void emptyPrice_throws() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> _20_ValidBusinessRules._20c_ValidPrice("")
        );
        // Adjust to whatever message you use for empty price
        assertTrue(ex.getMessage().toLowerCase().contains("price"));
        assertTrue(ex.getMessage().toLowerCase().contains("required"));
    }

    @Test
    void negativePrice_throws() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> _20_ValidBusinessRules._20c_ValidPrice("-5.00")
        );
        // e.g. "non-negative", "positive", etc. – match your rule
        assertTrue(ex.getMessage().toLowerCase().contains("non-negative")
                || ex.getMessage().toLowerCase().contains("positive"));
    }

    @Test
    void badlyFormattedPrice_throws() {
        // Comma instead of dot, or letters, etc.
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> _20_ValidBusinessRules._20c_ValidPrice("12,34")
        );
        // Match your wording here
        assertTrue(ex.getMessage().contains("Price required: non-negative number with up to 2 decimals"));
    }

    @Test
    void hugePrice_passes() {
        assertDoesNotThrow(() ->
                _20_ValidBusinessRules._20c_ValidPrice("9999999999.99")
        );
    }


}
