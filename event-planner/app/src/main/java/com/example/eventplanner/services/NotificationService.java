package com.example.eventplanner.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.eventplanner.R;
import com.example.eventplanner.activities.HomeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class NotificationService extends FirebaseMessagingService {
    String TAG="NestoSeDesilo";
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Bundle bundle = remoteMessage.toIntent().getExtras();

        Object value = bundle.get("title");
        if(value==null)return;
        String title=value.toString();

        value = bundle.get("body");
        if(value==null)return;
        String body=value.toString();

        value = bundle.get("topic");
        if (value == null) return;
        String topic = value.toString();

        sendNotification(title, body, topic);
    }
    private void sendNotification(String messageTitle, String messageBody, String messageTopic) {
        FirebaseAuth mAuth=FirebaseAuth.getInstance();
        FirebaseUser user=mAuth.getCurrentUser();

        if (user != null) {

            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 , intent,
                    PendingIntent.FLAG_IMMUTABLE);
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            if(messageTopic.equals("PUPV_Category") || messageTopic.equals(mAuth.getCurrentUser().getUid()+"PUPZTopic")) {
                NotificationCompat.Builder notificationBuilder;
                notificationBuilder = new NotificationCompat.Builder(this, "123")
                                    .setSmallIcon(R.drawable.ic_add)
                                    .setContentTitle(messageTitle)
                                    .setContentText(messageBody)
                                    .setAutoCancel(true)
                                    .setSound(defaultSoundUri)
                                    .setContentIntent(pendingIntent);

                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(0 , notificationBuilder.build());

            }
            else if(messageTopic.equals("AdminTopic")){
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "123");
                builder.setContentTitle(messageTitle);
                builder.setContentText(messageBody);
                builder.setSmallIcon(R.drawable.ic_android);
                builder.setAutoCancel(true);
                builder.setSound(defaultSoundUri);

                NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(0, builder.build());
            }
            else if(messageTopic.equals("PUPV_Comment")){
                NotificationCompat.Builder notificationBuilder;
                notificationBuilder = new NotificationCompat.Builder(this, "123")
                        .setSmallIcon(R.drawable.ic_add)
                        .setContentTitle(messageTitle)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(0 , notificationBuilder.build());
            }
            else if(messageTopic.equals("PUPZ_Reservation")){
                NotificationCompat.Builder notificationBuilder;
                notificationBuilder = new NotificationCompat.Builder(this, "123")
                        .setSmallIcon(R.drawable.ic_add)
                        .setContentTitle(messageTitle)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(0 , notificationBuilder.build());
            }

            else if(messageTopic.equals(user.getUid()+"Topic")){
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "123");
                builder.setContentTitle(messageTitle);
                builder.setContentText(messageBody);
                builder.setSmallIcon(R.drawable.ic_android);
                builder.setAutoCancel(true);
                builder.setSound(defaultSoundUri);

                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                notificationManager.notify(0, builder.build());
            }
            else if(messageTopic.equals("Message")){
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "123");
                builder.setContentTitle(messageTitle);
                builder.setContentText(messageBody);
                builder.setSmallIcon(R.drawable.ic_android);
                builder.setAutoCancel(true);
                builder.setSound(defaultSoundUri);

                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                notificationManager.notify(0, builder.build());
            }
        }

//        if (user != null) {
//
//            if(user.getDisplayName().equals("PUPV")){
//                Intent intent = new Intent(this, HomeActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
//                        PendingIntent.FLAG_IMMUTABLE);
//                String channelId = user.getUid();
//                Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//
//                NotificationCompat.Builder notificationBuilder;
//                if(messageTitle.equals("Update on your report") || messageTitle.equals("Reservation update")){
//                    notificationBuilder = new NotificationCompat.Builder(this, channelId + "Topic");
//                    notificationBuilder.setContentTitle(messageTitle);
//                    notificationBuilder.setContentText(messageBody);
//                    notificationBuilder.setSmallIcon(R.drawable.ic_android);
//                    notificationBuilder.setAutoCancel(true);
//                    notificationBuilder.setSound(defaultSoundUri);
//                }
//                else{
//                    notificationBuilder = new NotificationCompat.Builder(this, channelId)
//                                    .setSmallIcon(R.drawable.ic_add)
//                                    .setContentTitle(messageTitle)
//                                    .setContentText(messageBody)
//                                    .setAutoCancel(true)
//                                    .setSound(defaultSoundUri)
//                                    .setContentIntent(pendingIntent);
//                }
//
//
//                NotificationManager notificationManager =
//                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//                // Since android Oreo notification channel is needed.
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    NotificationChannel channel = new NotificationChannel(channelId + "Topic",
//                            "Channel human readable title",
//                            NotificationManager.IMPORTANCE_DEFAULT);
//                    notificationManager.createNotificationChannel(channel);
//                }
//
//                notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
//            }
//            else if(user.getDisplayName().equals("ADMIN")){
//                if(messageTitle.equals("New company report") || messageTitle.equals("New user report")) {
//                    String channelId = "AdminChannel";
//                    Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//
//                    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
//                    builder.setContentTitle(messageTitle);
//                    builder.setContentText(messageBody);
//                    builder.setSmallIcon(R.drawable.ic_android);
//                    builder.setAutoCancel(true);
//                    builder.setSound(defaultSoundUri);
//
//                    NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
//
//                    NotificationManager notificationManager =
//                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                        NotificationChannel channel = new NotificationChannel(channelId, "AdminChannel", NotificationManager.IMPORTANCE_DEFAULT);
//                        notificationManager.createNotificationChannel(channel);
//                    }
//
//                    notificationManager.notify(0, builder.build());
//                }
//            }
//            else if(user.getDisplayName().equals("OD")){
//                String channelId = user.getUid() + "Topic";
//                Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//
//                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
//                builder.setContentTitle(messageTitle);
//                builder.setContentText(messageBody);
//                builder.setSmallIcon(R.drawable.ic_android);
//                builder.setAutoCancel(true);
//                builder.setSound(defaultSoundUri);
//
//                NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
//
//                NotificationManager notificationManager =
//                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    NotificationChannel channel = new NotificationChannel(channelId, "OD channel", NotificationManager.IMPORTANCE_DEFAULT);
//                    notificationManager.createNotificationChannel(channel);
//                }
//
//                notificationManager.notify(0, builder.build());
//            }
        }

    }

