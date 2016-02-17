package halamish.reem.budget.main;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import halamish.reem.budget.R;
import halamish.reem.budget.data.BudgetItem;
import halamish.reem.budget.data.DatabaseHandler;

/**
 * Created by Re'em, in unknown time :)
 */
public class MessWithItemDialog extends Activity {
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
            edt_amount.setText(String.valueOf(oldItem.getAuto_update_amount()));
            edt_title.setText(oldItem.getPretty_name());
            cbx_monthly_weekly.setChecked(oldItem.getAuto_update().equals(BudgetItem.MONTHLY));
            btn_action.setText(R.string.dialog_btn_action_edit);
        } else {
            oldItem = null;
            btn_action.setText(R.string.dialog_btn_action_add);
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
                if (action == ItemAction.ADD) {
                    new DatabaseHandler(MessWithItemDialog.this).addBudgetItem(newItem);
                }
                else if (action == ItemAction.EDIT) {
                    // Log.d(TAG, "sending to db handler... method: " + newItem.getAuto_update());
                    if (oldItem == null) throw new AssertionError();
                    newItem.setName(oldItem.getName());
                    new DatabaseHandler(MessWithItemDialog.this).updateBudgetItem(oldItem, newItem, null);
                }
                setResult(RESULT_OK);
                finish();

            }
        });

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



