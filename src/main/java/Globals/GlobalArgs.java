package Globals;

import java.util.List;

public class GlobalArgs {
    public static String HELP = "-h";
    public static String HELP_LONG = "--help";
    public static String[] HELP_ANY = new String[] {HELP, HELP_LONG};

    public static String TRANSCRIBE = "--transcribe";
    public static String TRANSCRIBE_TO_FILE = "--transcribe-to-file";
    public static String[] TRANSCRIBE_ANY = new String[]{TRANSCRIBE, TRANSCRIBE_TO_FILE};

    public static String CLEAR_CACHE = "--clear-cache";
}
