package pack;

import static pack.Tools.*;

public class Color {

    // A method to print a new line resetting the colors to default console values
    public static void nl(int qtty) {
        for (int i = 0; i < qtty; i++) {
            println(Reset);
        }
    }
    //public static void nl() { System.out.print("\n"); }



    //  The ANSI Escape Sequences:

    //  The Hazeltine 2000 video terminal, released in 1970, the Tektronix 4010 series in 1972 and the DEC VT52
    //    in 1975 were the first using Escape Sequence characters on their "terminals."
    //  Although they were vendor specific then, they were standardized in 1976 with Ecma-48.

    //  This is a 4 bit Ansi colors (2^4 or 16 colors) standard escape codes:

    //  Foreground Colors: R,G,B,W and C,Y,M,K;
    public static final String Red="\u001b[31m", Green="\u001b[32m", Blue="\u001b[34m", Wht="\u001b[37m";
    public static final String Cyan="\u001b[36m", Yellow="\u001b[33m", Magenta="\u001b[35m", Black="\u001B[30m";

    //  Background Colors: R,G,B,W and C,Y,M,K;
    public static final String bRed="\u001B[41m", bGreen="\u001B[42m", bBlue="\u001B[44m", bWhite="\u001B[47m";
    public static final String bCyan="\u001B[46m", bYellow="\u001B[43m", bMagenta="\u001B[45m", bBlack="\u001B[40m", bGrey = "\u001B[47m";


    //  These are more advanced escape codes, to get that IBM DOS-era retro look:
    public static final String IBM = "\u001b[48;2;0;0;170m";  //  Deep blue background, from IBM PCs back in the 80s
    public static final String fntYlw = "\u001B[38;2;255;255;204m";  //  faint yellow
    public static final String bCharcoal = "\u001b[48;5;235m";  //  Dark charcoal



    //  The most important element: to take it back to the original console default colors
    public static final String Reset = "\u001b[0m";  //  in 2024 I called it "bk2Normal" but I used "\x1b[0m"

    //  The TRANSPARENT color:
    //public static final String Default = "\x1b[49m";  //  the default terminal color or transparent color
    //public static final String Default = "\\x1b[49m";  //  don't work either.
    //  neither of these work unless i use an ansi for the escape sequence literal "\" = "\u001b"
    public static final String Default = "\u001b[49m";  //  the real default terminal color or TRANSPARENT color

    //  Other character formatting
    public static final String Underline="\u001b[4m", Bold = "\u001b[1m", Italic="\u001b[3m";
    public static final String Invert="\u001B[7m", Dash="-";




    //  The printing in color method
    public static void prtColor(String fgr, String bgr, String Txt){
        print(fgr + bgr + Txt + Reset);
    }


    //  create a Shadow effect
    public static final String shdBtm="\u2580";  //  This is the "▀" top-half character
    public static final String Loading="\u2588";  //  This is the "█" full block character
    public static void shadow(int sp){ print(bBlack + " ".repeat(sp) + Reset); }
    public static void shadowIndent(String bgr, int sp){ print(bgr + " ".repeat(sp) ); }
    public static void btmShadow(int ind, int sp){
        shadowIndent(IBM, ind);
        prtColor(Black, IBM, "▀".repeat(sp) + Reset);
    }

    //  EXAMPLES

    //  1: Blank Space Fillers
    public static void Sp(String fgr, String bgr, int sp) {  prtColor(fgr, bgr, " ".repeat(sp));  }
    public static void SpNl(String fgr, String bgr, int sp) {  prtColor(fgr, bgr, " ".repeat(sp) + Reset + "\n");  }

    //  2: Tab Space Filler
    public static void Tb(String fgr, String bgr, int tb) {  prtColor(fgr, bgr, "    ".repeat(tb));  }

    //  3: New Line(s) Filler
    public static void Ln(String fgr, String bgr, int nl) {  prtColor(fgr, bgr, "\n".repeat(nl));  }


    //  Margin Left and Right
    public static void margin(int tab) {  Tb(Wht, IBM, tab);  }



    //  Page Background with Blank Screen Lines
    public static void blankDesktopFiller(String fgr, String bgr, int sp, int nl) {
        for (int i = 1; i <= nl; i++) {
            Sp(fgr, bgr, sp);
            nl(1);
        }
    }


    /*  I skipped this bc the console doesn't support it.

    Now I'm gonna use 256 (8 bit or 2^8) colors to get true color (16.7 million colors).
    The formula is a little different, but the range increase is considerable.
        each color is defined by its RGB components, in 3 separate numbers.
        For instance Red is (255,0,0), Blue is (0,255,0), etc.

    public static void prtColor(int fR, int fG, int fB, int bR, int bG, int bB, String Txt) {
        println("\u001b[38;2;" + fR + ";" + fG + ";" + fB + "m" +  //  foreground
                "\u001b[48;2;" + bR + ";" + bG + ";" + bB + "m" +  //  background
                Txt + Reset);  //  text content.
    }

    //  Example 1: Blank Space Filler
    public static void Sp(int fR, int fG, int fB, int bR, int bG, int bB, int sp) {
        prtColor(fR, fG, fB, bR, bG, bB, " ".repeat(sp));
    }

    //  Example 2: Tab Space Filler
    public static void Tb(int fR, int fG, int fB, int bR, int bG, int bB, int sp) {
        prtColor(fR, fG, fB, bR, bG, bB, "\t".repeat(sp));
    }

    //  Example 3: New Line(s) Filler
    public static void Ln(int bR, int bG, int bB, int nl) {  //  No foreground color is needed here.
        prtColor(0, 0, 0, bR, bG, bB, "\n".repeat(nl));
    }
    */



    //  After the splash screen we need to clear the console screen.
    public static void clearConsole() {
        print("\033[H\033[2J");
        System.out.flush();
    }









    //  To recreate the look and fell of the old PC's screen I'll be using boxes (with bg color)

    //  Single Line Borders:
    static final String TL="┌", TR="┐", BL="└", BR="┘", T="┬", M="┼", B="┴", H="─", V="│", L="├", R="┤";

    //  Double Line Borders:
    static final String dTL="╔", dTR="╗", dBL="╚", dBR="╝", dT="╦", dM="╬", dB="╩", dH="═", dV="║", dL="╠", dR="╣";


    //  BOX FORMATTING:
    //  The first thing is to know how wide should the columns be. It doesn't necessarily depend on our 'header.length'
    //    themselves. The content could be wider, as in Hdr: "Address" and Cont: "123 Somewhere St, Orlando FL 32801"
    //  So we have to either define it, say 20 chars wide, or have a method to find the width of each column in the given file.
    //  In this case, first we'll find the width of each header and then increase the column width, if the file's read
    //    content's width (like an address) is bigger than the header's width, which usually it is.
    //  We def a method to find the colWidths array, to define all (it could be 1 or many) heathers/columns widths.







    /*
    ANSI Escape Codes
    Lines:
        Horizontal: \u2500 (─)
        Vertical: \u2502 (│)

    Corners:
        Top-left: \u250c (┌)
        Top-right: \u2510 (┐)
        Bottom-left: \u2514 (└)
        Bottom-right: \u2518 (┘)

    T-Junctions:
        Left: \u251C(├)
        Right: \u2524 (┤)
        Top: \u252C (┬)
        Bottom: \u2534 (┴)
    Intersection Junction:
        Cross = \u253C (┼)


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

