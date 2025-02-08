package com.example.eventplanner.activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.eventplanner.R;
import com.example.eventplanner.databinding.ActivityUserInfoBinding;
import com.example.eventplanner.model.UserOD;
import com.example.eventplanner.model.UserPUPV;
import com.example.eventplanner.model.UserPUPZ;
import com.example.eventplanner.services.FCMHttpClient;
import com.example.eventplanner.services.NotificationService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class UserInfoActivity extends AppCompatActivity {

    ActivityUserInfoBinding binding;
    FirebaseFirestore db;
    FirebaseStorage storage;
    FirebaseAuth mAuth;
    FirebaseUser user;
    Object reportedUser;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        if (user.getDisplayName().equals("ADMIN")) {
            binding.reportOD.setVisibility(View.GONE);
        }

        userId = getIntent().getStringExtra("userId");

        getUser();

        if(!user.getDisplayName().equals("PUPV")){
            binding.reportOD.setVisibility(View.GONE);
        }

        binding.reportOD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = (LayoutInflater) UserInfoActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View popUpView = inflater.inflate(R.layout.popup_report_company, null);

                int width = ViewGroup.LayoutParams.MATCH_PARENT;
                int height = ViewGroup.LayoutParams.MATCH_PARENT;
                boolean focusable = true;
                PopupWindow popupWindow = new PopupWindow(popUpView, width, height, focusable);

                popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);

                TextInputLayout report = popUpView.findViewById(R.id.report);
                Button sendReport = popUpView.findViewById(R.id.send);

                sendReport.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Long id = new Random().nextLong();
                        Map<String, Object> doc = new HashMap<>();

                        doc.put("reporterId", user.getUid());
                        doc.put("reason", report.getEditText().getText().toString());
                        doc.put("reportedId", userId);
                        doc.put("dateOfReport", System.currentTimeMillis());
                        doc.put("status", "REPORTED");

                        db.collection("UserReports")
                                .document(id.toString())
                                .set(doc)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        popupWindow.dismiss();
                                        createNotification();
                                        Toast.makeText(UserInfoActivity.this, "Report sent successfully", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(UserInfoActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
            }
        });
    }

    private void createNotification(){
        Long id = new Random().nextLong();
        Map<String, Object> doc = new HashMap<>();

        String firstName = (reportedUser instanceof UserOD) ? ((UserOD) reportedUser).getFirstName() : ((UserPUPV) reportedUser).getFirstName();
        String lastName = (reportedUser instanceof UserOD) ? ((UserOD) reportedUser).getLastName() : ((UserPUPV) reportedUser).getLastName();

        doc.put("title", "New user report");
        doc.put("body", "User " + firstName + " " + lastName + " has been reported");
        doc.put("read", false);
        doc.put("userId", "e1ktzSoZY9ZdfEuL7PyShaRWI522");

        db.collection("Notifications")
                .document(id.toString())
                .set(doc)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        sendNotification();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UserInfoActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendNotification(){
        String serverKey="AAAA8GYmoZ8:APA91bHsjyzOSa2JtO_cQWFO-X1p9nMuHRO8DTfD1zhcY4mnqZ-2EZmIn8tMf1ISmnM31WB68Mzn2soeUgEISXlSc9WjRvcRhyYbmBgi7whJuYXX-24wkODByasquofLaMZydpg78esK";

        String firstName = (reportedUser instanceof UserOD) ? ((UserOD) reportedUser).getFirstName() : ((UserPUPV) reportedUser).getFirstName();
        String lastName = (reportedUser instanceof UserOD) ? ((UserOD) reportedUser).getLastName() : ((UserPUPV) reportedUser).getLastName();
        String jsonPayload = "{\"data\":{" +
                "\"title\":\"New user report\"," +
                "\"body\":\"User " + firstName + " " + lastName + " has been reported\"," +
                "\"topic\":\"AdminTopic\"" +
                "}," +
                "\"to\":\"/topics/" + "AdminTopic" + "\"}";

        FCMHttpClient httpClient = new FCMHttpClient();
        httpClient.sendMessageToTopic(serverKey, "AdminTopic", jsonPayload);
    }

    private void getUser(){
        db.collection("User")
                .document(userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        if(documentSnapshot.getString("UserType").equals("OD")) {
                            reportedUser = new UserOD(
                                    0l,
                                    documentSnapshot.getString("FirstName"),
                                    documentSnapshot.getString("LastName"),
                                    documentSnapshot.getString("E-mail"),
                                    documentSnapshot.getString("Password"),
                                    documentSnapshot.getString("Phone"),
                                    documentSnapshot.getString("Address"),
                                    documentSnapshot.getBoolean("IsValid")
                            );
                        }
                        else{
                            reportedUser = new UserPUPV(
                                    documentSnapshot.getString("FirstName"),
                                    documentSnapshot.getString("LastName"),
                                    documentSnapshot.getString("Email"),
                                    documentSnapshot.getString("Password"),
                                    documentSnapshot.getString("Phone"),
                                    documentSnapshot.getString("Address"),
                                    documentSnapshot.getBoolean("IsValid"),
                                    documentSnapshot.getString("CompanyName"),
                                    documentSnapshot.getString("CompanyDescription"),
                                    documentSnapshot.getString("CompanyAddress"),
                                    documentSnapshot.getString("CompanyEmail"),
                                    documentSnapshot.getString("CompanyPhone"),
                                    documentSnapshot.getString("WorkTime"));
                        }

                        userId = documentSnapshot.getId();

                        initializeComponents();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UserInfoActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void initializeComponents(){
        binding.name.getEditText().setText((reportedUser instanceof UserOD) ? ((UserOD) reportedUser).getFirstName() : ((UserPUPV) reportedUser).getFirstName());
        binding.lastname.getEditText().setText((reportedUser instanceof UserOD) ? ((UserOD) reportedUser).getLastName() : ((UserPUPV) reportedUser).getLastName());
        binding.email.getEditText().setText((reportedUser instanceof UserOD) ? ((UserOD) reportedUser).getEmail() : ((UserPUPV) reportedUser).getEmail());
        binding.phone.getEditText().setText((reportedUser instanceof UserOD) ? ((UserOD) reportedUser).getPhone() : ((UserPUPV) reportedUser).getPhone());
        binding.address.getEditText().setText((reportedUser instanceof UserOD) ? ((UserOD) reportedUser).getAddress() : ((UserPUPV) reportedUser).getAddress());

        StorageReference imageRef = storage.getReference().child("images/" + userId);
        imageRef.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(UserInfoActivity.this)
                                .load(uri)
                                .into(binding.profilePic);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UserInfoActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}