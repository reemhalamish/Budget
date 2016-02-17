package halamish.reem.budget;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import halamish.reem.budget.data.BudgetItem;
import halamish.reem.budget.data.BudgetLine;
import halamish.reem.budget.data.BudgetLinesParser;
import halamish.reem.budget.item.ListViewItemAdapter;
import halamish.reem.budget.main.GridViewItemAdapter;
import halamish.reem.budget.main.MainActivity;
import halamish.reem.budget.misc.BudgetApp;
import halamish.reem.budget.misc.Settings;
import halamish.reem.budget.misc.Utils;
import halamish.reem.budget.style.BudgetBaseActivity;

/**
 * Created by Re'em on 1/30/2016.
 */
public class TutorialActivity extends BudgetBaseActivity {
    private static final java.lang.String CURRENT_SCENE = "current_scene";
    private static final String TAG = "tutorial";
    private int PROGRESSVIEW_SIZE_BIG; // 24dp
    private int PROGRESSVIEW_SIZE_SMALL; // 16dp
    private ViewFlipper mViewFlipper;
    private GestureDetector mGestureDetector;
    private BudgetItem mBudgetItemSnacks;
    private ImageView[] mProgressViews;
    private RelativeLayout mMainLayout;

    private class CustomGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            int totalChildren, curChild;
            boolean hasLeftMovement, hasRightMovement;
            totalChildren = mViewFlipper.getChildCount();
            curChild = mViewFlipper.getDisplayedChild();
            hasLeftMovement = totalChildren > curChild+1;
            hasRightMovement = curChild > 0;


            // Swipe left (next)
            if (e1.getX() > e2.getX() && hasLeftMovement) {
                mViewFlipper.setInAnimation(TutorialActivity.this, R.anim.viewflipper_left_in);
                mViewFlipper.setOutAnimation(TutorialActivity.this, R.anim.viewflipper_left_out);
                mViewFlipper.showNext();
                updateProgressViews(mViewFlipper.getDisplayedChild());
            }

