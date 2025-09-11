package pack;

import static pack.Globals.*;
import static pack.Tools.*;
import static pack.MenuPage.*;
import static pack.Color.*;
import static pack.ASCII_Art.*;
import static pack.User_Intr_Color.*;
import static pack.Patron_Service.*;
import static pack.File_Handler.*;
import static pack.User_Interface.*;
import static pack.ViewTable.*;


//    Name: Tony Rojas
//    Course: CEN3024C-13950.
//    Module 3 | SDLC Assignment Part 2, version 2
//    Topics: Library Management System. Console App (in-memory)
//    Date: 9/4/25 - 9/10/25

//    ----------------------------------------------------------------------------------------------------------------

//    README:
//
//    This is a small "console" app to manage library patrons in memory (only while the program is running).
//
//    What this program does:
//     - Creates and updates a dynamic list (ArrayList) of Patron objects
//     - Validates in all the inputs from the Librarian, to avoid crashes and surprise errors
//     - Gives the librarian a menu to choose from.
//
//     Structure:
//     The program also separates all methods by the type of function they execute, following the Single Responsibility Principle.
//     So, to make the code easier to maintain and test, we've separated the main concerns into:
//          - Business logic:   Patron_Service.java
//          - File I/O:         File_Handler.java
//          - UI helpers:       User_Interface.java
//          - Globals/tools:    Tools.java
//     We also used descriptive variable names (to improve readability) and very detailed comments (helpful in the future).
//
//
//       VERSION 2:
//       Added Color support using Ansi Escape Codes
//       Oh, and I added a Splash Screen with a "progress bar" loader. (just for fun)
//





//  The main class handles the user flow and connects the major 3 components

public class Main {

    //  START PROGRAM

    public static void main(String[] args){

        //  Print a splash scree to begin with:
        nl(3);
        printSplash();

        //  Clear the screen, for the new table
        clearConsole();
        ProgressBar(40, 50);
        clearConsole();
        nl(1);

        //  MENU BAR
        //  We begin the main routine by offering the yellow TOP MENU to the user.
        theMemuBar();

        //  Then, we fill the WorkSpace (ws~) with an initial explanatory detailed view of the top menu
        wsOriginalMenu();

        //  Now we use the CLI and ask the user to enter a value
        commandBar1();

        //  Now we take in the input, from the Librarian. We pick the first letter
        String mnChoice1 = String.valueOf(Character.toUpperCase(menuInput().charAt(0)));

        //  And process the choice
        process1stChoice(mnChoice1);


        //  LOGIC:
        //  We pre-emptively load all patrons from the default file into the Patron-List Array at program startup.
        //  This allows us to work (or select a menu) on the patrons currently loaded in the program's memory
        //  We initialize the Patron-List Array by passing the arguments: "Patrons.txt" from Tools (business
        //    constant, line 28) and "patronsList" from Tools (Generic, line 60), to the File_Handler's loader (line 46)
        loadPatronsFromFile(PATRONS, patronsList);


        //  Menu Control Variable: true=show / false=quit
        boolean run = true;

        //  This loop shows the menu to the librarian, over and over until the user chooses Quit.
        while (run) {
            String mnChoice = printMenu();  //  User_Interface, line 12

            //  Based on the user's choice, we run different subroutines, or quit.
            switch (mnChoice) {

                case "1" -> {
                    String filename = loadFromFile();
                    loadPatronsFromFile(filename, patronsList);
                    println("File content:");
                    tblPatrons(filename);
                }

                case "2" -> addPatronManually();  //  Patron_Service, line 56

                case "3" -> {
                    if (prompt("Show the Patrons? (y/n): ").equalsIgnoreCase("y")) {
                        displayAllPatrons();
                        println("\n(Note: You can copy/paste from list)");
                    }
                    String id = getPatronID();   //  prompts until a valid 7-digit ID
                    removePatron(id);
                }

                case "4" -> displayAllPatrons();

                case "5" -> {
                    //  To make the "Edit Patron" functionality more user-friendly and practical,
                    //    we should display the list of all patrons first, so the librarian can see/find the existing IDs
                    if (prompt("Show the Patrons? (y/n): ").equalsIgnoreCase("y")) {
                        displayAllPatrons();
                        println("\n(Note: You can copy/paste from list)");
                    }
                    //  Now we ask for that ID:
                    String id = getPatronID();   //  prompts until a valid 7-digit ID
                    editPatron(id);
                }

                case "6" -> {
                    println("End program...\nGoodbye!");
                    run = false; // This will end the loop and exit the program
                }

                default -> println("Invalid menu choice. Try again, this time from 1-6.");
            }
        }
    }
    //  END PROGRAM
}




