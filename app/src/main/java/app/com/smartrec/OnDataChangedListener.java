package app.com.smartrec;

import java.util.List;

public interface OnDataChangedListener<T> {

    public void onListChanged(List<T> list);
}
