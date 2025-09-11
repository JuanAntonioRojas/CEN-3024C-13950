package pack;

import java.math.BigDecimal;


//  This is the home to all constants, used throughout the program
public class Globals {

    //  BUSINESS CONSTANTS (all caps and snake case by convention)
    public static final BigDecimal MIN_FINE = new BigDecimal("0.00");
    public static final BigDecimal MAX_FINE = new BigDecimal("1000.00");
    public static final String PATRONS = "Patrons.txt";

    //  Desktop Window Width
    public static final int dWd = 106;


    //  THE MAIN MENU
    //  This array will simplify its presentation and selection.
    public static final String[] MENU = new String[]{ "L", "oad", "A", "dd", "R", "emove", "E", "dit", "D", "isplay", "Q", "uit" };


    // Column headers for our table
    public static final String[] HEADERS = {"ID", "Name", "Address", "Fine"};
}
