package halamish.reem.budget;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Re'em on 10/17/2015.
 */
public class Utils {
    private static SimpleDateFormat fullDateFormatter, monthOnlyFormatter;

    public static void init() {
        fullDateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        monthOnlyFormatter = new SimpleDateFormat("MMM", Locale.getDefault());
    }

    public static String getToday() {
        Calendar today = Calendar.getInstance();
        return fullDateFormatter.format(today.getTime());
    }

    public static String millisecToDate(long milliseconds) {
        Calendar relevantTime = Calendar.getInstance();
        relevantTime.setTimeInMillis(milliseconds);
        return fullDateFormatter.format(relevantTime.getTime());
    }

    public static long getMillisecondNow() {
        return Calendar.getInstance().getTimeInMillis();
    }

    public static String getMonth(Date date) {
        return monthOnlyFormatter.format(date);
    }

    public static void freeMemory() {
        fullDateFormatter = null;
        monthOnlyFormatter = null;
    }
}
