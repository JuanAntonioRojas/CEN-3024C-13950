package com.ims;

/**
 *  This is a "Data Bucket" (a POJO) for a single User.
 *  Its only job is to 'hold' all the information for one user (their name, their *hashed* password, etc.).
 *  It's a "shared" class, used by both Client and Server.

 *  Baeldung - Java POJOs: https://www.baeldung.com/java-pojo-class
 *  Why: Explains exactly what a POJO is and why frameworks (like JavaFX) love them.
 */
public class _13_ModelUser {

    //  Private variables to hold the data.
    private String id;
    private String role;
    private String name;
    private String pwdHash; //  This holds the SECURE HASH, never the plain password.
    // It cannot be static, bc every time we load a user, it overwrites the password hash for the entire ModelUser class.
    // When a second user tries to log in, the app will check their password against the first user's hash!
    private String email;
    private String phone;
    private int loginAttempts; //  Used by the server for security.
    //  This holds the attempt count read from or written to the DB.


    // The "blueprint" or "CONSTRUCTOR" for making a new User object.
    public _13_ModelUser(String id, String role, String name, String pwdHash, String email, String phone, int loginAttempts) {
        this.id = id;
        this.role = role;
        this.name = name;
        this.pwdHash = pwdHash;
        this.email = email;
        this.phone = phone;
        this.loginAttempts = loginAttempts;  //  New users start with 0 failed attempts
        //  for new users this gets initialized at zero in _22_CtrlSignup.
    }






    // =========================================================================
    //  "GETTERS" AND "SETTERS"
    // =========================================================================

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPwdHash() { return pwdHash; }
    public void setPwdHash(String pwdHash) { this.pwdHash = pwdHash; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public int getLoginAttempts() { return loginAttempts; }
    public void setLoginAttempts(int attempts) { this.loginAttempts = attempts; }
}