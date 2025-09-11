package pack;

import static pack.File_Handler.*;
import static pack.Globals.*;
import static pack.Patron_Service.*;
import static pack.Tools.*;
import static pack.User_Interface.*;
import static pack.User_Intr_Color.*;
import static pack.ViewTable.*;


public class MenuPage {

    //  Now we can process the user's 1st menu choice: (L, A, R, E, D, Q)
    public static void process1stChoice(String mnChoice1) {

        //  Check first letter
        switch (mnChoice1) {
            case "L" -> {
                String filename = loadFromFile();
                loadPatronsFromFile(filename, patronsList);
                println("File content:");
                tblPatrons(filename);
            }

            case "A" -> addPatronManually();

            case "R" -> {
                if (prompt("Show the Patrons? (y/n): ").equalsIgnoreCase("y")) {
                    displayAllPatrons();
                    println("\n(Note: You can copy/paste from list)");
                }
                String id = getPatronID();   //  prompts until a valid 7-digit ID
                removePatron(id);
            }

            case "D" -> displayAllPatrons();

            case "Q" -> println("End program...\nGoodbye!");

            default -> println("Invalid menu choice. Please try again.");
        }
    }



    //  We need a constant "control" variable to repeat the menu
    public static boolean MnActive = true;
//
//    while (MnActive) {
//        //  We present the Menu in a colored frame
//        printMenu(Menu);                                            //  the choices are S, A or C.
//
//
//
//            //print(Blue + "\n\tHere is the Catalog:\n" + Reset);
//            //printCatalog(items, prices);
//            //println("Would you like to order an item? (A)");
//            case 'A' -> {        //  Show items first, and then ask to select item
//                println("\nSelect from out items catalog:");
//                //printCatalog(items, prices);
//                //print("Enter item number (1-" + items.length + "): ");
//                int selItem = scanner.nextInt() - 1;
//            }
//
//            //  Validate that entry.
//            //  --  If it's less than zero OR greater than the number of available items:
//
//
//            case 'C' ->
//                //checkOut(items, prices, cartQty);
//                    MnActive = false;
//        }
//    }




    public static String menuInput() {
        //String selection = SCANNER.next(); // Problem: this  will never return an empty string (""), nor "null". It will simply
        //    block and wait indefinitely for a non-whitespace input, even if the user types a series of spaces and then presses "Enter"
        //  By using nextLine() to capture the entire line, including empty input. An empty string is safer than "null"
        //
        String selection = SCANNER.nextLine();

        // Check if the selection is null or empty.  But null only happens on "SCANNER.next()"
        if (selection.isEmpty()) {  //  ".nextLine()" will never return null. It'll always return a String, even if it's an empty one.
            println("You've selected the default: \"" + PATRONS + "\" file.");
            return "";  //  Gotta return an empty string. It prevents from crashing w a "NullPointerException"
        } else {
            selection = selection.trim();
        }
        return selection;
    }
}
