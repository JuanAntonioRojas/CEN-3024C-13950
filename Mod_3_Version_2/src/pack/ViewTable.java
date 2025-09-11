package pack;

import static pack.Globals.*;
import static pack.Tools.*;
import static pack.Color.*;
import static pack.User_Intr_Color.theMemuBar;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class ViewTable {

    //  Now we read the text to place the content in the spreadsheet
    //static String filePath_name = "some/file/in/my/system";


    //  This is super important:
    //  TODO: fix the Column-Width method that return the array that will hold the spaces that each column needs
    public static int[] colWidths;

    public static int[] ColumnWidths(String[] headers, String filename) throws FileNotFoundException {

        //  We could predefine a particular array of column widths, but it's better to calculate it:
        colWidths = new int[headers.length];  //  How many predefined Headers like "ID, Name, Address" we have
        //  Fill the colWidths array with the length of each header, eg ID (2), Name (4), Address (7), etc
        for (int i = 0; i < headers.length; i++) {
            colWidths[i] = headers[i].length();
        }
        //  read the file content to extract each row and then each column on the csv.
        Scanner scanner = new Scanner(new File(filename));
        while (scanner.hasNextLine()) {
            String eachLine = scanner.nextLine();  //  get each line of text read
            String[] colText = eachLine.split("-");  //  extract columns: what's between the dashes ("-")
            for (int i = 0; i < colText.length; i++) {  //  loop through all the columns in a row of text
                if (colText[i].length() > colWidths[i])   colWidths[i] = colText[i].length();  //  grow the colWidths[]
            }
        }
        scanner.close();
        return colWidths;  //  This is an array with the widths of all the columns
    }


    // Method to print a row of data with calculated padding and a specified vertical separator
    public static void Print_Each_Row(String fgr, String bgr, String[] colText, int[] colWidths, String tableLft, String vertLine, String tableRgt) {
        //  Begin the table with a Left double-line border (in color)
        prtColor(fgr, bgr, tableLft);
        //  now we loop through all the data in the row: each column gets padded (2 blank spaces) in color
        for (int i=0; i<colText.length; i++) {
            //  separate with "padding-in"
            Sp(fgr, bgr, 2);
            //  This is the content of each column of text in the data (csv)
            String content = String.format("%-" + colWidths[i] + "s", colText[i]);  //  "%-" is right indented
            prtColor(fgr, bgr, content);
            //  separate with "padding-out"
            Sp(fgr, bgr, 2);
            //  Right (border)
            String borderRgt = (i < colText.length-1) ? vertLine : tableRgt; // table-right ends the table row
            prtColor(fgr, bgr, borderRgt);
        }
        shadow(1);
    }


    //  Next we print the data rows from the file, with a vertical line separating the content of each row
    public static void Print_Each_Column(String fgr, String bgr, int[] colWidths, String borderLft, String vertLine, String borderRgt, String filename)
    throws FileNotFoundException
    {
        //  read the file content again, from the csv.
        Scanner scanner = new Scanner(new File(filename));

        while (scanner.hasNextLine()) {
            String eachLine = scanner.nextLine();
            //  Get the content of each column and dump it in an array
            String[] colText = eachLine.split("-");

            nl(1);
            //  Print each row, column by column
            margin(2);  //  Margin Left
            Print_Each_Row(fgr, bgr, colText, colWidths, borderLft, vertLine, borderRgt);
            margin(3);  //  Margin Right
            nl(1);

            //  When it finished printing the line and THERE ARE MORE lines coming... Print the row's lower border
            if(scanner.hasNextLine()) {
                row_Lower_Border(fgr, bgr, colWidths);
            }
        }
        scanner.close();
    }


    // Top border of the header
    public static void Table_Header_Top_Border(String fgr, String bgr, int[] colWidths) {
        margin(2);  //  Margin Left
        prtColor(fgr, bgr, "╔");
        for (int i = 0; i < colWidths.length; i++) {
            prtColor(fgr, bgr, "═".repeat(colWidths[i] + 4)); // +2 for padding spaces
            if (i < colWidths.length - 1) {  prtColor(fgr, bgr, "╦");  }
        }
        prtColor(fgr, bgr, "╗");
        margin(3);  Sp(Blue, IBM, 1);//  Margin Right
    }

    //  Separator between header and body
    public static void Header_Body_Separator(String fgr, String bgr, int[] colWidths) {
        margin(2);  //  Margin Left
        prtColor(fgr, bgr, "╠");
        for (int i = 0; i < colWidths.length; i++) {
            prtColor(fgr, bgr, "═".repeat(colWidths[i] + 4));
            if (i < colWidths.length - 1) {  prtColor(fgr, bgr, "╬");  }
        }
        prtColor(fgr, bgr, "╣");
        shadow(1);
        margin(3);  //  Margin Right
    }


    //  Separator between data rows (single line)
    public static void row_Lower_Border(String fgr, String bgr, int[] colWidths) {
        margin(2);  //  Margin Left
        prtColor(fgr, bgr, "╠");
        for (int i = 0; i < colWidths.length; i++) {
            prtColor(fgr, bgr, "─".repeat(colWidths[i] + 4));
            if (i < colWidths.length - 1) {  prtColor(fgr, bgr, "┼");  }
        }
        prtColor(fgr, bgr, "╣");
        shadow(1);
        margin(3);  //  Margin Right
    }



    //  The Table's Bottom Border
    public static void Table_Bottom_Border(String fgr, String bgr, int[] colWidths) {
        margin(2);  //  Margin Left
        prtColor(fgr, bgr, "╚");
        for (int i = 0; i < colWidths.length; i++) {
            prtColor(fgr, bgr, "═".repeat(colWidths[i] + 4));
            if (i < colWidths.length - 1) {  prtColor(fgr, bgr, "╩");  }
        }
        prtColor(fgr, bgr, "╝");
        shadow(1);
        margin(3);  //  Margin Right
        nl(1);

        //shadowIndent(2);
        margin(2);  //  Margin Left
        btmShadow(1, 85);  //  hard coded for now. Find the total row width
        margin(3);  //  Margin Right
    }




    //  The text content line: Center of box
    public static void center(int marg, String color, String txt, int wid) {
        if(txt.length() > wid)  txt = txt.substring(0, wid);
        int padg = (wid - txt.length()) / 2;

        String bfr = "\t".repeat(marg) + color + "│" + " ".repeat(padg);
        String aft = " ".repeat(wid - txt.length() - padg - 2) + "│\n";
        print(bfr + txt + aft);
    }



    //  Print the table to the screen


    public static void tblPatrons(String filename) {

        theMemuBar();

        //  Present the table in a window.
        SpNl(Wht, IBM, dWd);
        prtDesktopLine("\tHere is the current list of Patrons:");
        SpNl(Wht, IBM, dWd);

        try {
            // Find the maximum width for each column to handle dynamic content
            int[] columnWidths = ColumnWidths(HEADERS, filename);

            // Print the top border of the header
            Table_Header_Top_Border(bYellow, Black, columnWidths);
            nl(1);
/*

            // Print the header row
            margin(2);  //  Margin Left
            Print_Each_Row(bYellow, Black, HEADERS, colWidths, "║", "║", "║");
            margin(3);  //  Margin Right
            nl(1);

            // Print the separator between header and data
            Header_Body_Separator(bYellow, Black, colWidths);

            // Print the report data rows with single-line separators
            Print_Each_Column(bYellow, Black, columnWidths, "║", "│", "║", filename);

            // Print the bottom border of the table
            Table_Bottom_Border(bYellow, Black, colWidths);
            nl(1);
*/

        } catch (FileNotFoundException e) {
            prtError("Error: The file '" + filename + "' was not found. Please create it and try again.");
        }
    }
}













