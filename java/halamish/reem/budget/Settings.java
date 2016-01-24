package halamish.reem.budget;

import android.content.SharedPreferences;
import android.content.res.Configuration;

import java.util.Locale;

/**
 * holds the settings
 * Created by Re'em on 1/13/2016.
 */
public class Settings {
    private static final String SETTINGS_KEEP_BALANCE_FROM_LAST = "settings_keep_balance_from_last";
    private static final String SETTINGS_USER_LANGUAGE = "settings_user_language";


    public enum Language{
        // TODO translate all...
        // http://stackoverflow.com/questions/13721668/using-different-string-files-in-android
        ENGLISH(0, "en", ""),
        HEBREW_UNISEX(10, "he", ""),
        HEBREW_MALE(11, "he", "ml_"),
        HEBREW_FEMALE(12, "he", "fml_");

        private int value;
        private String locale, prefix;
        Language(int val, String loc, String pre) {
            this.value = val;
            this.locale = loc;
            this.prefix = pre;
        }
        public int toInt() { return value;}
        public boolean Compare(int i){return value == i;}
        public static Language getFromInt(int _id)
        {
            Language[] all = Language.values();
            for(int i = 0; i < all.length; i++)
            {
                if(all[i].Compare(_id))
                    return all[i];
            }
            return Language.ENGLISH;
        }
        public String toLocale() { return locale;}
        public String getPrefix() {return prefix;}

    }
    private boolean keepBalanceFromLast;
    private Language userLanguage;
    private SharedPreferences prefs;
    private BudgetApp app;
    private static Settings s_instance;



    private Settings(BudgetApp app, SharedPreferences prefs) {
        this.app = app;
        this.prefs = prefs;
        this.keepBalanceFromLast = prefs.getBoolean(SETTINGS_KEEP_BALANCE_FROM_LAST, false);
        this.userLanguage = Settings.Language.getFromInt(prefs.getInt(SETTINGS_USER_LANGUAGE, 0));
        updateLocal(userLanguage);
    }

    public static synchronized Settings init(BudgetApp app,SharedPreferences prefs) {
        if (s_instance == null)
            s_instance = new Settings(app,prefs);
        return s_instance;
    }

    public static synchronized Settings getInstance() {
        return s_instance;
    }

    public void setKeepBalanceFromLast(boolean newValue) {
        if (keepBalanceFromLast != newValue) {
            this.keepBalanceFromLast = newValue;
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(SETTINGS_KEEP_BALANCE_FROM_LAST, newValue);
            editor.apply();
        }
    }

    /**
     *
     * @param newLanguage
     * @return true iff the language has changed
     */
    public boolean setUserLanguage(Language newLanguage) {
        Language oldLanguage = this.userLanguage;
        if (newLanguage == oldLanguage)
            return false;

        this.userLanguage = newLanguage;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(SETTINGS_USER_LANGUAGE, newLanguage.toInt());
        editor.apply();

        if (! oldLanguage.toLocale().equals(newLanguage.toLocale()))
            updateLocal(newLanguage);

        return true;
    }

    public void updateLocal() {updateLocal(userLanguage);}
    private void updateLocal(Language newLanguage) {
        String lclStr = newLanguage.toLocale();
        Locale locale = new Locale(lclStr);
        Locale.setDefault(locale);
        Configuration config = app.getBaseContext().getResources().getConfiguration();
        config.locale = locale;
        app.getBaseContext().getResources().updateConfiguration(config,
                app.getBaseContext().getResources().getDisplayMetrics());
    }


    public boolean isKeepBalanceFromLast() {
        return keepBalanceFromLast;
    }

    public Language getUserLanguage() {
        return userLanguage;
    }

    public boolean isUsingGenderDependent() {
        return userLanguage == Language.HEBREW_FEMALE || userLanguage == Language.HEBREW_MALE;
    }
}
