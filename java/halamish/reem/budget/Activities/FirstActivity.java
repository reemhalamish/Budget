package halamish.reem.budget.Activities;

import android.content.Intent;

/**
 * Created by Re'em on 1/29/2016.
 */
public interface FirstActivity {
    public String SHOULD_DISPLAY_ENGLISH_HEB_BUTTONS = "FirstActivityShouldDisplay";
    void appReadyToLaunch(Intent nextActivity);
}
