package com.example.eventplanner.fragments;

import static android.content.ContentValues.TAG;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.eventplanner.R;
import com.example.eventplanner.activities.EventTypesActivity;
import com.example.eventplanner.adapters.EventRecyclerViewAdapter;
import com.example.eventplanner.databinding.FragmentCreateEventBinding;
import com.example.eventplanner.model.Event;
import com.example.eventplanner.model.EventType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.example.eventplanner.adapters.SubcategoryListAdapter;
import com.example.eventplanner.model.Subcategory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class CreateEventFragment extends Fragment {

    private FragmentCreateEventBinding binding;
    TextInputEditText datetimeEventInput;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    FirebaseUser currentUser;

    private AutoCompleteTextView eventType;
    private TextInputEditText nameEvent;
    private TextInputEditText descriptionEvent;

    private  TextInputEditText maxNumberPeople;
    private RadioButton availableEventOpen;
    private RadioButton availableEventClose;

    private TextInputEditText placeEvent;
    private TextInputEditText maxDistance;

    private  TextInputEditText dateEvent;

    AutoCompleteTextView autoCompleteTextView;

    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

    private List<EventType> itemList=new ArrayList<>();

    public static CreateEventFragment newInstance() {
        return new CreateEventFragment();
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCreateEventBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        currentUser = mAuth.getCurrentUser();

        eventType = binding.autoCompleteTextView;
        nameEvent = binding.nameEventInuput;
        descriptionEvent = binding.descriptionEventInput;
        maxNumberPeople = binding.maxNumberPeopleInput;
        availableEventOpen = binding.radioButton1;
        availableEventClose = binding.radioButton2;
        placeEvent = binding.placeEventInput;
        maxDistance = binding.placeDistanceEventInput;
        dateEvent = binding.datetimeEventInput;

        getEventTypes();


        datetimeEventInput = binding.datetimeEventInput;
        datetimeEventInput.setKeyListener(null);

        datetimeEventInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                openDatePicker();
            }
        });



        Button createButton = binding.createButton;

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    if (validateCreteEventInput(eventType, nameEvent, descriptionEvent, maxNumberPeople, placeEvent, maxDistance, dateEvent))
                        return;
                    addNewEvent(new Event(0L, currentUser.getUid(), eventType.getText().toString(),
                            nameEvent.getText().toString(),descriptionEvent.getText().toString(),
                            Integer.parseInt(maxNumberPeople.getText().toString()),placeEvent.getText().toString(),
                            Integer.parseInt(maxDistance.getText().toString()), sdf.parse(dateEvent.getText().toString()),
                            true));


                    getParentFragmentManager().beginTransaction().remove(CreateEventFragment.this).commit();
                    //getParentFragmentManager().popBackStack();
                    Navigation.findNavController(v).navigate(R.id.nav_product_and_services);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        return root;
    }


    private void getEventTypes() {
        itemList=new ArrayList<>();
        db.collection("EventTypes")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> taskEvent) {
                        if (taskEvent.isSuccessful()) {
                            // Process eventType documents

                            // Perform subcategories query
                            db.collection("Subcategories")
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> taskSubcategories) {
                                            if (taskSubcategories.isSuccessful()) {
                                                for(DocumentSnapshot docEvent: taskEvent.getResult()){
                                                    List<String> subcategoryIds=(List<String>)docEvent.get("Subcategories");

                                                    List<Subcategory> subcategories = new ArrayList<>();
                                                    for(DocumentSnapshot doc: taskSubcategories.getResult()){
                                                        Long num=Long.parseLong(doc.getId());
                                                        if(subcategoryIds.contains(num.toString())){
                                                            Subcategory subcategory = new Subcategory(
                                                                    Long.parseLong(doc.getId()),
                                                                    doc.getString("CategoryName"),
                                                                    doc.getString("Name"),
                                                                    doc.getString("Description"),
                                                                    doc.getLong("Type").intValue()
                                                            );
                                                            subcategories.add(subcategory);
                                                        }

                                                    }
                                                    EventType type = new EventType(
                                                            Long.parseLong(docEvent.getId()),
                                                            docEvent.getBoolean("InUse"),
                                                            docEvent.getString("Name"),
                                                            docEvent.getString("Description"),
                                                            subcategories
                                                    );
                                                    itemList.add(type);

                                                }

                                                ArrayList<String> eventTypesList = new ArrayList<>();
                                                for(EventType et : itemList){
                                                    eventTypesList.add(et.getTypeName());
                                                }

                                                autoCompleteTextView = binding.autoCompleteTextView;
                                                ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, eventTypesList);
                                                autoCompleteTextView.setAdapter(adapter);



                                                autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
                                                    String selectedEventType = (String) parent.getItemAtPosition(position);
                                                    System.out.println("Selected event type: " + selectedEventType);

                                                    EventType et = new EventType();
                                                    for(EventType e: itemList){
                                                        if(e.getTypeName().equals(selectedEventType)){
                                                            et = e;
                                                        }
                                                    }

                                                    RecyclerView recyclerView = binding.categoryAndSubcategoryList;
                                                    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
                                                    recyclerView.setLayoutManager(layoutManager);

                                                    List<Subcategory> subcat = et.getRecomendedSubcategories();
                                                    ArrayList<Subcategory> subcategoriesArrayList = new ArrayList<>(subcat);
                                                    SubcategoryListAdapter adapterRecycle = new SubcategoryListAdapter(subcategoriesArrayList);
                                                    recyclerView.setAdapter(adapterRecycle);
                                                });




                                            }
                                        }

                                    });
                        }
                    }
                });
    }

    private void addNewEvent(Event event) {

        // Prvo izvršite upit koji broji postojeće događaje
        db.collection("Events")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        // Broj postojećih događaja
                        int numberOfEvents = queryDocumentSnapshots.size();

                        // Generišite novi ID koristeći broj postojećih događaja
                        long newEventId = numberOfEvents + 1;

                        // Dodajte novi događaj sa generisanim ID-om
                        event.setId(newEventId);
                        saveEventToFirestore(event);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error counting existing events", e);
                    }
                });
    }

    private boolean validateCreteEventInput(AutoCompleteTextView eventType, TextInputEditText nameEvent, TextInputEditText descriptionEvent,
                                            TextInputEditText maxNumberPeople, TextInputEditText placeEvent, TextInputEditText maxDistance,
                                            TextInputEditText dateEvent) {
        boolean error=false;
        if(TextUtils.isEmpty(eventType.getText())){
            eventType.setError("Select option!");
            error=true;
        }
        if(TextUtils.isEmpty(nameEvent.getText())){
            nameEvent.setError("Fill textfield!");
            error=true;
        }
        if(TextUtils.isEmpty(descriptionEvent.getText())){
            descriptionEvent.setError("Fill textfield!");
            error=true;
        }
        if(TextUtils.isEmpty(maxNumberPeople.getText())){
            maxNumberPeople.setError("Fill number!");
            error=true;
        }else{
            try {
                int people = Integer.parseInt(maxNumberPeople.getText().toString());
                if(people <= 0){
                    maxNumberPeople.setError("Number of people must be >0!");
                    error = true;
                }
            }catch (Exception e){
                maxNumberPeople.setError("Number people must be integer!");
                error = true;
            }
        }
        if(TextUtils.isEmpty(placeEvent.getText())){
            placeEvent.setError("Fill textfield!");
            error=true;
        }
        if(TextUtils.isEmpty(maxDistance.getText())){
            maxDistance.setError("Fill number!");
            error=true;
        }else{
            try {
                int distance = Integer.parseInt(maxDistance.getText().toString());
                if(distance <= 0){
                    maxDistance.setError("Distance must be >0!");
                    error = true;
                }
            }catch (Exception e){
                maxDistance.setError("Distance must be integer!");
                error = true;
            }
        }
        if(TextUtils.isEmpty(dateEvent.getText())){
            dateEvent.setError("Choose date!");
            error=true;
        }

        if(error) return true;

        return false;
    }

    private void saveEventToFirestore(Event event) {
        Map<String, Object> elememt = new HashMap<>();
        elememt.put("id", event.getId()); // Postavite ID na generisani ID
        elememt.put("userOdId", event.getUserODId());
        elememt.put("typeEvent", event.getTypeEvent());
        elememt.put("name", event.getName());
        elememt.put("description", event.getDescription());
        elememt.put("maxPeople", event.getMaxPeople());
        elememt.put("locationPlace", event.getLocationPlace());
        elememt.put("maxDistance", event.getMaxDistance());
        elememt.put("available", event.isAvailble());
        elememt.put("dateEvent", event.getDateEvent());

        // Dodajte novi dokument sa generisanim ID-om
        db.collection("Events").document(event.getId().toString())
                .set(elememt)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + event.getId());
                        Toast.makeText(getContext(), "Uspješno dodat događaj", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error adding document", e);
                    }
                });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void openDatePicker(){
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity() , new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                // Postavite odabrani datum u tekstualno polje
                datetimeEventInput.setText(String.valueOf(day)+ "."+String.valueOf(month + 1)+ "."+String.valueOf(year));
            }
        }, currentYear, currentMonth, currentDay);

        // Postavite minimalni datum na trenutni datum
        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());

        datePickerDialog.show();
    }
}
