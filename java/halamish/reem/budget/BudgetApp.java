package halamish.reem.budget;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.util.Log;

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
    Settings settings;


    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init();

        prefs = PreferenceManager.getDefaultSharedPreferences(BudgetApp.this);
        settings = Settings.init(this, prefs);
        Rsrcs.init(this); // AFTER Settings.init()

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
                    finishedSeason(dispCal, item, db);
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
                    finishedSeason(dispCal, item, db);
                }
                dispCal.add(Calendar.DATE, 7); // add a week for next line
            }
        }

        editor = prefs.edit();
        editor.putLong("budget.lastUpdated", today.getTimeInMillis());
        editor.apply();
    }

    void finishedSeason(Calendar cal, BudgetItem item, DatabaseHandler db) {
        int prevBalance = item.getCur_value();
        String autoUpdateTime = item.getAuto_update();
        String title;
        switch (autoUpdateTime) {
            case BudgetItem.WEEKLY:
                title = getString(R.string.app_autoupdate_weeklyadd) + " (" + cal.get(Calendar.WEEK_OF_YEAR) + ")";
                break;
            case BudgetItem.MONTHLY:
            default:
                title = getString(R.string.app_autoupdate_monthlyadd)+ " (" + Utils.getMonth(cal.getTime()) + ")";

        }

        BudgetLine newLine = new BudgetLine(
                title ,
                getString(R.string.app_autoupdate_details_automatic),
                item.getAuto_update_amount(),
                cal.getTime().getTime(),
                BudgetLine.BudgetLineEventType.AUTO_UPDATE
        );
        db.tblAddBudgetLine(item, newLine);

        // choosing what to do with the previous balance
        if (settings.isKeepBalanceFromLast()) {
            switch (autoUpdateTime) {
                case BudgetItem.WEEKLY:
                    title = getString(R.string.app_autoupdate_fromlast_week);
                    break;
                case BudgetItem.MONTHLY:
                default:
                    title = getString(R.string.app_autoupdate_fromlast_month);
            }

            newLine = new BudgetLine(
                    title,
                    getString(R.string.app_autoupdate_fromlast_details),
                    prevBalance,
                    cal.getTime().getTime() + 1,
                    BudgetLine.BudgetLineEventType.MOVE_FROM_LAST_TIME);
            db.tblAddBudgetLine(item, newLine);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "onConfigurationChanged");
        settings.updateLocal();
    }

    @Override
    public void onTerminate() {
        MainActivityMultiSelectHandler.getInstance().freeMemory();
        Utils.freeMemory();
        super.onTerminate();
    }
}


/*

TODO:

@ add button for cleaning item on long-press

@ add option to open a dialog with the line's info onPress on the line

 */


/*
ONEDAY

@ at ItemActivity when clicking the sum, it will open some nice animation of money and will show you some fancy graph
with the cur-amount of every week. with some noce massage telling you if you got better or worse

@ the design of BudgetLineParser will cause troubles when using the app long time - O(n)
should go for a better design - add column "archived" in BudgetLine SQLite tables and ask only for archived\all
(then the parser will need an access to the db itself?)
can be solved either by auto-deleting when reaching more then 1000 lines


@ add some cute intro like "it's your first time here, do you want to add some of our common budgets?
(supermarket, blahblahblah)
with a checkbox and a default value that can be changed, plus add some more with an empty line (another empty line will appear if this line gets populated)


@ make all work with db run in another thread and a nice spiralla thingy will move round until it's done

@ ACHIEVEMENTS - when user does good stuff (e.g. staying more then 0 for week, for month, for 3 times in a row, "recovering" from worse-than zero, staying at more than zero even after reduced auto_update in the budget, etc...)

 */