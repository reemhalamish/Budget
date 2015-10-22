package halamish.reem.budget;

import android.app.Application;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Re'em on 10/17/2015.
 */
public class BudgetApp extends Application {
    private static final String TAG = "bapp";
    SharedPreferences prefs;
    SharedPreferences.Editor editor;


    @Override
    public void onCreate() {
        super.onCreate();

        prefs = PreferenceManager.getDefaultSharedPreferences(BudgetApp.this);

        DatabaseHandler db = new DatabaseHandler(this);
        initSchedulerForAddBudgetToItems(db); // before db calculates all the curValue's of the items
        db.init();
    }

    private void initSchedulerForAddBudgetToItems(DatabaseHandler db) {

        Calendar lastUpdated = Calendar.getInstance();
        long lastTimeUsed = prefs.getLong("budget.lastUpdated", 0);
        lastUpdated.setTimeInMillis(lastTimeUsed);

        Calendar today = Calendar.getInstance();
        SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");

        Log.d(TAG, "today");

        int monthsPassed, weeksPassed;
        if (lastTimeUsed == 0) {
            monthsPassed = 1;
            weeksPassed = 1;
        } else {
            monthsPassed =
                    today.get(Calendar.MONTH) - lastUpdated.get(Calendar.MONTH)
                    + 12 * (today.get(Calendar.YEAR) - lastUpdated.get(Calendar.YEAR));

            weeksPassed =
                    today.get(Calendar.WEEK_OF_YEAR) - lastUpdated.get(Calendar.WEEK_OF_YEAR)
                    + 12 * (today.get(Calendar.YEAR) - lastUpdated.get(Calendar.YEAR));
        }
        if (monthsPassed > 0) {
            Log.d(TAG, "months passed: " + monthsPassed);
            for (BudgetItem item : db.getAllMonthlyUpdate()) {
                BudgetLine newLine = new BudgetLine(
                        "monthly add - " + today.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.ENGLISH),
                        "automatic",
                        item.getAuto_update_amount(),
                        formater.format(today.getTime()));
                db.tblAddBudgetLine(item, newLine, null);
            }
        }
        if (weeksPassed > 0) {
            Log.d(TAG, "weeks passed: " + weeksPassed);
            for (BudgetItem item : db.getAllWeeklyUpdate()) {
                BudgetLine newLine = new BudgetLine(
                        "weekly add - week No. " + today.getDisplayName(Calendar.WEEK_OF_YEAR, Calendar.LONG, Locale.ENGLISH),
                        "automatic",
                        item.getAuto_update_amount(),
                        formater.format(today.getTime())
                );
                db.tblAddBudgetLine(item, newLine, null);
            }
        }

        editor = prefs.edit();
        editor.putLong("budget.lastUpdated", today.getTimeInMillis());
        editor.apply();
    }
}


/*

TODO:
create handlers for the syncs, s.t. if i change something (add \ delete) it will show up immidately
add option to edit item\line on a long press, plus option to delete it
visual design!!!
import all this work to git
instructions file at google drive
 */