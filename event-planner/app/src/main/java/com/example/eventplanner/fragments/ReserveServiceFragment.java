package com.example.eventplanner.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eventplanner.R;
import com.example.eventplanner.model.DateSchedule;
import com.example.eventplanner.model.Event;
import com.example.eventplanner.model.EventPUPZ;
import com.example.eventplanner.model.Service;
import com.example.eventplanner.model.ServiceReservationRequest;
import com.example.eventplanner.model.UserPUPZ;
import com.example.eventplanner.services.FCMHttpClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;


public class ReserveServiceFragment extends BottomSheetDialogFragment {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    List<String> pupz_list_names=new ArrayList<>();
    List<UserPUPZ> pupz_list=new ArrayList<>();
    Service service;
    List<Event> events=new ArrayList<>();
    List<String> event_names=new ArrayList<>();
    UserPUPZ selectedPupz;
    DateSchedule dateScheule;
    Event selectedEvent;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    HashMap<String, List<EventPUPZ>> busyDates=new HashMap<>();
    TextInputEditText from;
    TextInputEditText to;

    String dayOfWeek="";

    View view;
    Long serviceId;
    boolean flag;

    private OnDataPass dataPasser;
    public interface OnDataPass {
        void onDataPass(ServiceReservationRequest data); // Define any method signature you need
    }
    public void setOnDataPass(OnDataPass dataPasser) {
        this.dataPasser = dataPasser;
    }
    private void passDataToActivity(ServiceReservationRequest data) {
        if (dataPasser != null) {
            dataPasser.onDataPass(data);
        }
    }

