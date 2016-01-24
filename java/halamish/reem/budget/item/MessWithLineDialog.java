package halamish.reem.budget.item;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import halamish.reem.budget.R;
import halamish.reem.budget.data.BudgetLine;
import halamish.reem.budget.data.DatabaseHandler;
import halamish.reem.budget.Utils;

/**
 * Created by Re'em on 10/17/2015.
 * this dialog appears when the user creates a new line or updates existing line
 */
public class MessWithLineDialog extends Activity {
    private static final String TAG = "add line dialog";
    public static final String ITEM_NAME = "item_name";
    public static final String LINE_ACTUAL = "line";
    Button btn_action;
    EditText edt_title, edt_details, edt_amount;
    CheckBox cbx_expense;
    private String item_name;
    private BudgetLine lineToEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_line);

        Log.d(TAG, "Starting...");

        if (savedInstanceState != null) {
            item_name = savedInstanceState.getString(ITEM_NAME);
            lineToEdit = (BudgetLine) savedInstanceState.getSerializable(LINE_ACTUAL);
        } else { // first time - will get by the intent
            Intent senderIntent = getIntent();
            item_name = senderIntent.getStringExtra(ITEM_NAME);
            lineToEdit = (BudgetLine) senderIntent.getSerializableExtra(LINE_ACTUAL);
        }

        edt_amount = (EditText) findViewById(R.id.edt_addline_amount);
        edt_details = (EditText) findViewById(R.id.edt_addline_details);
        edt_title = (EditText) findViewById(R.id.edt_addline_title);
        btn_action = (Button) findViewById(R.id.btn_addline_add);
        cbx_expense = (CheckBox) findViewById(R.id.cbx_addline_expense);

        final boolean editingLine = lineToEdit != null;
        if (editingLine) {
            btn_action.setText(R.string.dialog_btn_action_edit);
            edt_amount.setText(String.valueOf(Math.abs(lineToEdit.getAmount())));
            edt_details.setText(lineToEdit.getDetails());
            edt_title.setText(lineToEdit.getTitle());

            if (lineToEdit.getAmount() < 0)
                cbx_expense.setChecked(true);
            else
                cbx_expense.setChecked(false);

        } else {
            btn_action.setText(R.string.dialog_btn_action_add);
        }


        btn_action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BudgetLine newbie;
                String title, details;
                int amount = 0;

                title = edt_title.getText().toString();
                details = edt_details.getText().toString();
                amount = getAmount();
                if (cbx_expense.isChecked()) {
                    amount = -amount;
                }


                DatabaseHandler db = new DatabaseHandler(MessWithLineDialog.this);
                if (editingLine) {
                    lineToEdit.setDetails(details);
                    lineToEdit.setTitle(title);
                    lineToEdit.setAmount(amount);
                    db.tblUpdateBudgetLine(db.getBudgetItem(item_name), lineToEdit, null);
                } else {
                    final BudgetLine.BudgetLineEventType eventType = BudgetLine.BudgetLineEventType.USER_INPUT;
                    final long time = Utils.getMillisecondNow();
                    newbie = new BudgetLine(title, details, amount, time, eventType);
                    db.tblAddBudgetLine(db.getBudgetItem(item_name), newbie);
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

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(ITEM_NAME, item_name);
        outState.putSerializable(LINE_ACTUAL, lineToEdit);
        super.onSaveInstanceState(outState);
    }
}
