package com.example.eventplanner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.eventplanner.R;
import com.example.eventplanner.fragments.WorkerWeeklyScheduleFragment;
import com.example.eventplanner.databinding.ActivityWorkerDashboardBinding;
import com.example.eventplanner.model.DateSchedule;
import com.example.eventplanner.model.UserPUPZ;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirestoreKt;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class WorkerDashboardActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FragmentTransaction fragmentTransaction;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_worker_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ActivityWorkerDashboardBinding binding= ActivityWorkerDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        UserPUPZ worker = (UserPUPZ)this.getIntent().getSerializableExtra("selectedWorker");

        binding.workerDashboardNameSurname.setText(worker.getFirstName().concat(" ".concat(worker.getLastName())));
        binding.workerDashboardPhoneNumber.setText(worker.getPhone());
        binding.workerDashboardEmail.setText(worker.getEmail());
        binding.workerDashboardLocation.setText(worker.getAddress());

        fetchDateSchedulesForWorkerId(worker.getId(), binding);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        binding.backBtn.setOnClickListener((v)->{
            Intent intent = new Intent(this, OwnerDashboard.class);
            startActivity(intent);
        });

    }

    private void fetchDateSchedulesForWorkerId(Long workerId, ActivityWorkerDashboardBinding binding) {
        CollectionReference dateSchedulesRef = db.collection("DateSchedule");

        dateSchedulesRef.whereEqualTo("workerId", workerId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    RadioGroup radioGroup = binding.scheduleDateRadioGrp;
                    radioGroup.removeAllViews();
                    Toast.makeText(this, "WORKERID: "+ workerId, Toast.LENGTH_SHORT).show();

                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        DateSchedule dateSchedule = documentSnapshot.toObject(DateSchedule.class);
                        addRadioButtonToGroup(dateSchedule, radioGroup);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("YourActivity", "Error getting date schedules", e);
                });
    }

    private void addRadioButtonToGroup(DateSchedule dateSchedule, RadioGroup radioGroup) {
        RadioButton radioButton = new RadioButton(this);
        radioButton.setText(dateSchedule.getDateRange().getStartDate() + " - " + dateSchedule.getDateRange().getEndDate());
        radioButton.setLayoutParams(new RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT));
        radioButton.setBackground(ContextCompat.getDrawable(this, R.drawable.custom_radio_btn_selector));
        radioButton.setButtonDrawable(android.R.color.transparent);
        radioButton.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        radioButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);

        radioButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            WorkerWeeklyScheduleFragment fragment = new WorkerWeeklyScheduleFragment();
            bundle.putSerializable("dateSchedule", dateSchedule);
            fragment.setArguments(bundle);

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            fragmentTransaction.replace(R.id.weekly_schedule_fragment, fragment, "Fragment");
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        radioGroup.addView(radioButton);
    }
}