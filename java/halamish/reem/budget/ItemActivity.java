package halamish.reem.budget;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class ItemActivity extends AppCompatActivity {
    private static final String ITEM_NAME = "item_name";
    private static final int NEW_LINE_REQUEST = 1;


    ListView allItems;
    ListViewItemAdapter aa;
    private String budgetItemName;
    private DatabaseHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        if (savedInstanceState != null) {
            budgetItemName = savedInstanceState.getString(ITEM_NAME);
        } else {
            budgetItemName = getIntent().getStringExtra(ITEM_NAME);
        }
        // TODO can be erase at change orientation. put it inside the savedInstnaceState
        allItems =(ListView) findViewById(R.id.lv_itemactivity_main);
        db = new DatabaseHandler(this);
        BudgetItem curItem = db.getBudgetItem(budgetItemName, null);
        aa = new ListViewItemAdapter(this, R.layout.budget_line, db.tblGetAllBudgetLines(curItem, null), curItem, getNewLineListener());
        allItems.setAdapter(aa);
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
        getMenuInflater().inflate(R.menu.menu_item, menu);
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

    public View.OnClickListener getNewLineListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addLineIntent = new Intent(ItemActivity.this, AddLineDialog.class);
                addLineIntent.putExtra(AddLineDialog.ITEM_NAME, budgetItemName);
                startActivityForResult(addLineIntent, NEW_LINE_REQUEST);
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
                aa.notifyDataSetChanged();
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
