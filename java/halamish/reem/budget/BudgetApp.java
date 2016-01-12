package halamish.reem.budget;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import halamish.reem.budget.data.BudgetItem;
import halamish.reem.budget.data.BudgetLine;
import halamish.reem.budget.data.DatabaseHandler;
import halamish.reem.budget.main.MainActivityMultiSelectHandler;

/**
 * Created by Re'em on 10/17/2015.
 *
 * The app controller :)
 */
public class BudgetApp extends Application {
    private static final String TAG = "bapp";
    SharedPreferences prefs;
    SharedPreferences.Editor editor;


    @Override
    public void onCreate() {
        super.onCreate();
        utils.init();

        prefs = PreferenceManager.getDefaultSharedPreferences(BudgetApp.this);

//        testTime();

        DatabaseHandler db = new DatabaseHandler(this);
        MainActivityMultiSelectHandler.getInstance().init(db);
            // the handler is not using the db, don't worry

        initSchedulerForAddBudgetToItems(db);
            // before db calculates all the curValue's of the items at init()
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
            Calendar dispCal = Calendar.getInstance();
            dispCal.add(Calendar.MONTH, -1 * (monthsPassed - 1)); // start the display calendar
            for (int i = 0; i < monthsPassed; i++) {
                for (BudgetItem item : db.getAllMonthlyUpdate()) {
                    BudgetLine newLine = new BudgetLine(
                            "monthly add - " + utils.getMonth(dispCal.getTime()),
                            "automatic",
                            item.getAuto_update_amount(),
                            dispCal.getTime().getTime(),
                            BudgetLine.BudgetLineEventType.AUTO_UPDATE);
                    db.tblAddBudgetLine(item, newLine);
                }
                dispCal.add(Calendar.MONTH, 1); // prepare for next month
            }
        }
        if (weeksPassed > 0) {
            Log.d(TAG, "weeks passed: " + weeksPassed);
            Calendar dispCal = Calendar.getInstance();
            dispCal.add(Calendar.DATE, -7 * (weeksPassed -1 )); // start the display calendar

            for (int i = 0; i < weeksPassed; i++) {
                for (BudgetItem item : db.getAllWeeklyUpdate()) {
                    BudgetLine newLine = new BudgetLine(
                            "weekly add (" + dispCal.get(Calendar.WEEK_OF_YEAR) + ")",
                            "automatic",
                            item.getAuto_update_amount(),
                            dispCal.getTime().getTime(),
                            BudgetLine.BudgetLineEventType.AUTO_UPDATE
                    );
                    db.tblAddBudgetLine(item, newLine);
                }
                dispCal.add(Calendar.DATE, 7); // add a week for next line
            }
        }

        editor = prefs.edit();
        editor.putLong("budget.lastUpdated", today.getTimeInMillis());
        editor.apply();
    }

    @Override
    public void onTerminate() {
        MainActivityMultiSelectHandler.getInstance().freeMemory();
        utils.freeMemory();
        super.onTerminate();
    }
}


/*

TODO:
* add option to edit line on a long press, plus option to delete it
* update the "create csv output" option to reflect all the info

* visual design for line:
    show just two columns: number and an icon for each one of four (automattically\manually plus\minus)
    on pressing item, get into a dialog that shows it with all details.
    on creating or editing item, get into a dialog that let you pick:
        date(default)
        amount(empty),
        ?(choose an icon with default?)
        heading(empty)
        details(empty)
    on long pressing item, you can:
        edit
        delete
        view (same as clicking)
    after all of those, there will be extra line of "sum"
    after that line there will be another line of "add new?" in which the phone will roll into when onCreate() is called




 */


/*
ONEDAY
when clicking the sum, it will open some nice animation of money and will show you some fancy graph

the design of BudgetLineParsser will cause troubles when using long time - O(n)
should go for a better design - add column "archived" in BudgetLine SQLite tables and ask only for archived\all
(then the parser will need an access to the db itself?)
 */