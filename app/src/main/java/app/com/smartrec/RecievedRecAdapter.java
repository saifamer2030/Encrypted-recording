package app.com.smartrec;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RecievedRecAdapter extends RecyclerView.Adapter<RecievedRecViewHolder> {
    private List<recRecievedModel> list = new ArrayList<>();
    private Callback callback;

    @Override
    public RecievedRecViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recieved_rec_items, parent, false);
        return new RecievedRecViewHolder(view, callback);
    }

    @Override
    public void onBindViewHolder(RecievedRecViewHolder holder, int position) {
        holder.itemView.setLongClickable(true);
        holder.bindData(getItemByPosition(position));
    }

    public recRecievedModel getItemByPosition(int position) {
        return list.get(position);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void setList(List<recRecievedModel> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface Callback {

        void onLongItemClick(View view, int position);

        void recStream(String recIDtoStream, String streamKey, View view) throws IOException;
    }
}
