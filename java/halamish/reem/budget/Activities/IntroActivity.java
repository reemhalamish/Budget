package halamish.reem.budget.Activities;

import android.animation.Animator;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import halamish.reem.budget.R;
import halamish.reem.budget.misc.BudgetApp;
import halamish.reem.budget.misc.Settings;
import halamish.reem.budget.style.BudgetFullScreenActivity;

/**
 * Created by Re'em on 1/29/2016.
 *
 * the intro that the user sees until the app is ready
 */


public class IntroActivity extends BudgetFullScreenActivity implements FirstActivity {
    private static final String TAG = "ActivityIntro";
    private static final long BUBBLE_ANIM_DUR_MS = 300;
    private static final long WAIT_AFTER_READY_MS = 400;
    private static final long TIME_UNTIL_NEXT_TRY_MS = 50;


    private static final int BUBBLE_MARGIN = 50 + 100; // MRAGIN of bubble + MARGIN of scene
//    private boolean activityRegistered = false;
//    private boolean activityGotContinue = false;
//    private boolean alreadyAnimated = false;
    private Intent nextActivity = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_intro);
        ((BudgetApp) getApplication()).registerIntro(this);

        Typeface defaultFont = Settings.getInstance().getFont();
        ((TextView) findViewById(R.id.tv_intro_title)).setTypeface(defaultFont);

        findViewById(R.id.iv_intro_bubble2).setVisibility(View.VISIBLE);

        final View layout = findViewById(R.id.rl_intro);
        layout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startNextSceneIfReady();
                    }
                }, WAIT_AFTER_READY_MS);


                // remove the observer at the end of the use
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    //noinspection deprecation
                    layout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });


    }

    private void startNextSceneIfReady() {
        if (nextActivity == null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startNextSceneIfReady();
                }
            }, TIME_UNTIL_NEXT_TRY_MS);
            return;
        }
        if (nextActivity.getBooleanExtra(SHOULD_DISPLAY_ENGLISH_HEB_BUTTONS, true)) {
            moveBubbles();
        } else {
            exit_nice();
        }
    }

    /**
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are <em>not</em> resumed.  This means
     * that in some cases the previous state may still be saved, not allowing
     * fragment transactions that modify the state.  To correctly interact
     * with fragments in their proper state, you should instead override
     * {@link #onResumeFragments()}.
     */
    @Override
    protected void onResume() {
        super.onResume();
    }

//    @Override
//    public void onCloseWhenReady() {
//        finish();
//        overridePendingTransition(0,0); // exit without showing any animation
//    }

