package com.example.mymusicplayer;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class Notification {

    NotificationChannel musicChannel;
    Context mainContext;
    String channelID = "musicInfo";
    NotificationCompat.Builder builder;
    Intent notificationIntent;

    Notification(Intent notificationIntent, Context mainContext){
        this.mainContext = mainContext;
        this.notificationIntent = notificationIntent;
    }

    NotificationCompat.Builder createBuilder(String currPlayingSongName){
        builder = new NotificationCompat.Builder(mainContext, channelID)
                .setSmallIcon(R.drawable.ic_music)
                .setAutoCancel(false)
                .setContentTitle(currPlayingSongName)
                .setContentText("Uttkarsh");
        return builder;
    }

    void createChannel(){
        // This doesn't need to return something, it creates a channel and that's it.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = "Music Info";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            String description = "Shows you the information of the current track being played on My music app";

            NotificationChannel channel = new NotificationChannel(channelID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = mainContext.getSystemService(NotificationManager.class);

            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
        }
    }

    void showNotification(int notificationId, String currPlayingSongName, String currPlayingSongArtist){
        createChannel();
        createBuilder(currPlayingSongName);

        PendingIntent pendingIntent = PendingIntent.getActivity(mainContext, 0, notificationIntent, 0);


//        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(mainContext);
//        notificationManagerCompat.notify(notificationId, builder.build());
    }

}
