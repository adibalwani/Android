package edu.neu.madcourse.adibalwani.twoplayerwordgame;

import android.app.Activity;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

public class GCMIntentService extends IntentService {

    public static final int NOTIFICATION_ID = 1;

    public GCMIntentService() {
        super("GCMIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (!extras.isEmpty()) {
            String gameData = extras.getString("gamestate");
            String wait = extras.getString("wait");
            String opponent = extras.getString("opponent");
            if (gameData != null && !gameData.equals("null") && wait != null && opponent != null) {
                getSharedPreferences(GamePlayManager.PREF_FILE, GameActivity.MODE_PRIVATE).edit()
                        .putString(GamePlayManager.KEY_RESTORE, gameData)
                        .putBoolean(GamePlayManager.KEY_WAIT, Boolean.valueOf(wait))
                        .commit();
                getSharedPreferences(RegisterManager.PREF_FILE, Activity.MODE_PRIVATE).edit()
                        .putString(RegisterManager.PROPERTY_OPPONENT, opponent)
                        .commit();
                sendNotification("Continue the game", "Time to take your turn!!");
            } else if (gameData != null && gameData.equals("null") && wait != null && opponent != null) {
                getSharedPreferences(GamePlayManager.PREF_FILE, GameActivity.MODE_PRIVATE).edit()
                        .putString(GamePlayManager.KEY_RESTORE, null)
                        .commit();
                getSharedPreferences(RegisterManager.PREF_FILE, Activity.MODE_PRIVATE).edit()
                        .putString(RegisterManager.PROPERTY_OPPONENT, null)
                        .commit();
                String score = extras.getString("score");
                sendNotification("Game Ended", "You scored : " + score);
            }
        }

        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GCMBroadcastReceiver.completeWakefulIntent(intent);
    }


    /**
     * Put the message into a notification and post it.
     *
     * @parama title The title to put
     * @param message The message to put
     */
    private void sendNotification(String title, String message) {
        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent(this, GameActivity.class);
        if (title.equals("Game Ended")) {
            notificationIntent = new Intent(this, MainActivity.class);
        }
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationIntent.putExtra(Constants.INTENT_KEY_RESTORE, true);
        PendingIntent intent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentText(message)
                .setTicker(title)
                .setAutoCancel(true)
                .setContentIntent(intent);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
