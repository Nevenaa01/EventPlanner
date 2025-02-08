package com.example.eventplanner.fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.eventplanner.R;
import com.example.eventplanner.databinding.ActivityWorkerScheduleBinding;
import com.example.eventplanner.databinding.FragmentWorkerWeeklyScheduleBinding;
import com.example.eventplanner.model.DateSchedule;
import com.example.eventplanner.model.EventPUPZ;
import com.example.eventplanner.utils.DateRange;
import com.example.eventplanner.utils.WorkingHours;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.type.DateTime;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class WorkerWeeklyScheduleFragment extends Fragment {
    DateSchedule dateSchedule;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_worker_weekly_schedule, container, false);

        Bundle bundle = getArguments();
        if (bundle != null) {
            dateSchedule = (DateSchedule) bundle.getSerializable("dateSchedule");
            initializeScheduleTable(rootView);
            initializeEventForm(rootView);
        }

        FragmentWorkerWeeklyScheduleBinding binding = FragmentWorkerWeeklyScheduleBinding.bind(rootView);

        Button addEventBtn = rootView.findViewById(R.id.add_event_btn);
        addEventBtn.setOnClickListener(v->{

            Date currentDate = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String formattedDate = dateFormat.format(currentDate);

            getNumberOfItemsInUsersCollection().thenAccept(numberOfItems->{
                EventPUPZ event = new EventPUPZ(
                        numberOfItems+1,
                        binding.eventFromInput.getText().toString(),
                        binding.eventToInput.getText().toString(),
                        formattedDate,
                        dateSchedule.getId(),
                        binding.eventHeader.getText().toString(),
                        "BUSY",
                        dateSchedule.getWorkerId()
                );

                db.collection("Event").add(event).addOnCompleteListener(c ->{
                    Log.d("Success", "Succesfuly added event: " + event);
                    binding.eventFromInput.setText("");
                    binding.eventToInput.setText("");
                    binding.eventNameInput.setText("");
                })
                        .addOnFailureListener(e->{
                            Log.d("Error", "Error while adding event:"+event);
                            e.printStackTrace();
                        });
            });

        });

        return rootView;
    }
    private void initializeEventForm(View rootView){
        TextView header = rootView.findViewById(R.id.schedule_date_header);

        changeEventForm(dateSchedule
                .getSchedule().entrySet().stream()
                .filter(entry -> "MONDAY".equals(entry.getKey()))
                .findFirst()
                .orElse(null),
                rootView);

        header.setText(dateSchedule.getDateRange().getStartDate().concat(" - ".concat(dateSchedule.getDateRange().getEndDate())));
    }
    private void initializeScheduleTable(View rootView) {
        if (rootView != null) {

            TableLayout table = rootView.findViewById(R.id.fragment_worker_schedules_table);

            if (table != null && dateSchedule != null) {
                Map<String, WorkingHours> scheduleMap = dateSchedule.getSchedule();
                Set<Map.Entry<String, WorkingHours>> entries = scheduleMap.entrySet();
                for (Map.Entry<String, WorkingHours> entry : entries) {
                    String key = entry.getKey();
                    WorkingHours value = entry.getValue();

                    TableRow tableRow = new TableRow(requireContext());
                    insertColumn(key, tableRow, true);
                    insertColumn(value.getStartTime(), tableRow, false);
                    insertColumn(value.getEndTime(), tableRow, false);

                    tableRow.setOnClickListener(v->{
                        changeEventForm(entry, rootView);
                    });

                    table.addView(tableRow);
                }
            }
        }
    }

    private void insertColumn(String data, TableRow tableRow, boolean isUsername) {
        TextView textView = new TextView(this.getContext());
        textView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
        textView.setBackground(ContextCompat.getDrawable(this.getContext(), R.drawable.table_cell));
        textView.setTextColor(Color.BLACK);
        textView.setPadding(12, 12, 12, 12);
        textView.setTextSize(12);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setText(data);
        if(isUsername)
            textView.setTypeface(null, Typeface.BOLD);

        tableRow.addView(textView);
    }

    private void changeEventForm(Map.Entry<String, WorkingHours> dailySchedule, View rootView){
        TextView eventHeader = rootView.findViewById(R.id.event_header);
        TextView startHours = rootView.findViewById(R.id.start_hours);
        TextView endHours = rootView.findViewById(R.id.end_hours);

        getEventsByDateScheduleIdAndDay(dateSchedule.getId(), dailySchedule.getKey(), dateSchedule.getDateRange())
                .thenAccept(events ->{
                    populateEventViews(this.getContext(), events, rootView.findViewById(R.id.event_list));
                });

        eventHeader.setText(dailySchedule.getKey());
        startHours.setText(dailySchedule.getValue().getStartTime());
        endHours.setText(dailySchedule.getValue().getEndTime());
    }

    private CompletableFuture<Long> getNumberOfItemsInUsersCollection() {
        CollectionReference usersCollection = db.collection("Event");

        CompletableFuture<Long> future = new CompletableFuture<>();

        usersCollection.get().addOnSuccessListener(queryDocumentSnapshots -> {
            long numberOfItems;
            if (queryDocumentSnapshots.isEmpty()) {
                numberOfItems = 1L;
            } else {
                numberOfItems = (long) queryDocumentSnapshots.size();
            }
            future.complete(numberOfItems);
        }).addOnFailureListener(e -> {
            Log.e("Firestore", "Error getting documents: ", e);
            future.completeExceptionally(e);
        });

        return future;
    }

    public CompletableFuture<List<EventPUPZ>> getEventsByDateScheduleIdAndDay(long dateScheduleId, String day, DateRange dateRange) {
        CompletableFuture<List<EventPUPZ>> future = new CompletableFuture<>();

        try {
            CollectionReference eventCollection = db.collection("Event");
            Query query = eventCollection
                    .whereEqualTo("dateScheduleId", dateScheduleId)
                    .whereEqualTo("day", day)
                    .whereEqualTo("workerId", dateSchedule.getWorkerId());

            query.get().addOnCompleteListener(v->{
                if(v.isSuccessful()){
                    List<EventPUPZ> events = new ArrayList<>();
                    for (DocumentSnapshot document : v.getResult().getDocuments()) {
                        EventPUPZ event = document.toObject(EventPUPZ.class);

                        String eventDateStr = event.getOccurenceDate();
                        LocalDate eventDate = LocalDate.parse(eventDateStr);

                        String startDateStr = dateRange.getStartDate();
                        String endDateStr = dateRange.getEndDate();

                        LocalDate startDate = LocalDate.parse(startDateStr);
                        LocalDate endDate = LocalDate.parse(endDateStr);

                        if (!eventDate.isBefore(startDate) && !eventDate.isAfter(endDate)) {
                            events.add(event);
                        }
                    }
                    future.complete(events);
                } else {
                    future.completeExceptionally(new RuntimeException("Failed to fetch events"));
                }
            });
        } catch (Exception e) {
            future.completeExceptionally(e);
        }

        return future;
    }

    public void populateEventViews(Context context, List<EventPUPZ> events, LinearLayout parentLayout) {
        parentLayout.removeAllViews();
        if (events != null && !events.isEmpty()) {
            for (EventPUPZ event : events) {

                LinearLayout eventLayout = new LinearLayout(context);
                eventLayout.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                eventLayout.setOrientation(LinearLayout.HORIZONTAL);
                eventLayout.setGravity(Gravity.CENTER_VERTICAL);
                eventLayout.setPadding(0, 0, 0, 8);

                TextView timeTextView = new TextView(context);
                timeTextView.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                ));
                timeTextView.setText(event.getStartHours() + " - " + event.getEndHours());
                timeTextView.setTextSize(20);
                timeTextView.setTextColor(Color.BLACK);
                timeTextView.setPadding(12, 4, 12, 4);

                TextView statusTextView = new TextView(context);
                statusTextView.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                statusTextView.setText(event.getType());
                statusTextView.setTextSize(16);
                statusTextView.setTextColor(event.getType().equals("BUSY") ? Color.parseColor("#FF905FEA") : Color.parseColor("#FF2B1763"));
                statusTextView.setPadding(0, 0, 12, 0);
                statusTextView.setTypeface(null, Typeface.BOLD);

                eventLayout.addView(timeTextView);
                eventLayout.addView(statusTextView);

                parentLayout.addView(eventLayout);
            }
        }
    }
}