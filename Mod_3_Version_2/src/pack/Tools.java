package pack;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import static pack.Color.*;
import static pack.Color.IBM;
import static pack.Globals.dWd;


//  These are small, global helpers and constants used across the app, mostly to save some memory
//  We keep a single global list of patrons here (static), so there's only ONE source of truth used throughout.
//
public class Tools {

    //  UTILITIES:

    //  These are tiny print helpers (just to type less)
    public static void print(String s) { System.out.print(s); }
    public static void println(String s) { System.out.println(s); }
    public static void prtError(String e) { System.err.println(e); }

    //  Can't do 'printf' because of the %n, %s, %d, %f inserts







    //  INPUT

    //  We will use one Scanner object for the whole app. With this object we can read the user input typed in the
    //    console (the required default UI). It will read full lines, to avoid leftover newlines.
    public static final Scanner SCANNER = new Scanner(System.in);

    //  By "prompting" the user a question, using the scanner object, this method returns their trimmed response.
    //  This is used in the File_Handler and User_Interface, but mostly in the Patron_Service class, to enter or edit a patron.
    public static String prompt(String msg) {
        print(msg);
        return SCANNER.nextLine().trim();
    }


    public static void prtDesktopLine(String lineOfText) {
        //  Print the line
        prtColor(Wht, IBM, lineOfText);
        //  calculate and print the remaining space
        SpNl(Wht, IBM, (dWd - lineOfText.length() - 3));
    }


    //  VALIDATION RULES: from the Pattern class.
    //  - ID must be exactly 7 digits ( regex \\d{7} ), and the acceptable "FINE" range is [0.0, 1,000.0]
    public static final Pattern ID_PATTERN = Pattern.compile("\\d{7}"); //  7-digit ID

    // Accept integers or up to 2 decimals (no commas/underscores)
    //  - The Dollar sign "$" is left out as per Requirements
    //  It also rejects commas, spaces, underscores, etc. So "${12.50}", "$12.50", "1,000.00", "12_00" will all fail and be flagged as invalid
    //  the match..
    public static final Pattern MONEY_PATTERN = Pattern.compile("^\\d+(?:\\.\\d{1,2})?$");



    //  GENERICS

    //  For our in-memory data store ("database") we'll use a resizable generic array list of patrons that we will
    //  use everywhere; this undefined-length array-list uses the <Patron> type to ensure type safety
    public static final List<Patron> patronsList = new ArrayList<>();

   //   ANSI Escape Codes
   public static void prtLines(int nbr) {
       for(int i=0; i<nbr; i++)  print("─");   //  code for line: \u2500
       println(" ");

   }
}
