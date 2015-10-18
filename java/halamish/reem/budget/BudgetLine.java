package halamish.reem.budget;

import java.util.Calendar;

/**
 * Created by Re'em on 10/17/2015.
 */
public class BudgetLine {
    private int id;
    private String title;
    private String details;
    private int amount;
    private String date;


    public BudgetLine() {
        title = "automaticly created";
        details = "not any details";
        amount = -5;
        date = Calendar.getInstance().getTime().toString();
    }

    public BudgetLine(String title, String details, int amount, String date) {
        this.title = title;
        this.details = details;
        this.amount = amount;
        this.date = date;
    }

    public BudgetLine(int id, String title, String details, int amount, String date ) {
        this(title, details, amount, date);
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
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
}
