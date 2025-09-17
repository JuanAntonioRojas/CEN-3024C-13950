package com.example.ims;


/**
*   The Product class is a key component of our inventory system. It is a simple yet crucial class that
*     separates the data's structure from the business logic, making our code clean and easy to maintain.
*   Like a struct in C, it holds all the information and it serves as the data model for a single inventory item.
*   It is what's known as a Plain Old Java Object (POJO) because it contains only the most basic components:
*     private attributes to hold the product's data (SKU, name, description, quantity, and price). We're keeping all
*     these attributes private so nobody can mess with their data directly from outside
*   We added a public constructor to initialize these attributes and public getter and setter methods to allow
*     other parts of the program to safely access and modify the data; in other words to grab or change the information.
*   With private and public methods we ensure the integrity of the data (what they call encapsulation).
**/




public class Product {
    private String category, sku, name, description;
    private int quantity;
    private double price;



    public Product(String category, String sku, String name, String description, int quantity, double price) {
        this.category = category; //  new attribute
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.quantity = quantity;
        this.price = price;
    }


    // Getters and Setters

    public String getCategory() { return category; } // New getter
    public void setCategory(String category) { this.category = category; } // New setter

    public String getSku() {  return sku;  }
    public void setSku(String sku) {  this.sku = sku;  }



    public String getName() {  return name;  }
    public void setName(String name) {  this.name = name;  }


    public String getDescription() {  return description;  }
    public void setDescription(String description) {  this.description = description;  }


    public int getQuantity() {  return quantity;  }
    public void setQuantity(int quantity) {  this.quantity = quantity;  }


    public double getPrice() {  return price;  }
    public void setPrice(double price) {  this.price = price;  }





    //  This "legacy" method returns a string that contains meaningful information about the product's current state,
    //    such as the values of its attributes.  It's used in a statement like: "System.out.println(newProduct)"
    @Override
    public String toString() {
        return "Product{" +
                "  category='" + category + '\'' +
                ", sku='" + sku + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                '}';
    }
}
