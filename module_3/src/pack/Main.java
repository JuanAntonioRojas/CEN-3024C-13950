package pack;

import static pack.Tools.*;
import static pack.Patron_Service.*;
import static pack.File_Handler.*;
import static pack.User_Interface.*;





//    Name: Tony Rojas
//    Course: CEN3024C-13950.
//    Module 3 | SDLC Assignment Part 2
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
//     - Gives the librarian a menu to choose from:
//         1) Load patrons from a text file
//         2) Add a patron manually
//         3) Remove a patron by ID
//         4) Display all patrons
//         5) Edit a patron by ID
//         6) Quit
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
//    REQUIREMENTS:
//    File format: Expects one patron per line and the fields separated by hyphens '-'. ID-Name-Address-Fine
//    Notes:
//       - IDs are exactly 7 digits (leading zeros allowed).
//       - Fine should not include a "$" sign).
//    Examples:
//      1234567-Jane Doe-123 Pine St Apt #2-12.50
//      1245789-Sarah Jones-1136 Gorden Ave. Orlando, FL 32822-40.54
//      3256897-Mason Arby-6060 Saginaw St. Casselberry, FL 34852-0
//      4567891-Avery Jones-1919 Pine Lance Blvd. Oviedo, FL 32478-1.36
//
//    Validation:
//     - We use Pattern/regex -- (Pattern.compile("\\d{7}");) -- to make sure that IDs are 7 digits long.
//     - We parse the fine as a double and ensure it's within a safe range: 0 - 1,000.
//     - We use try-with-resources (for file handling) and try-catch blocks (for number parsing) to safely read files and
//         make the program more resilient to errors. They will also auto-close the reader.
//
//     Problem:
//     - There is quite a number of real names and addresses that have a dash ("-") in them.
//     For instance:
//       1234567-Mr. Sherlock Holmes-221-B Baker St., London-UK-15-0.00
//       9876543-Harry Potter-4 Privet Drive-The Cupboard under the Stairs, Little Whinging, Surrey-UK-0.0
//       0070070-James Bond-25 Wellington Square, Apartment 2-B, Chelsea, London-UK-0.07
//       1234567-Ms. Elizabeth Bennet-Longbourn Manor, White-Hall Rd., Meryton, Hertfordshire-UK-25.00
//       9876543-Alexandria Ocasio-Cortez-74-09 37th Ave, Suite 311, Jackson Heights, NY 11372-9997-1_000.00
//       1234567-David Ben-Gurion-17 Ben-Gurion Boulevard, Tel Aviv, Israel-1.07
//
//     Should they be EXCLUDED just because their name or addresses contain dashes?   I think not.
//     In an "inclusive" program, non-traditional names and addresses may contain dashes; parsing accounts for this.
//
//     Solution:
//       We have to use a carefully defined Regular Expression to match the pattern: ID-Name-Address-Fine
//       The key is to use a non-greedy matching regex pattern "(.*?)" to capture content of the inner fields
//       This relies on the known structure of the first (\\d{7}) and last fields -([\d.]+)$ to "frame" the inside fields (name & address)
//       We'll use the regex formula: "^(\\d{7})-(.*?)-(.*?)-([\\d.]+)$"
//           The first group (\\d{7}) ensures a 7-digit ID, accepting only 7 digits, no more and no less.
//           The first non-greedy group -(.*?) captures the entire name, it acts as a flexible container that can include dashes.
//           The second non-greedy -(.*?) captures everything: from the dash after the name, to the final dash before the fine
//           The final group -([\d.]+)$ captures the fine amount at the end of the line.
//       So then, if the input given does not match any of this regex requirements, the code will skip it as malformed input
//





//  The main class handles the user flow and connects the major 3 components

public class Main {

    //  START PROGRAM

    public static void main(String[] args){

        //  We pre-emptively load all patrons from the default file into the Patron-List Array at program startup.
        //  This allows us to work (or select a menu) on the patrons currently loaded in the program's memory
        //  We initialize the Patron-List Array by passing the arguments: "Patrons.txt" from Tools (business
        //    constant, line 28) and "patronsList" from Tools (Generic, line 60), to the File_Handler's loader (line 46)
        loadPatronsFromFile(PATRONS, patronsList);


        //  Menu Control Variable: true=show / false=quit
        boolean MENU = true;

        //  This loop shows the menu to the librarian, over and over until the user chooses Quit.
        while (MENU) {
            String mnChoice = printMenu();  //  User_Interface, line 12

            //  Based on the user's choice, we run different subroutines, or quit.
            switch (mnChoice) {

                case "1" -> {
                    String filename = promptForFilename();
                    loadPatronsFromFile(filename, patronsList);
                    println("File content:");
                    displayAllPatrons();
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
                    MENU = false; // This will end the loop and exit the program
                }

                default -> println("Invalid menu choice. Try again, this time from 1-6.");
            }
        }
    }
    //  END PROGRAM
}





