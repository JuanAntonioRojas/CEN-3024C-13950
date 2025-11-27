package com.ims;

import java.util.regex.Pattern;


/**
 *  The main components of this helper class are:
 *  1. String cleanup like safeTrim, checking blank, null, etc.
 *  2. Regex checks like qty.matches("\\d+"), email/phone patterns, etc.
 *  3. Parsing + throwing helpful exceptions (e.g., Integer.parseInt, catching NumberFormatException,
 *        throwing IllegalArgumentException with custom messages).

 *   WEBSITE: jenkov.com In his Java Regex tutorials Jakob Jenkov explain exactly what patterns like \\d+, ^...$, etc.
 *      mean and how they are matched against strings.

 *   YOUTUBE: Telusko’s site (https://www.youtube.com/playlist?list=PLsyeobzWxl7pe_IiTfNyr55kwJPWbgxB5)
 *   In his big Java beginner series he spends a lot of time on String methods (trim, isEmpty, etc.), Integer.parseInt,
 *      try/catch and custom error messages, defensive checks before using values.

 *   YOUTUBE: FreeCodeCamp: In project-style lessons they’ll say “we need to validate this field,” then show
 *      if (value == null || value.isEmpty()) and talk through why we must guard inputs, etc.
 */



//------------------------------------------------------------------------------------------------------------------
//   VALIDATION
//------------------------------------------------------------------------------------------------------------------


public class _20_ValidBusinessRules {

    //  For the validation I ask:
    public static String please = "Please enter a valid ";
    public static String chars = " alpha-numeric characters).";
    public static String passChars = " alpha-numeric (lowercase & UPPERCASE) characters, number(s), and symbol(s), and NO spaces).";


    //  REGEX:
    //     The "matching magic code" is a Regular Expression, or "Regex" that defines what a "valid" value should look like.
    //     = digits, letters, spaces, commas, dots, apostrophes, hyphens, and symbols (&, #, -, /, etc.)

    //  GENERAL NAME / TEXT REGEX (Simplified)
    //  This replaces the strict NAME_REGEX.

    //     1. \p{L} → any letter from any language (A–Z, á, ñ, ø, Ж, 字, etc.).
    //     2. \p{M} → combining marks (accent marks that follow a base letter).
    //     3. Digits (0-9), spaces ( ), dots (.), commas (,), apostrophes ('), hyphens (-), underscores (_), ampersands (&),
    //           question marks (?), and other symbols: at (@), hash (#), dollar ($), percent (%), caret (^)
    //     4. File paths: Dots . (img1.jpg), Forward slash / (Mac, Linux), Backslash \ (Windows: src\img\img1.jpg), Colon : (e.g., C:\src\)
    //  It does NOT enforce strict "Word Separator Word" structure, so "Acme Inc." works.

    //  NAMES/BRANDS:  e.g.: "Juan Carlos", "O’Connor", "Jean-Luc", "J. R. R. Tolkien"
    //  FILE PATHS: e.g.: C:\src\main\resources\img\2.jpg

    private static final String TEXT_REGEX = "^[\\p{L}\\p{M}0-9 '_@#$%^&/.:-]+$";


    //  SKU:   e.g., "ABC-123", "mx_200.5/BLK"
    private static final String SKU_REGEX = "^[A-Za-z0-9](?:[A-Za-z0-9._/-]{0,31})$"; // 1–32 chars, tweak length as needed

    //  CATEGORY:  e.g., "Men’s Shoes", "Audio/Video", "Books & Media"
    private static final String CATEGORY_REGEX = "^[\\p{L}\\p{M}0-9]+(?:[ &/+.'-][\\p{L}\\p{M}0-9]+)*$";


    //  DESCRIPTION:  allows punctuation, quotes, commas, etc.;
    //  A "mediumtext" in MySQL can hold up to 16,777,215 characters (about 16MB), so here I'll settle for
    //     length 1–10,000 chars (reasonable for product descriptions)
    private static final String DESCRIPTION_REGEX = "^[^\\p{Cntrl}<>]+$";
    private static final int MAX_DESCRIPTION_LENGTH = 10000; // 10k characters


    //  ADDRESS: = digits, letters, spaces, commas, dots, apostrophes, hyphens, etc.
    private static final String ADDRESS_REGEX = "^[\\p{L}\\p{M}\\p{N} ,.'\\u2019#\\-/()]+$";

    //  EMAIL: letters, digits a dot and @ symbol must be there.
    //     Get the regex magic code. This is complicated, but it's a standard way to check for "name@provider.com"
    //     This regex was defined by the Internet Engineering Task Force (IETF) standards.
    //     This is a very accurate regex, not perfect for every case, but it handles the vast majority of valid emails.
    private static final String EMAIL_REGEX =  "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

