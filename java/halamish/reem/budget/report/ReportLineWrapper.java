package halamish.reem.budget.report;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import halamish.reem.budget.misc.Logger;

/**
 * Created by Re'em on 5/2/2016.
 * used to wrap the report lines in the activity
 */
class ReportLinesWrapper implements Serializable {
    private List<ReportLine> actualLines;
    private ReportLineHeader currentlySortedBy;
    ReportLinesWrapper(List<ReportLine> lines) {
        actualLines = lines;
        currentlySortedBy = ReportLineHeader.NOTHING;
    }
    public void sort(ReportLineHeader parameter) {
        boolean onlyFlip = (parameter == currentlySortedBy);
        if (onlyFlip) {
            Collections.reverse(actualLines);
            return;
        }
        currentlySortedBy = parameter;
        ReportLine.sort(actualLines, parameter);
    }

    public int getSize() {
        return actualLines.size();
    }

    public List<ReportLine> getActualLines() {
        return actualLines;
    }
}
