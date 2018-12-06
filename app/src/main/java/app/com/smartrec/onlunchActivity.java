package app.com.smartrec;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by ${cosmic} on 7/2/18.
 */
public class onlunchActivity extends AppCompatActivity {
    private static final String TAG = onlunchActivity.class.getSimpleName();

    private SharedPrefManager sharedPrefManager;
    private RequestPermissionHandler mRequestPermissionHandler;
    Context context = this;
    private FirebaseAuth mAuth;

    private static final int REQUEST_CODE_PICK_CONTACTS = 1;
    private Uri uriContact;
    private String contactID;

    private RelativeLayout auth_rl, no_rec_rl, yes_rec_rl, rec_state_opt, cir_rl;
    private LinearLayout loc_lyt;
    private TextView rec_signup, rec_login, loc_view, loc_view1, rec_auto_yes, rec_auto_no, contact_added_details;
    private ImageView cir_btn, mic_in_nosetlyt, mic_in_yessetlyt, opt_reset;
    private RecyclerView recyclerView;
    private RecievedRecAdapter recievedRecAdapter;

    private boolean attemptToLoadProfiles = false;

    private FusedLocationProviderClient mFusedLocationClient;

    protected Location mLastLocation;

    private String mFilename = null;

    private String mLatitudeLabel;
    private String mLongitudeLabel;

    private String Contact_name, Contact_number;

    private FirebaseDatabase firebaseDatabase;
    private String UserID;

    private int contactdbCounts;

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private static final int RECLIMITTIME = 3000;

    private static final String ADDRESS_REQUESTED_KEY = "address-request-pending";
    private static final String LOCATION_ADDRESS_KEY = "location-address";

    private boolean mAddressRequested;
    private String mAddressOutput;

    private ProgressBar mProgressBar;
    private TextView mLocationAddressTextView;

