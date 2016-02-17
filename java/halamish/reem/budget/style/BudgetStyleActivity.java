package halamish.reem.budget.style;

import android.graphics.BitmapFactory;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;

import halamish.reem.budget.misc.BudgetApp;
import halamish.reem.budget.R;

/**
 * Created by Re'em on 1/28/2016.
 */
public class BudgetStyleActivity extends BudgetBaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        makeBubblesActionBar();
    }

    private void makeBubblesActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            BitmapDrawable drawable = new BitmapDrawable(
                    getResources(),
                    BitmapFactory.decodeResource(
                            getResources(),
                            R.drawable.bubbles_actionbar
                    )
            );

            drawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.CLAMP);
            actionBar.setBackgroundDrawable(drawable);
            actionBar.show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((BudgetApp) getApplication()).updateScheduleAmountIfNeeded();
    }

}
