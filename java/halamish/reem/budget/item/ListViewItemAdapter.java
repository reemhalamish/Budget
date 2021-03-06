package halamish.reem.budget.item;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import halamish.reem.budget.MyAdapter;
import halamish.reem.budget.R;
import halamish.reem.budget.misc.Settings;
import halamish.reem.budget.data.BudgetItem;
import halamish.reem.budget.data.BudgetLine;
import halamish.reem.budget.data.BudgetLinesParser;
import halamish.reem.budget.data.DatabaseHandler;
import halamish.reem.budget.misc.Utils;

/**
 * Created by Re'em on 10/17/2015.
 *
 * adapter for budget line
 */
public class ListViewItemAdapter extends MyAdapter<BudgetLine> {
    private static final String TAG = "listAdapter";
    private static final long SUM_ANIMATION_CYCLE_DUR_MS = 1000;
    private ListView actual_list;
    private List<BudgetLine> allItems;
    private View.OnClickListener newLineListener;
    private View.OnClickListener everyItemListener;
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
                               View.OnClickListener everyItemListener,
                               View.OnLongClickListener everyItemLongClickListener) {
        super(context, resource, parser.getNonArchived());
        this.parser = parser;
        this.allItems = parser.getNonArchived();
        this.item = item;
        this.newLineListener = newLineListener;
        this.actual_list = list;
        this.loadNonArchivedOnly = true;
        this.everyItemListener = everyItemListener;
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
//        position -= 1;

//        if (loadNonArchivedOnly && parser.moreInfoAtAllThenAtNonArchived())

//        if (position == -1) {
//            return loadEarlierView(convertView, parent);
//        }

//        // header
//        if (position == -1) return headerView(convertView, parent);

        // sum
        if (position == allItems.size()) return sumView(convertView, parent);

        // "add more?"
        if (position > allItems.size()) return addItemView(convertView, parent);

        View view;
        view = convertView;
        if (convertView == null || convertView.getId() != R.id.rl_line_main) {
            view = inflater.inflate(R.layout.budget_line, parent, false);
        } else {
            view = convertView;
            view.setAnimation(null); // clear the "sum" animation
        }
        view.setOnClickListener     (everyItemListener);
        view.setOnLongClickListener (everyItemLongClickListener);


//        LATERON
//        set some multiSelect state.

//        if (_dismissListener != null) {
//            view.setOnTouchListener(_dismissListener);
//            view.setOnClickListener(_clickListener);
//        }



        BudgetLine curLine = allItems.get(position);
        view.setTag(R.id.TAG_BUDGET_LINE, curLine);

        TextView txtTitle = (TextView) view.findViewById(R.id.tv_line_title);
        TextView txtAmount = (TextView) view.findViewById(R.id.tv_line_amount);
        TextView txtDetails = (TextView) view.findViewById(R.id.tv_line_details);
        TextView txtDate = (TextView) view.findViewById(R.id.tv_line_date);
//        TextView txtEventType = (TextView) view.findViewById(R.id.tv_line_event_type);

        txtTitle.setText(curLine.getTitle());
        txtAmount.setText(String.valueOf(curLine.getAmount()));
        txtDate.setText(Utils.millisecToDate(curLine.getDate()));
        txtDetails.setText(curLine.getDetails());
//        txtEventType.setText(curLine.getEventType().name());

        if (curLine.isArchived()) {
            txtTitle.setTextColor(Color.GRAY);
            txtAmount.setTextColor(Color.GRAY);
            txtDate.setTextColor(Color.GRAY);
            txtDetails.setTextColor(Color.GRAY);
        } else {
            txtTitle.setTextColor(Color.BLACK);
            txtDate.setTextColor(Color.BLACK);
            txtDetails.setTextColor(Color.BLACK);

            if (curLine.getAmount() >= 0) {
                txtAmount.setTextColor(ContextCompat.getColor(getContext(), R.color.balance_more_than_zero));
            } else {
                txtAmount.setTextColor(Color.RED);
            }
        }

        view.setTag(position);
        return view;
    }

    private View sumView(View convertView, ViewGroup parent) {
        final View currentBalance;
        if (convertView == null || convertView.getId() != R.id.rl_line_main) {
            currentBalance = inflater.inflate(R.layout.budget_line, parent, false);
        } else {
            currentBalance = convertView;
        }
        TextView txtTitle = (TextView) currentBalance.findViewById(R.id.tv_line_title);
        TextView txtAmount = (TextView) currentBalance.findViewById(R.id.tv_line_amount);
        TextView txtDetails = (TextView) currentBalance.findViewById(R.id.tv_line_details);
        TextView txtDate = (TextView) currentBalance.findViewById(R.id.tv_line_date);


        txtDate.setText(    "");
        txtDetails.setText("");
        txtAmount.setText("" + parser.getNonArchivedAmount());
        txtAmount.setTextColor(Color.BLACK);
        txtTitle.setText(getContext().getString(R.string.activity_item_cur_balance));
        txtTitle.setTextColor(Color.BLACK);

        if (Settings.getInstance().isActivity_item_sum_flickering()) {
            startFlickeringEffect(currentBalance);
        }

        currentBalance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentBalance.setAnimation(null);
                currentBalance.setAlpha(1);
                // ONEDAY add here some nice chart or something?
            }
        });
        currentBalance.setOnLongClickListener(null);
        return currentBalance;
    }

    public static void startFlickeringEffect(final View sumView) {
        final AlphaAnimation fadeIn = new AlphaAnimation( 1.0f , 0.0f );
        final AlphaAnimation fadeOut = new AlphaAnimation(0.0f , 1.0f );
        fadeIn.setDuration(SUM_ANIMATION_CYCLE_DUR_MS);
        fadeOut.setDuration(SUM_ANIMATION_CYCLE_DUR_MS);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                sumView.startAnimation(fadeOut);
            }

            public void onAnimationRepeat(Animation animation) {
            }
        });
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation)   {}
            public void onAnimationEnd(Animation animation)     {sumView.startAnimation(fadeIn);}
            public void onAnimationRepeat(Animation animation)  {}
        });
        sumView.startAnimation(fadeIn);
    }

