package halamish.reem.budget;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "main";
    private static final String REPORT_FILE_NAME = "budget_report.txt";
    private static final int ADD_ITEM_REQUEST = 1;
    private GridView gv_all_items;
    private GridViewItemAdapter aa;
    private DatabaseHandler db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gv_all_items = (GridView) findViewById(R.id.gv_main_all_items);
        db = new DatabaseHandler(this);
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
        aa = new GridViewItemAdapter(this, R.layout.budget_item, db.getAllBudgetItems(null), everyItemOnClickListener(), everyItemLongClickListener(), addNewItemListener());
        gv_all_items.setAdapter(aa);
    }

    private View.OnLongClickListener everyItemLongClickListener() {
        return new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                String itemName = (String) view.getTag();
                BudgetItem item = new DatabaseHandler(MainActivity.this).getBudgetItem(itemName, null);

                return true;
            }
        };
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

    View.OnClickListener everyItemOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gotoItem = new Intent(MainActivity.this, ItemActivity.class);
                gotoItem.putExtra("item_name", (String) view.getTag());
                MainActivity.this.startActivity(gotoItem);
            }
        };
    }

    View.OnClickListener addNewItemListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.startActivityForResult(new Intent(MainActivity.this, AddItemActivity.class), ADD_ITEM_REQUEST); // TODO for result
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
                Toast.makeText(this, "item added succefully!", Toast.LENGTH_SHORT).show();
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
