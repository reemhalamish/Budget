package halamish.reem.budget;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Re'em on 10/17/2015.
 */
public class utils {
    public static String getToday() {
        Calendar today = Calendar.getInstance();
        SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
        return formater.format(today.getTime());
    }
}