    //  PHONE
    //     Keep only digits and require 10–15 digits (works for most formats)
    private static final String PHONE_REGEX = "\\d{0,15}";

    //  IMAGE
    //     Characters allowed: letters, digits, dot, underscore, percent, plus, hyphen. Case-insensitive extension check using (?i:...).
    //     Allows nested folders: img/subdir/file.png, and only Jpeg, Png and Gif files (no webp, svg, or any other files).
   //private static final String IMAGE_REGEX = "^img(?:/[A-Za-z0-9_%]+)*\\\\.(?i:(?:png|jpe?g|gif))$";  //  with directory
    private static final String IMAGE_REGEX = ".*\\.(?i:(?:png|jpe?g|gif))$";



    //
    //  PASSWORD
    //    Explanation (piece by piece):
    //      ^                     -> start of the text
    //      (?=.*[a-z])           -> there must be at least one lowercase letter
    //      (?=.*[A-Z])           -> there must be at least one UPPERCASE letter
    //      (?=.*\\d)             -> there must be at least one digit (0-9)
    //      (?=.*[^A-Za-z0-9])    -> there must be at least one symbol (not a letter or digit)
    //      \\S{5,50}             -> total length between 8 and 64 characters, AND no whitespace
    //      $                     -> end of the text
    //
    //   NOTE: We double the backslashes in Java strings (e.g., \\d) because '\' is an escape character in Java.
    // private static final String PASSWD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9])\\S{4,64}$";
    //private static final String PASSWD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s])\\S{4,64}$";
    //  This is the best regex for pwd, however for Testing is horrible.
    //  Solution: Copy-Paste the email address. That way I'll never for get the testing password... LOL.

    //  Better solution:
    //  Test for each type separately:
    private static final Pattern PWD_LOWER = Pattern.compile("[a-z]");
    private static final Pattern PWD_UPPER = Pattern.compile("[A-Z]");
    private static final Pattern PWD_DIGIT = Pattern.compile("\\d");
    private static final Pattern PWD_SYMBOL = Pattern.compile("[^\\w\\s]");
    private static final Pattern PWD_SPACE = Pattern.compile("\\s");




    //  We "compile" the magic regex code once into a "Pattern" so the computer can use it really fast.
    private static final Pattern TEXT_PAT = Pattern.compile(TEXT_REGEX);
    private static final Pattern SKU_PAT = Pattern.compile(SKU_REGEX);
    //private static final Pattern CATEGORY_PAT = Pattern.compile(CATEGORY_REGEX);
    private static final Pattern DESCR_PAT = Pattern.compile(DESCRIPTION_REGEX);
    private static final Pattern ADDR_PAT = Pattern.compile(ADDRESS_REGEX);
    private static final Pattern EMAIL_PAT = Pattern.compile(EMAIL_REGEX);
    private static final Pattern PHONE_PAT = Pattern.compile(PHONE_REGEX);
    private static final Pattern IMAGE_PAT = Pattern.compile(IMAGE_REGEX);
    //private static final Pattern PASSWD_PAT = Pattern.compile(PASSWD_REGEX);



    /**
     *   This returns the given string trimmed, or an empty string if the input is null.
     *   Using ".esEmpty()" might throw a NullPointerException.
     *   This avoids NullPointerException and standardizes blank handling for validators.
     *   Examples:
     *     safeTrim(null)     -> ""
     *     safeTrim("  hi  ") -> "hi"
     *     safeTrim("   ")    -> ""   // still blank after trimming
     */
    public static String safeTrim(String txt) {
        return (txt == null) ? "" : txt.trim();
    }



    /**
     *   The logic of this validator is “exception-based” because it treats invalid input as a programming/data error
     *      and signals it by throwing an exception, rather than returning false or an error code.
     *   Here “exception-based” means: validation failure is reported via exceptions, not via return values.
     *   Method callers assume that if this method returns true, the value is valid. Instead, if there's something is wrong
     *      it never returns (not even a "false"), but throws an IllegalArgumentException (or wraps a NumberFormatException),
     *      with a clear message we can see in the UI (similar to _30i_showError), and No more code runs after the throw.
     *   The UI / controller code has to catches the exception and decide what to do:
     *      1. show an error message,“tell the user what’s wrong and let them try again.”
     *      2. keep the form open,
     *      3. keep the cursor in the bad field,
     *      4. let the user fix the field, type the value, and click “Save” one more time.
     *   So this exception-based validator only affects the “Save” path.
     *   Cancel (or Close, Back, etc.) is the escape hatch that never even touches the validator.
     */

