package app.com.smartrec;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;

public class RecievedRecViewHolder extends RecyclerView.ViewHolder {
    public static final String TAG = RecievedRecViewHolder.class.getSimpleName();

    private TextView senderName, senderPhone, senderLoc, datesent;
    private ImageView recImage;
    private RecievedRecAdapter.Callback callback;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    private Context context;

    public RecievedRecViewHolder(View itemView, final RecievedRecAdapter.Callback callback) {
        super(itemView);

        this.callback = callback;
        this.context = itemView.getContext();

        senderName = itemView.findViewById(R.id.senderName);
        senderPhone = itemView.findViewById(R.id.senderPhonenumber);
        senderLoc = itemView.findViewById(R.id.senderLoc);
        datesent = itemView.findViewById(R.id.senddate);
        recImage = itemView.findViewById(R.id.recImage);

        if (callback != null) {
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        callback.onLongItemClick(v, position);
                        return true;
                    }

                    return false;
                }
            });
        }
    }

    public void bindData(final recRecievedModel recRecievedModel) {

        final String authorId = recRecievedModel.getSenderUId();

        if (authorId != null)
            getProfileSingleValue(authorId, createOnProfileChangeListener(senderName, senderPhone));

        senderLoc.setText(recRecievedModel.getSenderLoc2() + " , " + recRecievedModel.getSenderLoc());

        CharSequence date = FormatterUtil.getRelativeTimeSpanString(context, recRecievedModel.getSendDate());
        datesent.setText(date);

        recImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    callback.recStream(recRecievedModel.getRecCloudPath(), recRecievedModel.getRecEncryKey(), v);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void getProfileSingleValue(String id, final OnObjectChangedListener<recUserModel> listener) {
        DatabaseReference databaseReference = database.getReference().child("smartrec-users").child(id);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                recUserModel profile = dataSnapshot.getValue(recUserModel.class);
                listener.onObjectChanged(profile);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "getProfileSingleValue(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });
    }

    private OnObjectChangedListener<recUserModel> createOnProfileChangeListener(final TextView senderName, final TextView senderPhone) {
        return new OnObjectChangedListener<recUserModel>() {
            @Override
            public void onObjectChanged(recUserModel obj) {
                String fullname = obj.getFullname();
                String phonenumber = obj.getPhonenumber();
                senderName.setText(fullname);
                senderPhone.setText(phonenumber);
            }
        };
    }
}
