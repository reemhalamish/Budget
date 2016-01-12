package halamish.reem.budget.item;



import android.animation.Animator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import halamish.reem.budget.R;
import halamish.reem.budget.data.BudgetItem;
import halamish.reem.budget.data.BudgetLine;
import halamish.reem.budget.data.BudgetLinesParser;
import halamish.reem.budget.data.DatabaseHandler;
import halamish.reem.budget.main.MessWithItemDialog;

public class ItemActivity extends AppCompatActivity {
    private static final String ITEM_NAME = "item_name";
    private static final int NEW_LINE_REQUEST = 1;
    private static final int EDIT_LINE_REQUEST = 2;
    private static final String TAG = "ItemActivity";
    private static final long FADE_AWAY_DUR = 500; // for the sandclock onCliCkListener
    private static final long FRAME_CHANGE_DUR = 300;
    private static final int[] SANDCLOCK_FRAMES = new int[]{
                R.mipmap.gray_converted_sandclock_0,
                R.mipmap.gray_converted_sandclock_10,
                R.mipmap.gray_converted_sandclock_25,
                R.mipmap.gray_converted_sandclock_40,
                R.mipmap.gray_converted_sandclock_55,
                R.mipmap.gray_converted_sandclock_70,
                R.mipmap.gray_converted_sandclock_85,
                R.mipmap.gray_converted_sandclock_100,
                R.mipmap.gray_converted_sandclock_broken
    };


    ListView lv_allItems;
    ListViewItemAdapter aa;
    private String budgetItemName;
    private DatabaseHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        // gather info to create the adapter
        if (savedInstanceState != null) {
            budgetItemName = savedInstanceState.getString(ITEM_NAME);
        } else {
            budgetItemName = getIntent().getStringExtra(ITEM_NAME);
        }
        lv_allItems =(ListView) findViewById(R.id.lv_itemactivity_main);
        db = new DatabaseHandler(this);
        BudgetItem curItem = db.getBudgetItem(budgetItemName);
        BudgetLinesParser parser = new BudgetLinesParser(db.tblGetAllBudgetLines(curItem, null));
        aa = new ListViewItemAdapter(this, R.layout.budget_line, lv_allItems, parser, curItem, getNewLineListener(), getEveryItemLongClickListener());
        lv_allItems.setAdapter(aa);
        db.insertAdapter(aa);


        // create the "load previous" button at top
        if (parser.moreInfoAtAllThenAtNonArchived()) { // if exist earlier lines
            final TextView tv_loadearlier_title = (TextView) findViewById(R.id.tv_itemactivity_loadearlier);
            tv_loadearlier_title.setVisibility(View.VISIBLE);
            tv_loadearlier_title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    aa.loadAll();
                    tv_loadearlier_title.setVisibility(View.GONE);
                }
            });
        }

        // manage the sandclock and the plus
        findViewById(R.id.iv_itemactivity_sandclock_container).setOnClickListener(getNewLineListener());
        startSandClockAnimationIfNeeded();

    }

    private void startSandClockAnimationIfNeeded() {
        final ImageView iv_sandclock = (ImageView) findViewById(R.id.iv_itemactivity_sandclock_container);
        iv_sandclock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iv_sandclock.animate()
                        .alpha(0.0f)
                        .setDuration(FADE_AWAY_DUR)
                        .setListener(new Animator.AnimatorListener() {
                            public void onAnimationStart(Animator animator) {}
                            public void onAnimationEnd(Animator animator) {
                                iv_sandclock.setVisibility(View.GONE);
                            }
                            public void onAnimationCancel(Animator animator) {}
                            public void onAnimationRepeat(Animator animator) {}
                        })
                        .start();
            }
        });

        BudgetItem curItem = db.getBudgetItem(budgetItemName);
        float maxVal = curItem.getAuto_update_amount();
        float curVal = curItem.getCur_value();
        final float percentage = curVal / maxVal;
        Log.d(TAG, "max: " + maxVal + ", cur: " + curVal + ", per: " + percentage);
        if (curVal > maxVal) return; // we have nothing to do, leave it with the crown




        new CountDownTimer(FRAME_CHANGE_DUR * SANDCLOCK_FRAMES.length, FRAME_CHANGE_DUR) {
            float progressPercentage = 1.0f;
            final float progressJumps = 1.0f / (SANDCLOCK_FRAMES.length-1); // I want 0 to get at ALMOST final frame
            int index = 0;
            public void onTick(long l) {
                if (progressPercentage >= percentage) {
                    iv_sandclock.setImageResource(SANDCLOCK_FRAMES[index]);
                    Log.d(TAG, "orgPer: " + percentage + ", curPer: " + progressPercentage + ", i: " + index);
                    progressPercentage -= progressJumps;
                    index += 1;
                }
            }
            public void onFinish() {onTick(0);} // make the last frame as well (the implementation is the same)
        }.start();






