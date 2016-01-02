package halamish.reem.budget;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Re'em on 10/17/2015.
 *
 * adapter for MainActivity for budgetItems
 */
public class GridViewItemAdapter extends MyAdapter<BudgetItem> {
    private static String TAG = "GridAdapter";


    private List<BudgetItem> allItems;
    private View.OnClickListener newItemListener, everyViewListener, everyViewMultiSelectListener;
    private View.OnLongClickListener everyViewLongClickListener;
    private boolean showAddNewItem = true;
    private MainActivityMultiSelectHandler multiSelectHandler;

    public GridViewItemAdapter(Context context,
                               int resource,
                               List<BudgetItem> objects,
                               View.OnClickListener everyItemListener,
                               View.OnClickListener everyViewMultiSelectListener,
                               View.OnLongClickListener everyItemLongClickListener,
                               View.OnClickListener newItemListener) {
        super(context, resource, objects);
        allItems = objects;
        this.everyViewListener = everyItemListener;
        this.everyViewLongClickListener = everyItemLongClickListener;
        this.everyViewMultiSelectListener = everyViewMultiSelectListener;
        this.newItemListener = newItemListener;
        this.multiSelectHandler = MainActivityMultiSelectHandler.getInstance();
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

        //listeners
        if (_multiSelectMode) {
            view.setOnClickListener(everyViewMultiSelectListener);
            view.setOnLongClickListener(null);
        } else {
            view.setOnClickListener(everyViewListener);
            view.setOnLongClickListener(everyViewLongClickListener);
        }

        // pixels
        BudgetItem curItem = allItems.get(position);
        int cur_value = curItem.getCur_value();
        TextView txtTitle = (TextView) view.findViewById(R.id.tv_item_name);
        TextView txtAmount = (TextView) view.findViewById(R.id.tv_item_amount);
        ImageView hiddenVi = (ImageView) view.findViewById(R.id.iv_item_hidden_vi);

        txtTitle.setText(curItem.getName());
        txtAmount.setText("" + cur_value);

        if (cur_value >= 0) {
            txtAmount.setTextColor(Color.parseColor("#008811"));
            txtTitle.setTextColor(Color.parseColor("#003311"));
        } else {
            txtAmount.setTextColor(Color.RED);
            txtTitle.setTextColor(Color.RED);
        }

        if (_multiSelectMode && multiSelectHandler.isPressed(curItem.getName())) {
            hiddenVi.setVisibility(View.VISIBLE);
            view.setBackgroundResource(R.drawable.grid_view_tile_back_selected);
        } else {
            hiddenVi.setVisibility(View.GONE);
            view.setBackgroundResource(R.drawable.grid_view_tile_back);
        }

        view.setTag(R.id.TAG_BUDGETITEM_NAME, curItem.getName());
//        view.setTag(VIEW_TAG_POS, position);

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
        int retval = super.getCount();
        if (showAddNewItem) {
            retval += 1;
        }
        return retval;
    }

    public void toggleAddNewItem(boolean toShow) {
        boolean temp = showAddNewItem;
        showAddNewItem = toShow;
        if (temp != toShow)
            notifyDataSetChanged();
    }

    @Override
    public void startMultiSelect() {
        super.startMultiSelect();
        toggleAddNewItem(false);
    }

    @Override
    public void endMultiSelect() {
        super.endMultiSelect();
        toggleAddNewItem(true);
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
