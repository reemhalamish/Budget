package halamish.reem.budget.report;

import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import halamish.reem.budget.R;
import halamish.reem.budget.data.BudgetItem;
import halamish.reem.budget.data.BudgetLine;
import halamish.reem.budget.data.DatabaseHandler;
import halamish.reem.budget.misc.Logger;
import halamish.reem.budget.style.BudgetStyleActivity;

/**
 * Created by Re'em on 4/29/2016.
 * the report activity
 *
 * TODO one day the report file generating process will go here
 */
public class ReportActivity extends BudgetStyleActivity {
    private static final String GET_ALL_REPORT_LINES = "get all report lines";
    ReportLinesWrapper mReportLines;
    TableLayout mTableItems;
    Map<ReportLine, TableRow> mReportLineToTableRow;

// TODO make it work as an activity


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        // generating the lines
        if (savedInstanceState != null)
            mReportLines = (ReportLinesWrapper) savedInstanceState.get(GET_ALL_REPORT_LINES);
        if (mReportLines == null) {
            DatabaseHandler db = new DatabaseHandler(this);
            List<BudgetItem> items = db.getAllBudgetItems();
            List<ReportLine> reportLines = new ArrayList<>();
            for (BudgetItem item : items) {
                List<BudgetLine> lines = db.tblGetAllBudgetLines(item);
                ReportLine newbie = new ReportLine(item, lines, this);
                reportLines.add(newbie);
            }
            mReportLines = new ReportLinesWrapper(reportLines);
        }

        // start some new views to mReportLineToTableRow
        mReportLineToTableRow = new HashMap<>();
        for (ReportLine line : mReportLines.getActualLines()) {
            TableRow newbieRow = new TableRow(this);
            TextView tvName, tvBudgetAve, tvExpAve, tvNetoAve, tvUpdated, tvNetoYearly;

            tvName = new TextView(this);
            tvName.setText(line.name);
            newbieRow.addView(tvName);
            tvBudgetAve = new TextView(this);
            tvBudgetAve.setText("" + line.budget_ave);
            newbieRow.addView(tvBudgetAve);
            tvExpAve = new TextView(this);
            tvBudgetAve.setText("" + line.expense_ave);
            newbieRow.addView(tvExpAve);
            tvNetoAve = new TextView(this);
            tvNetoAve.setText("" + line.neto_ave);
            newbieRow.addView(tvNetoAve);
            tvUpdated = new TextView(this);
            tvUpdated.setText(line.updated);
            newbieRow.addView(tvUpdated);
            tvNetoYearly = new TextView(this);
            tvNetoYearly.setText("" + line.neto_yearly);
            newbieRow.addView(tvNetoYearly);

            Logger.log(this, line.toString());
            Logger.log(this, newbieRow.toString());
            mReportLineToTableRow.put(line, newbieRow);
        }

        // now sort the lines and attach all of them to the table
        mTableItems = (TableLayout) findViewById(R.id.tl_report_items);
        addViewsSortedToItemsTable(ReportLineHeader.NETO_YEARLY);
    }

    private void addViewsSortedToItemsTable(ReportLineHeader parameter) {
        mReportLines.sort(parameter);
        for (ReportLine line : mReportLines.getActualLines()) {
            mTableItems.addView(mReportLineToTableRow.get(line));
        }

    }
    private void removeViewsSortedFromItemsTable() {
        mTableItems.removeViews(1, mReportLines.getSize());
    }

    /**
     * Save all appropriate fragment state.
     *
     * @param outState that thingy that goes out
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(GET_ALL_REPORT_LINES, mReportLines);
    }
}
