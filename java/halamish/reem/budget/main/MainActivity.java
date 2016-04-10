package halamish.reem.budget.main;

import android.animation.LayoutTransition;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
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
import java.util.ArrayList;
import java.util.List;

import halamish.reem.budget.style.BudgetStyleActivity;
import halamish.reem.budget.misc.Settings;
import halamish.reem.budget.Activities.SettingsActivity;
import halamish.reem.budget.item.ItemActivity;
import halamish.reem.budget.R;
import halamish.reem.budget.data.BudgetItem;
import halamish.reem.budget.data.BudgetLine;
import halamish.reem.budget.data.DatabaseHandler;
import halamish.reem.budget.misc.Utils;

public class MainActivity extends BudgetStyleActivity {
    private static final String TAG = "main";
    private static final String REPORT_FILE_ROOT_FOLDER = "Budget";
    private static final String REPORT_FILE_NAME = "budget_report";
    private static final String REPORT_FILE_EXT = ".csv";
    private static final int ADD_ITEM_REQUEST = 1;
    private static final int EDIT_ITEM_REQUEST = 2;
    private static final long ANIMATE_CHANGES_DUR = 300;

    private GridView gv_all_items;
    private GridViewItemAdapter aa;
    private DatabaseHandler db;
    private MainActivityMultiSelectHandler multiSelectHandler;
    private boolean needToForceReload = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent gotHereByIntent = getIntent();
        if (gotHereByIntent.getBooleanExtra(SettingsActivity.SHUTDOWN_THE_APP_EXTRA, false)) {
            finish();
            System.exit(0);
            return;
        }

        Settings.getInstance().updateLocal();               // BEFORE setting the content view
        setContentView(R.layout.activity_main);

        gv_all_items = (GridView) findViewById(R.id.gv_main_all_items);
        db = new DatabaseHandler(this);
        aa = new GridViewItemAdapter(
                this,
                R.layout.budget_item,
                db.getAllBudgetItems(null),
                getEveryItemOnClickListener(),
                getEveryItemOnClickMultiSelectModeListener(),
                getEveryItemLongClick(),
                getAddNewItemListener()
        );

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
        if (needToForceReload) { // happens when changing locale
            needToForceReload = false;
            forceReload();
        }
        aa.notifyDataSetChanged();
        super.onResume();
        findViewById(R.id.iv_main_add).setVisibility(View.VISIBLE); // the plus button
    }

    @Override
    protected void onDestroy() {
        if (db != null)
            db.removeAdapter(aa);
        super.onDestroy();
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

        if (id == R.id.action_main_choose_multiple && !multiSelectHandler.isAtMultiSelectMode()) {
            aa.startMultiSelect();
            multiSelectHandler.startMultiSelect(true);
        }

        if (id == R.id.action_main_report) {
            generateReportOnSD();
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_main_settings) {
            needToForceReload = true;
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        if (id == R.id.action_main_contact_us) {
            Intent contactUsMail = new Intent(
                    Intent.ACTION_SENDTO,
                    Uri.fromParts(
                            "mailto",
                            "budget.app.rhalamish@gmail.com",
                            null
                    )
            );
            contactUsMail.putExtra(Intent.EXTRA_SUBJECT, "Budget - contact us");
            contactUsMail.putExtra(Intent.EXTRA_TEXT, "");
            startActivity(Intent.createChooser(contactUsMail, "Contact Us"));
            // ONEDAY - add here some info about the phone...

            // got from here
            // http://stackoverflow.com/questions/8701634/send-email-intent
        }

        return super.onOptionsItemSelected(item);
    }

    View.OnClickListener getEveryItemOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gotoItem = new Intent(MainActivity.this, ItemActivity.class);
                gotoItem.putExtra("item_name", (String) view.getTag(R.id.TAG_BUDGET_ITEM_NAME));
                MainActivity.this.startActivity(gotoItem);
            }
        };
    }


    View.OnClickListener getEveryItemOnClickMultiSelectModeListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String itemName = (String) view.getTag(R.id.TAG_BUDGET_ITEM_NAME);
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
                multiSelectHandler.updateButtons(view);
//                final String itemName = (String) view.getTag(R.id.TAG_BUDGET_ITEM_NAME);

                return true;
            }

