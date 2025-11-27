package com.ims;

import org.mindrot.jbcrypt.BCrypt; //  The "magic" library for password hashing

/**
 * This is our "Toolbox" class. It's the foundation, the "_00_" file!
 * It's full of "utility" methods, which are just simple tools that any other class in our project (both Client and Server) can use.

 * We make the class 'final' so nobody can inherit from it, and we give it a 'private' constructor so nobody can ever create an
 *   'instance' of it. We only want to use its static "tools."
 */

public final class _00_Utils {
    /**
     *  This private constructor is a little trick to prevent anyone from ever creating a 'new _00_Utils()' object.
     *  We just want to use the tools directly, like _00_Utils.hash("...").

     *  FROM: "Effective Java" by Joshua Bloch, Item 4: "Enforce noninstantiability with a private constructor."
     *  Utility classes should not be instantiated. Making the constructor private prevents this.
     *  Making the class final prevents subclassing.
     */
    private _00_Utils() {}



//------------------------------------------------------------------------------------------------------------------
//   CONSOLE HELPERS
//------------------------------------------------------------------------------------------------------------------


    /**
     * A simple helper to print a message to the regular console. It's just a shortcut for System.out.println().
     *   @param txt The text to print.
     */
    public static void println(String txt){ System.out.println(txt); }

    /**
     * A helper to print an ERROR message to the console. This prints in red and keeps error messages separate.
     *   @param e The error text to print.

     *  FROM: Java Standard Edition Docs (System.err): "The 'standard' error output stream... is used to output error
     *     messages or other information that should come to the immediate attention of a user."
     *  This separates error logs from standard program output.
     */
    public static void error(String e){ System.err.println("❌ ERROR: " + e); }


    /**
     * A helper EXIT from execution. This stops the running subroutine or the entire application.
     *   @param exitNumber The error number. It could be a "0" (good exit) or a "1" (bad exit: something bad happened).
     */
    public static void exit(int exitNumber){ System.exit(exitNumber); }




    public static final String ROLE_ADMIN = "admin";




//------------------------------------------------------------------------------------------------------------------
//   SECURE PASSWORD HASHING (BCRYPT)
//------------------------------------------------------------------------------------------------------------------


    /**
     * This is the "cost" or "work factor." A higher number makes the hash *slower* to create on purpose.
     * This makes it *really* hard for hackers to guess passwords. 12 is a very strong, modern standard.

     *  FROM: OWASP Password Storage Cheat Sheet: "Use Argon2id, scrypt, or bcrypt for password hashing."
     *  BCrypt is designed to be slow (computationally expensive) to resist brute-force attacks.
     *  The "work factor" (cost) determines how slow it is.
     *  (Source: https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html)

     */
    private static final int COST = 12;  // Five years from now, computers will be faster, and we might want to bump it to 13 or 14.

    /**
     * Hashes a plain-text password using BCrypt.
     * This method creates a 'new random salt' every time.
     * The COST is an integer value that determines the work factor of the bcrypt hashing algorithm.
     * A higher cost factor increases the number of hashing rounds, making the password hashing process more CPU-intensive
     *   and slower. 10–14 is typical nowadays; 6 was the standard just a few years ago.
     *   @param txt The plain-text password the user typed.
     *   @return A unique, secure hash string (e.g., "$2a$12$...")
     */
    public static String hash(String txt) {
        //  Step 1: Make sure the password isn't null.
        if (txt == null) {
            throw new IllegalArgumentException("Password cannot be null!");
        }
        //  Step 2: Generate a random "salt" using our "COST."
        //     A "salt" is just random text mixed in with the password to make sure two identical passwords (like "12345") don't create the same hash.
        //     We salt the cost; e.g., "7h12_12_4_22_ch4R_54L7" leet for "This is a 22 char salt"
        String salt = BCrypt.gensalt(COST);

        //  Step 3: Combine the plain-text password and the random salt to create the final, super-secure hash.
        return BCrypt.hashpw(txt, salt);
    }



    /**
     * Verifies a plain-text password against a stored BCrypt hash.
     *   @param txt The plain-text password the user typed to log in.
     *   @param hashedPassword The hash we pulled from the database.
     *   @return 'true' if the password matches the hash, 'false' otherwise.
     */
    public static boolean verify(String txt, String hashedPassword) {
        //  Step 1: If either password is null or blank, they can't match.
        if (txt == null || hashedPassword == null || hashedPassword.isBlank()) {
            return false;
        }

        try {
            //  Step 2: Use BCrypt's magic "CHECKPW" function.
            //     It reads the salt 'from' the hashedPassword, re-hashes the plain-text 'txt', and sees if they match.
            return BCrypt.checkpw(txt, hashedPassword);

        } catch (IllegalArgumentException badHashFormat) {
            //  Step 3: If the hash in the DB is corrupted or not a real BCrypt hash, this 'catch' block will prevent the server from crashing.
            error("Bad hash format provided to 'verify'.");
            return false;
        }
    }
}


/**
 *  La Bégueule (1772), wrote, "Le mieux est l'ennemi du bien," which translates to "The better is the enemy of the good."
 *  In other words: "Perfect is the enemy of Good. This is good enough. Ship it!"
 *  This recent phrasing is often associated with the agile or Lean methodology.
 */