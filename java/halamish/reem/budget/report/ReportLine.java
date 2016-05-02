package halamish.reem.budget.report;

import android.content.Context;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import halamish.reem.budget.data.BudgetItem;
import halamish.reem.budget.data.BudgetLine;

/**
 * Created by Re'em on 5/2/2016.
 * represents one report line
 */
public class ReportLine implements Serializable {

    private static class NameSorter implements Comparator<ReportLine> {
        @Override
        public int compare(ReportLine r1, ReportLine r2) {
            return r1.name.compareTo(r2.name);
        }
    }
    private static class BudgetAveSorter implements Comparator<ReportLine> {
        @Override
        public int compare(ReportLine r1, ReportLine r2) {
            if (r1.budget_ave > r2.budget_ave)
                return 1;
            else if (r1.budget_ave < r2.budget_ave)
                return -1;
            return 0;
        }
    }
    private static class ExpenseAveSorter implements Comparator<ReportLine> {
        @Override
        public int compare(ReportLine r1, ReportLine r2) {
            if (r1.expense_ave > r2.expense_ave)
                return 1;
            else if (r1.expense_ave < r2.expense_ave)
                return -1;
            return 0;
        }
    }
    private static class NetoAveSorter implements Comparator<ReportLine> {
        @Override
        public int compare(ReportLine r1, ReportLine r2) {
            if (r1.neto_ave > r2.neto_ave)
                return 1;
            else if (r1.neto_ave < r2.neto_ave)
                return -1;
            return 0;
        }
    }
    private static class UpdatesEverySorter implements Comparator<ReportLine> {
        @Override
        public int compare(ReportLine r1, ReportLine r2) {
            return r1.updated.compareTo(r2.updated);
        }
    }
    private static class NetoYearlySorter implements Comparator<ReportLine> {
        @Override
        public int compare(ReportLine r1, ReportLine r2) {
            if (r1.neto_yearly > r2.neto_yearly)
                return 1;
            else if (r1.neto_yearly < r2.neto_yearly)
                return -1;
            return 0;
        }
    }
    private static class NullSorter implements Comparator<ReportLine> {
        @SuppressWarnings("ComparatorMethodParameterNotUsed")
        @Override
        public int compare(ReportLine r1, ReportLine r2) {
            return 1;
        }
    }

    public static void sort(List<ReportLine> input, ReportLineHeader sorter) {
        Comparator<ReportLine> comparator;
        switch (sorter) {
            case NAME:
                comparator = new NameSorter();
                break;
            case BUDGET_AVERAGE:
                comparator = new BudgetAveSorter();
                break;
            case EXPENSE_AVERAGE:
                comparator = new ExpenseAveSorter();
                break;
            case NETO_AVERAGE:
                comparator = new NetoAveSorter();
                break;
            case UPDATES_EVERY:
                comparator = new UpdatesEverySorter();
                break;
            case NETO_YEARLY:
                comparator = new NetoYearlySorter();
                break;
            case NOTHING:
            default:
                comparator = new NullSorter();
        }
        Collections.sort(input, comparator);
    }


    String name;
    long budget_ave, expense_ave, neto_ave;
    String updated;
    long neto_yearly;

//    String getName()        {return name;}
//    long getBudget_ave()    {return budget_ave;}
//    long getReport_ave()    {return expense_ave;}
//    long getNeto_ave()      {return neto_ave;}
//    String getUpdated()     {return updated;}
//    long getNeto_yearly()   {return neto_yearly;}

    ReportLine(BudgetItem item, List<BudgetLine> lines, Context context) {
        name = item.getPretty_name();
        updated = item.getLocalizedAuto_update(context);
        long budget_sum = 0;
        int budget_dividor = 0;
        long expenses_sum = 0;
        int expenses_dividor = 0;
        int netoYearlyFactor = item.getMultiplyFactorForYear();

        for (BudgetLine line : lines) {
            if (
                    line.getEventType().equals(BudgetLine.BudgetLineEventType.BUDGET_CREATED) ||
                            line.getEventType().equals(BudgetLine.BudgetLineEventType.AUTO_UPDATE)) {
                budget_sum += line.getAmount();
                budget_dividor += 1;
            } else if (
                    line.getEventType().equals(BudgetLine.BudgetLineEventType.BUDGET_CHANGE_AMOUNT)
                    ) {
                budget_sum += line.getAmount();
            } else if (
                    line.getEventType().equals(BudgetLine.BudgetLineEventType.USER_INPUT)
                    ) {
                expenses_sum += line.getAmount();
                expenses_dividor += 1;
            }

        }
        budget_ave = budget_sum / budget_dividor;
        expense_ave = expenses_sum / expenses_dividor;
        neto_ave = budget_ave - expense_ave;
        neto_yearly = neto_ave * netoYearlyFactor;

    }

}