//            private void createDeleteDialog(final String budgetItemName) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
//                builder.setTitle(getString(R.string.title_delete_confirm) + ' ' + budgetItemName);
//                builder.setMessage(getString(R.string.msg_delete_are_you_sure));
//                builder.setCancelable(true);
//                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        db.deleteBudgetItem(db.getBudgetItem(budgetItemName), null);
//                        dialog.dismiss();
//                    }
//                });
//
//                builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.cancel();
//                    }
//                });
//
//                AlertDialog alert = builder.create();
//                alert.show();
//            }
//
//            private void createEditDialog(final String itemName) {
//                Intent editItemIntent = new Intent(MainActivity.this, MessWithItemDialog.class);
//                editItemIntent.putExtra("item_action", MessWithItemDialog.ItemAction.EDIT);
//                editItemIntent.putExtra("item_name", itemName);
//                MainActivity.this.startActivityForResult(editItemIntent, EDIT_ITEM_REQUEST);
//            }
        };
    }

    public View.OnClickListener getAddNewItemListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addItemIntent = new Intent(MainActivity.this, MessWithItemDialog.class);
                addItemIntent.putExtra("item_action", MessWithItemDialog.ItemAction.ADD);
                MainActivity.this.startActivityForResult(addItemIntent, ADD_ITEM_REQUEST);
                findViewById(R.id.iv_main_add).setVisibility(View.GONE); // make the plus button disappear
            }
        };
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
                Toast.makeText(this, R.string.msg_item_success_add, Toast.LENGTH_SHORT).show();
                aa.notifyDataSetChanged();
            }
        } else if (requestCode == EDIT_ITEM_REQUEST) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, R.string.msg_item_success_edit, Toast.LENGTH_SHORT).show();
                aa.notifyDataSetChanged();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void generateReportOnSD(){
        try
        {
            File root = new File(Environment.getExternalStorageDirectory(), REPORT_FILE_ROOT_FOLDER);
            if (!root.exists()) {
                root.mkdirs();
            }
            String newDirectoryName = Utils.getToday() + "_" + REPORT_FILE_NAME;
            File todaysDirectory = new File(root, newDirectoryName);
            int copy_index = 0;
            while (todaysDirectory.exists()) {
                String copyDirectoryName = newDirectoryName + "_" + copy_index;
                todaysDirectory = new File(root, copyDirectoryName);
                copy_index += 1;
            }
            todaysDirectory.mkdirs(); // since I know it doesn't exists

            final ArrayList<File> allFiles = new ArrayList<>();

            String mainFileName = getString(R.string.report_filename_main) + REPORT_FILE_NAME + REPORT_FILE_EXT;
            File mainReportFile = new File(todaysDirectory, mainFileName);
            FileWriter mainWriter = new FileWriter(mainReportFile);
            String reportLine = getString(R.string.report_header_budgetitem) + "\n";
            mainWriter.append(reportLine);

            List<BudgetItem> allItems = db.getAllBudgetItems(null);
            for (BudgetItem item : allItems) {
                reportLine =
                    item.getId() + ", " +
                    item.getPretty_name() + ", " +
                    item.getLocalizedAuto_update(this) + ", " +
                    item.getAuto_update_amount() + ", " +
                    item.getCur_value() + "\n";
                mainWriter.append(reportLine);

                String itemFileName = REPORT_FILE_NAME + "_" + item.getPretty_name() + REPORT_FILE_EXT;
                File itemReportFile = new File(todaysDirectory, itemFileName);
                FileWriter itemWriter = new FileWriter(itemReportFile);
                reportLine = getString(R.string.report_header_budgetline) + "\n";
                itemWriter.append(reportLine);
                for (BudgetLine line : db.tblGetAllBudgetLines(item, null)) {
                    reportLine =
                            line.getId() + "," +
                            line.getAmount() + "," +
                            line.getEventType() + "," +
                            line.getTitle() + "," +
                            line.getDetails() + "," +
                            line.getDate() + "\n";
                    itemWriter.append(reportLine);
                }
                itemWriter.flush();
                itemWriter.close();
                allFiles.add(itemReportFile);

            }

            mainWriter.flush();
            mainWriter.close();
            allFiles.add(mainReportFile);

            final String absolutePath = todaysDirectory.getCanonicalPath() + "/";
            final Intent openFolderIntent = new Intent(Intent.ACTION_VIEW);
            openFolderIntent.setDataAndType(Uri.parse(absolutePath), "resource/folder");
            boolean folderOpenable = openFolderIntent.resolveActivityInfo(getPackageManager(), 0) != null;



            AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.report_msg_look_for_folder_budget) + "\n\n" + getString(R.string.report_msg_saved_under_folder) + ": \n" + absolutePath)
                    .setCancelable(true)
                    .setPositiveButton(R.string.report_share_as_attachment, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            final Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                            emailIntent.setType("text/plain");
                            emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.report_email_subject));
                            emailIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.report_email_body) + " " + Utils.getToday() + "\n\n" + getString(R.string.app_get_budget_app_today));
                            //has to be an ArrayList
                            ArrayList<Uri> uris = new ArrayList<>();
                            //convert from paths to Android friendly Parcelable Uri's
                            for (File f : allFiles)
                            {
                                Uri u = Uri.fromFile(f);
                                uris.add(u);
                            }
                            emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                            startActivity(Intent.createChooser(emailIntent, getString(R.string.report_share_as_attachment)));
                            // got from http://stackoverflow.com/questions/2264622/android-multiple-email-attachments-using-intent
                            // TODO add the hebrew version

                            dialogInterface.dismiss();
                        }
                    });

            if (folderOpenable) {
                dialog.setNegativeButton(R.string.report_msg_open_folder, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        startActivity(Intent.createChooser(openFolderIntent, getString(R.string.report_msg_open_folder)));
                        dialogInterface.dismiss();
                    }
                });
            } else {
                dialog.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
            }

            dialog.show();

        }
        catch(IOException e)
        {
            Toast.makeText(this, R.string.report_error, Toast.LENGTH_LONG).show();
            Log.e(TAG, e.getStackTrace().toString());
        }
    }
    private void forceReload() {
        finish();
        startActivity(getIntent());
    }

}
