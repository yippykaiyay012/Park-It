package com.yippykaiyay.parkit;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.support.v7.app.NotificationCompat;

/**
 * Created by Ryan on 23/04/2016.
 */
public class AlertReceiver extends BroadcastReceiver {




    Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

    @Override
    public void onReceive(Context context, Intent intent) {

        createNotification(context, "Park-It! Timer", "Your Parking Has Expired! Avoid Those Tickets!", "Park-It! Timer");


    }

    private void createNotification(Context context, String msg, String msgText, String msgAlert) {

        PendingIntent notificationIntent = PendingIntent.getActivity(context,0, new Intent(context, MapMainActivity.class), 0);

        NotificationCompat.Builder mBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_mascot)
                .setContentTitle(msg)
                .setTicker(msgAlert)
                .setContentText(msgText)
                .setSound(alarmSound)
                .setVibrate(new long[]{0, 2000, 200, 2000, 1000})
                .setLights(Color.RED, 5000, 200);

        mBuilder.setContentIntent(notificationIntent);

        mBuilder.setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(1, mBuilder.build());




    }
}
