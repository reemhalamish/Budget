package halamish.reem.budget;

/**
 * Created by Re'em on 1/24/2016.
 */
public class Rsrcs {
    private static Rsrcs s_instance;
    private BudgetApp app;
    private static Settings settings;

    public Rsrcs(BudgetApp app) {
        this.app = app;
        settings = Settings.getInstance();
    }

    public static synchronized void init(BudgetApp app) {
        if (s_instance == null) {
            s_instance = new Rsrcs(app);
        }
    }

    public static synchronized int getIdByName(String identifier) {
        final String newId;
        if (settings.isUsingGenderDependent()) {
            String prefix = settings.getUserLanguage().getPrefix();
            newId = prefix + identifier;
            int localeFirstTry = s_instance.app.getResources().getIdentifier(newId, "string", s_instance.app.getPackageName());
            if (localeFirstTry > 0)
                return localeFirstTry;
        }
        return s_instance.app.getResources().getIdentifier(identifier, "string", s_instance.app.getPackageName());
    }

    public static synchronized String getString(String identifier) {
        return s_instance.app.getResources().getString(getIdByName(identifier));
    }

    /**
     * TODO
     * create seperate string files (e.g. "main activity", "app_toasts", "settings" etc) under values and values-en (hebrew will be default)
     * at values-he create ONLY files for male and female strings. the files will be prefixed for readability.
     * the strings themselves will be prefixed with "ml_" and "fml_" for actuall need.
     * when choosing language male\female need to change the locale to be "he"
     */
}
