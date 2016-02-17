package halamish.reem.budget.data;

import android.content.Context;


import halamish.reem.budget.R;
import halamish.reem.budget.misc.Settings;


/**
 * Created by Re'em on 10/17/2015.
 */
public class BudgetItem {
    public static final String WEEKLY = "weekly", MONTHLY = "monthly";
    private static final int CUR_VALUE_DEFAULT = 0;
    private static final int AUTO_UPDATE_DEFAULT = 100;
    private static final String TAG = "BudgetItem";

    private int id;
    private String name;
    private int cur_value = CUR_VALUE_DEFAULT;
    private String auto_update = MONTHLY;
    private int auto_update_amount = AUTO_UPDATE_DEFAULT;
    private int order_id;
    private String pretty_name;



    public BudgetItem() {}


    public BudgetItem(String name, String auto_update, int auto_update_amount) {
        this.auto_update = auto_update;
        this.auto_update_amount = auto_update_amount;
        this.pretty_name = name;
    }

//    public BudgetItem(int id, String name, int cur_value, String auto_update, int auto_update_amount) {
//        this.id = id;
//        this.name = name;
//        this.cur_value = cur_value;
//        this.auto_update = auto_update;
//        this.auto_update_amount = auto_update_amount;
//        this.order_id = id;
//    }

    public BudgetItem(int id, String name, int cur_value, String auto_update, int auto_update_amount, int order_id, String pretty_name) {
        this.id = id;
        this.name = name;
        this.cur_value = cur_value;
        this.auto_update = auto_update;
        this.auto_update_amount = auto_update_amount;
        this.order_id = order_id;
        this.pretty_name = pretty_name;
    }

    /**
     * TO BE USED SOLELY IN THE TUTORIAL!
     * HAS NOTHING TO ACTUALLY INSERT TO THE DB!
     * @param prettyName
     * @param curAmount
     */
    public BudgetItem(String prettyName, int curAmount) {
        this.name = prettyName;
        this.pretty_name = prettyName;
        this.cur_value = curAmount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }



    public String getName() {
//        String good_name = name.replace(' ', '_');
//        String good_name = name.replace(' ', '_').replaceAll("[^\\u0590-\\u05FFa-zA-Z0-9_]", "");
//        return good_name;
        return name;
    }

    public String getPretty_name() {
        return pretty_name;
    }

    public void setPretty_name(String pretty_name) {
        this.pretty_name = pretty_name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCur_value() {
        return cur_value;
    }

    public void setCur_value(int cur_value) {
        this.cur_value = cur_value;
    }

    public void updateAmount(int amountToAdd) {
        // Log.d(TAG, "amount was " + cur_value);
        this.cur_value += amountToAdd;
        // Log.d(TAG, "amount now " + cur_value);
    }


    public String getAuto_update() {
        return auto_update;
    }
    public String getLocalizedAuto_update(Context context) {
        if (auto_update.equals(WEEKLY)) {
            return context.getString(R.string.budgetitem_weekly);
        } else {
            return context.getString(R.string.budgetitem_monthly);
        }
    }

    public void setAuto_update(String auto_update) {
        this.auto_update = auto_update;
    }

    public int getAuto_update_amount() {
        return auto_update_amount;
    }

    public void setAuto_update_amount(int auto_update_amount) {
        this.auto_update_amount = auto_update_amount;
    }

    public int getOrder_id() {
        return order_id;
    }

    public void setOrder_id(int order_id) {
        this.order_id = order_id;
    }

    public void logMyself() {
        // Log.d(TAG, name + " id: " + id + ", curValue: " + cur_value + ", autoUpdate: " + auto_update + ", autoUpdateAmount: " + auto_update_amount + ", order_id: " + order_id);
    }

    public Settings.Language getLanguage() {
        if (pretty_name.matches("[a-zA-Z0-9\\s_]+")) {
            return Settings.Language.ENGLISH;
        } else if (pretty_name.matches("[\\u0590-\\u05FF\\s0-9_]+")) {
            return Settings.Language.HEBREW_UNISEX;
        }
        return null;
    }

}

