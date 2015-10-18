package halamish.reem.budget;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

public class ItemActivity extends AppCompatActivity {
    ListView allItems;
    ListViewItemAdapter aa;
    private String budgetItemName;
    private DatabaseHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        budgetItemName = (String) getIntent().getStringExtra("item_name");
        // TODO can be erase at change orientation. put it inside the savedInstnaceState
        allItems =(ListView) findViewById(R.id.lv_itemactivity_main);
        db = new DatabaseHandler(this);
        aa = new ListViewItemAdapter(this, R.layout.budget_line, db.tblGetAllBudgetLines(new BudgetItem(budgetItemName), null), db.getBudgetItem(budgetItemName, null), getNewLineListener());
        allItems.setAdapter(aa);
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
                addLineIntent.putExtra("item_name", budgetItemName);
                startActivity(addLineIntent); // TODO forResult()
            }
        };
    }
}
