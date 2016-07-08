package edu.neu.madcourse.adibalwani.finalproject;


/**
 * Constants in the Module
 */
public class Constants {

    // Firabase
    public static final String FIREBASE_DB = "https://virtual-basketball.firebaseio.com/";
    public static final String USERS = "USERS";

    // Shared Preference
    public static final String PROPERTY_USERNAME = "USERNAME";
    public static final String PROPERTY_SCORE_HISTORY = "SCORE_HISTORY";
    public static final String PROPERTY_GAME_CONFIG = "GAME_CONFIG";
    public static final String PROPERTY_GAME_SCORE = "GAME_SCORE";
    public static final String PROPERTY_GAME_PAUSE = "GAME_PAUSE";
    public static final String PREF_FILE = "FINAL_PROJECT";
    public static final String INTENT_KEY_RESTORE = "INTENT_KEY_RESTORE";

    // Game Phases
    public static final String KEEP_DRIBBLING = "KEEP DRIBBLING";
    public static final String NICE_SHOT = "NICE SHOT";
    public static final String GAME_END = "GAME OVER";

    // Score Constants
    public static final String SCORE = "SCORE : ";
    public static final String YOU_SCORED = "YOU SCORED : ";

    // Dialog Tags
    public static final String REGISTER_DIALOG_TAG = "REGISTER_DIALOG";
    public static final String PAUSE_DIALOG_TAG = "PAUSE_DIALOG";
    public static final String END_GAME_DIALOG_TAG = "END_GAME_DIALOG";

    // Tutorial
    public static final String TUTORIAL_COMPLETED = "TUTORIAL_COMPLETED";

    // Success Messages
    public static final String SUCCESSFULLY_REGISTERED = "SUCCESSFULLY REGISTERED";

    // Error Messages
    public static final String INTERNET_UNAVAILABLE = "NO INTERNET CONNECTION";
    public static final String RETRY_INTERNET_UNAVAILABLE = "CHECK YOUR INTERNET CONNECTION";
    public static final String USERNAME_EXISTS = "PLAYER WITH THAT NAME EXISTS";
    public static final String RETRY_USERNAME_EXISTS = "RETRY WITH DIFFERENT NAME";
    public static final String SERVER_FAILED = "FAILED TO REGISTER";
    public static final String RETRY_SERVER_FAILED = "RETRY AGAIN";
    public static final String USERNAME_INVALID = "RETRY WITH A VALID USERNAME";

    public static final String TAG = "finalProject";

    private Constants() { }
}
