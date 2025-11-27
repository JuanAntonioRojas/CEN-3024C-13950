package com.ims;

/**
 * This is a "dummy" main class to work around the JavaFX "fat JAR"
 * launch bug.
 *
 * This class does NOT extend 'Application' and has NO JavaFX imports.
 *
 * 1. 'java -jar' will load THIS class first because it's simple.
 * 2. The main() method here will then call the 'main()' method in _04_Main.
 * 3. The _04_Main.main() method will then call 'launch()', which
 * will correctly find the bundled JavaFX libraries.
 */
public class Launcher {
    public static void main(String[] args) {
        // Call the 'main' method of your real application
        _04_Main.main(args);
    }
}