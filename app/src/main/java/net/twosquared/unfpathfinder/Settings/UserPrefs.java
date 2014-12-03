package net.twosquared.unfpathfinder.Settings;

public class UserPrefs extends AbstractPrefs {

    private static UserPrefs mUserPrefs;

    private static final String PREF_IS_ULTIMATE_LOGGER_MODE_ENABLED_KEY = "prefIsUltimateLoggerModeEnabledKey";

    /** Use `getInstance()` to instantiate this class. */
    private UserPrefs() {
        // Don't change this value. By changing it, you'll "delete" all of the users preferences.
        initialize("UserPrefs");
    }

    public static UserPrefs getInstance() {
        if (mUserPrefs == null) {
            mUserPrefs = new UserPrefs();
        }
        return mUserPrefs;
    }

    private static final String USER_PREFS_EULA = "eula";
    public void saveEulaPref(boolean rememberMe) { save(USER_PREFS_EULA, rememberMe); }
    public boolean getEulaPref() { return get(USER_PREFS_EULA, false); }

}
