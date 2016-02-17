package halamish.reem.budget.main;

import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import halamish.reem.budget.item.ItemActivity;
import halamish.reem.budget.MyAdapter;
import halamish.reem.budget.R;
import halamish.reem.budget.data.DatabaseHandler;

/**
 * Created by Re'em on 1/1/2016.
 *
 * the singleton member of this class will handle all the multiselect options
 */
public class MainActivityMultiSelectHandler {
    private static final String TAG = "MultiSelectHandler";
    private static final long DELAY_AFTER_SWAP_MS = 300;

    // private data needed for running
    private static MainActivityMultiSelectHandler s_instnace;
    private boolean atMultiSelectMode = false;

    // db and related
    private DatabaseHandler db;
    private HashSet<String> pressedBudgetItemsNames;
    private Map<String, String> itemName_to_prettyName;

    // Activity, view and related
    private MainActivity context;
    private ImageView normalStatePlus;
    private ImageView multi_delete, multi_swap, multi_info, multi_edit;
    private MyAdapter aa;

    private MainActivityMultiSelectHandler() {}
    public static MainActivityMultiSelectHandler getInstance() {
        if (s_instnace == null)
            s_instnace = new MainActivityMultiSelectHandler();
        return s_instnace;
    }
    public void init(DatabaseHandler db){
        this.db = db;
        pressedBudgetItemsNames = new HashSet<>();
        itemName_to_prettyName = new HashMap<>();
    }

    public void startMultiSelect(boolean startFresh) {
        atMultiSelectMode = true;
        aa.startMultiSelect();

        multi_delete.setOnClickListener(multipleDeleteDialogCreator());
        multi_info.setOnClickListener(multipleInfoDialogCreator());
        multi_swap.setOnClickListener(multipleSwapTransition());
        multi_edit.setOnClickListener(multipleEditInstant());

        if (startFresh) {
            pressedBudgetItemsNames = new HashSet<>();
            itemName_to_prettyName = new HashMap<>();
        } else {
            updateButtons(null);
        }
    }


    /** it's the onClick method of the view to call this method
     * this method will change the buttons on the screen s.t.
     * 1  item  pressed: delete(trash can), details(i), duplicate(duplicate icon), clear(brush), edit(pencil)
     * 2  items pressed: delete, swap(exchange stretched)
     * 3+ items pressed: delete
     * @param newSelectedItem the item being selected. Null can be passed at onCreate() with savedMemory to restore the buttons
     */
    public void updateButtons(@Nullable View newSelectedItem) {
        if (newSelectedItem != null) {
            String itemName = (String) newSelectedItem.getTag(R.id.TAG_BUDGET_ITEM_NAME);
            String prettyName = (String) newSelectedItem.getTag(R.id.TAG_BUDGET_ITEM_PRETTY_NAME);
            if (pressedBudgetItemsNames.contains(itemName)) {
                pressedBudgetItemsNames.remove(itemName);
            } else {
                pressedBudgetItemsNames.add(itemName);
                itemName_to_prettyName.put(itemName, prettyName);
            }
        }
        switch (pressedBudgetItemsNames.size()) {
            case 0:
                startNormalState();
                return;
            case 1:
                multi_delete.setVisibility(View.VISIBLE);
                multi_swap.setVisibility(View.GONE);
                multi_info.setVisibility(View.VISIBLE);
                multi_edit.setVisibility(View.VISIBLE);
                break;
            case 2:
                multi_delete.setVisibility(View.VISIBLE);
                multi_swap.setVisibility(View.VISIBLE);
                multi_info.setVisibility(View.GONE);
                multi_edit.setVisibility(View.GONE);
                break;
            case 3:
            default:
                multi_delete.setVisibility(View.VISIBLE);
                multi_swap.setVisibility(View.GONE);
                multi_info.setVisibility(View.GONE);
                multi_edit.setVisibility(View.GONE);
        }

    }


    /*
    remove all special buttons and get the normal plus button
     */
    public void startNormalState() {
        atMultiSelectMode = false;
        aa.endMultiSelect();
        // for the buttons if not == null then remove them. watch the order!
        multi_delete.setVisibility(View.GONE);
        multi_info.setVisibility(View.GONE);
        multi_swap.setVisibility(View.GONE);
        multi_edit.setVisibility(View.GONE);
        normalStatePlus.setVisibility(View.VISIBLE);
        normalStatePlus.setOnClickListener(context.getAddNewItemListener());

    }

