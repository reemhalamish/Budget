package halamish.reem.budget;

import android.content.Intent;
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

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "main";
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
                BudgetItem item = new DatabaseHandler().getBudgetItem(itemName, null);
                
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
                MainActivity.this.startActivity(new Intent(MainActivity.this, AddItemActivity.class)); // TODO for result
            }
        };
    }
}
