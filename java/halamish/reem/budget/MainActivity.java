package halamish.reem.budget;

import android.animation.LayoutTransition;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import halamish.reem.budget.data.BudgetItem;
import halamish.reem.budget.data.BudgetLine;
import halamish.reem.budget.data.DatabaseHandler;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "main";
    private static final String REPORT_FILE_NAME = "budget_report.txt";
    private static final int ADD_ITEM_REQUEST = 1;
    private static final int EDIT_ITEM_REQUEST = 2;
    private static final long ANIMATE_CHANGES_DUR = 300;

    private GridView gv_all_items;
    private GridViewItemAdapter aa;
    private DatabaseHandler db;
    private MainActivityMultiSelectHandler multiSelectHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gv_all_items = (GridView) findViewById(R.id.gv_main_all_items);
        db = new DatabaseHandler(this);
//        init_db();
        aa = new GridViewItemAdapter(this, R.layout.budget_item, db.getAllBudgetItems(null), getEveryItemOnClickListener(), getEveryItemOnClickMultiSelectModeListener(), getEveryItemLongClick(), getAddNewItemListener());

        multiSelectHandler = MainActivityMultiSelectHandler.getInstance();
        multiSelectHandler.startModeBasedOnMemory(this, aa);

        gv_all_items.setAdapter(aa);

        // transition of buttons:
        RelativeLayout main_layout = (RelativeLayout) findViewById(R.id.rl_all_main);
        LayoutTransition layoutTransition = main_layout.getLayoutTransition();
        layoutTransition.setDuration(ANIMATE_CHANGES_DUR);
        main_layout.setLayoutTransition(layoutTransition);

    }

    private void init_db() {

        db.deleteAll(null);
        String weekly = BudgetItem.WEEKLY, monthly = BudgetItem.MONTHLY;
        db.addBudgetItem(new BudgetItem("אוכל בחוץ", weekly, 45));
        db.addBudgetItem(new BudgetItem("כושר", monthly, 10));
        db.addBudgetItem(new BudgetItem("מתנות", monthly, 5));
        db.addBudgetItem(new BudgetItem("מאפים", weekly, 20));
        db.addBudgetItem(new BudgetItem("בריאות", monthly, 10));
        db.addBudgetItem(new BudgetItem("דייטים", monthly, 150));
        db.addBudgetItem(new BudgetItem("בגדים", monthly, 100));
        db.addBudgetItem(new BudgetItem("אקסטרה", weekly, 50));
        db.addBudgetItem(new BudgetItem("כלי כתיבה", monthly, 10));
        db.addBudgetItem(new BudgetItem("יציאה עם המשפחה", monthly, 30));
        db.addBudgetItem(new BudgetItem("נסיעות", monthly, 25));
        db.addBudgetItem(new BudgetItem("נעליים", monthly, 25));
        db.addBudgetItem(new BudgetItem("קפה", weekly, 10));
        db.addBudgetItem(new BudgetItem("כביסה", weekly, 14));
        db.addBudgetItem(new BudgetItem("תרופות", monthly, 50));
        db.addBudgetItem(new BudgetItem("הגיינה", monthly, 30));
        db.addBudgetItem(new BudgetItem("יציאות עם חברים", weekly, 15));
        db.addBudgetItem(new BudgetItem("שונות", monthly, 120));
        db.addBudgetItem(new BudgetItem("רמי לוי", monthly, 150));
        db.addBudgetItem(new BudgetItem("TODO", monthly, 0));


        //        db.deleteAll();
//        db.addBudgetItem(new BudgetItem("מותרות", 50));
//        db.addBudgetItem(new BudgetItem("שונות", 80));
//        db.addBudgetItem(new BudgetItem("weekly item", BudgetItem.WEEKLY, 20));


//        db.tblAddBudgetLine(new BudgetItem("מותרות"), new BudgetLine());
//        Log.d(TAG, "monthly: " + db.getAllMonthlyUpdate());
//        Log.d(TAG, "weekly: " + db.getAllWeeklyUpdate());

//        db.updateBudgetItemByName("שונות", 100);
//        db.addBudgetItem(new BudgetItem("יציאות", 40));
//        BudgetItem newbie = new BudgetItem("שונות", 50);
//        newbie.setAuto_update(BudgetItem.WEEKLY);
//        db.addBudgetItem(newbie);
//        db.updateBudgetItemByName("מותרות", -80);
    }

    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    @Override
    public void onBackPressed() {
        if (multiSelectHandler.isAtMultiSelectMode()) {
            aa.endMultiSelect();
            multiSelectHandler.startNormalState();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Dispatch onStart() to all fragments.  Ensure any created loaders are
     * now started.
     */
    @Override
    protected void onStart() {
        super.onStart();
        db.insertAdapter(aa);
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
        aa.notifyDataSetChanged();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.removeAdapter(aa);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_choose_multiple) {
            aa.startMultiSelect();
            multiSelectHandler.startMultiSelect(true);
            // TODO:
            /*
            ONEDAY disable and delete the "add new" item with motion
            change the shortPressListener of items to be selected (maybe changed color, maybe "v")
            options:
               (on 0 selected - normal plus  ???)
                on 1 selected - delete, edit, duplicate
                on 2 selected - delete, swap
                on 3+selected - delete

            make a function that will return the plus and the "add new" back at the end of action
             */
            Toast.makeText(this, "NOT READY YET", Toast.LENGTH_SHORT);
        }

        if (id == R.id.action_report) {
            generateReportOnSD();
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    View.OnClickListener getEveryItemOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gotoItem = new Intent(MainActivity.this, ItemActivity.class);
                gotoItem.putExtra("item_name", (String) view.getTag(R.id.TAG_BUDGETITEM_NAME));
                MainActivity.this.startActivity(gotoItem);
            }
        };
    }


    View.OnClickListener getEveryItemOnClickMultiSelectModeListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String itemName = (String) view.getTag(R.id.TAG_BUDGETITEM_NAME);
                MainActivityMultiSelectHandler.getInstance().updateButtons(view);
                aa.notifyDataSetChanged();
            }
        };
    }

    View.OnLongClickListener getEveryItemLongClick() {
        final Context context = this;
        return new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                multiSelectHandler.startMultiSelect(true);

                final String itemName = (String) view.getTag(R.id.TAG_BUDGETITEM_NAME);
                multiSelectHandler.updateButtons(view);
                return true;
//                if (1==1) {return true;} //
// TODO should i throw the rest down here? they were able to get once on long press... now they are reachable inside the item view
//  TODO solution: add an item looks like icon with (...) so that when pressing it this dialog will appear
//                // create a list of options
//                final CharSequence[] options = {"edit", "show details", "create new with same budget", "clear", "DELETE"};
//
//                new AlertDialog.Builder(context)
//                        .setTitle(itemName)
//                        .setItems(options, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                switch (i) {
//                                    case 0: // edit
//                                        createEditDialog(itemName);
//                                        break;
//                                    case 1: // show details
//                                        ItemActivity.createItemInfoDialog(context, itemName);
//                                        break;
//                                    case 2: // copy barebones - only the auto_update and update_amount with new name
//                                        // TODO implement it in the db
//                                        Toast.makeText(context, "not yet. TODO", Toast.LENGTH_SHORT).show();
//                                        break;
//                                    case 3: // clear
//                                        db.clearBudgetItem(db.getBudgetItem(itemName));
//                                        break;
//                                    case 4: // DELETE
//                                        createDeleteDialog(itemName);
//                                        break;
//
//                                }
//                            }
//                        })
//                        .setCancelable(true)
//                        .create()
//                        .show();
//                return true;
            }

            private void createDeleteDialog(final String budgetItemName) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
                builder.setTitle("Confirm deleting " + budgetItemName);
                builder.setMessage("sure to DELETE?");
                builder.setCancelable(true);
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
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

            private void createEditDialog(final String itemName) {
                Intent editItemIntent = new Intent(MainActivity.this, MessWithItemDialog.class);
                editItemIntent.putExtra("item_action", MessWithItemDialog.ItemAction.EDIT);
                editItemIntent.putExtra("item_name", itemName);
                MainActivity.this.startActivityForResult(editItemIntent, EDIT_ITEM_REQUEST);
            }
        };
    }

    public View.OnClickListener getAddNewItemListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addItemIntent = new Intent(MainActivity.this, MessWithItemDialog.class);
                addItemIntent.putExtra("item_action", MessWithItemDialog.ItemAction.ADD);
                MainActivity.this.startActivityForResult(addItemIntent, ADD_ITEM_REQUEST);
            }
        };
    }

    protected void finishLongPress() {
        aa.endMultiSelect();
    }

    /**
     * Dispatch incoming result to the correct fragment.
     *
     * @param requestCode - req
     * @param resultCode - okay\canceled\"first user"
     * @param data - the actual intent containing the data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ADD_ITEM_REQUEST) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "item added succefully!", Toast.LENGTH_SHORT).show();
                aa.notifyDataSetChanged();
            }
        } else if (requestCode == EDIT_ITEM_REQUEST) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "item edited succefully!", Toast.LENGTH_SHORT).show();
                aa.notifyDataSetChanged();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void generateReportOnSD(){
        try
        {
            File root = new File(Environment.getExternalStorageDirectory(), "Budget_myapp");
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, REPORT_FILE_NAME);
            FileWriter writer = new FileWriter(gpxfile);
            List<BudgetItem> allItems = db.getAllBudgetItems(null);
            for (BudgetItem item : allItems) {
                String reportLine = "id: " + item.getId() + ", name: " + item.getName() + ", adding_every: " + item.getAuto_update() + ", auto_amount: " + item.getAuto_update_amount() + ", cur_value: " + item.getCur_value() + "\n";
                writer.append(reportLine);
                writer.append("History:\n");
                for (BudgetLine line : db.tblGetAllBudgetLines(item, null)) {
                    reportLine = "line_id: " + line.getId() + ", amount: " + line.getAmount() + ", title: " + line.getTitle() + ", details: " + line.getDetails() + ", date: " + line.getDate() + "\n";
                    writer.append(reportLine);
                }

                writer.append("\n");
            }

            writer.flush();
            writer.close();
            Toast.makeText(this, "Saved file " + REPORT_FILE_NAME, Toast.LENGTH_SHORT).show();
        }
        catch(IOException e)
        {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
    }

}
