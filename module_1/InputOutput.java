package Mod1;

import java.util.Scanner;
import static Mod1.Tools.*;

//    Name: Tony Rojas
//    Course: CEN3024C-13950.
//    Module 1 - Basic input/output
//    Date: 8/28/2025
//
//    The objective of this program is to show the basics of Input and Output of data
//        and to demonstrate a basic understanding of Java, Java terminology, etc.
//    We want to write a program that:
//        1. uses variables and various data types
//        2. uses arithmetic expressions
//        3. will accept data from the user
//        4. will send output to the console
//
//    We will create a simple invoice for a grocery store:
//    The customer selects one or more items (of a pre-set price) and buys it or not (0).
//    The cashier hands out the itemized invoice for the customer to pay the total.
//
//    Process:
//      1st: We will offer (show) this list of items to sell: apples, bananas, milk, bread and eggs,
//      2nd: The cust will select how many of each or enter zero if none is selected, and
//      3rd: The cust gets an itemized bill




public class InputOutput {

    public static void main(String[] args) {
        println("\n\n--------------- Hello and welcome! ---------------\n");
        println("Our objective is to show the basics of Input and Output of data.\n");
        print("We will present a menu of items, then the customer will be asked to select");
        println(" a quantity for each,\n (or zero for none) and then we'll present him/her with a total.\n");

        Scanner scanner = new Scanner(System.in);

        //  Items and prices arrays
        String[] items  = {"Apples", "Bananas", "Milk", "Bread", "Eggs"};
        double[] prices = { 0.99,     0.59,      2.49,   1.99,    3.49  };

        //  This next array will reflect what the cust selected (added to shop cart):
        int[] qty = new int[items.length];

        // Show the groceries at the store:
        println("Items available:");
        for (int i = 0; i < items.length; i++) {
            //  Here we show a formatted collection of items
            System.out.printf("  %d)  %-10s $%.2f%n", i + 1, items[i], prices[i]);
        }

        //  Asking customer to make a selection:
        println("\nEnter quantities (whole numbers). Enter 0 to skip an item.\n");
        for (int i = 0; i < items.length; i++) {
            System.out.printf("Quantity for %s: ", items[i]);
            qty[i] = scanner.nextInt();          //  accepting the input
            if (qty[i] < 0) qty[i] = 0;          //  Just in case, there are no negative quantities
        }


        //  Printing the itemized bill using some ANSI Escape Codes for formatting

        println(Blue + "\n\tHere is your invoice:" + Reset);

        boxTop(Red,50);
        System.out.printf(Yellow + "\t\u2502  %-12s %5s %8s %16s %7s", "Item", "Qty", "Price", "Line Total", "\u2502\n" + Reset);
        horzDash(Yellow, 50);

        //  Line items
        double grandTotal = 0.0;
        for (int i = 0; i < items.length; i++) {
            if (qty[i] > 0) {       //  If the qty of a particular item selected is > 0
                double line = qty[i] * prices[i];  //  The line item cost
                grandTotal += line;         //  The aggegation of the line item costs
                System.out.printf(Reset + "\t\u2502  %-12s %5d %8.2f %14.2f %4s%n", items[i], qty[i], prices[i], line, "\u2502");
            }
        }

        //  Totals:
        horzLine(Red, 50);
        System.out.printf(Blue + "\t\u2502  %-12s %20s %8.2f %8s%n", "TOTAL", "$", grandTotal, "\u2502" + Reset);

        //  Finish the boxing format
        boxBottom(Red,50);

        //  End program:
        println("\n\tThank you for shopping with us!");

        //print("Enter book's Title (full or partial):\n");
        //String title = scanner.nextLine().trim();  //
    }
}
