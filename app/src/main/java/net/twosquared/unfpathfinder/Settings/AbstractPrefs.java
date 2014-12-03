package net.twosquared.unfpathfinder.Settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import net.twosquared.unfpathfinder.Search.SearchManager;


public abstract class AbstractPrefs {

    protected static Context mAppContext = SearchManager.getInstance().getContext();

    private SharedPreferences settings;


    protected void initialize(String nameOfPrefs) {

        settings = mAppContext.getSharedPreferences(nameOfPrefs, Context.MODE_MULTI_PROCESS); // Requires API 11.
    }

    /** Used mainly for preferences from ActivityPreferences. */
    protected void initializeDefaultPreferences() {
        settings = PreferenceManager.getDefaultSharedPreferences(mAppContext);
    }

    protected void save(String key, boolean value) {
        SharedPreferences.Editor editor = settings.edit(); // Needed to make changes
        editor.putBoolean(key, value);
        editor.commit(); // This line saves the edits
    }
    protected void save(String key, float value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat(key, value);
        editor.commit();
    }
    protected void save(String key, int value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(key, value);
        editor.commit();
    }
    protected void save(String key, long value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(key, value);
        editor.commit();
    }
    protected void save(String key, String value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.commit();
    }

    protected boolean get(String key, boolean defaultValue) {
        return settings.getBoolean(key, defaultValue);
    }
    protected float get(String key, float defaultValue) {
        return settings.getFloat(key, defaultValue);
    }
    protected int get(String key, int defaultValue) {
        return settings.getInt(key, defaultValue);
    }
    protected long get(String key, long defaultValue) {
        return settings.getLong(key, defaultValue);
    }
    protected String get(String key, String defaultValue) {
        return settings.getString(key, defaultValue);
    }


    protected void delete(String key) {
        settings.edit().remove(key).commit();
    }

    protected void deleteAll() {
        settings.edit().clear().commit();
    }

}
