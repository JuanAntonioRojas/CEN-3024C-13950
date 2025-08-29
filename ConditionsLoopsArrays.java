package Mod1;

//    Name: Tony Rojas
//    Course: CEN3024C-13950.
//    Module 1 - Arrays, Decision and Repetition Statements
//    Date: 8/28/2025
//
//    The objective of this program is to:
//        1. Design and implement selections constructs using 'if' and 'if-else' statements
//        2. Design and implement selection constructs using the 'switch' construct
//        3. Design and implement repetition using 'while', 'do - while', and 'for' loops
//        4. Design and implement nested selection and repetition
//
//        5. Combining sections 2 (Conditionals and Loops) and 3 (Arrays) of Module 1.
//
//    We will create a simple “POS” (point-of-sale) console app that clearly uses:
//        1. Arrays: In defining the cart items and their prices, as global/static variables.
//        2. Repetition: For and While Loops.
//        2. Conditionals: If-else and a switch statement (for menu choices)
//
//    The Process:
//      1st: We will show a menu list of things to do: View catalog, Add Item, View cart and Checkout
//      2nd: After the cust selects a menu item, the program will process the selection and show the menu again
//      3rd: If the cust decides to Check out the program will exit.

import java.util.Scanner;
import static Mod1.Tools.*;

public class ConditionsLoopsArrays {

    //  Catalog of Items and their Prices
    static String[] items  = {"Hot Coffee", "Hot Tea", "Cold Soda", "Hot Chocolate", "Bottled Water"};
    static double[] prices = { 1.99, 1.59, 1.49, 2.49, 0.99 };


    //  We need a CART array to hold the items and quantities chosen by the customer
    static int[] cartQty = new int[items.length];
    //  We also need a total to accumulate the customer's line-item subtotals.
    static double total = 0.0;


    public static void main(String[] args) {
        println("\n\nOur objective is to show the basics of Arrays, Loops and Conditionals.");
        println("We are presenting a menu, and ask the customer to choose an item by\n\tselecting the \"Underlined\" first character of that menu.\n\n");
        println("\t\t===  Welcome to our Mini Starbucks POS  ===");

        Scanner scanner = new Scanner(System.in);


        //  We begin the main routine with this sub-routine:
        //    offering the MENU to the customer.
        //  This array will simplify its presentation and selection
        String[] Mn = new String[]{"S", "how catalog", "A", "dd item", "C", "heckout"};


        //  We need a constant "control" variable to repeat the menu
        boolean shopping = true;
        while (shopping) {
            //  We present the Menu in a colored frame
            printMenu(Mn);                                              //  the choices are S, A or C.
            String selection = scanner.next();                          //  Cust can type 1st letter or entire word
            char choice = Character.toUpperCase(selection.charAt(0));   //  This will only take the 1st letter, in uppercase


            //  Now we present the user the result of his/her choices:
            switch (choice) {
                case 'S':
                    print(Blue + "\n\tHere is the Catalog:\n" + Reset);
                    printCatalog(items, prices);
                    println("Would you like to order an item? (A)");
                    break;

                case 'A':        //  Show items first, and then ask to select item
                    println("\nSelect from out items catalog:");
                    printCatalog(items, prices);
                    print("Enter item number (1-" + items.length + "): ");
                    int selItem = scanner.nextInt() - 1;

                    //  Validate that entry.
                    //  --  If it's less than zero OR greater than the number of available items:
                    if (selItem < 0 || selItem >= items.length) {
                        println("Invalid item number.");
                        break;
                    }

                    //  Enter the quantity of that item they want to add to the cart
                    print("Quantity to add (>=0): ");
                    int qty = scanner.nextInt();
                    if (qty < 0) {
                        println("Quantity cannot be negative.");
                        break;
                    }
                    cartQty[selItem] += qty;  //  Add that qtty to the array for that item
                    println("\nYou Added " + qty + " x " + items[selItem] + ".\n\nAnything else?");
                    break;

                case 'C':
                    checkOut(items, prices, cartQty);
                    shopping = false;
                    break;
            }
        }
    }




    //  MENU
    private static void printMenu(String[] Mn) {
        boxTop(Blue, 52);
        for (int i = 0; i < Mn.length; i += 2) {
            print("\t\u2502\t" + Bold + Underline + Yellow + Mn[i] + Reset + Mn[i + 1] + "  ");
        }
        println(" \u2502");
        boxBottom(Blue, 52);

        //  Asking the cust to choose
        print("Choose a menu (S, A or C): ");
    }




    //  CATALOG
    private static void printCatalog(String[] items, double[] prices) {
        boxTop(Blue, 32);
        for (int i = 0; i < items.length; i++) {
            System.out.printf("\t\u2502  %d) %-16s $%.2f %3s%n", i + 1, items[i], prices[i], "\u2502");
        }
        boxBottom(Blue, 32);
    }




    //  CHECK OUT
    private static void checkOut(String[] items, double[] prices, int[] cartQty) {
        boolean isCartEmpty = true;

        //  Header
        newLn();
        boxTop(Blue, 51);
        System.out.printf(Red + "\t\u2502 %32s %14s %s%n", "--> Final Bill <--", "", "\u2502");

        //  Titles
        horzLine(Blue, 51);
        System.out.printf(Yellow + "\t\u2502  %-14s %5s %10s %13s %s%n", "Item", "Qty", "Price", "Line-Item", " \u2502" + Reset);
        horzDash(Blue, 51);
        for (int i = 0; i < items.length; i++) {
            if (cartQty[i] > 0) {
                isCartEmpty = false;  //  switch it, so that we'll scape the next "if" and go to "else" (print cart)
                //  This total is an accumulator in the loop
                double line = cartQty[i] * prices[i];
                total += line;
                //  Line Items
                System.out.printf("\t\u2502  %-12s %6d %11.2f %13.2f %s%n", items[i], cartQty[i], prices[i], line, "\u2502");
            }
        }
        if (isCartEmpty) {
            horzDash(Blue, 51);
            println("\t\u2502\t\t\tWhat would you like to order?\t\t \u2502");
            boxBottom(Blue, 51);
        } else {
            //  Cart Total
            horzDash(Blue, 51);
            double tax = total * 0.07;
            System.out.printf("\t\u2502  %-12s %33.2f %s%n", "Tax", tax, "\u2502");
            horzLine(Blue, 51);
            System.out.printf("\t\u2502  %-12s %33.2f %s%n", "TOTAL", total + tax, "\u2502");
            boxBottom(Blue, 51);
            print("\nThank you for shopping at Mini Starbucks!\nY'all come back now, y'hear...!\n\n");
        }
    }
}