    public static final int SAMPLE_RATE = 16000;
    private AudioRecord mRecorder;
    private File mRecording;
    private short[] mBuffer;
    private boolean mIsRecording = false;
    private android.os.Handler Handler = new Handler();


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.onlunch_settings);

        sharedPrefManager = new SharedPrefManager(context);
        mRequestPermissionHandler = new RequestPermissionHandler();

        handleButtonClicked();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        firebaseDatabase = FirebaseDatabase.getInstance();

        mAuth = FirebaseAuth.getInstance();
        final SmartRecBackend smartRecBackend = new SmartRecBackend(this);

        auth_rl = findViewById(R.id.auth_layout);
        no_rec_rl = findViewById(R.id.no_rec_layout);
        yes_rec_rl = findViewById(R.id.yes_rec_layout);
        cir_rl = findViewById(R.id.circle_layout);
        rec_signup = findViewById(R.id.rec_signup);
        rec_login = findViewById(R.id.rec_login);
        loc_view = findViewById(R.id.latitude_text);
        loc_view1 = findViewById(R.id.longitude_text);
        loc_lyt = findViewById(R.id.loc_layout);
        rec_auto_no = findViewById(R.id.onlunch_rec_no);
        rec_auto_yes = findViewById(R.id.onlunch_rec_yes);
        rec_state_opt = findViewById(R.id.option_rec_layout);
        cir_btn = findViewById(R.id.add_to_circle);
        mic_in_nosetlyt = findViewById(R.id.mic_in_nolayout);
        mic_in_yessetlyt = findViewById(R.id.mic_in_yeslayout);
        recyclerView = findViewById(R.id.recievedRec_views);
        mProgressBar = findViewById(R.id.recievedRec_loader);
        contact_added_details = findViewById(R.id.total_contact_added_details);
        opt_reset = findViewById(R.id.option_reset_btn);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        View view = new View(this);

        if (user != null){
            UserID = user.getUid();
            initRecyclerView();
            contactsdetails(view);
        }
        else
            initRecorder();

        opt_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rec_state_opt.setVisibility(View.VISIBLE);
                yes_rec_rl.setVisibility(View.GONE);
                no_rec_rl.setVisibility(View.GONE);
            }
        });

        // Set defaults, then update using values stored in the Bundle.
        mAddressRequested = false;

        rec_auto_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPrefManager.saverecodrdingState(context, "yes");
                rec_state_opt.setVisibility(View.GONE);
                yes_rec_rl.setVisibility(View.VISIBLE);
                mIsRecording = true;
                smartRecBackend.startRecording();
                Handler = new Handler();
                Runnable r = new Runnable() {
                    public void run() {
                        smartRecBackend.stopRecording();
                        no_rec_rl.setVisibility(View.VISIBLE);
                        yes_rec_rl.setVisibility(View.GONE);
                        mIsRecording = false;
                    }
                };

                Handler.postDelayed(r, RECLIMITTIME);
            }
        });

        rec_auto_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPrefManager.saverecodrdingState(context, "no");
                rec_state_opt.setVisibility(View.GONE);
                no_rec_rl.setVisibility(View.VISIBLE);
            }
        });

        if (sharedPrefManager.getrecodrdingState().equals("yes")){
            rec_state_opt.setVisibility(View.GONE);
            no_rec_rl.setVisibility(View.GONE);
            yes_rec_rl.setVisibility(View.VISIBLE);

            mIsRecording = true;
            smartRecBackend.startRecording();
            Handler = new Handler();
            Runnable r = new Runnable() {
                public void run() {
                    smartRecBackend.stopRecording();
                    no_rec_rl.setVisibility(View.VISIBLE);
                    yes_rec_rl.setVisibility(View.GONE);
                    mIsRecording = false;
                }
            };

            Handler.postDelayed(r, RECLIMITTIME);

        } else if (sharedPrefManager.getrecodrdingState().equals("no")){
            rec_state_opt.setVisibility(View.GONE);
            yes_rec_rl.setVisibility(View.GONE);
            no_rec_rl.setVisibility(View.VISIBLE);
        }

        mic_in_nosetlyt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                no_rec_rl.setVisibility(View.GONE);
                yes_rec_rl.setVisibility(View.VISIBLE);
                mIsRecording = true;
                smartRecBackend.startRecording();

                Handler = new Handler();
                Runnable r = new Runnable() {
                    public void run() {
                        smartRecBackend.stopRecording();
                        no_rec_rl.setVisibility(View.VISIBLE);
                        yes_rec_rl.setVisibility(View.GONE);
                        mIsRecording = false;
                    }
                };
                Handler.postDelayed(r, RECLIMITTIME);
            }
        });

        mic_in_yessetlyt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsRecording = false;
                smartRecBackend.stopRecording();
                no_rec_rl.setVisibility(View.VISIBLE);
                yes_rec_rl.setVisibility(View.GONE);
            }
        });

        if (user != null) {
            auth_rl.setVisibility(View.GONE);
            loc_lyt.setVisibility(View.VISIBLE);
        }

        rec_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(onlunchActivity.this, SignupAuth.class));
                finish();
            }
        });

        rec_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(onlunchActivity.this, LoginAuth.class));
                finish();
            }
        });

        cir_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickSelectContact();
            }
        });
    }

    private void handleButtonClicked() {
        mRequestPermissionHandler.requestPermission(this, new String[]{
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
        }, 123, new RequestPermissionHandler.RequestPermissionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailed() {
                Toast.makeText(onlunchActivity.this, "request permission failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!checkPermissions()) {
            handleButtonClicked();
        } else {
            getLastLocation();
        }
    }

    public void onClickSelectContact() {
        startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), REQUEST_CODE_PICK_CONTACTS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_CONTACTS && resultCode == RESULT_OK) {
            Log.d(TAG, "Response: " + data.toString());
            uriContact = data.getData();

            if(UserID != null){
                retrieveContactName();
                retrieveContactNumber();
                Log.d(TAG, "contant_details: " + Contact_name + " " + Contact_number);
                HashMap<String, Object> contactCircles = new HashMap<>();
                contactCircles.put(Contact_name, ConatactNum_util(Contact_number));
                firebaseDatabase.getReference("smartrec-users").child(UserID).child("ContactCircle").updateChildren(contactCircles);
            }else {
                Toast.makeText(context, "Please sign in or login first to add contact", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public String ConatactNum_util(String contact){
        String newContact = "";
        if(!contact.contains("+234")){
            char[] contactchar = contact.toCharArray();
            contactchar[0] = ' ';
            newContact = "+234" + String.valueOf(contactchar).replace(" ", "");
        }
        else{
            return  contact;
        }
        return newContact;
    }

    @SuppressWarnings("MissingPermission")
    private void getLastLocation() {
        mFusedLocationClient.getLastLocation()
                .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            mLastLocation = task.getResult();

                            loc_view.setText(String.format(Locale.ENGLISH, "%s: %f",
                                    "Latitude",
                                    mLastLocation.getLatitude()));
                            recAudioModel.getIntance().setLat(String.valueOf(mLastLocation.getLatitude()));
                            loc_view1.setText(String.format(Locale.ENGLISH, "%s: %f",
                                    "Longitude",
                                    mLastLocation.getLongitude()));
                            recAudioModel.getIntance().setLong(String.valueOf(mLastLocation.getLongitude()));
                        } else {
                            Log.w(TAG, "getLastLocation:exception", task.getException());
                            //showSnackbar(getString(R.string.no_location_detected));
                        }
                    }
                });
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void retrieveContactNumber() {

        String contactNumber = null;
        // getting contacts ID
        Cursor cursorID = context.getContentResolver().query(uriContact, new String[]{ContactsContract.Contacts._ID}, null, null, null);

        if (cursorID.moveToFirst()) {
            contactID = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));
        }

        cursorID.close();
        // Using the contact ID now we will get contact phone number
        Cursor cursorPhone = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},

                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                        ContactsContract.CommonDataKinds.Phone.TYPE_HOME +
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE +
                        ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK +
                        ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME +
                        ContactsContract.CommonDataKinds.Phone.TYPE_PAGER +
                        ContactsContract.CommonDataKinds.Phone.TYPE_OTHER +
                        ContactsContract.CommonDataKinds.Phone.TYPE_CALLBACK +
                        ContactsContract.CommonDataKinds.Phone.TYPE_CAR +
                        ContactsContract.CommonDataKinds.Phone.TYPE_COMPANY_MAIN +
                        ContactsContract.CommonDataKinds.Phone.TYPE_OTHER_FAX +
                        ContactsContract.CommonDataKinds.Phone.TYPE_RADIO +
                        ContactsContract.CommonDataKinds.Phone.TYPE_TELEX +
                        ContactsContract.CommonDataKinds.Phone.TYPE_TTY_TDD +
                        ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE +
                        ContactsContract.CommonDataKinds.Phone.TYPE_WORK_PAGER +
                        ContactsContract.CommonDataKinds.Phone.TYPE_ASSISTANT +
                        ContactsContract.CommonDataKinds.Phone.TYPE_MMS,

                new String[]{contactID},
                null);

        while (cursorPhone.moveToNext()){
            contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA));
            Contact_number = contactNumber;
        }

        cursorPhone.close();
    }

    private void retrieveContactName() {

        String contactName = null;
        // querying contact data store
        Cursor cursor = getContentResolver().query(uriContact, null, null, null, null);
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        }

        cursor.close();
        Contact_name = contactName;

    }

    @Override
    public void onDestroy() {
        //mRecorder.release();
        super.onDestroy();
    }

    private void initRecorder() {
        int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        mBuffer = new short[bufferSize];
        mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize);
    }

    private void initRecyclerView() {

        recievedRecAdapter = new RecievedRecAdapter();
        recievedRecAdapter.setCallback(new RecievedRecAdapter.Callback() {
            @Override
            public void onLongItemClick(View view, int position) {

            }

            @Override
            public void recStream(final String recIDtoStream, final String recStreamKey, View view) throws IOException {
                if (recIDtoStream != null){
                    SmartRecBackend smartRecBackend = new SmartRecBackend(context);
                    smartRecBackend.StreamRec(recIDtoStream);
                }
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(recievedRecAdapter);
        recyclerView.setNestedScrollingEnabled(false);

        getRecRecievedList(createOnProfilesChangedDataListener());
    }

    private OnDataChangedListener<recRecievedModel> createOnProfilesChangedDataListener() {
        attemptToLoadProfiles = true;

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (attemptToLoadProfiles) {
                    mProgressBar.setVisibility(View.GONE);

                }
            }
        }, 30000);

        return new OnDataChangedListener<recRecievedModel>() {
            @Override
            public void onListChanged(List<recRecievedModel> list) {
                attemptToLoadProfiles = false;
                mProgressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                recievedRecAdapter.setList(list);
            }
        };
    }

    public void contactsdetails(View view){
        firebaseDatabase.getReference("smartrec-users").child(UserID).child("ContactCircle").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                contactdbCounts = Integer.parseInt(String.valueOf(dataSnapshot.getChildrenCount()));
                //Log.i(TAG, String.valueOf(contactdbCounts));
                contact_added_details.setText("Total contacts you added to your cloud - ".concat(String.valueOf(contactdbCounts)));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void getRecRecievedList(final OnDataChangedListener<recRecievedModel> onDataChangedListener) {
        DatabaseReference databaseReference = firebaseDatabase.getReference().child("smartrec-users").child(UserID).child("recievedEnRec");
        ValueEventListener valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                recRecievedModel recRecievedModel = new recRecievedModel();
                List<recRecievedModel> list = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    recRecievedModel.setRecFilename(snapshot.child("RecFilename").getValue().toString());
                    recRecievedModel.setRecCloudPath(snapshot.child("SentRec").getValue().toString());
                    recRecievedModel.setRecEncryKey(snapshot.child("RecKey").getValue().toString());
                    recRecievedModel.setSenderUId(snapshot.child("senderUid").getValue().toString());
                    recRecievedModel.setSendDate(Long.parseLong(snapshot.child("sentDate").getValue().toString()));
                    recRecievedModel.setSenderLoc(snapshot.child("senderLongitude").getValue().toString());
                    recRecievedModel.setSenderLoc2(snapshot.child("senderLatitude").getValue().toString());
                    list.add(recRecievedModel);
                }

                Collections.sort(list, new Comparator<recRecievedModel>() {
                    @Override
                    public int compare(recRecievedModel lhs, recRecievedModel rhs) {
                        return ((Long) rhs.getSendDate()).compareTo((Long) lhs.getSendDate());
                    }
                });
                onDataChangedListener.onListChanged(list);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "getCommentsList(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });
        //activeListeners.put(valueEventListener, databaseReference);
    }


    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(ADDRESS_REQUESTED_KEY, mAddressRequested);
        savedInstanceState.putString(LOCATION_ADDRESS_KEY, mAddressOutput);
        super.onSaveInstanceState(savedInstanceState);
    }

    private void showSnackbar(final String text) {
        View container = findViewById(android.R.id.content);
        if (container != null) {
            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
        }
    }

    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mRequestPermissionHandler.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
            } else {

                showSnackbar(R.string.permission_denied_explanation, R.string.settings,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
        }
    }
}
