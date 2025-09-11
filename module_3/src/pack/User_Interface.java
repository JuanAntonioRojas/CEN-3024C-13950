package pack;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static pack.Patron_Service.findByID;
import static pack.Tools.*;


//   This class separates the display of information from the business logic of managing the patrons.
//   We'll use this class to display information in a CLI for now. Later we'll use a graphical user interface (GUI),
public class User_Interface {

    //  PRINT MENU

    public static String printMenu() {
        println("\n\n\tLibrary Manager");
        prtLines(26);
        println("1) Load patrons from file");
        println("2) Add patron manually");
        println("3) Remove patron by ID");
        println("4) Display all patrons");
        println("5) Edit a patron's information");
        println("6) Quit\n");

        //  The prompt utility shows a message and reads a full line of input. It goes to and from the user
        return prompt("Choose an option: ");
    }


    //  MENU 2: ADD a PATRON

    public static String patronID;
    //  1. VALID ID: exactly 7 digits long
    public static String getPatronID(){
        while (true) {
            String id = prompt("Enter an ID (7 digits): ").trim();

            if (!ID_PATTERN.matcher(id).matches()) {
                println("Invalid ID. Must be exactly 7 digits.");
                continue;  // loop again
            }
            patronID = id;
            return id;
        }
    }

    //  2. UNIQUE ID:
    //  We prompt the user for input until a valid value is provided. Then we "break" out of the while.
    public static String getUniquePatronID(){
        while (true) {
            //  First we prompt the user for input (above) until a 7 digit value is provided.
            String id = getPatronID();

            //  Then we find out if that id already exists. If it does, we loop again.
            if (findByID(id).isPresent()) {  //  boolean
                println("That ID already exists. Try another.");
                continue;  //  getPatronID() will prompt again, in this while loop
            }
            patronID = id;
            return id;  // The while loop stops here, bc the ID was valid and unique, so we go to Name
        }
    }


    //  NAME (non-empty)
    public static String getPatronName() {
        String name;

        while (true) {
            name = prompt("Name: ");       //  The scanner object will give us a string:= name.
            if (!name.isEmpty()) break;         //  When name is not empty the loop terminates, we go to Address
            println("Name cannot be empty.");   //  a basic "else"
        }
        return name;
    }


    //  ADDRESS (non-empty)
    public static String getPatronAddress() {
        String pAddress;
        while (true) {
            pAddress = prompt("Address: ");
            if (!pAddress.isEmpty()) break;      //  When address is not empty the loop terminates, we go to the FINE ($)
            println("Address cannot be empty.");
        }
        return pAddress;
    }


    //  FINE (must be a double and within range).
    public static BigDecimal getPatronFine() {
        //  We reuse the prompt helper method to keep asking until the user enters a valid double number, in range [min, max]
        //    the ".formatted()" method is essential because it pre-formats the string "Fine amount (%.2f - %.2f): "
        //    so that "promptMoneyInRange()" receives the single string, correctly formatted, that it's expecting.
        return promptMoneyInRange("Fine amount (%.2f - %,.2f): ".formatted(MIN_FINE, MAX_FINE), MIN_FINE, MAX_FINE);
    }



    //  MENU 4: SHOW PATRONS
    public static void displayAllPatrons() {
        if (patronsList.isEmpty())  println("Sorry. There are no patrons to display.");

        //  Headers
        println("\n\t\t\tPATRONS");
        prtLines(100);
        //  Now we set up a system of predefined widths that match Patron.toString())
        System.out.printf("%-10s  %-24s  %-48s  %-9s%n", "ID", "Name", "Address", "Fine");
        prtLines(100);

        //  Here we call the Patron.toString() method in the Patron's class (in this package)
        for (Patron eachPatron : patronsList) {
            //   We bring (or return) and display to the librarian each line:  "ID  Name  Address  Fine"
            //   formatted with tab spaces instead of the unreadable, poor delimiter, dashes.
            //   We're not required to submit a GUI, but at least we have to make it readable.
            println(eachPatron.toString());  //  toString is in Patron, line 38
        }
    }


    //  DISPLAY A SINGLE PATRON
    public static String showSinglePatron(Patron newPatron) {
        println("Patron added:\t" + newPatron.getId() + " - " + newPatron.getName() + " - " + newPatron.getAddress() + " - " + newPatron.getFineAmount() + "\n");
        return null;
    }



    public static String editThisField() {
        println("\n What would you like to edit?");
        println("1) Name");
        println("2) Address");
        println("3) Fine Amount");
        return prompt("Choose an option: 1, 2 or 3: ").toLowerCase();
    }





    //  The FINE COLLECTING FUNCTION
    //  It repeats the same prompt until "good value" (a valid double [min, max]) is given
    //  Because of the nature of BigDecimal we created this special function:
    public static BigDecimal promptMoneyInRange(String msg, BigDecimal min, BigDecimal max) {
        while (true) {  //  continuous loop until the user enters a valid number in range
            String askFineAmt = prompt(msg).trim();  //  this will return a String

            if (!MONEY_PATTERN.matcher(askFineAmt).matches()) {
                println("Invalid amount. Use digits with optional .xx (e.g., 0, 12.5, 12.50).");
                continue;
            }
            BigDecimal fineAmount = new BigDecimal(askFineAmt).setScale(2, RoundingMode.HALF_UP);
            if (fineAmount.compareTo(min) < 0 || fineAmount.compareTo(max) > 0) {
                System.out.printf("Out of range. Enter between %s and %s.%n",
                        min.toPlainString(), max.toPlainString());
                continue;
            }
            return fineAmount;
        }
    }
}
