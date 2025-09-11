package pack;



import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static pack.Globals.*;
import static pack.Tools.*;
import static pack.Patron_Service.*;
import java.math.BigDecimal;
import java.math.RoundingMode;



//  This class is responsible for all file I/O operations, including saving the data.
//  TODO: In the future we could easily swap out this File_Handler for a Database_Handler



public class File_Handler {

    //  Ask for a filename (or press Enter for default)
    public static String promptForFilename() {
        String fileName = prompt("Enter a filename, or press Enter to load the \"" + PATRONS + "\" file (in the parent dir): ");

        //  FIXME:
        /*if (fileName.isEmpty()) {  //  If the input is empty (or null, if they press Enter)
            return PATRONS;  //  we return a default filename: "Patrons.txt"
        }
        return fileName;*/
        //  Fixed:
        return fileName.isEmpty() ? PATRONS : fileName;  //  Ternary operators are always fun.
    }






    //   LOAD PATRONS from a text file into the arrayList "patronsList":
    //     - Skip blank and malformed lines
    //     - Accept "ID-Name-Address-Fine" (with dashes allowed inside name/address)
    //     - Split each line by '-' into exactly 4 parts: ID, Name, Address, Fine (this is not a CSV, but similar)
    //     - Validate ID and do not add duplicate IDs (IDs are 7 digits long)
    //     - Validate Fine range. Reject lines that don't match: ID-Name-Address-Fine

    public static void loadPatronsFromFile(String filename, List<Patron> myPatronsList) {
        //  Defs
        int  lineNbr = 0,  addedPatrons = 0,  skippedLines = 0;
        String lineReadFromFile;


        //  We have to clear the "in-memory" list to prevent or avoid duplicates from multiple loads (re-loading)
        myPatronsList.clear();

        //  We'll use the regex formula: "^(\\d{7})-(.*?)-(.*?)-([\\d.]+)$" described as:
        //      The first group (\\d{7}) ensures a 7-digit ID, accepting only 7 digits. Leading zeros are ok.
        //      The first (non-greedy) group -(.*?) captures the entire name, it acts as a flexible container that can include dashes.
        //      The second (non-greedy) group -(.*?) captures everything: from the dash after the name, to the final dash before the fine
        //      The final group -([\d.]+)$ captures the fine amount at the end of the line.
        //  Finally, if the input given does not match any of this regex requirements, the code will skip it as malformed input.

        //  We've chosen the "Pattern" regex class to represent a Regular Expression used for matching a sequence of characters
        //  No need to abstract this complex regex into the "Globals" class, since it's used only once.
        Pattern validationPattern = Pattern.compile("^(\\d{7})-(.*?)-(.*?)-(\\d+(?:\\.\\d{1,2})?)$");



        //  By using "try-with-resources" the BufferedReader will close itself, even if an error happens.
        try ( BufferedReader bufferReader = new BufferedReader(  new FileReader(filename, StandardCharsets.UTF_8) ) )
            {
            while ((lineReadFromFile = bufferReader.readLine()) != null) {  //  while there still a line to be read...
                lineNbr++;  //  we grow the external counter of how many lines we've read
                lineReadFromFile = lineReadFromFile.trim();  //  and clear out margins (tabs or spaces) on the line

                //  jump out of the while if there are no more lines to read
                if (lineReadFromFile.isEmpty()) continue;
                //  Note:
                //  In Java's regular expression API, the Pattern class (above) has a method called matcher() that creates and returns a Matcher object.
                //  The Matcher is the engine that actually performs the searches against each input (lineRead) and matches them.
                //  This Matcher import improves the validation, bc the non-greedy (.*?) captures everything read between the known delimiters (-)
                //  Now we find a match between the regex (def above) and what's inside the read lines:
                Matcher matchingPattern = validationPattern.matcher(lineReadFromFile);

                //  If the line does not match the regex, it's malformed (ain't no good)
                if (!matchingPattern.matches()) {
                    System.err.printf("Line %d is malformed. It needs to match the pattern 'ID-Name-Address-Fine': %s%n", lineNbr, lineReadFromFile);
                    skippedLines++;  //  we grow the external counter of how many lines we've skipped
                    continue;
                }

                // Then we extract the fields using regex groups (a method of the Matcher class) provided by the matchingPattern
                String id = matchingPattern.group(1).trim();
                String name = matchingPattern.group(2).trim();
                String address = matchingPattern.group(3).trim();
                String fineStr = matchingPattern.group(4).trim();


                //  VALIDATIONS:

                //  1. Validate the ID compared to the ID_PATTERN using our 7-digit regex, given initially in Tools, line 50
                if (!ID_PATTERN.matcher(id).matches()) {  //  the matcher() method is a method of the Pattern class
                    System.err.printf("Line %d: invalid ID '%s' (expect 7 digits).%n", lineNbr, id);
                    skippedLines++;  //  count the number of lines tht are no good
                    continue;
                }

                //  2. Do not allow duplicate IDs
                if (findByID(id).isPresent()) {  //  The "isPresent()" method returns a boolean (true or false)
                    //  indicating whether an existing Optional object contains a value
                    System.err.printf("Line %d: duplicate ID '%s' was skipped.%n", lineNbr, id);
                    skippedLines++;
                    continue;
                }

                //  3. The Fine's error handling
                BigDecimal fine;
                try {
                    fine = new BigDecimal(fineStr).setScale(2, RoundingMode.HALF_UP);  //  Parse the fine as a BigDecimal
                } catch (NumberFormatException e) {
                    System.err.printf("Line %d: invalid fine '%s'.%n", lineNbr, fineStr);
                    skippedLines++;
                    continue;
                }

                //  4. Make sure that the fine falls within the allowed range
                if (fine.compareTo(MIN_FINE) < 0 || fine.compareTo(MAX_FINE) > 0) {
                    System.err.printf("Line %d: fine %.2f out of range [%.2f - %.2f].%n", lineNbr, fine, MIN_FINE, MAX_FINE);
                    skippedLines++;
                    continue;
                }

                //  ADD THE NEW PATRON OBJECT TO THE LIST (Generics ArrayList<>)

                myPatronsList.add(new Patron(id, name, address, fine));   //  This will pass on to the Patrons object
                addedPatrons++;  //  we grow the external counter of how many patrons we've added
            }
        }
        //  We need to tell the librarian what kind of "reading file error" we encountered
        catch (Exception e) {
            // Common reasons: file not found, no read permission, wrong path, etc.
            System.err.println("Error reading '" + filename + "' It has restrictions: " + e.getMessage());
        }
    }





    //  SAVE TO NEW PATRON_LIST
    public static void savePatrons() {
        //  Now, we offer to save the Patron List (ArrayList) to the given file ("Patrons.txt"), or ask for a new file:
        if (prompt("Save changes to a file? (y/n): ").equalsIgnoreCase("y")) {
            String filename = promptForFilename();  //  separation of concerns
            // Get the list of patrons from the service and pass it
            savePatronsToFile(filename, patronsList);  //  each function does one thing
        }
    }



    //  Save the global list to the given file in "ID-Name-Address-Fine" format
    public static void savePatronsToFile(String filename, List<Patron> patrons) {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, StandardCharsets.UTF_8))) {
            for (Patron eachPatron : patrons) {
                // Format the patron data as a single line before writing
                writer.write(eachPatron.toStringForFile() + "\n");
            }
        } catch (IOException e) {
            System.err.println("Error saving file '" + filename + "': " + e.getMessage());
        }
    }
}
























