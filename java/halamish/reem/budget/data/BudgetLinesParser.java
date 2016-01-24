package halamish.reem.budget.data;

import java.util.LinkedList;
import java.util.List;
/**
 * Created by Re'em on 1/6/2016.
 */
public class BudgetLinesParser {
    List<BudgetLine> allItems, nonArchivedItems;
    int nonArchivedAmount;
    public BudgetLinesParser(List<BudgetLine> items) {
        parseNewList(items);
    }

    public List<BudgetLine> getNonArchived() {
        return nonArchivedItems;
    }
    public List<BudgetLine> getAll() {
        return allItems;
    }

    /**
     * here is the actual commitment of the list to a field
     * @param allItems the list to parse
     */
    public void parseNewList(List<BudgetLine> allItems) {
        this.allItems = new LinkedList<>();
        this.nonArchivedItems = new LinkedList<>();
        for (BudgetLine line : allItems) {
            this.allItems.add(line);

            if (line.getEventType().equals(BudgetLine.BudgetLineEventType.AUTO_UPDATE)) {
            // update the previous lines to be archived then
                for (BudgetLine archivedLine : nonArchivedItems)
                    archivedLine.setArchived(true);
                this.nonArchivedItems = new LinkedList<>();
            }
            nonArchivedItems.add(line);
        }
        nonArchivedAmount = 0;
        for (BudgetLine line : nonArchivedItems)
            nonArchivedAmount += line.getAmount();

    }

    public int getNonArchivedAmount() {
        return nonArchivedAmount;
    }

    public boolean moreInfoAtAllThenAtNonArchived() {
        return nonArchivedItems.size() < allItems.size();
    }

    public int getStartPositionOfNonArchivedInAll() {
        return allItems.size() - nonArchivedItems.size();
    }
}