    public static boolean _20a_validAlphaNumeric(String fieldType, String fieldText, int minLength, int maxLength) {  //  Valid is TRUE

        //  NULL: Normalize/clean the input.
        //        If the caller passes null or only whitespace, we don't even bother with regex.
        if (fieldText == null || fieldText.isBlank()) {
            throw new IllegalArgumentException(fieldType + " must not be empty");
        }

        //  LENGTH: Enforce min/max length.
        //          Make sure the raw text respects the global min/max for that field, before trimming/collapsing spaces.
        if (fieldText.length() < minLength)
            throw new IllegalArgumentException(fieldType + " too short (min " + minLength + " characters)");
        if (fieldText.length() > maxLength)
            throw new IllegalArgumentException(fieldType + " too long (max " + maxLength + " characters)");

        //  TRIM:  We hav to trim() to remove leading and trailing whitespace.
        //         Then "replaceAll" collapses any spurt of whitespace (spaces, tabs, newlines, non-breaking spaces, etc.) to a single space.

        String fldInput = safeTrim(fieldText);   //  trim only (spaces preserved inside)
        //  Or trim + collapse all internal whitespace (spaces, tabs, etc.) into a single space.
        String txtInput = safeTrim(fieldText).replaceAll("\\s+", " ");

        /*  No bueno. It's better to separate them by type. Now using the private static vars set above.
        //  BASIC 4 in 1:
        if(fieldType.equals("Name") || fieldType.equals("Sku") || fieldType.equals("Category") || fieldType.equals("Description")) {
            if (!txtInput.matches("^[A-Za-z0-9-]+$")) {
                throw new IllegalArgumentException( fieldType + " contains invalid characters. Only letters, numbers, and special characters are allowed." );
            }
        }*/

        //  Bueno: Based on `fieldType`, we apply a specific regex Pattern and rules (NAME_PAT, SKU_PAT, etc.).
        //  The big idea: one method, multiple “profiles” of validation.

        switch (fieldType) {
            case "Name":
                //   Return 'true' and break, only if the name 'exactly' matches the pattern.
                if (!TEXT_PAT.matcher(txtInput).matches()) {
                    throw new IllegalArgumentException("Name: " + minLength + " to " + maxLength + " letters, spaces, apostrophes, hyphens, and periods only.");
                }
                break;


            case "Sku":
                if (!SKU_PAT.matcher(fldInput).matches()) {
                    throw new IllegalArgumentException("Sku: " + minLength + " to " + maxLength + " letters/digits plus . _ - /, no spaces.");
                }
                break;

            /*
             */
            case "Brand":  //  Deprecated in favor of TEXT_REGEX
                //if (!txtInput.matches(String.valueOf(CATEGORY_PAT))) {//  incorrect...
                //  We're converting a Pattern object to a String, which doesn't give us the regex
                if (!TEXT_PAT.matcher(txtInput).matches()) {  //  Correct
                    throw new IllegalArgumentException("Brand: " + minLength + " to " + maxLength +
                                    " characters, using letters, digits, spaces, and standard punctuation.");
                }
                break;


            case "Description":
                if (fldInput == null || fldInput.isBlank()) break; // Allow empty/null descriptions

                //  LENGTH. To prevent someone from accidentally (or maliciously) filling the DB with huge text blobs.
                if (fldInput.length() > MAX_DESCRIPTION_LENGTH) {
                    throw new IllegalArgumentException("This is too long (max " + MAX_DESCRIPTION_LENGTH + " characters, got " + fldInput.length() + ")");
                }

                // Check format (no control characters or < >)
                if (!DESCR_PAT.matcher(fldInput).matches()) {
                    throw new IllegalArgumentException("Please use printable chars only; no control chars (tab, enter, etc.) or < >.");
                }
                break;


            case "Address":
                if (!ADDR_PAT.matcher(txtInput).matches()) {
                    throw new IllegalArgumentException("Address contains invalid characters.");
                }
                break;


            case "Email":
                if (!EMAIL_PAT.matcher(txtInput).matches()) {
                    throw new IllegalArgumentException("Email error" + please + "email format.");
                }
                break;


            case "Phone":
                if (!PHONE_PAT.matcher(txtInput).matches()) {
                    throw new IllegalArgumentException(please + "Phone number (no dashes nor parenthesis).");
                }
                break;

            //     Characters allowed: letters, digits, dot, underscore, percent, plus, hyphen. Case-insensitive extension check using (?i:...).
            //     Allows nested folders: img/subdir/file.png, and only Jpeg, Png and Gif files (no webp, svg, or any other files).

            case "Image":
                if (!IMAGE_PAT.matcher(txtInput).matches()) {
                    throw new IllegalArgumentException(please + "Only jpeg, png or gif files, using only letters, digits, dots, underscores, percent, or hyphen");
                }
                break;


            case "Password":
                /*  OLD WAY: if (!PASSWD_PAT.matcher(txtInput).matches()) {
                    throw new IllegalArgumentException(please + "password from " + minLength + " to " + maxLength + " chars, that includes: lowercase, uppercase, number, and symbol (no spaces).");
                }*/
                //  NEW WAY:
                //     Now we're using the trimmed, original text (no collapsed spaces)
                String pwd = fldInput;

                //  And collect the problems, so we can give a precise message
                StringBuilder problems = new StringBuilder();

                // Length (already checked earlier, but this makes the message clearer)
                if (pwd.length() < minLength || pwd.length() > maxLength) {
                    problems.append("be between ").append(minLength).append(" and ").append(maxLength).append(" characters long; ");
                }

                //  1. No spaces
                if (pwd.chars().anyMatch(Character::isWhitespace)) {
                    problems.append("not contain spaces; ");
                }

                //  2. At least one lowercase
                if (!pwd.matches(".*[a-z].*")) {
                    problems.append("include at least one lowercase letter; ");
                }

                //  3. At least one uppercase
                if (!pwd.matches(".*[A-Z].*")) {
                    problems.append("include at least one uppercase letter; ");
                }

                //  4. At least one digit
                if (!pwd.matches(".*\\d.*")) {
                    problems.append("include at least one digit (0–9); ");
                }

                //  5. At least one symbol
                if (!pwd.matches(".*[^\\w\\s].*")) {   // non-word, non-space
                    problems.append("include at least one symbol (e.g. !, @, #, $, %); ");
                }

                if (problems.length() > 0) {
                    // Trim trailing "; " and throw a targeted message
                    String msg = problems.toString().trim();
                    if (msg.endsWith(";")) msg = msg.substring(0, msg.length() - 1);

                    throw new IllegalArgumentException(
                            "Password must " + msg
                    );
                }
                break;

            //  SAFETY NET:
            default: {
                throw new IllegalArgumentException("Unknown field type: " + fieldType);
            }
        }
        //  If we reach this point, no exception was thrown in the switch.
        //  That means the text is valid for the specified fieldType.
        return true;
    }