            // Swipe right (previous)
            if (e1.getX() < e2.getX() && hasRightMovement) {
                mViewFlipper.setInAnimation(TutorialActivity.this, R.anim.viewflipper_right_in);
                mViewFlipper.setOutAnimation(TutorialActivity.this, R.anim.viewflipper_right_out);
                mViewFlipper.showPrevious();
                updateProgressViews(mViewFlipper.getDisplayedChild());
            }

            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }

    private void updateProgressViews(int displayedChild) {
        for (int i = 0; i < mProgressViews.length; i++) {
            ImageView view = mProgressViews[i];
            if (i == displayedChild) {
                view.setImageResource(R.drawable.converted_bubble_green);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mProgressViews[i].getLayoutParams();
                params.height = PROGRESSVIEW_SIZE_BIG;
                params.width = PROGRESSVIEW_SIZE_BIG;
                view.setLayoutParams(params);
            } else {
                view.setImageResource(R.drawable.converted_bubble_blue);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mProgressViews[i].getLayoutParams();
                params.height = PROGRESSVIEW_SIZE_SMALL;
                params.width = PROGRESSVIEW_SIZE_SMALL;
                view.setLayoutParams(params);
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Log.d(TAG, "created!");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        mViewFlipper = (ViewFlipper) findViewById(R.id.vf_tut_main_flipper);
        mGestureDetector = new GestureDetector(this, new CustomGestureDetector());

        mViewFlipper.setInAnimation(this, android.R.anim.fade_in);
        mViewFlipper.setOutAnimation(this, android.R.anim.fade_out);

        makeAndConnectView0();
        makeAndConnectView1();
        makeAndConnectView2();
        makeAndConnectView3();

        makeProgressViewsSizes();

        if (savedInstanceState != null) {
            int currentScene = savedInstanceState.getInt(CURRENT_SCENE);
            mViewFlipper.setDisplayedChild(currentScene);
            updateProgressViews(currentScene);
        }


    }

    private void makeProgressViewsSizes() {
        mProgressViews = new ImageView[] {
                (ImageView) findViewById(R.id.iv_tutorial_start),
                (ImageView) findViewById(R.id.iv_tutorial_prev),
                (ImageView) findViewById(R.id.iv_tutorial_mid),
                (ImageView) findViewById(R.id.iv_tutorial_next)
        };
        Resources resources = getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        PROGRESSVIEW_SIZE_BIG = (int) (24 * (metrics.densityDpi / 160f));
        PROGRESSVIEW_SIZE_SMALL = (int) (16 * (metrics.densityDpi / 160f));
    }

    private void makeAndConnectView0() {
        int[] allTextViews = new int[]{
                R.id.tv_tut_0_title,
                R.id.tv_tut_0_intro_line,
                R.id.tv_tut_0_explanation1
        };
        setFontOnTextViews(allTextViews);
        ImageView iv_arrow = (ImageView) findViewById(R.id.iv_tut_0_arrow);
        ListViewItemAdapter.startFlickeringEffect(iv_arrow);
        iv_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mViewFlipper.setInAnimation(TutorialActivity.this, R.anim.viewflipper_left_in);
                mViewFlipper.setOutAnimation(TutorialActivity.this, R.anim.viewflipper_left_out);
                mViewFlipper.showNext();
                updateProgressViews(1);

            }
        });
    }

    private void makeAndConnectView1() {
        List<BudgetItem> objects = new LinkedList<>();
        BudgetItem bi1st, bi2nd, bi3rd;
        bi1st = new BudgetItem(
                getString(R.string.tut_v1_entry1),
                595
        );
        bi2nd = new BudgetItem(
                getString(R.string.tut_v1_entry2),
                74
        );
        bi3rd = new BudgetItem(
                getString(R.string.tut_v1_entry3),
                30
        );
        mBudgetItemSnacks = bi3rd;
        objects.add(bi1st);
        objects.add(bi2nd);
        objects.add(bi3rd);

        GridView gridView = (GridView) findViewById(R.id.gv_tut_vf_1);
        GridViewItemAdapter aa = new GridViewItemAdapter(
                this,
                R.layout.budget_item,
                objects,
                null,
                null,
                null,
                null
        );

        gridView.setAdapter(aa);

        int[] expalantionTexts = new int[]{
                R.id.tv_tut_1_explanation1,
                R.id.tv_tut_1_explanation2};
        setFontOnTextViews(expalantionTexts);
    }

    private void makeAndConnectView2() {
        long oneDayInMs = 1000 * 60 * 60 * 24;
        long timeRightNow = Utils.getMillisecondNow();
        BudgetLine a1, a2, a3, w, u1, u2;
//        a1 = new BudgetLine(
//                "Chocolate",
//                "that was a great one",
//                -18,
//                timeRightNow - 8 * oneDayInMs,
//                BudgetLine.BudgetLineEventType.USER_INPUT
//        );
//        a2 = new BudgetLine(
//                "Machine coffee at work",
//                "was tired",
//                -5,
//                timeRightNow - 6 * oneDayInMs,
//                BudgetLine.BudgetLineEventType.USER_INPUT
//        );
//        a3 = new BudgetLine(
//                "Cookies",
//                "the coffee didn't really help",
//                -12,
//                timeRightNow - 6 * oneDayInMs,
//                BudgetLine.BudgetLineEventType.USER_INPUT
//        );
        w = new BudgetLine(
                getString(R.string.app_autoupdate_weeklyadd) + " (5)",
                getString(R.string.app_autoupdate_details_automatic),
                70,
                timeRightNow - 3 * oneDayInMs,
                BudgetLine.BudgetLineEventType.AUTO_UPDATE
                );
        u1 = new BudgetLine(
                getString(R.string.tut_v2_entry1_title),
                getString(R.string.tut_v2_entry1_details),
                -5,
                timeRightNow - 1 * oneDayInMs,
                BudgetLine.BudgetLineEventType.USER_INPUT
        );
        u2 = new BudgetLine(
                getString(R.string.tut_v2_entry2_title),
                getString(R.string.tut_v2_entry2_details),
                -16,
                timeRightNow - 1 * oneDayInMs,
                BudgetLine.BudgetLineEventType.USER_INPUT
        );
        ListView listView = (ListView) findViewById(R.id.lv_tut_vf_2);
        List<BudgetLine> objects = new LinkedList<>(Arrays.asList(w, u1, u2));
        ListViewItemAdapter aa = new ListViewItemAdapter(
                this,
                R.layout.budget_line,
                listView,
                new BudgetLinesParser(objects),
                mBudgetItemSnacks,
                null,
                null,
                null
        );
        aa.loadAll();
        listView.setAdapter(aa);

        findViewById(R.id.inc_tut_2_header).setAlpha(0.12f);

        int[] explanationTextViews = new int[] {
                R.id.tv_tut_2_explanation_listview,
                R.id.tv_tut_2_explanation_sandclock
        };
        setFontOnTextViews(explanationTextViews);
    }

    private void makeAndConnectView3() {
        int[] allTextViews = new int[] {
                R.id.tv_tut_3_text1,
                R.id.tv_tut_3_text2,
                R.id.tv_tut_3_press_to_start
        };
        setFontOnTextViews(allTextViews);
        findViewById(R.id.tv_tut_3_press_to_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                Intent startApp = new Intent(TutorialActivity.this, MainActivity.class);
                startActivity(startApp);
                ((BudgetApp) getApplication()).finishedTutorial();
            }
        });
    }

    public void setFontOnTextViews(int[] textViews) {
        for (int tv : textViews) {
            TextView explanationText = (TextView) findViewById(tv);
            explanationText.setTypeface(Settings.getInstance().getFont());
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    /**
     * Save all appropriate fragment state.
     *
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(CURRENT_SCENE, mViewFlipper.getDisplayedChild());
        super.onSaveInstanceState(outState);
    }
}
