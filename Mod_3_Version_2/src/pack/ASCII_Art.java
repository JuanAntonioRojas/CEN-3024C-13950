package pack;

import java.util.concurrent.TimeUnit;

import static pack.Tools.*;
import static pack.Color.*;



//  SPLASH SCREEN WITH ASCII ART
//  https://www.asciiart.eu/text-to-ascii-art
/*
         _      _  _
        | |    (_)| |__   _ __  __ _  _ __  _   _
        | |    | || '_ \ | '__|/ _` || '__|| | | |
        | |___ | || |_) || |  | (_| || |   | |_| |
        |_____||_||_.__/ |_|   \__,_||_|    \__, |
         __  __                             |___/                          _
        |  \/  |  __ _  _ __    __ _   __ _   ___  _ __ ___    ___  _ __  | |_
        | |\/| | / _` || '_ \  / _` | / _` | / _ \| '_ ` _ \  / _ \| '_ \ | __|
        | |  | || (_| || | | || (_| || (_| ||  __/| | | | | ||  __/| | | || |_
        |_|  |_| \__,_||_| |_| \__,_| \__, | \___||_| |_| |_| \___||_| |_| \__|
         ____               _         |___/
        / ___|  _   _  ___ | |_  ___  _ __ ___
        \___ \ | | | |/ __|| __|/ _ \| '_ ` _ \
         ___) || |_| |\__ \| |_|  __/| | | | | |
        |____/  \__, ||___/ \__|\___||_| |_| |_|
                |___/
*/



public class ASCII_Art {

    // The desired total width of the output in columns
    private static final int lnWidth = 90;

    // A helper method to handle printing with colors
    public static void prtColor(String fgr, String bgr, String Txt) {
        print(bgr + fgr + Txt);
    }


    // Methods to draw basic ASCII characters
    public static void Sp(String fgr, String bgr, int sp) {
        if (sp >= 0) {
            prtColor(fgr, bgr, " ".repeat(sp));
        }
    }

    public static void undScr(String fgr, String bgr, int qtty) {
        prtColor(fgr, bgr, "_".repeat(qtty));
    }

    public static void pipe(String fgr, String bgr, int qtty) {
        prtColor(fgr, bgr, "|".repeat(qtty));
    }

    public static void fSlash(String fgr, String bgr, int qtty) {
        prtColor(fgr, bgr, "/".repeat(qtty));
    }

    public static void bSlash(String fgr, String bgr, int qtty) {
        prtColor(fgr, bgr, "\\".repeat(qtty));
    }





