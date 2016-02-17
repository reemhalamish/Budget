package halamish.reem.budget.style;

import android.os.Bundle;
import android.view.Window;

/**
 * Created by Re'em on 2/17/2016.
 */
public class BudgetFullScreenActivity extends BudgetBaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
    }
}
