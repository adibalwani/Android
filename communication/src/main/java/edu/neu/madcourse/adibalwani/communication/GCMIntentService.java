package edu.neu.madcourse.adibalwani.communication;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

public class GCMIntentService extends IntentService {

    private static final int NOTIFICATION_ID = 1;
    public static final String GCM_MESSAGE_RECEIVED = "GCM_MESSAGE_RECEIVED";
    public static final String GCM_MESSAGE_KEY = "GCM_MESSAGE_KEY";

    public GCMIntentService() {
        super("GCMIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (!extras.isEmpty()) {
            String message = extras.getString("message");
            if (message != null) {
                sendNotification(message);
            }
        }

        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GCMBroadcastReceiver.completeWakefulIntent(intent);
    }


    /**
     * Put the message into a notification and post it.
     *
     * @param message The message to put
     */
    private void sendNotification(String message) {
        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        notificationIntent.putExtra("show_response", "show_response");
        PendingIntent intent = PendingIntent.getActivity(
                this,
                0,
                new Intent(this, MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Received GCM Message")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentText(message)
                .setTicker(message)
                .setAutoCancel(true)
                .setContentIntent(intent);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
