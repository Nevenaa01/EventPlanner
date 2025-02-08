package com.example.eventplanner.activities;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventplanner.R;
import com.example.eventplanner.adapters.NotificationAdapter;
import com.example.eventplanner.databinding.ActivityNotificationsViewBinding;
import com.example.eventplanner.model.Notification;
import com.example.eventplanner.services.ShakeDetector;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class NotificationsViewActivity extends AppCompatActivity implements ShakeDetector.OnShakeListener{

    ActivityNotificationsViewBinding binding;
    ArrayList<Notification> notifications;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    FirebaseUser user;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;
    private String firstForShake;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationsViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        notifications = new ArrayList<>();
        firstForShake = "unread";

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector(this);

        getNotifictions();

        binding.read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NotificationAdapter notificationAdapter = new NotificationAdapter(NotificationsViewActivity.this,
                        R.layout.notification_card,
                        new ArrayList<>(notifications.stream().filter(n -> n.getRead()).collect(Collectors.toList())));
                binding.notificationList.setAdapter(notificationAdapter);
            }
        });

        binding.unread.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NotificationAdapter notificationAdapter = new NotificationAdapter(NotificationsViewActivity.this,
                        R.layout.notification_card,
                        new ArrayList<>(notifications.stream().filter(n -> !n.getRead()).collect(Collectors.toList())));
                binding.notificationList.setAdapter(notificationAdapter);
            }
        });

        binding.all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NotificationAdapter notificationAdapter = new NotificationAdapter(NotificationsViewActivity.this,
                        R.layout.notification_card,
                        notifications);
                binding.notificationList.setAdapter(notificationAdapter);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(mShakeDetector);
    }

    @Override
    public void onShake(int count) {
        if(firstForShake.equals("unread")){
            NotificationAdapter notificationAdapter = new NotificationAdapter(NotificationsViewActivity.this,
                    R.layout.notification_card,
                    new ArrayList<>(notifications.stream().filter(n -> !n.getRead()).collect(Collectors.toList())));
            binding.notificationList.setAdapter(notificationAdapter);
            binding.unread.setChecked(true);

            firstForShake = "read";
        }
        else if(firstForShake.equals("read")){
            NotificationAdapter notificationAdapter = new NotificationAdapter(NotificationsViewActivity.this,
                    R.layout.notification_card,
                    new ArrayList<>(notifications.stream().filter(n -> n.getRead()).collect(Collectors.toList())));
            binding.notificationList.setAdapter(notificationAdapter);
            binding.read.setChecked(true);

            firstForShake = "all";
        }
        else{
            NotificationAdapter notificationAdapter = new NotificationAdapter(NotificationsViewActivity.this,
                    R.layout.notification_card,
                    notifications);
            binding.notificationList.setAdapter(notificationAdapter);
            binding.all.setChecked(true);

            firstForShake = "unread";
        }

    }

    private void getNotifictions(){
        db.collection("Notifications")
                .whereEqualTo("userId", user.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(DocumentSnapshot doc : queryDocumentSnapshots){
                            Notification notification = new Notification(
                                    Long.parseLong(doc.getId()),
                                    doc.getString("title"),
                                    doc.getString("body"),
                                    doc.getBoolean("read"),
                                    doc.getString("userId")
                            );

                            notifications.add(notification);
                        }

                        NotificationAdapter notificationAdapter = new NotificationAdapter(NotificationsViewActivity.this, R.layout.notification_card, notifications);
                        binding.notificationList.setAdapter(notificationAdapter);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(NotificationsViewActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}