package Mod1;

//  We are creating some common usage tools (classes) that will save on memory


public class Tools {
    public static void print(String s) { System.out.print(s); }
    public static void println(String s) { System.out.println(s); }
    public static void newLn() { System.out.println("\n"); }

    //  Ansi and Unicode Characters:
    //  Foreground Letters
    static String Yellow = "\u001b[33m";
    static String Red = "\u001b[31m";
    static String Blue = "\u001b[34m";
    static String Cyan = "\u001b[36m";
    static String White = " \u001b[37m";
    static String Black  =	"\u001B[30m";

    //  Background Colors:
    static String bRed    =	"\u001B[41m";
    static String bGreen  =	"\u001B[42m";
    static String bYellow =	"\u001B[43m";
    static String bBlue   =	"\u001B[44m";
    static String bMagenta= "\u001B[45m";
    static String bCyan   =	"\u001B[46m";
    static String bWhite  =	"\u001B[47m";
    static String bBlack  =	"\u001B[40m";

    static String Underline = "\u001b[4m";
    static String Bold = "\u001b[1m";
    static String Reset = "\u001b[0m";



    //  Box printing:

    public static void horzDash(String color, int width) {
        print(color + "\t\u2502");
        for (int i = 0; i < width-2; i++) { print("-"); }
        println("\u2502" + Reset);
    }
    //  Horizontal line within the corners
    public static void horzBorder(String color, int width) {
        for (int i = 0; i < width-2; i++) { print(color + "\u2500"); }
    }
    public static void horzLine(String color, int width) {
        print(color + "\t\u2502");
        for (int i = 0; i < width-2; i++) { print("\u2500"); }
        println("\u2502" + Reset);
    }


    public static void boxTop(String color, int width) {
        print(color + "\t\u250c");   //  Top-left corner
        horzBorder(color, width);    //  Horizontal line within the corners
        println("\u2510" + Reset);   //  Top-right corner
    }

    public static void boxBottom(String color, int width) {
        print(color + "\t\u2514");     //  Bottom-left corner
        horzBorder(color, width);      //  Horizontal line within the corners
        println("\u2518" + Reset);     //  Bottom-right corner
    }


    /*
    ANSI Escape Codes

    Corners:
        Top-left: \u250c (┌)
        Top-right: \u2510 (┐)
        Bottom-left: \u2514 (└)
        Bottom-right: \u2518 (┘)
    Lines:
        Horizontal: \u2500 (─)
        Vertical: \u2502 (│)
    Foreground Colors:
        Red: \u001b[31m
        Green: \u001b[32m
        Blue: \u001b[34m
        Yellow: \u001b[33m
        Cyan: \u001b[36m
        Magenta: \u001b[35m
        Cyan: \u001b[36m
        White: \u001b[37m
    Background Colors:
        bBlack	\u001B[40m
        bRed	\u001B[41m
        bGreen	\u001B[42m
        bYellow	\u001B[43m
        bBlue	\u001B[44m
        bMagenta \u001B[45m
        bCyan	\u001B[46m
        bWhite	\u001B[47m

White:
    Underline: \u001b[4m
    Bold: \u001b[1m

    Reset All: \u001b[0m
    */
}