    public boolean isAtMultiSelectMode() {
        return atMultiSelectMode;
    }

    public boolean isPressed(String budgetItemName) {
        return pressedBudgetItemsNames.contains(budgetItemName);
    }

    /**
     * is called from the activity
     * therefor, all the buttons need to be re-created
     * @param context the MainActivity instance that sent you here
     * @param aa the adapter to call end\start multipleSelect() on
     */
    public void startModeBasedOnMemory(MainActivity context, MyAdapter aa) {
        this.context = context;
        this.aa = aa;
        normalStatePlus = (ImageView) context.findViewById(R.id.iv_main_add);
        multi_delete    = (ImageView) context.findViewById(R.id.iv_main_multi_delete);
        multi_info      = (ImageView) context.findViewById(R.id.iv_main_multi_info);
        multi_swap      = (ImageView) context.findViewById(R.id.iv_main_multi_swap);
        multi_edit      = (ImageView) context.findViewById(R.id.iv_main_multi_edit);

        if (atMultiSelectMode) {
            startMultiSelect(false);
        } else {
            startNormalState();
        }
    }

    private int convertPxlToDp(int pxls) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pxls, context.getResources().getDisplayMetrics());
    }

    private View.OnClickListener multipleDeleteDialogCreator() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = context.getString(R.string.msg_delete_confirm_multiple) + "\n";
                for (String item : pressedBudgetItemsNames) {
                    msg = msg + "\n* " + itemName_to_prettyName.get(item);
                }
                msg = msg + "\n\n" + context.getString(R.string.msg_delete_cant_be_undone);

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(context.getString(R.string.title_delete_multiple));
                builder.setMessage(msg);
                builder.setCancelable(true);
                builder.setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Collection<String> toDelete = new HashSet<String>();
//                        for (View item : pressedBudgetItemsNames)
//                            toDelete.add((String) item.getTag(R.id.TAG_BUDGET_ITEM_NAME));
                        db.deleteBudgetItems(pressedBudgetItemsNames, null);
                        pressedBudgetItemsNames = new HashSet<>();
                        itemName_to_prettyName = new HashMap<>();
                        startNormalState();
                        dialog.dismiss();
                    }
                });

                builder.setNegativeButton(context.getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        };
    }

    private View.OnClickListener multipleInfoDialogCreator() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ItemActivity.createItemInfoDialog(context, getFirstItemFromPressed());
                startNormalState();
            }
        };
    }

    private View.OnClickListener multipleSwapTransition() {
        final GridView gridView = (GridView) context.findViewById(R.id.gv_main_all_items);

        return new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (pressedBudgetItemsNames.size() != 2)
                    throw new AssertionError("size incorrect for swap: " + pressedBudgetItemsNames.size());
                final String firstItem, secondItem;
                Iterator<String> iterator = pressedBudgetItemsNames.iterator();
                firstItem = iterator.next();
                secondItem = iterator.next();
                db.swap_two_items_order(
                        db.getBudgetItem(firstItem),
                        db.getBudgetItem((secondItem)),
                        null
                );

                // ONEDAY make it use animation for the swap
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        startNormalState();
                    }
                }, DELAY_AFTER_SWAP_MS);



//                firstItem.startAnimation(ta2);
//                firstItem.bringToFront();
//                secondItem.startAnimation(ta1);
//                secondItem.bringToFront();
            }
        };
    }

    private View.OnClickListener multipleEditInstant() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent editItemIntent = new Intent(context, MessWithItemDialog.class);
                editItemIntent.putExtra("item_action", MessWithItemDialog.ItemAction.EDIT);
                editItemIntent.putExtra("item_name", getFirstItemFromPressed());
                context.startActivity(editItemIntent);
                startNormalState();
            }
        };
    }

    public void freeMemory() {
        db = null;
        pressedBudgetItemsNames = null;
        normalStatePlus = null;
        multi_info = null;
        multi_swap = null;
        multi_delete = null;
        multi_edit = null;
        s_instnace = null;
    }

    public String getFirstItemFromPressed() {
        Iterator<String> oneItemIterator = pressedBudgetItemsNames.iterator();
        if (!oneItemIterator.hasNext())
            throw new AssertionError("trying to show info when nothing is here");
        return oneItemIterator.next();
    }
}
