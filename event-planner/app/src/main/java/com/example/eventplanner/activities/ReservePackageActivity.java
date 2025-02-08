package com.example.eventplanner.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventplanner.R;
import com.example.eventplanner.fragments.ReserveServiceFragment;
import com.example.eventplanner.model.Event;
import com.example.eventplanner.model.EventPUPZ;
import com.example.eventplanner.model.Package;
import com.example.eventplanner.model.PackageReservationRequest;
import com.example.eventplanner.model.Product;
import com.example.eventplanner.model.Service;
import com.example.eventplanner.model.ServiceReservationRequest;
import com.example.eventplanner.services.FCMHttpClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReservePackageActivity extends AppCompatActivity implements ReserveServiceFragment.OnDataPass{
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth= FirebaseAuth.getInstance();
    Package pac;
    List<Service> services=new ArrayList<>();
    Event selectedEvent;
    List<Event> events=new ArrayList<>();
    List<String> event_names=new ArrayList<>();
    List<Product> products=new ArrayList<>();

    List<ServiceReservationRequest> reservations=new ArrayList<>();
    String packageId;
    @Override
    public void onDataPass(ServiceReservationRequest data) {
        reservations.add(data);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reserve_package);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        packageId = getIntent().getStringExtra("id");

        findViewById(R.id.reservePackage).setOnClickListener(v->{
           createPackageRequest();
        });

        getPackage();
        getEvents();
    }

    void getService(){
        db.collection("Services").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot doc : task.getResult()) {
                        if(pac.getServiceIds().contains(Long.parseLong(doc.getId()))){
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
                            services.add(yourObject);
                        }


                    }
                    createTable();
                } else {
                    Log.w("TAG", "Error getting documents.", task.getException());
                }
            }
        });

    }
    private void getPackage(){
        db.collection("Packages")
                .document(packageId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        pac = new Package(
                                Long.parseLong(doc.getId()),
                                doc.getString("pupvId"),
                                doc.getString("name"),
                                doc.getString("description"),
                                ((Number) doc.get("discount")).doubleValue(),
                                doc.getBoolean("available"),
                                doc.getBoolean("visible"),
                                Long.parseLong(doc.getString("categoryId")),
                                convertStringArrayToLong((ArrayList<String>) doc.get("subcategoryIds")),
                                convertStringArrayToLong((ArrayList<String>)doc.get("productIds")),
                                convertStringArrayToLong((ArrayList<String>)doc.get("serviceIds")),
                                convertStringArrayToLong((ArrayList<String>) doc.get("eventTypeIds")),
                                ((Number) doc.get("price")).doubleValue(),
                                new ArrayList<>(), //images
                                doc.getString("reservationDue"),
                                doc.getString("cancelationDue"),
                                doc.getBoolean("automaticAffirmation"),
                                doc.getBoolean("deleted"));

                    } else {
                        Log.d("Firestore", "No such document");
                    }
                    getService();
                    getProducts();
                })
                .addOnFailureListener(e -> {
                    Log.w("Firestore", "Error fetching document", e);
                });
    }
    private void getProducts(){
        db.collection("Products").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot doc : task.getResult()) {
                        if(pac.getProductIds().contains(Long.parseLong(doc.getId()))){
                            Product product = new Product(
                                    Long.parseLong(doc.getId()),
                                    doc.getString("pupvId"),
                                    Long.parseLong(doc.getString("categoryId")),
                                    Long.parseLong(doc.getString("subcategoryId")),
                                    doc.getString("name"),
                                    doc.getString("description"),
                                    doc.getDouble("price"),
                                    doc.getDouble("discount"),
                                    new ArrayList<>(),
                                    new ArrayList<>(), //convertStringArrayToLong((ArrayList<String>) doc.get("eventTypeIds")),
                                    doc.getBoolean("available"),
                                    doc.getBoolean("visible"),
                                    doc.getBoolean("pending"),
                                    doc.getBoolean("deleted")
                            );
                            products.add(product);
                        }
                    }

                } else {
                    Log.w("TAG", "Error getting documents.", task.getException());
                }
            }
        });
    }
    private void createTable(){
        TableLayout tableLayout = findViewById(R.id.tableLayout);
        for (Service service:services) {
            TableRow newRow = new TableRow(this);
            newRow.setGravity(Gravity.CENTER);

            TextView textView1 = new TextView(this);
            textView1.setText(service.getName());
            textView1.setGravity(Gravity.CENTER);

            Button button = new Button(this);
            button.setText("Select");
            button.setGravity(Gravity.CENTER);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showBottomSheetDialog(service.getId());
                    button.setVisibility(View.GONE);
                }
            });

            newRow.addView(textView1);
            newRow.addView(button);

            tableLayout.addView(newRow);
        };
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
                        TextInputLayout textInputLayout = findViewById(R.id.event_pick);
                        AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) textInputLayout.getEditText();
                        ArrayAdapter<String> eventAdapter = new ArrayAdapter<>(ReservePackageActivity.this, android.R.layout.simple_dropdown_item_1line, event_names);
                        autoCompleteTextView.setAdapter(eventAdapter);

                        autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
                            selectedEvent = events.get(position);
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ReservePackageActivity.this, e.getMessage(), Toast.LENGTH_SHORT);
                });
    }
    private void showBottomSheetDialog(Long id) {
        ReserveServiceFragment bottomSheetDialogFragment = new ReserveServiceFragment(id,false,selectedEvent);
        bottomSheetDialogFragment.setOnDataPass(this);
        bottomSheetDialogFragment.show(getSupportFragmentManager(), "ReserveServiceFragment");
    }

    private void createPackageRequest(){
        if(services.stream().count()!=reservations.stream().count()){
            Toast.makeText(getApplicationContext(), "Please register all events", Toast.LENGTH_SHORT).show();
            return;
        }
        if(selectedEvent==null) {
            Toast.makeText(getApplicationContext(), "Please select an event", Toast.LENGTH_SHORT).show();
            return;
        }
        for (ServiceReservationRequest res:reservations) {
            res.setOccurenceDate(selectedEvent.getDateEvent().toString());
        }

        PackageReservationRequest req=new PackageReservationRequest(
                reservations,
                mAuth.getCurrentUser().getUid().toString(),
                pac.getPupvId(),
                products,
                "NEW"
        );
        
        db.collection("PackageReservationRequest").add(req)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Data added successfully", Toast.LENGTH_SHORT).show();
                        for (Product prod:products) {
                            if(prod.getAvailable())reserveProduct(prod);
                        }
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to add data", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void reserveProduct(Product product){
        Map<String,Object> map= new HashMap<>();
        map.put("product",product);
        map.put("userId",mAuth.getCurrentUser().getUid());

        db.collection("ProductReservation").add(map)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Data added successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to add data", Toast.LENGTH_SHORT).show();
                    }
                });
    }


}