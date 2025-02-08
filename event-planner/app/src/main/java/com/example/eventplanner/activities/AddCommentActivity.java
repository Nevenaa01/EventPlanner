package com.example.eventplanner.activities;

import android.content.Intent;
import android.opengl.Visibility;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventplanner.R;
import com.example.eventplanner.databinding.ActivityAddCommentBinding;
import com.example.eventplanner.databinding.ActivityOwnerDashboardBinding;
import com.example.eventplanner.model.Comment;
import com.example.eventplanner.model.Report;
import com.example.eventplanner.model.ServiceReservationRequest;
import com.example.eventplanner.model.UserPUPV;
import com.example.eventplanner.model.UserPUPZ;
import com.example.eventplanner.services.FCMHttpClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class AddCommentActivity extends AppCompatActivity {

    private LinearLayout companiesContainer;
    ActivityAddCommentBinding binding;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    String serverKey="AAAA8GYmoZ8:APA91bHsjyzOSa2JtO_cQWFO-X1p9nMuHRO8DTfD1zhcY4mnqZ-2EZmIn8tMf1ISmnM31WB68Mzn2soeUgEISXlSc9WjRvcRhyYbmBgi7whJuYXX-24wkODByasquofLaMZydpg78esK";
    public static void sendMessage(String serverKey, String jsonPayload) {
        FCMHttpClient httpClient = new FCMHttpClient();
        httpClient.sendMessageToTopic(serverKey, "PUPV", jsonPayload);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_comment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding= ActivityAddCommentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        companiesContainer = binding.companiesContainer;

        binding.backBtn.setOnClickListener(v->{
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        });

        retrieveCompaniesByLoggedUserId(mAuth.getCurrentUser() == null? "error" : mAuth.getCurrentUser().getUid());
    }

    private void retrieveCompaniesByLoggedUserId(String userId){
        Set<String> workerIds = new HashSet<>();

        db.collection("ServiceReservationRequest")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        ServiceReservationRequest reservation = documentSnapshot.toObject(ServiceReservationRequest.class);

                        if(reservation.getStatus().equals("APPROVED") || reservation.getStatus().contains("DENIED"))
                            workerIds.add(reservation.getWorkerId());
                    }

                    workerIds.forEach(id -> {
                        db.collection("User")
                                .whereEqualTo("id", Double.parseDouble(id))
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshotss -> {
                                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshotss) {
                                        db.collection("User")
                                                .document(documentSnapshot.toObject(UserPUPZ.class).getOwnerId())
                                                .get()
                                                .addOnSuccessListener(document ->{
                                                    displayCompany(
                                                            document.getString("CompanyName"),
                                                            document.getString("CompanyAddress"),
                                                            document.getString("CompanyEmail"),
                                                            document.getId());
                                                });
                                        break;
                                    }
                                });
                    });
                });
    }
    private void displayCompany(String companyName, String companyAdress, String Email, String documentCompanyId){
        View cardView = LayoutInflater.from(this).inflate(R.layout.company_card, companiesContainer, false);

        TextView companyNmae = cardView.findViewById(R.id.company_name_value);
        companyNmae.setText(companyName);

        TextView companyAddress = cardView.findViewById(R.id.company_location_value);
        companyAddress.setText(companyAdress);

        TextView companyEmail = cardView.findViewById(R.id.company_email_value);
        companyAddress.setText(Email);

        ImageButton showAddCommentBtn = cardView.findViewById(R.id.show_add_comment_btn);
        LinearLayout commentContainer = cardView.findViewById(R.id.comment_container);
        showAddCommentBtn.setOnClickListener(v ->{
            commentContainer.setVisibility(View.VISIBLE);
        });

        Button addCommentBtn = cardView.findViewById(R.id.add_comment_btn);
        EditText description = cardView.findViewById(R.id.comment_description_value);
        EditText grade = cardView.findViewById(R.id.comment_grade_value);
        addCommentBtn.setOnClickListener(v -> {
                Date currentDate = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

                Comment comment = new Comment(
                        grade.getText().toString(),
                        description.getText().toString(),
                        dateFormat.format(currentDate),
                        "PENDING",
                        new Report(),
                        mAuth.getCurrentUser().getUid(),
                        documentCompanyId
                );

            db.collection("Comment")
                    .add(comment)
                    .addOnSuccessListener(documentReference -> {
                        description.setText("");
                        grade.setText("");
                        commentContainer.setVisibility(View.GONE);
                        Toast.makeText(this, "Successfuly sent comment!", Toast.LENGTH_SHORT).show();

                        String jsonPayload = "{\"data\":{" +
                                "\"title\":\"New comment!\"," +
                                "\"body\":\"" + comment.getDescription() + "\"," +
                                "\"topic\":\"PUPV_Comment\"" +
                                "}," +
                                "\"to\":\"/topics/" + documentCompanyId + "Topic" +  "\"}";
                        sendMessage(serverKey,jsonPayload);
                        addNotification(comment, documentCompanyId);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error adding comment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        companiesContainer.addView(cardView);
    }

    private void addNotification(Comment comment, String reciever){
        Long id = new Random().nextLong();
        Map<String,Object> map=new HashMap<>();
        map.put("body",comment.getDescription());
        map.put("title","New comment!");
        map.put("read",false);
        map.put("userId", reciever);

        db.collection("Notifications")
                .document(id.toString())
                .set(map);


    }
}