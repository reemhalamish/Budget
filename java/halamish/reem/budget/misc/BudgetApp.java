package halamish.reem.budget.misc;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.preference.PreferenceManager;


import java.util.Calendar;

import halamish.reem.budget.Activities.FirstActivity;
import halamish.reem.budget.R;
import halamish.reem.budget.Activities.TutorialActivity;
import halamish.reem.budget.data.BudgetItem;
import halamish.reem.budget.data.BudgetLine;
import halamish.reem.budget.data.DatabaseHandler;
import halamish.reem.budget.main.MainActivity;
import halamish.reem.budget.main.MainActivityMultiSelectHandler;

/**
 * Created by Re'em on 10/17/2015.
 *
 * The app controller :)
 */
public class BudgetApp extends Application {
    private static final String TAG = "bapp";
    private static final long TIME_TO_SLEEP_AFTER_READY_MS = 750;
    private static final String FINISHED_TUTORIAL = "budget.finished_tutorial";
    private static final String LAST_UPDATED = "budget.lastUpdated";
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    Settings settings;
    FirstActivity introActivity = null; // will be registered when the activity will load
    private boolean endedInitiation = false;
    Calendar lastTime_AFTER_AppOpened = null; // optimization

    private enum ThingsToInit {
        MISC,
        SCHEDULER;
    };

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        settings = Settings.init(this, prefs); // before anything, not on the Async task since it creates a race when combined with onConfigurationChanged
        new InitAll().execute(
                ThingsToInit.MISC,
                ThingsToInit.SCHEDULER
        );
    }

    private boolean checkIfNeededToInitScheduler() {
        return lastTime_AFTER_AppOpened == null ||
                lastTime_AFTER_AppOpened.get(Calendar.DAY_OF_MONTH) !=
                        Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

    }

    private synchronized void initSchedulerForAddBudgetToItems(DatabaseHandler db) {
        if (! checkIfNeededToInitScheduler())
            return;

        Calendar rightNow, lastUpdated;
        rightNow = Calendar.getInstance();
        lastTime_AFTER_AppOpened = rightNow;
        lastUpdated = Calendar.getInstance();
        long lastTimeUsed = prefs.getLong(LAST_UPDATED, 0);
        lastUpdated.setTimeInMillis(lastTimeUsed);


        int monthsPassed, weeksPassed;
        if (lastTimeUsed == 0) {
            return;
        } else {
            monthsPassed =
                    rightNow.get(Calendar.MONTH) - lastUpdated.get(Calendar.MONTH)
                    + 12 * (rightNow.get(Calendar.YEAR) - lastUpdated.get(Calendar.YEAR));

            weeksPassed =
                    rightNow.get(Calendar.WEEK_OF_YEAR) - lastUpdated.get(Calendar.WEEK_OF_YEAR)
                    + 12 * (rightNow.get(Calendar.YEAR) - lastUpdated.get(Calendar.YEAR));
        }
        if (monthsPassed > 0) {
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
        editor.putLong(LAST_UPDATED, rightNow.getTimeInMillis());
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


    private synchronized void closeIntroAndLaunchRelevantActivitiesWhenReady() {
        if (introActivity == null || !endedInitiation)
            return;

        boolean finishedTutorial = prefs.getBoolean(FINISHED_TUTORIAL, false);

        final Intent nextActivity;
        if (finishedTutorial) {
            nextActivity = new Intent(BudgetApp.this, MainActivity.class);
            nextActivity.putExtra(FirstActivity.SHOULD_DISPLAY_ENGLISH_HEB_BUTTONS, false);
        } else {
            nextActivity = new Intent(BudgetApp.this, TutorialActivity.class);
            nextActivity.putExtra(FirstActivity.SHOULD_DISPLAY_ENGLISH_HEB_BUTTONS, true);
        }
        introActivity.appReadyToLaunch(nextActivity);
        introActivity = null;
    }

    public void finishedTutorial() {
        editor = prefs.edit();
        editor.putBoolean(FINISHED_TUTORIAL, true);
        editor.apply();
    }



    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        settings.updateLocal();
    }

    @Override
    public void onTerminate() {
        MainActivityMultiSelectHandler.getInstance().freeMemory();
        Utils.freeMemory();
        super.onTerminate();
    }

    /**
     * this function registers the intro activity to be closed when the app is ready to begin
     * @param intro the activity
     */
    public void registerIntro(FirstActivity intro) {
        introActivity = intro;
        updateScheduleAmountIfNeeded();
        closeIntroAndLaunchRelevantActivitiesWhenReady();
    }


    /**
     * is called on onResume() of activities s.t. every time user logs in the amount gets updated
     */
    public void updateScheduleAmountIfNeeded() {
        if (checkIfNeededToInitScheduler()) {
            // Log.d(TAG, "calling InitAll");
            new InitAll().execute(ThingsToInit.SCHEDULER);
        }
    }

    private class InitAll extends AsyncTask<ThingsToInit, Void, Void> {
        BudgetApp app = BudgetApp.this;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            app.endedInitiation = false;
        }

        @Override
        protected Void doInBackground(ThingsToInit... args) {
            // Log.d(TAG, "starting...");
            DatabaseHandler db = new DatabaseHandler(app);
            for (ThingsToInit t : args) {
                switch (t) {
                    case MISC:
                        Utils.init();
                        Rsrcs.init(app); // AFTER Settings.init() ONEDAY will be used
                        MainActivityMultiSelectHandler.getInstance().init(db);
                        // the handler is not using the db at init, don't worry
                        // it does uses it when swapping objects, deleting objects etc

                        break;
                    case SCHEDULER:
                        initSchedulerForAddBudgetToItems(db);
                        break;
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void ignored) {
            super.onPostExecute(ignored);
            app.endedInitiation = true;
            app.closeIntroAndLaunchRelevantActivitiesWhenReady();
        }
    }

}



/*
ONEDAY

@ animate swapping two items at MainActivity

@ MainActivity clicking the info - add "average user input" (by summing all USER_INPUT and BALANCE_CHANGED lines
            and dividing by amount of "AUTO_UPDATE" + "BUDGET_CREATED"))
             or dividing by diff in weeks\months (firstLineInBudget.getDate(), Utils.getNow())

@ make some "YUPI HOO!!" when detecting user succeeded to stay below zero.
    when reset mode, msg will be "You saved ... , well done! (you can make the app add the money to the new week if you want, just change it in the settings!)"
    when move to new mode, msg will be postfixed with "the money was added to current week\month's balance, so you have now some extra cash to spend on that budget this month, Woohoo!"
  and (at the same dialog?) make a msg of "you out-stepped in following budget(s) (...)  by total amount of ..."
  when reset mode, postfix "but don't worry! Every day is a new day and the budgets reset-ed! try to do better this week :)
  when move to new mode, add "but it's ok, the balance has moved to the new week\month and you just have to LEHADEK HAGORA and pass this month with some decreased starting balance.
        it's totally doable, you can do it!"

@ at ItemActivity when clicking the sum, it will open some nice animation of money and will show you some fancy graph
with the cur-amount of every week. with some nice massage telling you if you got better or worse

@ the design of BudgetLineParser will cause troubles when using the app long time - O(n)
should go for a better design - add column "archived" in BudgetLine SQLite tables and ask only for archived\all
(then the parser will need an access to the db itself?)
can be solved either by auto-deleting when reaching more then 1000 lines


@ add some cute intro like "it's your first time here, do you want to add some of our common budgets?
(supermarket, blahblahblah)
with a checkbox and a default value that can be changed, plus add some more with an empty line (another empty line will appear if this line gets populated)


@ make all work with db run in another thread and a nice spiralla thingy will move round until it's done

@ ACHIEVEMENTS - when user does good stuff (double - one for week, one for month) e.g.:
   budget >0 at weekend for 1 time in a row,
                        for 3 times in a row,
                        for 5 times in a row,
                        for 10 times in a row,
                        for 20 times in a row,
                        for 50 times in a row,
                        for 100 times in a row,
    "recovering" from worse-than zero:
        same budget still <0 weekend later, but in smaller abs()
        same budget >0 weekend later
    staying at more than zero even after reduced auto_update in the budget,
    etc...

 */