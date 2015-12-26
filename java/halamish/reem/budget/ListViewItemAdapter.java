package halamish.reem.budget;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Re'em on 10/17/2015.
 */
public class ListViewItemAdapter extends MyAdapter<BudgetLine> {
    private static final String TAG = "listAdapter";
    private List<BudgetLine> allItems;
    private View.OnClickListener newLineListener;
    private BudgetItem item;
    public ListViewItemAdapter(Context context,
                               int resource,
                               List<BudgetLine> objects,
                               BudgetItem item,
                               View.OnClickListener newLineListener) {
        super(context, resource, objects);
        allItems = objects;
        this.item = item;
        this.newLineListener = newLineListener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position >= allItems.size()) {
            return addItemView(convertView, parent);
        }

        View view;
        LayoutInflater inflater = LayoutInflater.from(getContext());
        view = inflater.inflate(R.layout.budget_line, parent, false);

//
//        LATERON
//        if (_dismissListener != null) {
//            view.setOnTouchListener(_dismissListener);
//            view.setOnClickListener(_clickListener);
//        }


        view.findViewById(R.id.iv_line_separator).setBackgroundColor(Color.DKGRAY);

        BudgetLine curLine = allItems.get(position);
        int cur_value = curLine.getAmount();
        TextView txtTitle = (TextView) view.findViewById(R.id.tv_line_title);
        TextView txtAmount = (TextView) view.findViewById(R.id.tv_line_amount);
        TextView txtDetails = (TextView) view.findViewById(R.id.tv_line_details);
        TextView txtDate = (TextView) view.findViewById(R.id.tv_line_date);

        txtTitle.setText(curLine.getTitle());
        txtAmount.setText("" + cur_value);
        txtDate.setText(curLine.getDate());
        txtDetails.setText(curLine.getDetails());

        if (cur_value >= 0) {
            txtAmount.setTextColor(Color.GREEN);
        } else {
            txtAmount.setTextColor(Color.RED);
        }

        view.setTag(position);
        return view;
    }

    private View addItemView(View convertView, ViewGroup parent) {
        View view;
        LayoutInflater inflater = LayoutInflater.from(getContext());
        view = inflater.inflate(R.layout.budget_line_newline, parent, false);

        TextView txtAdd = (TextView) view.findViewById(R.id.tv_line_addline_title);
        txtAdd.setTextColor(Color.BLUE);
        view.setOnClickListener(newLineListener);

        return view;
    }

    @Override
    public int getCount() {
        return super.getCount() + 1;
    }


    @Override
    public void updateAdapter() {
        clear();
        allItems = new DatabaseHandler(getContext()).tblGetAllBudgetLines(item, null);
        addAll(allItems);
        notifyDataSetChanged();
    }
}