    public ReserveServiceFragment(Long serviceId,boolean flag,Event event) {
        this.serviceId=serviceId;
        this.flag=flag;
        this.selectedEvent=event;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.activity_reserve_service, container, false);
        if(!flag)view.findViewById(R.id.event_pick).setVisibility(View.GONE);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getService();
        from=view.findViewById(R.id.fromTime);
        to=view.findViewById(R.id.toTime);
        from.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && from.getText()!=null && !from.getText().equals("")) {
                    String timeString=from.getText().toString();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                    LocalTime time = LocalTime.parse(timeString, formatter);
                    if(service.getDuration()!=null){
                        time=time.plusMinutes((int)(service.getDuration()*60));
                    }
                    String endtime=timeString;
                    if(dateScheule!=null)endtime=dateScheule.getSchedule().get(dayOfWeek.toUpperCase()).getEndTime().split(" ")[0];
                    LocalTime endTime=LocalTime.parse(endtime, formatter);
                    if(time.isBefore(endTime)){
                        to.setText(time.toString());
                        return;
                    }
                    to.setText(endTime.toString());
                }
            }
        });

        view.findViewById(R.id.reserveService).setOnClickListener(v->{
            if(flag){
                createReservation();
            }else{
                ServiceReservationRequest event = new ServiceReservationRequest(
                        from.getText().toString(),
                        to.getText().toString(),
                        "",
                        dateScheule.getId().toString(),
                        selectedPupz.getId().toString(),
                        dayOfWeek.toUpperCase(),
                        "RESERVED",
                        "NEW",
                        service,
                        mAuth.getCurrentUser().getUid().toString()
                );

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                LocalTime start = LocalTime.parse(from.getText().toString(), formatter);
                LocalTime end = LocalTime.parse(to.getText().toString(), formatter);

                for (EventPUPZ time:busyDates.get(dayOfWeek.toUpperCase())) {
                    LocalTime startBusy = LocalTime.parse(time.getStartHours(), formatter);
                    LocalTime endBusy = LocalTime.parse(time.getEndHours(), formatter);
                    if(!(start.isAfter(endBusy) || end.isBefore(startBusy))){
                        Toast.makeText(getContext(), "Please select valid dates!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                passDataToActivity(event);
                getActivity().getSupportFragmentManager().beginTransaction().remove(ReserveServiceFragment.this).commit();
            }

        });

    }
    void getPupz(){
        db.collection("User")
                .whereEqualTo("userType", "PUPZ")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            for (DocumentSnapshot doc : task.getResult()) {
                                if(service.getPupIds().contains(doc.getId())){
                                    pupz_list_names.add(doc.getString("firstName") + " " + doc.getString("lastName"));
                                    UserPUPZ user = doc.toObject(UserPUPZ.class);
                                    pupz_list.add(user);
                                }

                            }
                            TextInputLayout textInputLayout = view.findViewById(R.id.pupz_pick);
                            AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) textInputLayout.getEditText();
                            ArrayAdapter<String> pupzAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, pupz_list_names);
                            autoCompleteTextView.setAdapter(pupzAdapter);

                            autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
                                selectedPupz = pupz_list.get(position) ;
                                getDateSchedules();
                            });
                        } else {
                            Toast.makeText(getContext(), "Getting data failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }
    void getService(){
        db.collection("Services")
                .document(serviceId.toString())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Service yourObject = new Service(
                                Long.parseLong(doc.getId()),
                                doc.getString("pupvId"),
                                Long.parseLong(doc.getString("categoryId")),
                                Long.parseLong(doc.getString("subcategoryId")),
                                doc.getString("name"),
                                doc.getString("description"),
                                new ArrayList<>(), //images
                                doc.getString("specific"),
                                ((Number) doc.get("pricePerHour")).doubleValue(),
                                ((Number) doc.get("fullPrice")).doubleValue(),
                                doc.get("duration") != null ? ((Number) doc.get("duration")).doubleValue() : null,
                                doc.get("durationMin") != null ? ((Number) doc.get("durationMin")).doubleValue() : null,
                                doc.get("durationMax") != null ? ((Number) doc.get("durationMax")).doubleValue() : null,
                                doc.getString("location"),
                                ((Number) doc.get("discount")).doubleValue(),
                                (ArrayList<String>) doc.get("pupIds"),
                                convertStringArrayToLong((ArrayList<String>) doc.get("eventTypeIds")),
                                doc.getString("reservationDue"),
                                doc.getString("cancelationDue"),
                                doc.getBoolean("automaticAffirmation"),
                                doc.getBoolean("available"),
                                doc.getBoolean("visible"),
                                doc.getBoolean("pending"),
                                doc.getBoolean("deleted"));
                        if (yourObject != null) {
                            service=yourObject;
                            getEvents();
                            getPupz();
                        }
                    } else {
                        Log.d("Firestore", "No such document");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w("Firestore", "Error fetching document", e);
                });
    }

    private ArrayList<Long> convertStringArrayToLong(ArrayList<String> list){
        ArrayList<Long> ids = new ArrayList<>();

        for(String item: list){
            ids.add(Long.parseLong(item));
        }

        return ids;
    }

    void getEvents(){
        db.collection("Events").get().addOnCompleteListener(task ->  {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot doc : task.getResult()) {
                            if (doc.exists()) {
                                Event yourObject = new Event(
                                        Long.parseLong(doc.getId()),
                                        doc.getString("userOdId"),
                                        doc.getString("typeEvent"),
                                        doc.getString("name"),
                                        doc.getString("description"),
                                        doc.getLong("maxPeople").intValue(),
                                        doc.getString("locationPlace"),
                                        doc.getLong("maxDistance").intValue(),
                                        doc.getDate("dateEvent"),
                                        doc.getBoolean("available")
                                );
                                events.add(yourObject);
                                event_names.add(yourObject.getName());
                            }
                        }
                        TextInputLayout textInputLayout = view.findViewById(R.id.event_pick);
                        AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) textInputLayout.getEditText();
                        ArrayAdapter<String> eventAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, event_names);
                        autoCompleteTextView.setAdapter(eventAdapter);

                        autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
                            selectedEvent = events.get(position);
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT);
                });
    }

    void getDateSchedules(){
        db.collection("DateSchedule")
                .whereEqualTo("workerId",selectedPupz.getId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            dateScheule=null;
                            for (DocumentSnapshot doc : task.getResult()) {
                                DateSchedule schedule= doc.toObject(DateSchedule.class);
                                try {
                                    Date startDate=dateFormat.parse(schedule.getDateRange().getStartDate());
                                    Date endDate=dateFormat.parse(schedule.getDateRange().getEndDate());
                                    Date date=new Date(selectedEvent.getDateEvent().getTime());
                                    if (date.after(startDate) && date.before(endDate)) {
                                        dateScheule=schedule;
                                        getBusyDates();
                                        break;
                                    } else {
                                        System.out.println("The date is outside the range.");
                                    }
                                } catch (ParseException e) {
                                    throw new RuntimeException(e);
                                }


                            }
                        } else {
                            Toast.makeText(getContext(), "Getting data failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    void getBusyDates(){
        db.collection("Event")
                .whereEqualTo("workerId",selectedPupz.getId())
                .whereEqualTo("dateScheduleId",dateScheule.getId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot doc : task.getResult()) {
                                EventPUPZ yourObject = doc.toObject(EventPUPZ.class);
                                yourObject.setStartHours(yourObject.getStartHours().split(" ")[0]);
                                yourObject.setEndHours(yourObject.getEndHours().split(" ")[0]);
                                if(busyDates.get(yourObject.getDay())==null){
                                    List<EventPUPZ> list=new ArrayList<>();
                                    list.add(yourObject);
                                    busyDates.put(yourObject.getDay(),list);
                                }else{
                                    busyDates.get(yourObject.getDay()).add(yourObject);
                                }
                            }
                            displayTable();

                        } else {
                            Toast.makeText(getContext(), "Getting data failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    void displayTable(){
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.getDefault());
        dayOfWeek = sdf.format(selectedEvent.getDateEvent());
        TableLayout tableLayout = view.findViewById(R.id.tableLayout);
        //dayOfWeek="Friday";

        Collections.sort(busyDates.get(dayOfWeek.toUpperCase()), new Comparator<EventPUPZ>() {
            @Override
            public int compare(EventPUPZ o1, EventPUPZ o2) {
                return o1.getStartHours().compareTo(o2.getStartHours());
            }
        });

        for (EventPUPZ day:busyDates.get(dayOfWeek.toUpperCase())) {
            TableRow newRow = new TableRow(getContext());
            newRow.setGravity(Gravity.CENTER);

            TextView textView1 = new TextView(getContext());
            textView1.setText(day.getStartHours());
            textView1.setGravity(Gravity.CENTER);

            TextView textView2 = new TextView(getContext());
            textView2.setText(day.getEndHours());
            textView2.setGravity(Gravity.CENTER);

            TextView textView3 = new TextView(getContext());
            textView3.setText(day.getType());
            textView3.setGravity(Gravity.CENTER);

            newRow.addView(textView1);
            newRow.addView(textView2);
            newRow.addView(textView3);

            tableLayout.addView(newRow);
        };
    }
    String serverKey="AAAA8GYmoZ8:APA91bHsjyzOSa2JtO_cQWFO-X1p9nMuHRO8DTfD1zhcY4mnqZ-2EZmIn8tMf1ISmnM31WB68Mzn2soeUgEISXlSc9WjRvcRhyYbmBgi7whJuYXX-24wkODByasquofLaMZydpg78esK";
    public static void sendMessage(String serverKey, String jsonPayload) {
        FCMHttpClient httpClient = new FCMHttpClient();
        httpClient.sendMessageToTopic(serverKey, "", jsonPayload);
    }
    void createReservation(){
        ServiceReservationRequest event = new ServiceReservationRequest(
                from.getText().toString(),
                to.getText().toString(),
                selectedEvent.getDateEvent().toString(),
                dateScheule.getId().toString(),
                selectedPupz.getId().toString(),
                dayOfWeek.toUpperCase(),
                "RESERVED",
                "NEW",
                service,
                mAuth.getCurrentUser().getUid().toString()
        );

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime start = LocalTime.parse(from.getText().toString(), formatter);
        LocalTime end = LocalTime.parse(to.getText().toString(), formatter);

        for (EventPUPZ time:busyDates.get(dayOfWeek.toUpperCase())) {
            LocalTime startBusy = LocalTime.parse(time.getStartHours(), formatter);
            LocalTime endBusy = LocalTime.parse(time.getEndHours(), formatter);
            if(!(start.isAfter(endBusy) || end.isBefore(startBusy))){
                Toast.makeText(getContext(), "Please select valid dates!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        db.collection("ServiceReservationRequest").add(event)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Data added successfully", Toast.LENGTH_SHORT).show();
                        String jsonPayload = "{\"data\":{" +
                                "\"title\":\"Reservation\"," +
                                "\"body\":\"" + "You have a new reservation!" + "\"," +
                                "\"topic\":\"" + "NtN0ByBAvgc4Utxx2m4La3vAtDL2" + "PUPZTopic\"" +//treba skloniti zakucane vrijednosti
                                "}," +
                                "\"to\":\"/topics/" + "NtN0ByBAvgc4Utxx2m4La3vAtDL2" + "PUPZTopic" + "\"}";
                        sendMessage(serverKey,jsonPayload);
                        addNotification();
                        getActivity().getSupportFragmentManager().beginTransaction().remove(ReserveServiceFragment.this).commit();
                    } else {
                        Toast.makeText(getContext(), "Failed to add data", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void addNotification(){
        Long id = new Random().nextLong();
        Map<String,Object> map=new HashMap<>();
        map.put("body","You have a new reservation!");
        map.put("title","Reservation");
        map.put("read",false);
        map.put("userId","NtN0ByBAvgc4Utxx2m4La3vAtDL2");

        db.collection("Notifications")
                .document(id.toString())
                .set(map);


    }


}