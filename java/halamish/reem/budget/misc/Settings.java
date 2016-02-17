package halamish.reem.budget.misc;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;

import java.util.Locale;

/**
 * holds the settings
 * Created by Re'em on 1/13/2016.
 */
public class Settings {
    private static final String SETTINGS_KEEP_BALANCE_FROM_LAST =   "budget.settings_keep_balance_from_last";
    private static final String SETTINGS_USER_LANGUAGE =            "budget.settings_user_language";
    private static final String SETTINGS_ITEM_SUM_FLICKERING =      "budget.settings_activity_item_flickering_sum";

    public Typeface getFont() {
        return font;
    }
    public Typeface getFont(Language language) {
        if (language == null) return Typeface.DEFAULT;
        return Typeface.createFromAsset(app.getAssets(),language.getFontYoung());
    }


    public enum Language{
        // http://stackoverflow.com/questions/13721668/using-different-string-files-in-android
        ENGLISH(0, "en", ""),
        HEBREW_UNISEX(10, "he", "");
//        HEBREW_MALE(11, "he", "ml_"),     ONEDAY
//        HEBREW_FEMALE(12, "he", "fml_");  ONEDAY

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
        public String getFontYoung() {
            if (locale == "he")
                return "fonts/hebrew_young.ttf";
            return "fonts/english_young.ttf";

        }
    }
    private boolean keepBalanceFromLast;
    private Language userLanguage;
    private SharedPreferences prefs;
    private BudgetApp app;
    private Typeface font;
    private boolean activity_item_sum_flickering;
    private static Settings s_instance;



    private Settings(BudgetApp app, SharedPreferences prefs) {
        this.app = app;
        this.prefs = prefs;
        this.keepBalanceFromLast = prefs.getBoolean(SETTINGS_KEEP_BALANCE_FROM_LAST, false);
        this.userLanguage = Settings.Language.getFromInt(prefs.getInt(SETTINGS_USER_LANGUAGE, 0));
        this.activity_item_sum_flickering = prefs.getBoolean(SETTINGS_ITEM_SUM_FLICKERING, true);
        updateLocal(userLanguage);
        updateFont();
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

        if (! oldLanguage.toLocale().equals(newLanguage.toLocale())) {
            updateLocal(newLanguage);
            updateFont();
        }

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

    private void updateFont() {
        font = Typeface.createFromAsset(app.getAssets(),userLanguage.getFontYoung());
    }


    public boolean isKeepBalanceFromLast() {
        return keepBalanceFromLast;
    }

    public Language getUserLanguage() {
        return userLanguage;
    }

    public boolean isUsingGenderDependent() {
        return false;
//        return userLanguage == Language.HEBREW_FEMALE || userLanguage == Language.HEBREW_MALE; ONEDAY
    }

    public boolean isActivity_item_sum_flickering() {
        return activity_item_sum_flickering;
    }
    public void setActivity_item_sum_flickering(boolean newValue) {
        activity_item_sum_flickering = newValue;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(SETTINGS_ITEM_SUM_FLICKERING, newValue);
        editor.apply();
    }

}
