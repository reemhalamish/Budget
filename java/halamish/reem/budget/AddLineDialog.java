package halamish.reem.budget;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

/**
 * Created by Re'em on 10/17/2015.
 */
public class AddLineDialog extends Activity {
    private static final String TAG = "add line dialog";
    Button btn_add;
    EditText edt_title, edt_details, edt_amount;
    CheckBox cbx_expense;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_line);

        final String item_name = getIntent().getStringExtra("item_name");
        // TODO somehow understand which item are we talking about. pass through intent will be the best

        edt_amount = (EditText) findViewById(R.id.edt_addline_amount);
        edt_details = (EditText) findViewById(R.id.edt_addline_details);
        edt_title = (EditText) findViewById(R.id.edt_addline_title);
        btn_add = (Button) findViewById(R.id.btn_addline_add);
        cbx_expense = (CheckBox) findViewById(R.id.cbx_addline_expense);


        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BudgetLine toPrepare;
                String title, details;
                int amount = 0;

                title = edt_title.getText().toString();
                details = edt_details.getText().toString();
                amount = getAmount();
                if (cbx_expense.isChecked()) {
                    amount = -amount;
                }

                toPrepare = new BudgetLine(title, details, amount, utils.getToday());
                DatabaseHandler db = new DatabaseHandler(AddLineDialog.this);
                db.tblAddBudgetLine(new BudgetItem(item_name) , toPrepare, null);

                finish();

                // TODO forResult() on MainActivity, need to put HERE the result code so it will notify the aa
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
