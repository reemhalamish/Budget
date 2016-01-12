package halamish.reem.budget.item;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import halamish.reem.budget.MyAdapter;
import halamish.reem.budget.R;
import halamish.reem.budget.data.BudgetItem;
import halamish.reem.budget.data.BudgetLine;
import halamish.reem.budget.data.BudgetLinesParser;
import halamish.reem.budget.data.DatabaseHandler;
import halamish.reem.budget.utils;

/**
 * Created by Re'em on 10/17/2015.
 *
 * adapter for budget line
 */
public class ListViewItemAdapter extends MyAdapter<BudgetLine> {
    private static final String TAG = "listAdapter";
    private ListView actual_list;
    private List<BudgetLine> allItems;
    private View.OnClickListener newLineListener;
    private BudgetItem item;
    private BudgetLinesParser parser;
    private boolean loadNonArchivedOnly;
    private LayoutInflater inflater;
    private View.OnLongClickListener everyItemLongClickListener;
    public ListViewItemAdapter(Context context,
                               int resource,
                               ListView list,
                               BudgetLinesParser parser,
                               BudgetItem item,
                               View.OnClickListener newLineListener,
                               View.OnLongClickListener everyItemLongClickListener) {
        super(context, resource, parser.getNonArchived());
        this.parser = parser;
        this.allItems = parser.getNonArchived();
        this.item = item;
        this.newLineListener = newLineListener;
        this.actual_list = list;
        this.loadNonArchivedOnly = true;
        this.everyItemLongClickListener = everyItemLongClickListener;
        inflater = LayoutInflater.from(getContext());
        scrollToPosition(getCount() - 1);
    }

    private void scrollToPosition(final int position) {
        actual_list.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
//                actual_list.setSelection();
                actual_list.smoothScrollToPosition(position);
            }
        });
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
//        Log.d(TAG, "calling position " + position);

//        if (loadNonArchivedOnly && parser.moreInfoAtAllThenAtNonArchived())
//            position -= 1;

//        if (position == -1) {
//            return loadEarlierView(convertView, parent);
//        }

        if (position >= allItems.size()) {
            return addItemView(convertView, parent);
        }

        View view = convertView;
        if (view == null || view.getId() != R.id.rl_line_main) {
            view = inflater.inflate(R.layout.budget_line, parent, false);
        }
        view.setOnClickListener(null);
        view.setOnLongClickListener(everyItemLongClickListener);


//        LATERON
//        set some multiSelect state.

//        if (_dismissListener != null) {
//            view.setOnTouchListener(_dismissListener);
//            view.setOnClickListener(_clickListener);
//        }


//        view.findViewById(R.id.iv_line_separator).setBackgroundColor(Color.DKGRAY);
// TODO retrn this ^ maybe

        // TODO add listener that will open line with all details
        BudgetLine curLine = allItems.get(position);
        view.setTag(R.id.TAG_BUDGET_LINE, curLine);

        TextView txtTitle = (TextView) view.findViewById(R.id.tv_line_title);
        TextView txtAmount = (TextView) view.findViewById(R.id.tv_line_amount);
//        TextView txtDetails = (TextView) view.findViewById(R.id.tv_line_details);
        TextView txtDate = (TextView) view.findViewById(R.id.tv_line_date);
//        TextView txtEventType = (TextView) view.findViewById(R.id.tv_line_event_type);

        txtTitle.setText(curLine.getTitle());
        txtAmount.setText(String.valueOf(curLine.getAmount()));
        txtDate.setText(utils.millisecToDate(curLine.getDate()));
//        txtDetails.setText(curLine.getDetails());
//        txtEventType.setText(curLine.getEventType().name());

        if (curLine.isArchived()) {
            txtTitle.setTextColor(Color.GRAY);
            txtAmount.setTextColor(Color.GRAY);
            txtDate.setTextColor(Color.GRAY);
        } else {
            if (curLine.getAmount() >= 0) {
                txtAmount.setTextColor(Color.GREEN);
            } else {
                txtAmount.setTextColor(Color.RED);
            }
        }

        view.setTag(position);
        return view;
    }

//    private View loadEarlierView(View convertView, final ViewGroup parent) {
//        if (convertView == null || convertView.getId() != R.id.rl_line_main_loadealier) {
//            LayoutInflater inflater = LayoutInflater.from(getContext());
//            convertView = inflater.inflate(R.layout.budget_line_header, parent, false);
//        }
//        TextView txtAdd = (TextView) convertView.findViewById(R.id.tv_line_loadearlier_title);
//        txtAdd.setTextColor(Color.DKGRAY);
//        convertView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//            }
//        });
//        return convertView;
//    }

    private View addItemView(View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null || view.getId() != R.id.rl_line_main_newline) {

            view = inflater.inflate(R.layout.budget_line_newline, parent, false);
        }
        TextView txtAdd = (TextView) view.findViewById(R.id.tv_line_addline_title);
        txtAdd.setTextColor(Color.BLUE);
        view.setOnClickListener(newLineListener);
        view.setOnLongClickListener(null);
        return view;
    }

    @Override
    public int getCount() {
        int retval;
        retval = this.allItems.size() + 1; // only "add new" at bottom
        return retval;
    }


    @Override
    public void updateAdapter() {
        List<BudgetLine> newItems = (new DatabaseHandler(getContext()).tblGetAllBudgetLines(item, null));
        parser.parseNewList(newItems);
        loadFromParserAndNotify();
    }

    public void loadAll() {
        ListViewItemAdapter.this.loadNonArchivedOnly = false;
        scrollToPosition(parser.getStartPositionOfNonArchivedInAll());
        loadFromParserAndNotify();
    }

    /**
     * using the boolean field loadNonArchivedOnly
     *
     * so make sure to update it BEFORE calling this method!
     */
    private void loadFromParserAndNotify() {
        List<BudgetLine> newItems;
        if (loadNonArchivedOnly)
            newItems = parser.getNonArchived();
        else
            newItems = parser.getAll();

        Log.d(TAG, "list got: (length " + newItems.size() + ")");


        Log.d(TAG, "list size(1): " + newItems.size());

        clear();
        Log.d(TAG, "list size(2): " + newItems.size());

        addAll(newItems);
        Log.d(TAG, "list size(3): " + newItems.size());
        this.allItems = newItems;
        notifyDataSetChanged();
        Log.d(TAG, "lv_allItems size(4): " + allItems.size());


    }
}
