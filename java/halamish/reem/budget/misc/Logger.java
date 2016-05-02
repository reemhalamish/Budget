package halamish.reem.budget.misc;

import android.content.Context;
import android.util.Log;

/**
 * Created by Re'em on 5/2/2016.
 */
public class Logger {
    public static void log(Object caller, String msg) {
        Log.i(caller.getClass().getSimpleName(), msg);
    }
    public static void log(Object something) {
        Log.i("something", something.toString());
    }
}
