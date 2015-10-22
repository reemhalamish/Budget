package halamish.reem.budget;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class AddItemActivity extends AppCompatActivity {
    Button btn_add;
    EditText edt_title, edt_amount;
    CheckBox cbx_monthly_weekly;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        edt_amount = (EditText) findViewById(R.id.edt_additem_amount);
        edt_title = (EditText) findViewById(R.id.edt_additem_title);
        cbx_monthly_weekly = (CheckBox) findViewById(R.id.cbx_additem_monthly_weekly);
        btn_add = (Button) findViewById(R.id.btn_additem_add);


        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BudgetItem newItem;
                String title;
                int amount = 0;

                title = edt_title.getText().toString();
                amount = getAmount();
                boolean monthlyOnTrueElseWeekly = cbx_monthly_weekly.isPressed();

                if (monthlyOnTrueElseWeekly) {
                    newItem = new BudgetItem(title, BudgetItem.MONTHLY, amount);
                } else {
                    newItem = new BudgetItem(title, BudgetItem.WEEKLY, amount);
                }
                new DatabaseHandler(AddItemActivity.this).addBudgetItem(newItem, null);

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
