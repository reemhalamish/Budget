package halamish.reem.budget;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.util.TypedValue;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by Re'em on 1/1/2016.
 *
 * the singleton member of this class will handle all the multiselect options
 */
public class MainActivityMultiSelectHandler {
    private static final String TAG = "MultiSelectHandler";

    // private data needed for running
    private static MainActivityMultiSelectHandler instnace;
    private boolean atMultiSelectMode = false;

    // db and related
    private DatabaseHandler db;
    private HashSet<String> pressedBudgetItemsNames;

    // Activity, view and related
    private MainActivity context;
    private ImageView normalStatePlus;
    private ImageView multi_delete, multi_swap, multi_info, multi_edit;
    private MyAdapter aa;

    private MainActivityMultiSelectHandler() {}
    public static MainActivityMultiSelectHandler getInstance() {
        if (instnace == null)
            instnace = new MainActivityMultiSelectHandler();
        return instnace;
    }
    public void init(DatabaseHandler db){
        this.db = db;
        pressedBudgetItemsNames = new HashSet<>();
    }

    public void startMultiSelect(boolean startFresh) {
        atMultiSelectMode = true;
        aa.startMultiSelect();

        multi_delete.setVisibility(View.VISIBLE);
        multi_info.setVisibility(View.GONE);
        multi_swap.setVisibility(View.GONE);
        multi_edit.setVisibility(View.GONE);
        normalStatePlus.setVisibility(View.GONE);

        multi_delete.setOnClickListener(multipleDeleteDialogCreator());
        multi_info.setOnClickListener(multipleInfoDialogCreator());
        multi_swap.setOnClickListener(multipleSwapTransition());
        multi_edit.setOnClickListener(multipleEditInstant());

        if (startFresh) {
            pressedBudgetItemsNames = new HashSet<>();
        }
    }


    /** it's the onClick method of the view to call this method
     * this method will change the buttons on the screen s.t.
     * 1  item  pressed: delete(trash can), details(i), duplicate(duplicate icon), clear(brush), edit(pencil)
     * 2  items pressed: delete, swap(exchange stretched)
     * 3+ items pressed: delete
     * @param newSelectedItem the item being selected
     */
    public void updateButtons(View newSelectedItem) {
        String itemName = (String) newSelectedItem.getTag(R.id.TAG_BUDGETITEM_NAME);
        if (pressedBudgetItemsNames.contains(itemName)) {
            pressedBudgetItemsNames.remove(itemName);
        } else {
            pressedBudgetItemsNames.add(itemName);
        }
        switch (pressedBudgetItemsNames.size()) {
            case 0:
                startNormalState();
                return;
            case 1:
                multi_swap.setVisibility(View.GONE);
                multi_info.setVisibility(View.VISIBLE);
                multi_edit.setVisibility(View.VISIBLE);
                break;
            case 2:
                multi_swap.setVisibility(View.VISIBLE);
                multi_info.setVisibility(View.GONE);
                multi_edit.setVisibility(View.GONE);
                break;
            case 3:
            default:
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
                String msg = "sure to DELETE?\n";
                for (String item : pressedBudgetItemsNames) {
                    msg = msg + "\n" + item;
                }
                msg = msg + "\n\nthe deletion can't be undone!";

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Confirm deleting multiple items");
                builder.setMessage(msg);
                builder.setCancelable(true);
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Collection<String> toDelete = new HashSet<String>();
//                        for (View item : pressedBudgetItemsNames)
//                            toDelete.add((String) item.getTag(R.id.TAG_BUDGETITEM_NAME));
                        db.deleteBudgetItems(pressedBudgetItemsNames, null);
                        pressedBudgetItemsNames = new HashSet<>();
                        atMultiSelectMode = false;
                        startNormalState();
                        dialog.dismiss();
                    }
                });

                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
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
                atMultiSelectMode = false;
                startNormalState();
            }
        };
    }

    private View.OnClickListener multipleSwapTransition() {
        final GridView gridView = (GridView) context.findViewById(R.id.gv_main_all_items);

        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

//                        atMultiSelectMode = false;
//                        startNormalState();
                    }
                }, 1);

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
        instnace = null;
    }

    public String getFirstItemFromPressed() {
        Iterator<String> oneItemIterator = pressedBudgetItemsNames.iterator();
        if (!oneItemIterator.hasNext())
            throw new AssertionError("trying to show info when nothing is here");
        return oneItemIterator.next();
    }
}