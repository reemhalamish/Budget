package halamish.reem.budget.main;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import halamish.reem.budget.MyAdapter;
import halamish.reem.budget.R;
import halamish.reem.budget.misc.Settings;
import halamish.reem.budget.data.BudgetItem;
import halamish.reem.budget.data.DatabaseHandler;

/**
 * Created by Re'em on 10/17/2015.
 *
 * adapter for MainActivity for budgetItems
 */
public class GridViewItemAdapter extends MyAdapter<BudgetItem> {
    private static String TAG = "GridAdapter";
    private final LayoutInflater inflater;


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
        inflater = LayoutInflater.from(getContext());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
        // create the view from scratch
        // and set the basic font
        // (else - view exists, just update text and stuff...)
            view = inflater.inflate(R.layout.budget_item, parent, false);
            ((TextView) view.findViewById(R.id.tv_item_name))
                    .setTypeface(Settings.getInstance().getFont());
        }
        if (position >= allItems.size()) {
            return createViewForAdd(view, parent);
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
        TextView txtTitle = (TextView) view.findViewById(R.id.tv_item_name);
        TextView txtAmount = (TextView) view.findViewById(R.id.tv_item_amount);
        ImageView hiddenBubblesBg = (ImageView) view.findViewById(R.id.iv_item_hidden_bg);
        int cur_value = curItem.getCur_value();

        txtTitle.setText(curItem.getPretty_name());
        txtTitle.setTypeface(Settings.getInstance().getFont(curItem.getLanguage()));
        txtAmount.setText("" + cur_value);
        txtAmount.setVisibility(View.VISIBLE);

        if (cur_value >= 0) {
            txtAmount.setTextColor(Color.parseColor("#008811"));
            txtTitle.setTextColor(Color.parseColor("#003311"));
        } else {
            txtAmount.setTextColor(Color.RED);
            txtTitle.setTextColor(Color.RED);
        }

        if (_multiSelectMode && multiSelectHandler.isPressed(curItem.getName())) {
            hiddenBubblesBg.setVisibility(View.VISIBLE);
            view.setBackgroundResource(R.drawable.grid_view_tile_back_selected);
        } else {
            hiddenBubblesBg.setVisibility(View.GONE);
            view.setBackgroundResource(R.drawable.grid_view_tile_back);
        }

        view.setTag(R.id.TAG_BUDGET_ITEM_NAME, curItem.getName());
        view.setTag(R.id.TAG_BUDGET_ITEM_PRETTY_NAME, curItem.getPretty_name());
//        view.setTag(VIEW_TAG_POS, position);

        return view;
    }

    private View createViewForAdd(View nonNullConvertView, ViewGroup parent) {
        View view = nonNullConvertView; // as sent from getView after inflate() has been called
        view.setBackgroundResource(R.drawable.grid_view_tile_back);


        view.setOnClickListener(newItemListener);

        TextView txtTitle = (TextView) view.findViewById(R.id.tv_item_name);
        TextView txtAmount = (TextView) view.findViewById(R.id.tv_item_amount);
        ImageView hiddenBubblesBg = (ImageView) view.findViewById(R.id.iv_item_hidden_bg);

        txtTitle.setText(R.string.title_add_new_budgetitem);
        txtTitle.setTypeface(Settings.getInstance().getFont());
        txtTitle.setTextColor(Color.BLACK);
        txtAmount.setVisibility(View.GONE);
        hiddenBubblesBg.setVisibility(View.VISIBLE);


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
        List<BudgetItem> newItems = new DatabaseHandler(getContext()).getAllBudgetItems();
        addAll(newItems);
        allItems = newItems;
        notifyDataSetChanged();
    }
}