   /* public static int _20b_ValidQuantity(String qtty) {
        if (!qtty.matches("^\\d+$")) throw new IllegalArgumentException("Quantity required: a non-negative whole number. We got " + qtty);
        else return Integer.parseInt(qtty);
    }*/

    /** QUANTITY:
     *     1. Required (cannot be blank)
     *     2. Must be digits only (no minus sign, no decimal point)
     *     3. Must fit within the range of an int (no overflow)
     *  On failure, throws IllegalArgumentException with a clear message.
     */
    public static int _20b_ValidQuantity(String qtty) {
        //  1. normalize input
        String qty = safeTrim(qtty);  // null -> "", trim

        //  2. basic "required" check
        if (qty.isBlank()) throw new IllegalArgumentException("Quantity is required.");

        //  3. Regex: ensure only digits 0–9 (no negative sign, no decimal point)
        if (!qty.matches("\\d+")) throw new IllegalArgumentException("Quantity required: a non-negative whole number. We got '" + qty + "'.");

        // parse and guard against overflow
        try { return Integer.parseInt(qty); }
        catch (NumberFormatException ex) {throw new IllegalArgumentException( "Quantity is too large. We got '" + qty + "'.", ex); }
    }




    /** PRICE:
     *     1. Required (cannot be blank)
     *     2. Non-negative number
     *     3. Up to 2 decimal places
     *     4. Must parse into a double (no overflow / weird format)
     *  On failure, throws IllegalArgumentException with a clear message.
     */
    public static double _20c_ValidPrice(String inpPrice) {
        //  1. Normalize input: convert null to "", trim.
        //     Using String.valueOf handles null safely ("null" → "null" string, but you're wrapping that with safeTrim in practice).
        String price = safeTrim(String.valueOf(inpPrice));

        //  2. Basic "required" check
        if (price.isBlank()) throw new IllegalArgumentException("Price is required.");

        //  3. Regex: non-negative number with optional decimal part and up to 2 decimal places.
        //    ^\\d+             -> one or more digits at start
        //    (?:\\.\\d{1,2})?  -> optional: dot followed by 1–2 digits
        //    $                 -> end
        if (!price.matches("^\\d+(?:\\.\\d{1,2})?$")) throw new IllegalArgumentException("Price required: non-negative number with up to 2 decimals. We got $" + price);

        //  4. Parse to double and guard against overflow / weird numeric formats.
        try { return Double.parseDouble(price); }
        catch (NumberFormatException ex) {throw new IllegalArgumentException( "Price is too large. We got '" + price + "'.", ex); }
    }

}