//    private View headerView(View convertView, ViewGroup parent) {
//        View view = convertView;
//        if (view == null || view.getId() != R.id.rl_line_main) {
//            view = inflater.inflate(R.layout.budget_line, parent, false);
//        }
//
//        TextView txtTitle = (TextView) view.findViewById(R.id.tv_line_title);
//        TextView txtAmount = (TextView) view.findViewById(R.id.tv_line_amount);
//        TextView txtDetails = (TextView) view.findViewById(R.id.tv_line_details);
//        TextView txtDate = (TextView) view.findViewById(R.id.tv_line_date);
//
//        txtTitle.setText(R.string.itemactivity_header_title);
//        txtAmount.setText(R.string.itemactivity_header_Amount);
//        txtDate.setText(R.string.itemactivity_header_date);
//        txtDetails.setText(R.string.itemactivity_header_details);
//
//        txtTitle.setTextColor(Color.BLACK);
//        txtDate.setTextColor(Color.BLACK);
//        txtDetails.setTextColor(Color.BLACK);
//        txtAmount.setTextColor(Color.BLACK);
//
//
//        return view;
//    }

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
        txtAdd.setTypeface(Settings.getInstance().getFont());
        view.setOnClickListener(newLineListener);
        view.setOnLongClickListener(null);
        return view;
    }

    @Override
    public int getCount() {
        int retval;
        retval = this.allItems.size() + 2; // "sum" + "add new" at bottom
        return retval;
    }


    @Override
    public void updateAdapter() {
        List<BudgetLine> newItems = (new DatabaseHandler(getContext()).tblGetAllBudgetLines(item));
        parser.parseNewList(newItems);
        loadFromParserAndNotify();
    }

    public void loadAll() {
        ListViewItemAdapter.this.loadNonArchivedOnly = false;
        scrollToPosition(parser.getLastPositionOfArchived());
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
        clear();
        addAll(newItems);
        this.allItems = newItems;
        notifyDataSetChanged();
    }
}
