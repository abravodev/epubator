package it.iiizio.epubator.presentation.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import it.iiizio.epubator.R;

import static android.content.Context.NOTIFICATION_SERVICE;

public class NotificationHelper {

    public static Notification makeNotification(Context context,
            String title, String text, boolean fixed,
            Intent tappedIntent){

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                tappedIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_stat_app)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setContentIntent(contentIntent)
                .setOngoing(fixed)
                .setAutoCancel(!fixed)
                .build();
    }

    public static void sendNotification(Context context,
            String title, String text, boolean fixed,
            Intent tappedIntent){

        Notification notification = makeNotification(context, title, text, fixed, tappedIntent);
        sendNotification(context, R.string.app_name, notification);
    }

    public static void sendNotification(Context context, int notificationId, Notification notification){
        getNotificationManager(context).notify(notificationId, notification);
    }

    private static NotificationManager getNotificationManager(Context context){
        return (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
    }

}
