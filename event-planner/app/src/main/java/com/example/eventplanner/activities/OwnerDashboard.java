package com.example.eventplanner.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventplanner.R;
import com.example.eventplanner.databinding.ActivityHomeBinding;
import com.example.eventplanner.databinding.ActivityOwnerDashboardBinding;
import com.example.eventplanner.model.DateSchedule;
import com.example.eventplanner.model.UserPUPZ;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class OwnerDashboard extends AppCompatActivity {

    private boolean showSearchInput = false;
    private LinearLayout workersWrapper;
    private List<UserPUPZ> workers;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_owner_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ActivityOwnerDashboardBinding binding= ActivityOwnerDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        workersWrapper = binding.ownerDashboardWorkersWrapper;

        initializeWorkerCards().thenAccept(workers->{
            this.workers = workers;
            binding.searchInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String newText = s.toString();

                    if(newText.isEmpty())
                        displayUsersAsCards(workers);
                    else{
                        List<UserPUPZ> filteredUsers = new ArrayList<>();
                        for (UserPUPZ user : workers) {
                            String address = user.getAddress();
                            String firstName = user.getFirstName();
                            String lastName = user.getLastName();

                            if (address.contains(newText) || firstName.contains(newText) || lastName.contains(newText)) {
                                filteredUsers.add(user);
                            }
                        }
                        displayUsersAsCards(filteredUsers);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        });

        initializeScheduleTable(binding);

        binding.addWorkerBtn.setOnClickListener(v->{
            Intent intent = new Intent(OwnerDashboard.this, RegisterWorkerActivity.class);
            startActivity(intent);
        });


        binding.searchBtn.setOnClickListener(v->{
            showSearchInput = !showSearchInput;
            binding.searchInput.setVisibility(showSearchInput ? View.VISIBLE : View.GONE);
        });

        binding.backBtn.setOnClickListener(v->{
            Intent intent = new Intent(OwnerDashboard.this, HomeActivity.class);
            startActivity(intent);
        });

    }

    private CompletableFuture<List<UserPUPZ>> initializeWorkerCards() {
        CollectionReference usersRef = db.collection("User");
        CompletableFuture<List<UserPUPZ>> future = new CompletableFuture<>();

        usersRef.whereEqualTo("userType", "PUPZ")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<UserPUPZ> userList = new ArrayList<>();

                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        UserPUPZ user = documentSnapshot.toObject(UserPUPZ.class);
                        userList.add(user);
                    }

                    displayUsersAsCards(userList);
                    future.complete(userList);
                })
                .addOnFailureListener(e -> {
                    Log.e("YourActivity", "Error getting documents", e);
                    future.completeExceptionally(e);
                });
        return future;
    }

    private void initializeScheduleTable(ActivityOwnerDashboardBinding binding){

        TableLayout table = binding.workerSchedulesTable;

        db.collection("DateSchedule")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {

                            DateSchedule schedule = DateSchedule.fromFirestoreData(document.getData());

                            TableRow tableRow = new TableRow(this);

                            tableRow.setOnClickListener(v ->{
                                Intent intent = new Intent(OwnerDashboard.this, WorkerScheduleActivity.class);
                                intent.putExtra("workerId", schedule.getWorkerId());
                                intent.putExtra("schedule", schedule);
                                startActivity(intent);
                            });

                            getUserBySchedule(schedule)
                                    .thenAccept(user -> {
                                        insertColumn(user.getFirstName().concat(" ".concat(user.getLastName())), tableRow,true);
                                        insertColumn(schedule.getDateRange().getStartDate(), tableRow,false);
                                        insertColumn(schedule.getDateRange().getEndDate(), tableRow,false);

                                        table.addView(tableRow);
                                    })
                                    .exceptionally(e -> {
                                        Log.e("Firestore", "Error getting user: ", e);
                                        return null;
                                    });
                        }
                    } else {
                        Log.d("Error", "Error getting documents: ", task.getException());
                    }
                });
    }

    private void insertColumn(Object data, TableRow tableRow, boolean isUsername) {
        TextView textView = new TextView(this);
        textView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
        textView.setBackground(ContextCompat.getDrawable(this, R.drawable.table_cell));
        textView.setTextColor(Color.BLACK);
        textView.setPadding(12, 12, 12, 12);
        textView.setTextSize(12);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setText(data.toString());
        if(isUsername)
            textView.setTypeface(null, Typeface.BOLD);

        tableRow.addView(textView);
    }

    private CompletableFuture<UserPUPZ> getUserBySchedule(DateSchedule schedule) {
        CompletableFuture<UserPUPZ> future = new CompletableFuture<>();

        Query query = db.collection("User").whereEqualTo("id", schedule.getWorkerId());

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                DocumentSnapshot lastDocument = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                UserPUPZ user = lastDocument.toObject(UserPUPZ.class);
                future.complete(user);
            } else {
                future.completeExceptionally(new Exception("User not found"));
            }
        }).addOnFailureListener(e -> {
            future.completeExceptionally(e);
        });

        return future;
    }

    private void displayUsersAsCards(List<UserPUPZ> userList) {
        workersWrapper.removeAllViews();

        for (UserPUPZ user : userList) {
            View cardView = LayoutInflater.from(this).inflate(R.layout.adapter_worker_card, workersWrapper, false);

            TextView nameTextView = cardView.findViewById(R.id.worker_name_surname);
            nameTextView.setText(user.getFirstName() + " " + user.getLastName());

            TextView phoneTextView = cardView.findViewById(R.id.worker_phone_number);
            phoneTextView.setText(user.getPhone());

            TextView emailTextView = cardView.findViewById(R.id.worker_email);
            emailTextView.setText(user.getEmail());

            TextView locationTextView = cardView.findViewById(R.id.worker_location);
            locationTextView.setText(user.getAddress());

            LinearLayout verificationPill = cardView.findViewById(R.id.worker_is_verified_wrapper);
            if(!user.isValid())
                verificationPill.setVisibility(View.GONE);

            cardView.setOnClickListener(v->{
                Intent intent = new Intent(OwnerDashboard.this, WorkerDashboardActivity.class);
                intent.putExtra("selectedWorker", user);
                startActivity(intent);
            });

            workersWrapper.addView(cardView);
        }
    }
}