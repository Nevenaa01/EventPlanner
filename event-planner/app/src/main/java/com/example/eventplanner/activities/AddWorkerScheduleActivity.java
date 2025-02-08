package com.example.eventplanner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.time.LocalTime;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventplanner.R;
import com.example.eventplanner.databinding.ActivityAddWorkerScheduleBinding;
import com.example.eventplanner.databinding.ActivityHomeBinding;
import com.example.eventplanner.model.DateSchedule;
import com.example.eventplanner.model.UserPUPZ;
import com.example.eventplanner.utils.DateRange;
import com.example.eventplanner.utils.Days;
import com.example.eventplanner.utils.WorkingHours;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;


public class AddWorkerScheduleActivity extends AppCompatActivity {

    private DateSchedule dateSchedule = new DateSchedule();
    private Long workerId;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth= FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_worker_schedule);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        workerId = getIntent().getExtras() == null ? -1 : getIntent().getExtras().getLong("workerId");
        UserPUPZ worker = (UserPUPZ) getIntent().getSerializableExtra("worker");

        ActivityAddWorkerScheduleBinding binding= ActivityAddWorkerScheduleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Spinner spinner  = binding.daysSpinner;

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getApplication(),
                android.R.layout.simple_dropdown_item_1line,
                getResources().getStringArray(R.array.days_array));

        arrayAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        spinner.setAdapter(arrayAdapter);

        binding.finishBtn.setOnClickListener(v->{

            addWorkerAndSchedule(worker);
        });

        binding.enterHoursBtn.setOnClickListener(v -> {
            String fromTime = binding.fromInput.getText().toString().trim();
            String toTime = binding.toInput.getText().toString().trim();

            String timePattern = "(0?[1-9]|(1[012])):[0-5][0-9] [APap][Mm]";


            int position = binding.daysSpinner.getSelectedItemPosition();
            Days day = Days.values()[position];

            dateSchedule.setItem(day.toString(), new WorkingHours(fromTime, toTime));
            Toast.makeText(this, "Added:" + new WorkingHours(fromTime, toTime).toString(), Toast.LENGTH_SHORT).show();
        });

        binding.skipBtn.setOnClickListener(v->{
            addWorkerAndSchedule(worker);
        });

        binding.backBtn.setOnClickListener(v->{
            Intent intent = new Intent(this, RegisterWorkerActivity.class);
            startActivity(intent);
        });
    }

    private void addWorkerAndSchedule(UserPUPZ worker) {
        getNumberOfItemsInUsersCollection().thenAccept(numberOfItems -> {

            DateRange dateRange = new DateRange(LocalDate.now().toString(), LocalDate.now().plusDays(7).toString());
            dateSchedule.setDateRange(dateRange);
            dateSchedule.setWorkerId(workerId);
            dateSchedule.setId(1L);

            for (Map.Entry<String, WorkingHours> entry : dateSchedule.getSchedule().entrySet()) {
                if (entry.getValue() == null) {
                    entry.setValue(new WorkingHours("9:00 AM", "17:00 PM"));
                }
            }
            db.collection("DateSchedule")
                    .add(dateSchedule)
                    .addOnSuccessListener(documentReference -> {
                        Log.d("Firestore", "Document added with ID: " + documentReference.getId());
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error adding document", e);
                    });

            mAuth.createUserWithEmailAndPassword(worker.getEmail(), worker.getPassword())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            updateUserType();

                            db.collection("User").document(mAuth.getCurrentUser().getUid()).set(worker)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                              @Override
                                                              public void onSuccess(Void unused) {
                                                                  sendVerificationEmail(mAuth.getCurrentUser());
                                                              }
                                                          }
                                    );

                        } else {
                            Log.e("FirebaseAuth", "Failed to create user: " + task.getException());
                        }
                    });

            Intent intent = new Intent(this, OwnerDashboard.class);
            startActivity(intent);
        });
    }

    private void updateUserType(){
        FirebaseUser user = mAuth.getCurrentUser();
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName("PUPZ").build();

        user.updateProfile(profileUpdates);
    }

    private CompletableFuture<Long> getNumberOfItemsInUsersCollection() {
        CollectionReference usersCollection = db.collection("DateSchedule");

        CompletableFuture<Long> future = new CompletableFuture<>();

        usersCollection.get().addOnCompleteListener(task -> {
            long numberOfItems = 0;
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot doc : task.getResult()){
                    numberOfItems++;
                }
            }
            future.complete(numberOfItems);
        }).addOnFailureListener(e -> {
            Log.d("MarkoMarkoMarkoMarko", "Error getting documents: ");
            future.completeExceptionally(e);
        });

        return future;
    }

    private void sendVerificationEmail(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Registered user should accept the verification within 24h.", Toast.LENGTH_SHORT).show();
                        Log.d("EmailVerification", "Verification email sent to " + user.getEmail());
                    } else {
                        // Handle failure
                        Log.e("EmailVerification", "Failed to send verification email: " + task.getException());
                    }
                });
    }
}