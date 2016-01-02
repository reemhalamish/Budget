package halamish.reem.budget;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Created by Re'em on 11/23/2015.
 *
 * base class for the adapters s.t. the db handler can call updateAdapter() when needed
 */
public abstract class MyAdapter<T> extends ArrayAdapter<T> {
    public MyAdapter(Context context, int resource, List<T> objects) {
        super(context, resource, objects);
    }
    protected boolean _multiSelectMode = false;

    public abstract void updateAdapter();

    public void startMultiSelect() {_multiSelectMode = true;}
    public void endMultiSelect() {_multiSelectMode = false;}
}
