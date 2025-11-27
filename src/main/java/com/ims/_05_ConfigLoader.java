package com.ims;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

//  Import our "toolbox"
import static com.ims._00_Utils.*;


/**
 *  This is the server's "Little Black Book" reader:
 *  It's a class to load the config file from the resources' folder.
 *  It's a simple "utility" class (like _00_Utils) that has 'one' job: Find and read the 'config.properties' file.
 *  This ensures that all configuration is centralized and easy to manage.
 *  This keeps all our "secrets" (like database passwords) in one file, outside our Java code.

 *   YOUTUBE: “12.2 Properties File in Java” (https://www.youtube.com/watch?v=w7D5YB2U2jU)
 *   This is basically the video version of what the ConfigLoader is doing under the hood.
 */
public final class _05_ConfigLoader {

    /**
     *  A private constructor to prevent anyone from creating an instance of this class.
     *  We just want to use the static 'load()' tool.
     */
    private _05_ConfigLoader() {}



    /**
     *  Loads the properties from the config.properties file.
     *     @return A 'Properties' object (which is like a HashMap) containing all the key-value pairs from the file.
     */
    public static Properties load() {
        //  Step 1: Create a new, empty 'phone book' (Properties object).
        Properties properties = new Properties();

        //  Step 2: Define the name of the file to find.
        //     The "/" at the beginning means "look in the 'resources' folder."
        String fileName = "/config.properties";

        //  Step 3: We use a "try-with-resources" block to safely open a "stream" (a data tunnel) to the file.
        //     This 'try' guarantees the 'input' tunnel is closed.
        try (InputStream input = _05_ConfigLoader.class.getResourceAsStream(fileName)) {

            //  Step 4: Check if the file was actually found.
            if (input == null) {
                error("Sorry, unable to find " + fileName);
                //  If we can't find it, we return 'null' to tell the _01_InventoryServer to stop.
                return null;
            }

            //  Step 5: "Load" the file's contents into our 'properties' object. This does all the parsing for us.
            properties.load(input);

        } catch (IOException ex) {
            //  If the file is a-com (e.g., we don't have permission to read it), we'll catch the error.
            error("Error reading the properties file: " + ex.getMessage());
        }

        //  Step 6: Return the 'properties' object (the "phone book") to whoever asked for it (_01_InventoryServer).
        return properties;
    }
}