//        iv_sandclock.setImageResource(R.drawable.sandclock_animation);
//        AnimationDrawable frameAnimation = (AnimationDrawable) iv_sandclock.getDrawable();
//        frameAnimation.setOneShot(true);
//        frameAnimation.start();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.removeAdapter(aa);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_item_show_details) {
            createItemInfoDialog(this, budgetItemName);
            return true;
        }

        else if (id == R.id.action_item_clear) {
            db.clearBudgetItem(db.getBudgetItem(budgetItemName));
            startSandClockAnimationIfNeeded();
            return true;
        }

        else if (id == R.id.action_item_del) {
            createDeleteItemDialog(this, budgetItemName);
            return true;
        }

        else if (id == R.id.action_item_edit) {
            createEditItemDialog(this, budgetItemName);
        }


        return super.onOptionsItemSelected(item);
    }

    private void createEditItemDialog(Context context, final String itemName) {
        finish();
        Intent editItemIntent = new Intent(context, MessWithItemDialog.class);
        editItemIntent.putExtra("item_action", MessWithItemDialog.ItemAction.EDIT);
        editItemIntent.putExtra("item_name", itemName);
        context.startActivity(editItemIntent);

    }

    private void createDeleteItemDialog(Context context, final String budgetItemName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirm deleting " + budgetItemName);
        builder.setMessage("sure to DELETE?");
        builder.setCancelable(true);
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
                db.deleteBudgetItem(db.getBudgetItem(budgetItemName), null);
                dialog.dismiss();

            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void createItemInfoDialog(Context context, String budgetItemName) {
        final BudgetItem budgetItem= new DatabaseHandler(context).getBudgetItem(budgetItemName);
        new AlertDialog.Builder(context)
                .setTitle(budgetItemName + " Details:")
                .setMessage("updated " + budgetItem.getAuto_update() + " for " + budgetItem.getAuto_update_amount() + " cash.\ncurrent amount: " + budgetItem.getCur_value() + "\n\n(Inner info: id is " + budgetItem.getId() + ")")
                .setCancelable(true)
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .show();
    }

    public View.OnClickListener getNewLineListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addLineIntent = new Intent(ItemActivity.this, MessWithLineDialog.class);
                addLineIntent.putExtra(MessWithLineDialog.ITEM_NAME, budgetItemName);
                startActivityForResult(addLineIntent, NEW_LINE_REQUEST);
            }
        };
    }

    public View.OnLongClickListener getEveryItemLongClickListener() {
        final Context context = this;
        final BudgetLine line;
        return new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                BudgetLine line = (BudgetLine) view.getTag(R.id.TAG_BUDGET_LINE);
                createOptionsDialog(line);
                return true;
            }

            private void createOptionsDialog(final BudgetLine line) {
                // create a list of options
                final CharSequence[] options = {"edit", "DELETE"};

                new AlertDialog.Builder(context)
                        .setTitle("Line: " + line.getTitle())
                        .setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                switch (i) {
                                    case 0: // edit
                                        createLineEditDialog(line);
                                        dialogInterface.dismiss();
                                        break;
                                    case 1: // DELETE
                                        createDeleteLineDialog(line);
                                        dialogInterface.dismiss();
                                        break;

                                }
                            }

                            private void createDeleteLineDialog(final BudgetLine line) {
                                String msg = "Are you sure to DELETE? (the action can't be un-done!)";
                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setTitle("Confirm Deleting")
                                        .setMessage(msg)
                                        .setCancelable(true)
                                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                db.tblDeleteBudgetLine(db.getBudgetItem(budgetItemName), line, null);
                                                dialog.dismiss();
                                                startSandClockAnimationIfNeeded();
                                            }
                                        })
                                        .setNegativeButton("no", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.cancel();
                                            }
                                        })
                                        .show();
                            }

                            private void createLineEditDialog(BudgetLine line) {
                                Intent addLineIntent = new Intent(ItemActivity.this, MessWithLineDialog.class);
                                addLineIntent.putExtra(MessWithLineDialog.ITEM_NAME, budgetItemName);
                                addLineIntent.putExtra(MessWithLineDialog.LINE_ACTUAL, line);
                                Log.d(TAG, "Starting activity MessWithLineDialog");
                                startActivityForResult(addLineIntent, EDIT_LINE_REQUEST);
                            }
                        })

                        // back at the "options" dialog
                        .setCancelable(true)
                        .create()
                        .show();
            }
        };
    }
    /**
     * Dispatch incoming result to the correct fragment.
     *
     * @param requestCode - the req code
     * @param resultCode - result ok or not
     * @param data - the intent itself
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NEW_LINE_REQUEST) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "line has been added!", Toast.LENGTH_SHORT).show();
                aa.updateAdapter();
                startSandClockAnimationIfNeeded();
            }
        } else if (requestCode == EDIT_LINE_REQUEST) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "line has been modified!", Toast.LENGTH_SHORT).show();
                aa.updateAdapter();
                startSandClockAnimationIfNeeded();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Save all appropriate fragment state.
     *
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(ITEM_NAME, budgetItemName);
        super.onSaveInstanceState(outState);
    }
}
