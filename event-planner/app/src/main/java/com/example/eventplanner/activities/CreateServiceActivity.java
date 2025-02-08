package com.example.eventplanner.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventplanner.R;
import com.example.eventplanner.adapters.CategoryListAdapter;
import com.example.eventplanner.adapters.EventListAdapter;
import com.example.eventplanner.adapters.ImageAdapter;
import com.example.eventplanner.adapters.SubcategoryAdapter;
import com.example.eventplanner.databinding.ActivityCreateServiceBinding;
import com.example.eventplanner.model.Category;
import com.example.eventplanner.model.Event;
import com.example.eventplanner.model.Subcategory;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CreateServiceActivity extends AppCompatActivity {

    Button addCategory, addSubcategory, createBtn;
    FloatingActionButton addImage;
    TextInputLayout name, description, specific, price, pricePerHour, discount, duration,
            durationMin, durationMax, location, eventTextInputLayout, reservationDue, cancelationDue;
    CheckBox available, visible, automaticAffirmation;
    MultiAutoCompleteTextView eventMultiAutoCompleteTextView;
    RecyclerView recyclerView;

    ArrayList<Event> eventsFromDb;
    ArrayList<Category> categoriesFromDb;
    ArrayList<Subcategory> subcategoriesFromDb;

    ArrayList<Long> eventIds;
    Long categoryId;
    String categoryName;
    Long subcategoryId;
    ArrayList<Uri> images;
    ArrayList<String> imageUrls;
    Boolean pending;

    FirebaseFirestore db;
    StorageReference storageRootReference;

    Subcategory requestedSubcategory;

    ImageAdapter imageAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_service);

        images = new ArrayList<>();
        imageUrls = new ArrayList<>();
        subcategoriesFromDb = new ArrayList<>();

        db = FirebaseFirestore.getInstance();
        storageRootReference  = FirebaseStorage.getInstance().getReference();

        getCategories();
        getEvents();

        addSubcategory = findViewById(R.id.add_subcategory);
        addSubcategory.setEnabled(false);

        String[] providers = {"marko marko", "nananan nanananan", "Marko marko", "asdasd asdasd", "z z"};

        TextInputLayout textInputLayout = findViewById(R.id.categories);
        AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) textInputLayout.getEditText();
        CategoryListAdapter categoryAdapter = new CategoryListAdapter(CreateServiceActivity.this, android.R.layout.simple_dropdown_item_1line, categoriesFromDb);
        autoCompleteTextView.setAdapter(categoryAdapter);

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Category selectedCategory = (Category) parent.getItemAtPosition(position);
                categoryId = selectedCategory.getId();
                categoryName = selectedCategory.getName();
                subcategoriesFromDb.clear();
                getSubcategories(categoryName);
                addSubcategory.setEnabled(true);
            }
        });

        TextInputLayout subcategoryTextInputLayout = findViewById(R.id.subcategories);
        AutoCompleteTextView subcategoryAutoCompleteTextView = (AutoCompleteTextView) subcategoryTextInputLayout.getEditText();
        SubcategoryAdapter subcategoryAdapter = new SubcategoryAdapter(CreateServiceActivity.this, android.R.layout.simple_dropdown_item_1line, subcategoriesFromDb);
        subcategoryAutoCompleteTextView.setAdapter(subcategoryAdapter);

        subcategoryAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Subcategory selectedSubcategory = (Subcategory) parent.getItemAtPosition(position);
                subcategoryId = selectedSubcategory.getId();
                pending = false;
            }
        });

        addSubcategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = (LayoutInflater) CreateServiceActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View popUpView = inflater.inflate(R.layout.activity_add_subcategory, null);

                popUpView.setBackground(ContextCompat.getDrawable(CreateServiceActivity.this, R.drawable.gradient_background_2));

                TextInputEditText categoryNameTextInput = popUpView.findViewById(R.id.categoryName);
                categoryNameTextInput.setText(categoryName);

                RadioGroup type = popUpView.findViewById(R.id.radioGroup);
                RadioButton productRadioButton = popUpView.findViewById(R.id.serviceRadio);
                type.check(productRadioButton.getId());
                type.getChildAt(1).setClickable(false);

                int width = ViewGroup.LayoutParams.MATCH_PARENT;
                int height = ViewGroup.LayoutParams.WRAP_CONTENT;
                boolean focusable = true;
                PopupWindow popupWindow = new PopupWindow(popUpView, width, height, focusable);

                popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);

                Button addSubcategory = popUpView.findViewById(R.id.addSubcategory);
                addSubcategory.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TextInputEditText subcategoryNameTextInput = popUpView.findViewById(R.id.subcategoryName);
                        TextInputEditText description = popUpView.findViewById(R.id.subcategoryDescription);

                        requestedSubcategory = new Subcategory(
                                categoryName,
                                subcategoryNameTextInput.getText().toString(),
                                description.getText().toString(),
                                1);

                        subcategoryAutoCompleteTextView.setText(subcategoryNameTextInput.getText().toString()
                                +  " - " + description.getText().toString());

                        pending = true;
                        popupWindow.dismiss();
                    }
                });
            }
        });

        eventTextInputLayout = findViewById(R.id.events);
        eventMultiAutoCompleteTextView = (MultiAutoCompleteTextView) eventTextInputLayout.getEditText();
        EventListAdapter eventAdapter = new EventListAdapter(CreateServiceActivity.this, android.R.layout.simple_dropdown_item_1line, eventsFromDb);
        eventMultiAutoCompleteTextView.setAdapter(eventAdapter);
        eventMultiAutoCompleteTextView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        eventMultiAutoCompleteTextView.setInputType(InputType.TYPE_NULL);

        eventMultiAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Event selectedItem = (Event) parent.getItemAtPosition(position);
                if(!eventIds.contains(selectedItem.getId()))
                    eventIds.add(selectedItem.getId());
                else{
                    eventIds.remove(selectedItem.getId());
                }
            }
        });



        TextInputLayout providerTextInputLayout = findViewById(R.id.providers);
        MultiAutoCompleteTextView multiAutoCompleteTextViewProviders = (MultiAutoCompleteTextView) providerTextInputLayout.getEditText();
        ArrayAdapter<String> providerAdapter = new ArrayAdapter<>(CreateServiceActivity.this, android.R.layout.simple_dropdown_item_1line, providers);
        multiAutoCompleteTextViewProviders.setAdapter(providerAdapter);
        multiAutoCompleteTextViewProviders.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        multiAutoCompleteTextViewProviders.setInputType(InputType.TYPE_NULL);

        addImage = findViewById(R.id.add_image);
        recyclerView = findViewById(R.id.recycler);
        recyclerView.setAdapter(imageAdapter);
        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 3);
            }
        });

        name = findViewById(R.id.name);
        description = findViewById(R.id.description);
        specific = findViewById(R.id.specific);
        price = findViewById(R.id.price);
        pricePerHour = findViewById(R.id.pricePerHour);
        discount = findViewById(R.id.discount);
        duration = findViewById(R.id.duration);
        durationMin = findViewById(R.id.durationMin);
        durationMax = findViewById(R.id.durationMax);
        location = findViewById(R.id.location);
        reservationDue = findViewById(R.id.reservation_due);
        cancelationDue = findViewById(R.id.cancelation_due);
        available = findViewById(R.id.availability);
        visible = findViewById(R.id.visibility);
        automaticAffirmation = findViewById(R.id.automatic_affirmation);

        createBtn = findViewById(R.id.create_button);
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Long id = new Random().nextLong();

                Map<String, Object> doc = new HashMap<>();
                if(pending) {
                    Long subcategoryId = new Random().nextLong();

                    Map<String, Object> docSubcategory = new HashMap<>();
                    docSubcategory.put("cateogryName", requestedSubcategory.getCategoryName());
                    docSubcategory.put("name", requestedSubcategory.getName());
                    docSubcategory.put("description", requestedSubcategory.getDescription());
                    docSubcategory.put("type", requestedSubcategory.getType());

                    db.collection("SuggestedSubcategories")
                            .document(id.toString())
                            .set(docSubcategory)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(CreateServiceActivity.this, "Request for subcategory created", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(CreateServiceActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
                else
                    doc.put("categoryId", categoryId);
                doc.put("subcategoryId", subcategoryId);
                doc.put("name", name.getEditText().getText().toString());
                doc.put("description", description.getEditText().getText().toString());
                doc.put("specific", specific.getEditText().getText().toString());
                doc.put("fullPrice", Double.parseDouble(price.getEditText().getText().toString()));
                doc.put("pricePerHour", Double.parseDouble(pricePerHour.getEditText().getText().toString()));
                doc.put("discount", Double.parseDouble(discount.getEditText().getText().toString()));
                doc.put("duration", Double.parseDouble(duration.getEditText().getText().toString()));
                doc.put("durationMin", Double.parseDouble(durationMin.getEditText().getText().toString()));
                doc.put("durationMax", Double.parseDouble(durationMax.getEditText().getText().toString()));
                doc.put("location", location.getEditText().getText().toString());
                doc.put("reservationDue", reservationDue.getEditText().getText().toString());
                doc.put("cancelationDue", cancelationDue.getEditText().getText().toString());
                doc.put("eventIds", eventIds);
                doc.put("providers", Arrays.asList(providers));
                doc.put("available", available.isChecked());
                doc.put("visible", visible.isChecked());
                doc.put("automaticAffirmation", automaticAffirmation.isChecked());
                doc.put("pending", pending);
                doc.put("deleted", false);
                for (Uri image : images) {
                    Long imageFile = new Random().nextLong();
                    String imageFileName = imageFile.toString();
                    String location = "images/Services/" + imageFileName;
                    StorageReference imageStorageReference = storageRootReference.child(location);
                    imageUrls.add(location);

                    imageStorageReference.putFile(image)
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(CreateServiceActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
                doc.put("imageUrls", imageUrls);

                db.collection("Services")
                        .document(id.toString())
                        .set(doc)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(CreateServiceActivity.this, "Service created", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(CreateServiceActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && data != null && data.getData() != null){
            images.add(data.getData());
            imageAdapter = new ImageAdapter(CreateServiceActivity.this, R.layout.image_carousel_card, images);
            recyclerView.setAdapter(imageAdapter);
        }
    }

    private void getCategories(){
        categoriesFromDb = new ArrayList<>();

        db.collection("Categories")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for(DocumentSnapshot doc: task.getResult()){
                            Category category = new Category(
                                    Long.parseLong(doc.getId()),
                                    doc.getString("Name"),
                                    doc.getString("Description")
                            );

                            categoriesFromDb.add(category);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CreateServiceActivity.this, e.getMessage(), Toast.LENGTH_SHORT);
                    }
                });
    }

    private void getSubcategories(String catName){
        db.collection("Subcategories")
                .whereEqualTo("CategoryName", catName)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for(DocumentSnapshot doc: task.getResult()){
                            Subcategory subcategory = new Subcategory(
                                    Long.parseLong(doc.getId()),
                                    doc.getString("CategoryName"),
                                    doc.getString("Name"),
                                    doc.getString("Description"),
                                    (int) (long)doc.getLong("Type"));

                            subcategoriesFromDb.add(subcategory);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CreateServiceActivity.this, e.getMessage(), Toast.LENGTH_SHORT);
                    }
                });
    }

    private void getEvents(){
        eventsFromDb = new ArrayList<>();
        eventIds = new ArrayList<>();

        db.collection("Events")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for (DocumentSnapshot doc : task.getResult()) {
                            Event event = new Event(
                                    Long.parseLong(doc.getId()),
                                    doc.getString("userOdId"),
                                    doc.getString("typeEvent"),
                                    doc.getString("name"),
                                    doc.getString("description"),
                                    Integer.parseInt(String.valueOf(doc.getLong("maxPeople"))),
                                    doc.getString("locationPlace"),
                                    Integer.parseInt(String.valueOf(doc.getLong("maxDistance"))),
                                    doc.getDate("dateEvent"),
                                    doc.getBoolean("available"));

                            eventsFromDb.add(event);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CreateServiceActivity.this, e.getMessage(), Toast.LENGTH_SHORT);
                    }
                });
    }
}