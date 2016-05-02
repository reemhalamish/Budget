package halamish.reem.budget.data;

import android.content.Context;
import android.os.Parcelable;


import java.io.Serializable;

/**
 * Created by Re'em on 10/17/2015.
 */
public class BudgetLine implements Serializable{
    private static final String TAG = "BudgetLine";

    public enum BudgetLineEventType {
        AUTO_UPDATE(0),
        USER_INPUT(1),
        ARCHIVE_ENDS_HERE(2), // not used
        BUDGET_CREATED(3),
        MOVE_FROM_LAST_TIME(4),
        BUDGET_CHANGE_AMOUNT(5);

        private int value;
        BudgetLineEventType(int val){
            this.value = val;
        }
        public int toInt() { return value;}
        public boolean Compare(int i){return value == i;}
        public static BudgetLineEventType getEnum(int _id)
        {
            BudgetLineEventType[] all = BudgetLineEventType.values();
            for(int i = 0; i < all.length; i++)
            {
                if(all[i].Compare(_id))
                    return all[i];
            }
            return BudgetLineEventType.USER_INPUT;
        }
        public static BudgetLineEventType getEnum(String _id) {
            return getEnum(Integer.parseInt(_id));
        }

        public String getLocalizedName(Context context) {
            return toString();
            // ONEDAY support hebrew or something...
        }
    }

    private int id;
    private String title;
    private String details;
    private int amount;
    private long date;
    private BudgetLineEventType eventType;
    private boolean archived;



//    public BudgetLine() {
//        title = "automaticly created";
//        details = "not any details";
//        amount = -5;
//        date = Calendar.getInstance().getTime().toString();
//    }

    public BudgetLine(String title, String details, int amount, long date, BudgetLineEventType eventType) {
        this.title = title;
        this.details = details;
        this.amount = amount;
        this.date = date;
        this.eventType = eventType;
    }

    public BudgetLine(int id, String title, String details, int amount, long date, BudgetLineEventType eventType) {
        this(title, details, amount, date, eventType);
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public BudgetLineEventType getEventType() {
        return eventType;
    }

    public void setEventType(BudgetLineEventType eventType) {
        this.eventType = eventType;
    }

    public void logMe() {
        // Log.d(TAG, "BudgetLine. id: " + id + ", amount: " + amount + ", eventType: " + eventType.name() + ", Title: " + title + ", details: " + details);
    }

    // is set by the parser, this info isn't stored at the SQLite db!!
    public boolean isArchived() {
        return archived;
    }
    public void setArchived(boolean newValue) {
        archived = newValue;
    }
}
