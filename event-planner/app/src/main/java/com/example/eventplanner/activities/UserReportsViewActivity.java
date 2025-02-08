package com.example.eventplanner.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventplanner.R;
import com.example.eventplanner.adapters.ReportedUsersAdapter;
import com.example.eventplanner.databinding.ActivityUserInfoBinding;
import com.example.eventplanner.databinding.ActivityUserReportsViewBinding;
import com.example.eventplanner.model.UserReport;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

public class UserReportsViewActivity extends AppCompatActivity {

    ActivityUserReportsViewBinding binding;
    ArrayList<UserReport> reports;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserReportsViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        reports = new ArrayList<>();

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        getReports();
    }

    private void initializeComponents(){
        ReportedUsersAdapter reportReportedUsersAdapter = new ReportedUsersAdapter(UserReportsViewActivity.this, R.layout.user_report_card, reports);
        binding.reportList.setAdapter(reportReportedUsersAdapter);
    }

    private void getReports(){
        db.collection("UserReports")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(DocumentSnapshot doc : queryDocumentSnapshots){
                            UserReport report = new UserReport(
                                    Long.parseLong(doc.getId()),
                                    doc.getString("reportedId"),
                                    doc.getString("reporterId"),
                                    doc.getString("reason"),
                                    doc.getLong("dateOfReport"),
                                    convertToStatus(doc.getString("status"))
                            );

                            reports.add(report);
                        }

                        initializeComponents();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UserReportsViewActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private UserReport.Status convertToStatus(String status){
        switch (status){
            case "REPORTED":
                return UserReport.Status.REPORTED;
            case "APPROVED":
                return UserReport.Status.APPROVED;
            case "DENIED":
                return UserReport.Status.DENIED;
        }

        return UserReport.Status.REPORTED;
    }
}