    // This method draws the complex ASCII art you provided
    public static void printSplash() {
        prtColor(Black, bYellow, "\tWelcome to the Library Management System!");
        Sp(Black, bYellow, 61);
        nl(1);
        prtColor(Wht, IBM, "\tPlease wait while we load...");
        Sp(Wht, IBM, 74);
        nl(1);

        int printed;
        String temp;
        final int indent = 20;

        // Line 1:  _      _  _
        printed = 0;
        Sp(Yellow, IBM, indent); printed += indent;
        Sp(Yellow, IBM, 1); printed++;
        undScr(Wht, IBM, 1); printed++;
        Sp(Yellow, IBM, 6); printed += 6;
        undScr(Wht, IBM, 1); printed++;
        Sp(Yellow, IBM, 2); printed += 2;
        undScr(Wht, IBM, 1); printed++;
        Sp(Wht, IBM, lnWidth - printed + 16);
        nl(1);

        // Line 2: | |    (_)| |__   _ __  __ _  _ __  _   _
        printed = 0;
        Sp(Yellow, IBM, indent); printed += indent;
        pipe(Wht, IBM, 1); printed++;
        Sp(Yellow, IBM, 1); printed++;
        pipe(Wht, IBM, 1); printed++;
        Sp(Yellow, IBM, 4); printed += 4;
        temp = "(_)| |__   _ __  __ _  _ __  _   _";
        prtColor(Wht, IBM, temp);
        printed += temp.length();
        Sp(Wht, IBM, lnWidth - printed + 16);
        nl(1);

        // Line 3: | |    | || '_ \ | '__|/ _` || '__|| | | |
        printed = 0;
        Sp(Yellow, IBM, indent); printed += indent;
        pipe(Wht, IBM, 1); printed++;
        Sp(Yellow, IBM, 1); printed++;
        pipe(Wht, IBM, 1); printed++;
        Sp(Yellow, IBM, 4); printed += 4;
        temp = "| || '_ \\ | '__|/ _` || '__|| | | |";
        prtColor(Wht, IBM, temp);
        printed += temp.length();
        Sp(Wht, IBM, lnWidth - printed + 16);
        nl(1);

        // Line 4: | |___ | || |_) || |  | (_| || |   | |_| |
        printed = 0;
        Sp(Yellow, IBM, indent); printed += indent;
        pipe(Wht, IBM, 1); printed++;
        Sp(Yellow, IBM, 1); printed++;
        pipe(Wht, IBM, 1); printed++;
        undScr(Wht, IBM, 3); printed += 3;
        Sp(Yellow, IBM, 1); printed++;
        pipe(Wht, IBM, 1); printed++;
        temp = " || |_) || |  | (_| || |   | |_| |";
        prtColor(Wht, IBM, temp);
        printed += temp.length();
        Sp(Wht, IBM, lnWidth - printed + 16);
        nl(1);

        // Line 5: |_____||_||_.__/ |_|   \__,_||_|    \__, |
        printed = 0;
        Sp(Yellow, IBM, indent); printed += indent;
        pipe(Wht, IBM, 1); printed++;
        undScr(Wht, IBM, 5); printed += 5;
        pipe(Wht, IBM, 1); printed++;
        temp = "|_||_.__/ |_|   \\__,_||_|    \\__, |";
        prtColor(Wht, IBM, temp);
        printed += temp.length();
        Sp(Wht, IBM, lnWidth - printed + 16);
        nl(1);

        // Line 6:  __  __                             |___/                          _
        printed = 0;
        Sp(Yellow, IBM, indent); printed += indent;
        Sp(Yellow, IBM, 1); printed++;
        temp = "__  __                             |___/                          _";
        prtColor(Wht, IBM, temp);
        printed += temp.length();
        Sp(Wht, IBM, lnWidth - printed + 16);
        nl(1);

        // Line 7: |  \/  |  __ _  _ __    __ _   __ _   ___  _ __ ___    ___  _ __  | |_
        printed = 0;
        Sp(Yellow, IBM, indent); printed += indent;
        pipe(Wht, IBM, 1); printed++;
        Sp(Yellow, IBM, 1); printed += 2;
        bSlash(Wht, IBM, 1); printed++;
        Sp(Yellow, IBM, 1); printed++;
        fSlash(Wht, IBM, 1); printed++;
        Sp(Yellow, IBM, 2); printed += 2;
        pipe(Wht, IBM, 1); printed++;
        temp = "  __ _  _ __    __ _   __ _   ___  _ __ ___    ___  _ __  | |_";
        prtColor(Wht, IBM, temp);
        printed += temp.length();
        Sp(Wht, IBM, lnWidth - printed + 17);
        nl(1);

        // Line 8: | |\/| | / _` || '_ \  / _` | / _` | / _ \| '_ ` _ \  / _ \| '_ \ | __|
        printed = 0;
        Sp(Yellow, IBM, indent); printed += indent;
        pipe(Wht, IBM, 1); printed++;
        Sp(Yellow, IBM, 1); printed++;
        pipe(Wht, IBM, 1); printed++;
        fSlash(Wht, IBM, 1); printed++;
        bSlash(Wht, IBM, 1); printed++;
        pipe(Wht, IBM, 1); printed++;
        Sp(Yellow, IBM, 1); printed++;
        pipe(Wht, IBM, 1); printed++;
        temp = " / _` || '_ \\  / _` | / _` | / _ \\| '_ ` _ \\  / _ \\| '_ \\ | __|";
        prtColor(Wht, IBM, temp);
        printed += temp.length();
        Sp(Wht, IBM, lnWidth - printed + 16);
        nl(1);

        // Line 9: | |  | || (_| || | | || (_| || (_| ||  __/| | | | | ||  __/| | | || |_
        printed = 0;
        Sp(Yellow, IBM, indent); printed += indent;
        pipe(Wht, IBM, 1); printed++;
        Sp(Yellow, IBM, 1); printed++;
        pipe(Wht, IBM, 1); printed++;
        Sp(Yellow, IBM, 2); printed += 2;
        pipe(Wht, IBM, 1); printed++;
        Sp(Yellow, IBM, 1); printed++;
        pipe(Wht, IBM, 1); printed++;
        temp = "| (_| || | | || (_| || (_| ||  __/| | | | | ||  __/| | | || |_";
        prtColor(Wht, IBM, temp);
        printed += temp.length();
        Sp(Wht, IBM, lnWidth - printed + 16);
        nl(1);

        // Line 10: |_|  |_| \__,_||_| |_| \__,_| \__, | \___||_| |_| |_| \___||_| |_| \__|
        printed = 0;
        Sp(Yellow, IBM, indent); printed += indent;
        temp = "|_|  |_| \\__,_||_| |_| \\__,_| \\__, | \\___||_| |_| |_| \\___||_| |_| \\__|";
        prtColor(Wht, IBM, temp);
        printed += temp.length();
        Sp(Wht, IBM, lnWidth - printed + 16);
        nl(1);

        // Line 11:  ____               _         |___/
        printed = 0;
        Sp(Yellow, IBM, indent); printed += indent;
        Sp(Yellow, IBM, 1); printed++;
        temp = "____               _         |___/";
        prtColor(Wht, IBM, temp);
        printed += temp.length();
        Sp(Wht, IBM, lnWidth - printed + 16);
        nl(1);

        // Line 12: / ___|  _   _  ___ | |_  ___  _ __ ___
        printed = 0;
        Sp(Yellow, IBM, indent); printed += indent;
        fSlash(Wht, IBM, 1); printed++;
        temp = " ___|  _   _  ___ | |_  ___  _ __ ___";
        prtColor(Wht, IBM, temp);
        printed += temp.length();
        Sp(Wht, IBM, lnWidth - printed + 16);
        nl(1);

        // Line 13: \___ \ | | | |/ __|| __|/ _ \| '_ ` _ \
        printed = 0;
        Sp(Yellow, IBM, indent); printed += indent;
        bSlash(Wht, IBM, 1); printed++;
        temp = "___ \\ | | | |/ __|| __|/ _ \\| '_ ` _ \\";
        prtColor(Wht, IBM, temp);
        printed += temp.length();
        Sp(Wht, IBM, lnWidth - printed + 16);
        nl(1);

        // Line 14:  ___) || |_| |\__ \| |_|  __/| | | | | |
        printed = 0;
        Sp(Yellow, IBM, indent); printed += indent;
        Sp(Yellow, IBM, 1); printed++;
        temp = "___) || |_| |\\__ \\| |_|  __/| | | | | |";
        prtColor(Wht, IBM, temp);
        printed += temp.length();
        Sp(Wht, IBM, lnWidth - printed + 16);
        nl(1);

        // Line 15: |____/  \__, ||___/ \__|\___||_| |_| |_|
        printed = 0;
        Sp(Yellow, IBM, indent); printed += indent;
        pipe(Wht, IBM, 1); printed++;
        temp = "____/  \\__, ||___/ \\__ \\___||_| |_| |_|";
        prtColor(Wht, IBM, temp);
        printed += temp.length();
        Sp(Wht, IBM, lnWidth - printed + 16);
        nl(1);

        // Line 16:         |___/
        printed = 0;
        Sp(Yellow, IBM, indent); printed += indent;
        Sp(Yellow, IBM, 8); printed += 8;
        pipe(Wht, IBM, 1); printed++;
        temp = "___/";
        prtColor(Wht, IBM, temp);
        printed += temp.length();
        Sp(Wht, IBM, lnWidth - printed + 16);
        nl(1);
    }



    public static void ProgressBar(int barLength, int timeOut) {

        SpNl(Blue, IBM, 106);

        //  Looping loading bar:
        for (int i = 0; i <= barLength; i++) {
            // Print the progress bar and overwrite the same line
            String bar = "█".repeat(i);
            String spaces = " ".repeat(barLength - i);
            String fullBar = bar + spaces;

            // Here we have to use "\r" to return to the beginning of the line, erasing what was there bf.
            print("\r" + IBM + Wht + "\tProgress: [ " + fullBar + " ]");
            Sp(Blue, IBM, 48);

            //  The key: Pause this For Loop (of each "█") for a short timeOut. This will overwrite itself.
            try {  TimeUnit.MILLISECONDS.sleep(timeOut);  }
            catch (InterruptedException e) {  e.printStackTrace();  }
        }
        print(Wht + IBM + "\n\tLoading Complete!" );
        SpNl(Blue, IBM, 85);
        SpNl(Blue, IBM, 106);
    }
}