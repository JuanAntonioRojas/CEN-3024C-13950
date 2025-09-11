package pack;

import static pack.Globals.*;
import static pack.Tools.*;
import static pack.Color.*;
import static pack.MenuPage.*;

public class User_Intr_Color {

    //  YELLOW MENU:
    public static void theMemuBar() {
        nl(3);
        Sp(IBM, bYellow, 10);
        //  Printing the menu on the Finder (the menu bar)
        for (int i = 0; i < MENU.length; i += 2) {
            print(Bold + Underline + Italic + Black + bYellow + MENU[i] + Reset + Black + bYellow + MENU[i + 1] + "\t  ");
        }
        SpNl(IBM, bYellow, 40);
    }




    //  ORIGINAL CONTENT (DETAILED MENU):
    public static void wsOriginalMenu() {

        //  Now we present the user the result of his/her choices:
        blankDesktopFiller(Wht, IBM, dWd, 3);
        prtColor(Wht, IBM, "\tYour choices are:");  SpNl(Wht, IBM, 85);
        prtColor(Wht, IBM, "\t\tL) Load patrons from a text file");  SpNl(Wht, IBM, 66);
        prtColor(Wht, IBM, "\t\tA) Add a patron manually");  SpNl(Wht, IBM, 74);
        prtColor(Wht, IBM, "\t\tR) Remove a patron by ID");  SpNl(Wht, IBM, 74);
        prtColor(Wht, IBM, "\t\tD) Display all patrons");  SpNl(Wht, IBM, 76);
        prtColor(Wht, IBM, "\t\tE) Edit a patron by ID");  SpNl(Wht, IBM, 76);
        prtColor(Wht, IBM, "\t\tQ) Quit");  SpNl(Wht, IBM, 91);
        blankDesktopFiller(Wht, IBM, dWd, 6);
    }




    //  LOAD PATRONS FROM FILE:
    public static String loadFromFile() {
        //  start w the yellow menu
        theMemuBar();

        //  On the Desktop ask for a filename (or press Enter for default)
        blankDesktopFiller(Wht, IBM, dWd, 5);
        prtDesktopLine("\tEnter a filename, or press \"Enter\" to load the:");
        prtDesktopLine("\t\"" + PATRONS + "\" file (in the parent dir): ");
        blankDesktopFiller(Wht, IBM, dWd, 9);

        prtDesktopLine("\tFile Name: ");

        //  Now we take in the input
        String fileName = menuInput();
        //  Ternary return of file name:
        return fileName.isEmpty() ? PATRONS : fileName;
    }





    //  This subroutine will take the input from the CLI
    public static void commandBar1() {
        //  We first create an empty list that will contain only the first letter of every menu word
        StringBuilder fstLetters = new StringBuilder();

        // now we have to iterate through the (global) Menu array, skipping every other element
            for (int i = 0; i < MENU.length; i += 2) {
            fstLetters.append(MENU[i]);  // We increase the list by appending each letter
            //  We also append a comma and a space, except for the last letter
            if (i < MENU.length - 2) fstLetters.append(", ");
        }

        //  Finally, we ask the Librarian to choose any of the first letters of the menu
        prtColor(Wht, IBM, "\tChoose a menu letter: (" + fstLetters + "): ");
        SpNl(Wht, IBM, 60);
    }

}
