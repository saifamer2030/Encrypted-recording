package app.com.smartrec;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by alexey on 13.04.17.
 */


public class SmartRecFirebaseNotiService extends FirebaseMessagingService {

    private static final String TAG = SmartRecFirebaseNotiService.class.getSimpleName();

    private static int notificationId = 0;

    private static final String POST_ID_KEY = "postId";
    private static final String AUTHOR_ID_KEY = "authorId";
    private static final String TITLE_KEY = "title";
    private static final String BODY_KEY = "body";
    private static final String ICON_KEY = "icon";
    private static final String ACTION_TYPE_NEW_POST = "new_post";
    private onlunchActivity onlunchActivity;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if (remoteMessage.getData() != null ) {

            handleRemoteMessage(remoteMessage);

        } else {
            Log.e(TAG, "onMessageReceived()", new RuntimeException("FCM remoteMessage doesn't contains Action Type"));
        }
    }

    private void handleRemoteMessage(RemoteMessage remoteMessage) {

        handleNewPostCreatedAction(remoteMessage);
    }

    private void handleNewPostCreatedAction(RemoteMessage remoteMessage) {
        String postAuthorId = remoteMessage.getData().get(AUTHOR_ID_KEY);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        //Send notification for each users except author of post.
        if (firebaseUser != null && !firebaseUser.getUid().equals(postAuthorId)) {
        }
    }

    public Bitmap getBitmapFromUrl(String imageUrl) {
        try {
            return Glide.with(this)
                    .load(imageUrl)
                    .asBitmap()
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(256, 256)
                    .get();

        } catch (Exception e) {
            Log.e(TAG, "getBitmapfromUrl", e);
            return null;
        }
    }

    private void sendNotification(String notificationTitle, String notificationBody, Bitmap bitmap, Intent intent) {
        sendNotification(notificationTitle, notificationBody, bitmap, intent, null);
    }

    private void sendNotification(String notificationTitle, String notificationBody, Bitmap bitmap, Intent intent, Intent backIntent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent;

        if(backIntent != null) {
            backIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Intent[] intents = new Intent[] {backIntent, intent};
            pendingIntent = PendingIntent.getActivities(this, notificationId++, intents, PendingIntent.FLAG_ONE_SHOT);
        } else {
            pendingIntent = PendingIntent.getActivity(this, notificationId++, intent, PendingIntent.FLAG_ONE_SHOT);
        }

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setAutoCancel(true)   //Automatically delete the notification
                .setSmallIcon(R.drawable.ic_keyboard_voice_black_24dp) //Notification icon
                .setContentIntent(pendingIntent)
                .setContentTitle(notificationTitle)
                .setContentText(notificationBody)
                .setLargeIcon(bitmap)
                .setSound(defaultSoundUri);


        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId++ /* ID of notification */, notificationBuilder.build());
    }

}
