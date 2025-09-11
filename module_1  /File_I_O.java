package Mod1;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import static Mod1.Tools.*;

//    Name: Tony Rojas
//    Course: CEN3024C-13950.
//    Module 1 - File input/output
//    Date: 8/29/2025
//
//    The objective of this program is to show the basics of File Input and Output
//        and since there is a chance the program will crash or “Throw an Exception”
//        we will add to the method signature: "throws IOException {"
//
//    We want to write a program that:
//        1. reads an external CSV text file of customers
//        2. shows the required File checks
//        3. uses throws IOException
//        4. writes to a small "report" file using "PrintWriter varOut = new PrintWriter(variableName)"
//
//    We will create a simple database in a flat text file with Comma Separated Values (CSV).
//    The text file (CSV) is of the 20 most famous Authors in Classical Literature (Authors.txt).

//    Process:
//      1st: We read the external file, in this case a text file calle Authors.txt
//      2nd: we present the read lines in the console, as presented in the file
//      3rd: We output the read info into a new file (newReport.txt) and
//      4th: We create a formatted display of this newly created report file


public class File_I_O {

    public static void main(String[] args) throws IOException {

        File inputFile = new File("C:\\Users\\Tony Rojas\\Desktop\\CEN 3024\\Mod_1\\src\\Mod1\\Authors.txt");

        // These are the required File checks on the input file, specified on the assignment
        println("INPUT FILE: " + inputFile.getPath());
        println("  exists?      " + inputFile.exists());
        println("  length(bytes)" + inputFile.length());
        println("  canRead?     " + inputFile.canRead());
        println("  canWrite?    " + inputFile.canWrite());
        println("  isDirectory? " + inputFile.isDirectory());
        println("  isAbsolute?  " + inputFile.isAbsolute());
        newLn();
        println("This is what was read from the csv file:\n");

        //  First we need to make sure the file exists and can be read:
        if (!inputFile.exists() || !inputFile.canRead()) {
            println("Input file missing or unreadable. Find and fix the path, and try again.");
            return;
        }

        //  Next we prepare an OUTPUT file (in the same folder as the input, we named it "report.txt")
        File newReport = new File(inputFile.getParentFile(), "report.txt");
        //  A more modern way (Java 7+) is:
        //  Path outputPath = Paths.get(inputFile.getParent(), "report.txt"); // it needs java.nio.file.Path;

        int count = 0;

        //  Now we read the input and write a simple report

        try (Scanner scanner = new Scanner(inputFile);
             PrintWriter varOut = new PrintWriter(newReport)) {

            //varOut.println("Name,Address,Email,Phone"); // CSV HEADER for our report

            //  We read the file, line by line, until there are no new lines:
            while (scanner.hasNextLine()) {

                String line = scanner.nextLine().trim();    //  In case each line has extra spaces @ beg. or end
                if (line.isEmpty()) continue;               //  skip line if there is an empty line on the CSV

                //  Now we go to the CSV (comma separated values): Name, Address, Phone, Email
                //    as a way to extract the info. (It could be Tab Separated Values also).
                //  Next, we define the Separator (,) using Regular Expressions
                //    that split the string by a comma that may have spaces (s*) before or after the comma.
                //  The split() method returns a String[] array, where each element is one of these parts
                String[] parts = line.split("\\s*,\\s*");
                if (parts.length < 4) continue; // skip malformed (if a line only had one or two commas).

                String name    = parts[0];  //  eg: John Doe
                String address = parts[1];  //  123 Any St Orlando FL
                String phone   = parts[2];  //  123-456-7890
                String email   = parts[3];  //  john@gmail.com

                //  We create each line with: name, address, email, phone
                String formattedLine = String.format("%s,%s,%s,%s%n", name, address, phone, email);
                //  and write to the file
                varOut.printf(formattedLine);
                //  and write to the console:
                print(formattedLine);

                count++;  //  to assure we're counting the lines read by the scanner.
            }
        }
        // Print the final count
        System.out.printf("\nProcessed %d lines.%n", count);

        //  by using a try with resources, we don't need to close the scanner
        //close();  //  the JVM does that automatically when the block ends


        // Optionally, re-open the report file and print its contents to the console
        println("\n--- We verify that the Report.txt exist by reprinting it ---\n");

        try (Scanner scannedReportFile = new Scanner(newReport)) {

            //  The header is boxed up in blue
            boxTop(Blue, 108);
            System.out.printf(Blue + "\t\u2502 %-23s \u2502 %-33s \u2502 %-14s \u2502 %-25s \u2502%n" + Reset,
                    "Name", "Address", "Phone", "Email");
            boxBottom(Blue, 108);

            while (scannedReportFile.hasNextLine()) {

                String line = scannedReportFile.nextLine();
                String[] parts = line.split("\\s*,\\s*");
                //  In this case there are no malformed lines

                //  Separate the parts for printing
                String name    = parts[0];  //  eg: John Doe
                String address = parts[1];  //  123 Any St Orlando FL
                String phone   = parts[2];  //  123-456-7890
                String email   = parts[3];  //  john@gmail.com

                //  Print the Report File, line by line, with vertical separators and fixed-width columns
                System.out.printf("\t\u2502 %-23s \u2502 %-33s \u2502 %-14s \u2502 %-25s \u2502%n",
                        name, address, phone, email);
            }
        }
        boxBottom(Blue, 108);
    }
}
