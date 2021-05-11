package com.arhiser.todolist;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.arhiser.todolist.screens.details.NoteDetailsActivity;
import com.arhiser.todolist.screens.main.Adapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class EgorService extends Service{

    public static final String CHANNEL_ORG_ID = "channelorg";
    NotificationChannel OrgChannel;
    NotificationManager OrgNotificationManager;
    public int CurrentInterval;

    public void onCreate() {super.onCreate();}

    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            OrgChannel = new NotificationChannel(CHANNEL_ORG_ID, "Channel Org", NotificationManager.IMPORTANCE_HIGH);
            OrgChannel.setDescription("Organizer");
            OrgNotificationManager = getSystemService(NotificationManager.class);
            OrgNotificationManager.createNotificationChannel(OrgChannel);
        }

        Intent NotificationIntent = new Intent(this, Adapter.class);
        NotificationIntent.putExtra("Notification", "ON");
        PendingIntent PendingNotificationIntent = PendingIntent.getActivity(this, 1, NotificationIntent, 0);

        Notification OrgNotification = new NotificationCompat.Builder(this, CHANNEL_ORG_ID)
                .setAutoCancel(true)
                .setTicker("Organizer")
                .setContentTitle(intent.getStringExtra("Objective Name"))
                .setContentText(intent.getStringExtra("Objective Text"))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(PendingNotificationIntent)
                .setOngoing(false)
                .build();
        startForeground(1, OrgNotification);

        return START_NOT_STICKY;
    }
}