//    @Override
//    /**
//     * show the language buttons, invisiblate the other bubbles and start TutorialActivity if pressing
//     */
//    public void onContinueYourself() {
//        activityGotContinue = true;
//        final ImageView iv_bubble2;
//
//        tvEng =         (TextView)  findViewById(R.id.tv_intro_bubble1_eng);
//        iv_bubble2 =    (ImageView) findViewById(R.id.iv_intro_bubble2);
//        tvHeb =         (TextView)  findViewById(R.id.tv_intro_bubble3_heb);
//        rl_intro = (RelativeLayout) findViewById(R.id.rl_intro);
//
//        // Log.d(TAG, "onContinueYourself()");
//        // Log.d(TAG, "left: " + tvEng.getLeft());
//
//
//        // set the font
//        Typeface hebrewFont, englishFont, defaultFont;
//        hebrewFont = Typeface.createFromAsset(getApplication().getAssets(), Settings.Language.HEBREW_UNISEX.getFontYoung());
//        englishFont = Typeface.createFromAsset(getApplication().getAssets(), Settings.Language.ENGLISH.getFontYoung());
//        defaultFont = Settings.getInstance().getFont();
//        tvEng.setTypeface(englishFont);
//        tvHeb.setTypeface(hebrewFont);
//        ((TextView)findViewById(R.id.tv_intro_line)).setTypeface(defaultFont);
//        findViewById(           R.id.tv_intro_line).setVisibility(View.VISIBLE);
//
//        moveBubbles(true);
//
//        iv_bubble2.animate()
//                .alpha(0.0f)
//                .setListener(new Animator.AnimatorListener() {
//                    @Override
//                    public void onAnimationStart(Animator animator) {
//
//                    }
//
//                    @Override
//                    public void onAnimationEnd(Animator animator) {
//                        iv_bubble2.setVisibility(View.INVISIBLE);
//                    }
//
//                    @Override
//                    public void onAnimationCancel(Animator animator) {
//
//                    }
//
//                    @Override
//                    public void onAnimationRepeat(Animator animator) {
//
//                    }
//                }); // invisible at the end
//
//        // set click listeners to start the tutorial
//        final Intent startTutorial = new Intent(this, TutorialActivity.class);
//        startTutorial.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
//
//        tvEng.setOnClickListener(
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        Settings.getInstance().setUserLanguage(Settings.Language.ENGLISH);
//                        finish();
//                        startActivity(startTutorial);
//                    }
//                }
//        );
//
//        tvHeb.setOnClickListener(
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        Settings.getInstance().setUserLanguage(Settings.Language.HEBREW_UNISEX);
//                        finish();
//                        startActivity(startTutorial);
//                    }
//                }
//        );
//
//    }
//
    private void moveBubbles() {
        final TextView tvHeb, tvEng;
        final ImageView ivBubble2;
        RelativeLayout rl_intro;

        tvEng =         (TextView)  findViewById(R.id.tv_intro_bubble1_eng);
        tvHeb =         (TextView)  findViewById(R.id.tv_intro_bubble3_heb);
        ivBubble2 =     (ImageView) findViewById(R.id.iv_intro_bubble2);
        rl_intro = (RelativeLayout) findViewById(R.id.rl_intro);

        float parentTop = rl_intro.getTop() + BUBBLE_MARGIN;
        float parentBot = rl_intro.getBottom() - BUBBLE_MARGIN;
        float parentLeft = rl_intro.getLeft() + BUBBLE_MARGIN;
        float parentRight = rl_intro.getRight() - BUBBLE_MARGIN;

        float bubble1midX = tvEng.getX() + tvEng.getWidth()/2;
        float bubble1midY = tvEng.getY() + tvEng.getHeight()/2;

        float bubble3midX = tvHeb.getX() + tvHeb.getWidth()/2;
        float bubble3midY = tvHeb.getY() + tvHeb.getHeight()/2;

            tvEng.animate()
                    .translationX(parentRight - bubble1midX)
                    .translationY(parentTop - bubble1midY)
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            tvEng.setText("English?");
                            tvEng.setTypeface(Settings.getInstance().getFont(Settings.Language.ENGLISH));
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {

                        }
                    }) // "English?"
                    .setDuration(BUBBLE_ANIM_DUR_MS);

            tvHeb.animate()
                    .translationX(parentLeft - bubble3midX)
                    .translationY(parentTop - bubble3midY)
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            tvHeb.setText("עברית?");
                            tvHeb.setTypeface(Settings.getInstance().getFont(Settings.Language.HEBREW_UNISEX));
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {

                        }
                    }) // "עברית"?
                    .setDuration(BUBBLE_ANIM_DUR_MS);

            ivBubble2.animate()
                    .alpha(0)
                    .setDuration(BUBBLE_ANIM_DUR_MS)
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            ivBubble2.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {

                        }
                    }); // Visibilty.INVISIBLE
        nextActivity.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

        tvEng.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Settings.getInstance().setUserLanguage(Settings.Language.ENGLISH);
                        exit_nice();
                    }
                }
        ); // finish() startActivity()
        tvHeb.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Settings.getInstance().setUserLanguage(Settings.Language.HEBREW_UNISEX);
                        exit_nice();
                    }
                }
        ); // finish() startActivity()

    }

    private void exit_nice() {
        int exitThis = R.anim.abc_shrink_fade_out_from_bottom;
        int enterNext = R.anim.abc_grow_fade_in_from_bottom;

        finish();
        startActivity(nextActivity);
        overridePendingTransition(enterNext, exitThis); // exit without showing any animation
    }

    @Override
    public void appReadyToLaunch(Intent nextActivity) {
        this.nextActivity = nextActivity;
    }
}
