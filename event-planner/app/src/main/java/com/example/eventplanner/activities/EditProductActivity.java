package com.example.eventplanner.activities;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventplanner.R;
import com.example.eventplanner.adapters.ProductListPupvAdapter;
import com.example.eventplanner.adapters.SubcategoryAdapter;
import com.example.eventplanner.databinding.ActivityEditProductBinding;
import com.example.eventplanner.model.Event;
import com.example.eventplanner.model.Product;
import com.example.eventplanner.model.Subcategory;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Collectors;

import com.example.eventplanner.adapters.EventListAdapter;
import com.example.eventplanner.adapters.ImageAdapter;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class EditProductActivity extends AppCompatActivity {

    ActivityEditProductBinding binding;
    FloatingActionButton addImage;
    TextInputLayout subcategoryTextInputLayout, eventTextInputLayout;
    MultiAutoCompleteTextView eventMultiAutoCompleteTextView;
    RecyclerView recyclerView;
    ImageAdapter imageAdapter;

    Product product;
    ArrayList<Subcategory> subcategoriesFromDb;
    ArrayList<Event> events;
    ArrayList<Event> eventsFromDb;
    ArrayList<Long> eventIds;
    ArrayList<Event> eventsToAdd;
    ArrayList<String> imagesFromDb;
    ArrayList<Uri> imagesFromDbUri;


    Long subcategoryId;
    FirebaseFirestore db;
    FirebaseStorage storage;
    StorageReference storageRootReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        subcategoriesFromDb = new ArrayList<>();
        events = new ArrayList<>();
        eventsFromDb = new ArrayList<>();
        eventsToAdd = new ArrayList<>();

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRootReference  = FirebaseStorage.getInstance().getReference();

        getEventsFromdb();

        Long productId = getIntent().getLongExtra("productId", 0);
        getProduct(productId);
    }

    private void initilazeComponents() {
        addImage = findViewById(R.id.add_image);
        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 3);
            }
        });

        getCategoryById(product.getCategoryId());

        subcategoryTextInputLayout = findViewById(R.id.subcategories);
        AutoCompleteTextView subcategoryAutoCompleteTextView = (AutoCompleteTextView) subcategoryTextInputLayout.getEditText();
        SubcategoryAdapter subcategoryAdapter = new SubcategoryAdapter(EditProductActivity.this, android.R.layout.simple_dropdown_item_1line, subcategoriesFromDb);
        subcategoryAutoCompleteTextView.setAdapter(subcategoryAdapter);


        subcategoryAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Subcategory selectedSubcategory = (Subcategory) parent.getItemAtPosition(position);
                subcategoryId = selectedSubcategory.getId();
            }
        });

        TextInputLayout name = findViewById(R.id.name);
        TextInputEditText nameAutoComplete = (TextInputEditText) name.getEditText();
        nameAutoComplete.setText(product.getName());

        TextInputLayout description = findViewById(R.id.description);
        TextInputEditText descriptionAutoComplete = (TextInputEditText) description.getEditText();
        descriptionAutoComplete.setText(product.getDescription());

        TextInputLayout price = findViewById(R.id.price);
        TextInputEditText priceAutoComplete = (TextInputEditText) price.getEditText();
        priceAutoComplete.setText(product.getPrice().toString());

        TextInputLayout discount = findViewById(R.id.discount);
        TextInputEditText discountAutoComplete = (TextInputEditText) discount.getEditText();
        discountAutoComplete.setText(product.getDiscount().toString());

        recyclerView = findViewById(R.id.recycler);
        imageAdapter = new ImageAdapter(EditProductActivity.this, R.layout.image_carousel_card, product.getImages());
        recyclerView.setAdapter(imageAdapter);

        EventListAdapter eventListAdapter = new EventListAdapter(this, R.layout.event_card ,events);
        binding.eventList.setAdapter(eventListAdapter);

        CheckBox available = findViewById(R.id.availability);
        available.setChecked(product.getAvailable());

        CheckBox visibility = findViewById(R.id.visibility);
        visibility.setChecked(product.getVisible());

        binding.addEvent.setOnClickListener(v -> {
            LayoutInflater inflater = (LayoutInflater) EditProductActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View popUpView = inflater.inflate(R.layout.popup_pick_event, null);

            popUpView.setBackground(ContextCompat.getDrawable(EditProductActivity.this, R.drawable.gradient_background_2));

            int popupWidth = ViewGroup.LayoutParams.MATCH_PARENT;
            int popupHeight = ViewGroup.LayoutParams.MATCH_PARENT;
            boolean focusable = true;
            PopupWindow popupWindow = new PopupWindow(popUpView, popupWidth, popupHeight, focusable);

            popupWindow.setElevation(10);

            popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);

            eventTextInputLayout = popUpView.findViewById(R.id.events);
            eventMultiAutoCompleteTextView = (MultiAutoCompleteTextView) eventTextInputLayout.getEditText();
            EventListAdapter eventAdapter = new EventListAdapter(EditProductActivity.this, android.R.layout.simple_dropdown_item_1line, eventsFromDb);
            eventMultiAutoCompleteTextView.setAdapter(eventAdapter);
            eventMultiAutoCompleteTextView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
            eventMultiAutoCompleteTextView.setInputType(InputType.TYPE_NULL);

            eventMultiAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Event selectedItem = (Event) parent.getItemAtPosition(position);
                    if (!eventIds.contains(selectedItem.getId())) {
                        eventIds.add(selectedItem.getId());
                        eventsToAdd.add(eventsFromDb
                                .stream()
                                .filter(e -> e.getId().equals(selectedItem.getId()))
                                .findFirst().get());
                    }
                    else{
                        eventIds.remove(selectedItem.getId());
                    }
                }
            });

            Button addEvents = popUpView.findViewById(R.id.add_events);

            addEvents.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    events.addAll(eventsToAdd
                            .stream()
                            .filter(e -> eventIds.contains(e.getId()))
                            .collect(Collectors.toList()));
                    eventsToAdd.clear();
                    EventListAdapter eventListAdapter = new EventListAdapter(EditProductActivity.this, R.layout.event_card ,events);
                    binding.eventList.setAdapter(eventListAdapter);
                    popupWindow.dismiss();
                }
            });

        });

        binding.cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DocumentReference productRef = db.collection("Products").document(product.getId().toString());

                ArrayList<Long> updatedEventIds = new ArrayList<>();

                for (Event event : events) {
                    updatedEventIds.add(event.getId());
                }

                ArrayList<Uri> images = product.getImages();
                ArrayList<String> imageUrls = new ArrayList<>();
                int i =0;
                for (Uri image : images) {
                    if(imagesFromDbUri.contains(image)) {
                        imageUrls.add(imagesFromDb.get(i++));
                        continue;
                    }
                    i++;
                    Long imageFile = new Random().nextLong();
                    String imageFileName = imageFile.toString();
                    String location = "images/" + imageFileName;
                    StorageReference imageStorageReference = storageRootReference.child(location);
                    imageUrls.add(location);

                    imageStorageReference.putFile(image)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Log.d(TAG, "Image added successfully");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(EditProductActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }

                productRef.update("subcategoryId", subcategoryId,
                                "name", nameAutoComplete.getText().toString(),
                                "description", descriptionAutoComplete.getText().toString(),
                                "price", Double.parseDouble(priceAutoComplete.getText().toString()),
                                "discount", Double.parseDouble(discountAutoComplete.getText().toString()),
                                "eventIds", updatedEventIds,
                                "imageUrls", imageUrls,
                                "available", available.isChecked(),
                                "visible", visibility.isChecked())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                finish();
                            }
                        })

                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(EditProductActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });
    }

    private void getEventsFromdb(){
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
                        Toast.makeText(EditProductActivity.this, e.getMessage(), Toast.LENGTH_SHORT);
                    }
                });
    }

    private void getProduct(Long id){
        db.collection("Products")
                .document(id.toString())
                .get()
                .addOnSuccessListener(documentSnapshot ->  {
                        if(documentSnapshot.exists()){
                            product = new Product(/*id,
                                    documentSnapshot.getLong("categoryId"),
                                    documentSnapshot.getLong("subcategoryId"),
                                    documentSnapshot.getString("name"),
                                    documentSnapshot.getString("description"),
                                    documentSnapshot.getDouble("price"),
                                    documentSnapshot.getDouble("discount"),
                                    new ArrayList<>(),
                                    (ArrayList<Long>) documentSnapshot.get("eventIds"),
                                    documentSnapshot.getBoolean("available"),
                                    documentSnapshot.getBoolean("visible"),
                                    documentSnapshot.getBoolean("pending"),
                                    documentSnapshot.getBoolean("deleted")*/);

                            imagesFromDb = (ArrayList<String>) documentSnapshot.get("imageUrls");
                            final int numImages = imagesFromDb.size();
                            final int numEvents = product.getEventTypeIds().size();

                            for (String imageUrl : imagesFromDb) {
                                StorageReference imageRef = storage.getReference().child(imageUrl);
                                imageRef.getDownloadUrl()
                                        .addOnSuccessListener(uri ->  {
                                                product.getImages().add(uri);

                                                if (product.getImages().size() == numImages) {
                                                    getEvents(numImages, numEvents);
                                                    imagesFromDbUri = new ArrayList<>(product.getImages());
                                                }
                                        })
                                        .addOnFailureListener(e -> {
                                                Toast.makeText(EditProductActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                        });
                            }


                        }
                })
                .addOnFailureListener(e ->  {
                        Toast.makeText(EditProductActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void getEvents(int numImages, int numEvents){
        ArrayList<Long> eventIds = product.getEventTypeIds();
        for(Long id: eventIds){
            db.collection("Events")
                    .document(id.toString())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        Event event = new Event(
                                Long.parseLong(documentSnapshot.getId()),
                                documentSnapshot.getString("userOdId"),
                                documentSnapshot.getString("typeEvent"),
                                documentSnapshot.getString("name"),
                                documentSnapshot.getString("description"),
                                Integer.parseInt(String.valueOf(documentSnapshot.getLong("maxPeople"))),
                                documentSnapshot.getString("locationPlace"),
                                Integer.parseInt(String.valueOf(documentSnapshot.getLong("maxDistance"))),
                                documentSnapshot.getDate("dateEvent"),
                                documentSnapshot.getBoolean("available"));

                        events.add(event);

                        if(events.size() == numEvents) {
                            initilazeComponents();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(EditProductActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }

    private void getCategoryById(Long productCategory) {
        db.collection("Categories")
                .document(productCategory.toString())
                .get()
                .addOnSuccessListener(documentSnapshot ->  {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("Name");
                            TextInputLayout category = findViewById(R.id.category);
                            TextInputEditText categoryAutoComplete = (TextInputEditText) category.getEditText();
                            categoryAutoComplete.setText(name);

                            getSubcategories(name);
                        }
                })
                .addOnFailureListener(e ->  {
                        Toast.makeText(EditProductActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void getSubcategoryById(Long id){
        db.collection("Subcategories")
                .document(id.toString())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("Name");
                            String descirption = documentSnapshot.getString("Description");

                            subcategoryTextInputLayout.getEditText().setText(name + " - " + descirption);
                            subcategoryId = Long.parseLong(documentSnapshot.getId());
                        }
                })
                .addOnFailureListener(e -> {
                        Toast.makeText(EditProductActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && data != null){
            product.getImages().add(data.getData());
            Uri selectedImage = data.getData();
            imageAdapter = new ImageAdapter(EditProductActivity.this, R.layout.image_carousel_card, product.getImages());
            recyclerView.setAdapter(imageAdapter);
        }
    }

    private void getSubcategories(String catName){
        db.collection("Subcategories")
                .whereEqualTo("CategoryName", catName)
                .get()
                .addOnCompleteListener(task ->  {
                        for(DocumentSnapshot doc: task.getResult()){
                            Subcategory subcategory = new Subcategory(
                                    Long.parseLong(doc.getId()),
                                    doc.getString("CategoryName"),
                                    doc.getString("Name"),
                                    doc.getString("Description"),
                                    (int) (long)doc.getLong("Type"));

                            subcategoriesFromDb.add(subcategory);

                            if(subcategory.getId().equals(product.getSubcategoryId())) {
                                String name = subcategory.getName();
                                String descirption = subcategory.getDescription();

                                subcategoryTextInputLayout.getEditText().setText(name + " - " + descirption);
                                subcategoryId = subcategory.getId();
                            }
                        }
                })
                .addOnFailureListener(e -> {
                        Toast.makeText(EditProductActivity.this, e.getMessage(), Toast.LENGTH_SHORT);
                });
    }
}