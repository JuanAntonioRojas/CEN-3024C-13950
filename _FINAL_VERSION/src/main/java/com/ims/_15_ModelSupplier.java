package com.ims;

/**
 * This is a "Data Bucket" (a POJO) for a single Supplier.
 * Its only job is to *hold* all the information for one supplier (company name, contact, etc.).
 * It's a "shared" class, used by both Client and Server.
 */
public class _15_ModelSupplier {
    //  Private variables to hold the data.
    private String id;
    private String company;
    private String contactName;
    private String phone;
    private String email;
    private String address;
    private String notes;


    //The "blueprint" or "constructor" for making a new Supplier object.
    public _15_ModelSupplier(String id, String company, String contactName, String phone, String email, String address, String notes) {
        this.id          = id;
        this.company     = company;
        this.contactName = contactName;
        this.phone       = phone;
        this.email       = email;
        this.address     = address;
        this.notes       = notes;
    }

    // =========================================================================
    //  "GETTERS" AND "SETTERS"
    //  Public 'doors' to get or change the private data.
    // =========================================================================

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCompanyName() { return company; }
    public void setCompanyName(String company) { this.company = company; }

    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}