package halamish.reem.budget;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Created by Re'em on 11/23/2015.
 */
public abstract class MyAdapter<T> extends ArrayAdapter<T> {
    public MyAdapter(Context context, int resource, List<T> objects) {
        super(context, resource, objects);
    }

    public abstract void updateAdapter();
}
