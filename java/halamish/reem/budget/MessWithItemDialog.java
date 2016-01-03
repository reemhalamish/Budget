package halamish.reem.budget;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import halamish.reem.budget.data.BudgetItem;
import halamish.reem.budget.data.DatabaseHandler;

public class MessWithItemDialog extends AppCompatActivity {
    public enum ItemAction{ADD, EDIT}
    ItemAction action;
    Button btn_action;
    EditText edt_title, edt_amount;
    CheckBox cbx_monthly_weekly;

    private static final String TAG = "MessWithItem";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_item);
        edt_amount = (EditText) findViewById(R.id.edt_item_amount);
        edt_title = (EditText) findViewById(R.id.edt_item_title);
        cbx_monthly_weekly = (CheckBox) findViewById(R.id.cbx_item_monthly_weekly);
        btn_action = (Button) findViewById(R.id.btn_item_action);

        Intent senderIntent = getIntent();
        final BudgetItem oldItem;
        action = (ItemAction) senderIntent.getSerializableExtra("item_action");
        if (action == ItemAction.EDIT) {
            oldItem = new DatabaseHandler(this).getBudgetItem(senderIntent.getStringExtra("item_name"));
            Log.d(TAG, "old item is" + oldItem + " with name " + oldItem.getName());
            edt_amount.setText(String.valueOf(oldItem.getAuto_update_amount()));
            edt_title.setText(oldItem.getName());
            Log.d(TAG, "monthly? " + oldItem.getAuto_update() + " " + String.valueOf(oldItem.getAuto_update().equals(BudgetItem.MONTHLY)));
            cbx_monthly_weekly.setChecked(oldItem.getAuto_update().equals(BudgetItem.MONTHLY));
            btn_action.setText("Edit!");
        } else {
            oldItem = null;
            btn_action.setText("Add!");
        }

        btn_action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BudgetItem newItem;
                String title;
                int amount;

                title = edt_title.getText().toString();
                amount = getAmount();
                boolean monthlyOnTrueElseWeekly = cbx_monthly_weekly.isChecked();

                if (monthlyOnTrueElseWeekly) {
                    newItem = new BudgetItem(title, BudgetItem.MONTHLY, amount);
                } else {
                    newItem = new BudgetItem(title, BudgetItem.WEEKLY, amount);
                }
                if (action == ItemAction.ADD)
                    new DatabaseHandler(MessWithItemDialog.this).addBudgetItem(newItem);
                else if (action == ItemAction.EDIT)
                    Log.d(TAG, "sending to db handler... method: " + newItem.getAuto_update());
                    new DatabaseHandler(MessWithItemDialog.this).updateBudgetItem(oldItem, newItem, null);

                Intent returnIntent = new Intent();
                setResult(RESULT_OK, returnIntent);
                finish();

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_item, menu);
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

    public int getAmount() {
        try {
            return Integer.parseInt(edt_amount.getText().toString());
        } catch (NumberFormatException nfe) {
            // Log exception.
            return 0;
        }

    }
}



