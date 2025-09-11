package pack;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Optional;

import static pack.Globals.*;
import static pack.Tools.*;
import static pack.User_Interface.*;
import static pack.File_Handler.*;
import java.math.BigDecimal;
import java.math.RoundingMode;




//  This class handles all the business logic related to patrons. Its job is to manage patron data in memory (adding, removing, editing).
//  It manages the list of Patron objects, the Patrons List and contain all the validation logic
//  We deliberately use the single global list "patronsList", in Tools (line 51), that although is a final, it's not a constant
//
//
//  INDEX:                        Line:
//  1. Find Patron                  31
//  2. Add Patron Manually          57
//  3. Remove a Patron by ID        76
//  4. Edit a Patron's Information 108
//  Note: A lot of the work is done on the UI side.

public class Patron_Service {

    //  FIND A PATRON: "Optional" helper method

    //  We need to make sure whether a patron with a given ID already exists or not.
    //  That means: have to FIND a patron by his or her ID number (the parameter).
    //  We'll use the "Optional" object as a container to represent a value that may or may not be there
    //    and handle cases where a patron isn't found.  The type <T> passed to this Generic object is of the type "Patron".
    public static Optional<Patron> findByID(String patronID)
    {
        for (Patron eachPatron : patronsList) {  //  This loop goes through each Patron object in the PATRONS arrayList
            //  Now, we compare eachPatron's ID attribute on the arrayList to the argument given: patronID.
            if (eachPatron.getId().equals(patronID))
                return Optional.of(eachPatron);  //  The "of()" Optional's method creates and returns a new Optional object
            //  containing the eachPatron object we passed to it. We put the found patron inside the Optional object.
        }
        return Optional.empty();  //  If the loop finishes without finding a match.

    /*  This part was left out bc of any UI is handled separately:
        //  The Optional class does not have a mechanism to give the user a message alongside the empty state.
        //  So, by choosing a "throw-type exception" we can return a message: No patron with that ID exists
        //throw new IllegalArgumentException("Patron with ID " + patronID + " not found."); //  We commented this out:
        //   because returning an "empty Optional" is actually a standard and more flexible way to handle "not found"
        //   cases without relying on exceptions for normal program flow.
    */
    }






    //  MENU 2:    ADD ONE PATRON AT A TIME, by asking the librarian for each field.

    public static void addPatronManually() {

        //  Because of the Separation of Concerns, all UI interaction are done at the User_Interface.java file
        String patronID = getUniquePatronID();
        String patrName = getPatronName();
        String pAddress = getPatronAddress();
        BigDecimal fine = promptMoneyInRange(
                "Fine amount (%s - %s): ".formatted(MIN_FINE.toPlainString(), MAX_FINE.toPlainString()),
                MIN_FINE, MAX_FINE);

        //  ADD THE NEW PATRON TO THE GLOBAL ARRAY LIST

        Patron newPatron = new Patron(patronID, patrName, pAddress, fine);
        patronsList.add(newPatron);
        //  Now we show the new patron
        showSinglePatron(newPatron);
        //  Offer to save to the external file:
        savePatrons();
    }





    //  MENU 3:    REMOVE A PATRON BY ID
    public static void removePatron(String patronID) {

/* Old way:
        //  Once the ID is valid we create an Iterator object, so we can call its ".remove()" method during the iteration
        //  It acts on the "patrons" arrayList and gets a "snapshot" (the Iterator object) of the collection
        Iterator<Patron> itrPatronToRemove = patronsList.iterator();  //  Generics will be of "Patron" type
        //  Then, we go through the entire arrayList:
        while (itrPatronToRemove.hasNext()) {
            if (itrPatronToRemove.next().getId().equals(patronID)) {  //  calling getID from the Patron.java class
                itrPatronToRemove.remove(); // safe removal while iterating
                println("Removed patron with ID " + patronID);
                return;  //  go back to the "while" until there is no "hasNext()" patronsToRemove
            }
        }
*/
        //  TODO: use a lambda expression
        //  Done: In the new way (simpler) we use the lambda with the removeIf (Java 8)
        boolean removed = patronsList.removeIf(patronToRemove -> patronToRemove.getId().equals(patronID));

        println(removed ?
                "Removed patron with ID " + patronID
                : "Could not remove patron with ID " + patronID + ". It wasn't found.");
        //  Offer to save to the external file:
        savePatrons();
    }



    //  MENU 5:    EDIT A PATRON'S INFORMATION
    //  This edits (name/address/fine) one patron at a time, by ID

    public static void editPatron(String patronID) {

        try {
            //  By choosing the Optional container object we represent a value that may or may not be there
            Optional<Patron> patronFound = findByID(patronID);

            if (patronFound.isPresent()) {
                Patron patronToEdit = patronFound.get();  //  once found, we get the entire line (ID-Name-etc..)
                println(new StringBuilder().append("Patron found: ").append(showSinglePatron(patronToEdit)).toString());

                //  We make the UI ask the user what to edit (name/address/fine)
                String editChoice = editThisField();  //  the UI returns a String

                //  FIXME: We should put these 3 questions as a UI method as well, but the text is all varied.
                switch (editChoice) {
                    case "1" -> {
                        String newName;
                        while (true) {
                            newName = prompt("Enter new name: ");
                            if (!newName.isEmpty()) break;
                            println("Name cannot be empty.");
                        }
                        //  Now we go to the (found) patron object and call the setter method to change/set his/her name
                        patronToEdit.setName(newName);
                        println("Name updated successfully!");
                    }
                    case "2" -> {
                        String newAddress;
                        while (true) {
                            newAddress = prompt("Enter new address: ");
                            if (!newAddress.isEmpty()) break;
                            println("Address cannot be empty.");
                        }
                        patronToEdit.setAddress(newAddress);
                        println("Address updated successfully!");
                    }
                    case "3" -> {
                        BigDecimal newFine = promptMoneyInRange("Enter new fine amount (%.2f..%.2f): ".formatted(MIN_FINE, MAX_FINE), MIN_FINE, MAX_FINE);
                        patronToEdit.setFineAmount(newFine);
                        println("Fine updated successfully!");
                    }
                    default -> println("Invalid choice. No changes were made.");
                }
                savePatrons(); // Call the save method
            }
            else {
                // The exception from findByID() is already handled, but this is a fallback.
                println("Patron with ID " + patronID + " not found.");
            }
        } catch (IllegalArgumentException e) {
            println(e.getMessage());
        }
    }
}




