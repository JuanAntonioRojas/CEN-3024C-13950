package com.ims;

/**
 *   The Product class is a key component of our inventory system.
 *   It doesn't 'do' anything smart. It is a simple yet crucial class that separates the data's structure from the
 *     business logic, making our system clean and easy to maintain.
 *   Like a "struct" in C, it's only job is to holds all the information, and it serves as the data model for a single inventory item.
 *   This is a "Data Bucket" or a POJO (Plain Old Java Object) contains only the most basic components:
 *     1. private attributes to hold the product's data (SKU, name, description, quantity, and price).
 *        We're keeping all these attributes private so nobody can mess with their data directly from outside
 *     2. public getters and setters methods that can safely access and modify said attributes and data/information.
 *     3. public constructor to instantiate these attributes and methods as objects in a Table View Grid.
 *   With private and public methods we ensure the integrity of the data (what they call encapsulation).
 *   This class is "shared," meaning a copy of it exists in 'both' the Client (to show in tables) and
 *      the Server (to pull data from the DB).
 */

public class _11_ModelProd {

    // CLASS CONSTANTS

    /**
     *  This is our new "Business Rule"!
     *  We're deciding that '10' is the magic number. Any product with this quantity or less is 'low stock'.
     */
    private static final int LOW_STOCK_THRESHOLD = 10;  //  TODO: pass this number to config later. It doesnt justify 40 lines of code.


    // INSTANCE VARIABLES

    //  These are 'private' to protect them (Encapsulation).
    private String brand, sku, name, description, imageUrl;
    private int quantity;
    private double price;


    /**
     *  The "blueprint" or "constructor" for making a new Product object.
     *  It's a "Data Bucket" (a POJO) for a single product.
     *  It just takes all the data and stores it in the private variables.

     *  GeeksforGeeks - Encapsulation in Java: https://www.geeksforgeeks.org/encapsulation-in-java/
     *  Why: Simple, clear examples of why we use private fields and public getters/setters.
     */
    public _11_ModelProd(String sku, String brand, String name, String description, int quantity, double price, String imageUrl) {
        this.sku = sku;
        this.brand = brand;
        this.name = name;
        this.description = description;
        this.quantity = quantity;
        this.price = price;
        this.imageUrl = imageUrl;
    }

    // =========================================================================
    //  "GETTERS" AND "SETTERS"
    //  These are the public 'doors' to get or change the private data.
    // =========================================================================

    //  SKU
    public String getSku() {  return sku;  }
    public void setSku(String sku) {  this.sku = sku;  }

    //  CATEGORY
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    //  NAME
    public String getProdName() {  return name;  }
    public void setProdName(String name) {  this.name = name;  }

    //  DESCRIPTION
    public String getDescription() {  return description;  }
    public void setDescription(String description) {  this.description = description;  }

    //  QUANTITY
    public int getQuantity() {  return quantity;  }
    public void setQuantity(int quantity) {  this.quantity = quantity;  }

    //  PRICE
    public double getPrice() {  return price;  }
    public void setPrice(double price) {  this.price = price;  }

    //  IMAGE
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }






    // =========================================================================
    //  "SMART" GETTER (The added feature!)
    // =========================================================================

    /**
     *  This is the "brain" for the new added feature!
     *  It's a simple method that checks the product's 'own' quantity against our business rule.
     *     @return 'true' if the quantity is at or below the threshold, 'false' otherwise.
     */
    public boolean _11g_isLowStock() {
        //  Step 1: Check if our quantity is less than or equal to the rule.
        return this.quantity <= LOW_STOCK_THRESHOLD;  //  may be this should go into config.properties.
    }
}