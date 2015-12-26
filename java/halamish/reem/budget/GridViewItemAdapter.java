package halamish.reem.budget;

import android.content.Context;
import android.content.Intent;
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
public class GridViewItemAdapter extends MyAdapter<BudgetItem> {
    private List<BudgetItem> allItems;
    private View.OnClickListener newItemListener, everyViewListener;
    private View.OnLongClickListener everyViewLongClickListener;

    public GridViewItemAdapter(Context context,
                               int resource,
                               List<BudgetItem> objects,
                               View.OnClickListener everyItemListener,
                               View.OnLongClickListener everyItemLongClickListener,
                               View.OnClickListener newItemListener) {
        super(context, resource, objects);
        allItems = objects;
        this.everyViewListener = everyItemListener;
        this.everyViewLongClickListener = everyItemLongClickListener;
        this.newItemListener = newItemListener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position >= allItems.size()) {
            return createViewForAdd(convertView, parent);
        }


        View view = convertView;
        if (view == null) { // create the view from scratch (else - view exists, just update)
            LayoutInflater inflater = LayoutInflater.from(getContext());
            view = inflater.inflate(R.layout.budget_item, parent, false);
        }

        view.setOnClickListener(everyViewListener);
        view.setOnLongClickListener(everyViewLongClickListener);

        BudgetItem curItem = allItems.get(position);
        int cur_value = curItem.getCur_value();
        TextView txtTitle = (TextView) view.findViewById(R.id.tv_item_name);
        TextView txtAmount = (TextView) view.findViewById(R.id.tv_item_amount);

        txtTitle.setText(curItem.getName());
        txtAmount.setText("" + cur_value);

        if (cur_value >= 0) {
            txtAmount.setTextColor(Color.parseColor("#008811"));
            txtTitle.setTextColor(Color.GREEN);
        } else {
            txtAmount.setTextColor(Color.RED);
            txtTitle.setTextColor(Color.RED);
        }

        view.setTag(allItems.get(position).getName());

        return view;
    }

    private View createViewForAdd(View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) { // create the view from scratch (else - view exists, just update)
            LayoutInflater inflater = LayoutInflater.from(getContext());
            view = inflater.inflate(R.layout.budget_item, parent, false);
        }

        view.setOnClickListener(newItemListener);

        TextView txtTitle = (TextView) view.findViewById(R.id.tv_item_name);
        TextView txtAmount = (TextView) view.findViewById(R.id.tv_item_amount);

        txtTitle.setText("Add new?");
        txtTitle.setTextColor(Color.BLUE);

        txtAmount.setText("");

        return view;
    }

    @Override
    public int getCount() {
        return super.getCount() + 1;
    }


    @Override
    public void updateAdapter() {
        clear();
        List<BudgetItem> newItems = new DatabaseHandler(getContext()).getAllBudgetItems(null);
        addAll(newItems);
        allItems = newItems;
        notifyDataSetChanged();
    }
}
