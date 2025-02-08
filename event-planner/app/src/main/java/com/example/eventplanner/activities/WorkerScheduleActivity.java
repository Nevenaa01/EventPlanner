package com.example.eventplanner.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventplanner.R;
import com.example.eventplanner.databinding.ActivityOwnerDashboardBinding;
import com.example.eventplanner.databinding.ActivityRegisterWorkerBinding;
import com.example.eventplanner.databinding.ActivityWorkerScheduleBinding;
import com.example.eventplanner.model.DateSchedule;
import com.example.eventplanner.model.UserPUPZ;
import com.example.eventplanner.utils.DateRange;
import com.example.eventplanner.utils.Days;
import com.example.eventplanner.utils.WorkingHours;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.security.acl.Owner;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class WorkerScheduleActivity extends AppCompatActivity {

    DateSchedule schedule = new DateSchedule();
    DateSchedule currentSchedule = new DateSchedule();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_worker_schedule);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        currentSchedule = (DateSchedule) this.getIntent().getSerializableExtra("schedule");
        schedule.setWorkerId(this.getIntent().getLongExtra("workerId", -1));

        ActivityWorkerScheduleBinding binding= ActivityWorkerScheduleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getUserBySchedule(currentSchedule).thenAccept(user->{
            binding.header.setText(user.getFirstName().concat("'s schedule"));
        });

        binding.scheduleDateSpan.setText(currentSchedule.getDateRange().getStartDate().concat(" - ".concat(currentSchedule.getDateRange().getEndDate())));

        Spinner spinner  = binding.daysSpinner;

        initializeScheduleTable(binding);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getApplication(),
                android.R.layout.simple_dropdown_item_1line,
                getResources().getStringArray(R.array.days_array));

        arrayAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        spinner.setAdapter(arrayAdapter);

        binding.addScheduleBtn.setOnClickListener((v)->{

            String dateSpan = binding.dateRangePicker.getText().toString();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            try {

                String[] dates = dateSpan.split("-");
                String startDateString = dates[0].trim();
                String endDateString = dates[1].trim();

                startDateString = startDateString.replace("/", "-");
                endDateString = endDateString.replace("/", "-");

                schedule.setDateRange(new DateRange(startDateString, endDateString));
                getNumberOfItemsInUsersCollection().thenAccept(numberOfItems ->{

                    for (Map.Entry<String, WorkingHours> entry : schedule.getSchedule().entrySet()) {
                        if (entry.getValue() == null) {
                            entry.setValue(new WorkingHours("9:00 AM", "17:00 PM"));
                        }
                    }
                    schedule.setId(numberOfItems+1);
                    db.collection("DateSchedule")
                            .add(schedule)
                            .addOnSuccessListener(documentReference -> {
                                Log.d("Firestore", "Document added with ID: " + documentReference.getId());
                            })
                            .addOnFailureListener(e -> {
                                Log.e("Firestore", "Error adding document", e);
                            });
                });
            }
            catch (Exception e){
                e.printStackTrace();
            }

            Intent intent = new Intent(this, OwnerDashboard.class);
            startActivity(intent);
        });

        binding.enterHoursBtn.setOnClickListener(v -> {
            String fromTime = binding.fromInput.getText().toString().trim();
            String toTime = binding.toInput.getText().toString().trim();

            String timePattern = "(0?[1-9]|(1[012])):[0-5][0-9] [APap][Mm]";

            /*if (!fromTime.matches(timePattern) || !toTime.matches(timePattern)) {
                Toast.makeText(this, "Invalid time format! Please enter time in HH:MM AM/PM format.", Toast.LENGTH_SHORT).show();
                return;
            }*/

            int position = binding.daysSpinner.getSelectedItemPosition();
            Days day = Days.values()[position];

            schedule.setItem(day.toString(), new WorkingHours(fromTime, toTime));
            Toast.makeText(this, "Added:" + new WorkingHours(fromTime, toTime).toString(), Toast.LENGTH_SHORT).show();
        });

        binding.cancelBtn.setOnClickListener((v)->{
            Intent intent = new Intent(this, OwnerDashboard.class);
            startActivity(intent);
        });

        binding.backBtn.setOnClickListener((v)->{
            Intent intent = new Intent(this, OwnerDashboard.class);
            startActivity(intent);
        });
    }

    private CompletableFuture<Long> getNumberOfItemsInUsersCollection() {
        CollectionReference dateScheduleCollection = db.collection("DateSchedule");

        CompletableFuture<Long> future = new CompletableFuture<>();

        dateScheduleCollection.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                long numberOfItems = task.getResult().size();
                future.complete(numberOfItems);
            } else {
                future.completeExceptionally(task.getException());
            }
        });

        return future;
    }

    private void initializeScheduleTable(ActivityWorkerScheduleBinding binding) {
        TableLayout table = binding.workerSchedulesTable;

        Map<String, WorkingHours> scheduleMap = currentSchedule.getSchedule();
        Set<Map.Entry<String, WorkingHours>> entries = scheduleMap.entrySet();
        for (Map.Entry<String, WorkingHours> entry : entries) {
            String key = entry.getKey();
            WorkingHours value = entry.getValue();

            TableRow tableRow = new TableRow(this);

            insertColumn(key, tableRow, true);
            insertColumn(value.getStartTime(), tableRow, false);
            insertColumn(value.getEndTime(), tableRow, false);

            table.addView(tableRow);
        }
    }

    private void insertColumn(String data, TableRow tableRow, boolean isUsername) {
        TextView textView = new TextView(this);
        textView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
        textView.setBackground(ContextCompat.getDrawable(this, R.drawable.table_cell));
        textView.setTextColor(Color.BLACK);
        textView.setPadding(12, 12, 12, 12);
        textView.setTextSize(12);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setText(data);
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
}