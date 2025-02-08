package com.example.eventplanner.activities;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventplanner.R;
import com.example.eventplanner.adapters.CategoryListAdapter;
import com.example.eventplanner.adapters.EventListAdapter;
import com.example.eventplanner.adapters.ImageAdapter;
import com.example.eventplanner.adapters.ProductListAdapter;
import com.example.eventplanner.adapters.ProductListPupvAdapter;
import com.example.eventplanner.adapters.ServiceListAdapter;
import com.example.eventplanner.adapters.ServiceListPupvAdapter;
import com.example.eventplanner.adapters.SubcategoryAdapter;
import com.example.eventplanner.model.Category;
import com.example.eventplanner.model.Event;
import com.example.eventplanner.model.Subcategory;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.example.eventplanner.adapters.ProductListAddAdapter;
import com.example.eventplanner.adapters.ServiceListAddAdapter;
import com.example.eventplanner.model.Product;
import com.example.eventplanner.model.Service;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class CreatePackageActivity extends AppCompatActivity implements ProductListAdapter.OnItemRemovedListener, ServiceListAdapter.OnItemRemovedListener {

    Button addProduct, addService, createBtn;
    FloatingActionButton addImage;
    TextInputLayout subcategoryTextInputLayout, eventTextInputLayout, reservationDueTextInputLayout, cancelationDueTextInputLayout,
            name, description, price, discount;
    TextInputEditText editPrice, editReservationDue, editCancelationDue;
    AutoCompleteTextView subcategoryAutoCompleteTextView, eventAutoCompleteTextView;
    CheckBox available, visible, automaticAffirmation;
    MultiAutoCompleteTextView eventMultiAutoCompleteTextView;
    RecyclerView recyclerView;
    ListView listview, serviceListview;

    ArrayList<Event> eventsFromDb;
    ArrayList<Category> categoriesFromDb;
    ArrayList<Subcategory> subcategoriesFromDb;

    ArrayList<Long> eventIds;
    ArrayList<Long> subcategoryIds;
    ArrayList<Long> productIds;
    ArrayList<Long> serviceIds;
    Long categoryId;
    String categoryName;
    Long subcategoryId;
    ArrayList<Uri> images;
    ArrayList<Uri> packageImages;
    ArrayList<String> imageUrls;
    ArrayList<Subcategory> packageSubcategories;
    ArrayList<Event> packageEvents;
    Double priceSum;
    Boolean pending;

    ArrayList<Product> products;
    ArrayList<Service> services;

    ArrayList<Product> productsFromDb;
    ArrayList<Service> servicesFromDb;

    ProductListAdapter productListAdapter;
    ServiceListAdapter serviceListAdapter;
    SubcategoryAdapter subcategoryAdapter;
    ImageAdapter imageAdapter;
    EventListAdapter eventListAdapter;

    FirebaseFirestore db;
    FirebaseStorage storage;
    StorageReference storageRootReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_package);

        eventIds = new ArrayList<>();
        subcategoryIds = new ArrayList<>();
        productIds = new ArrayList<>();
        serviceIds = new ArrayList<>();
        images = new ArrayList<>();
        packageImages = new ArrayList<>();
        imageUrls = new ArrayList<>();
        subcategoriesFromDb = new ArrayList<>();
        products = new ArrayList<>();
        services = new ArrayList<>();
        productsFromDb = new ArrayList<>();
        servicesFromDb = new ArrayList<>();
        packageSubcategories = new ArrayList<>();
        packageEvents = new ArrayList<>();

        priceSum = 0.0;

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRootReference = storage.getReference();

        getCategories();

        recyclerView = findViewById(R.id.recycler);

        TextInputLayout textInputLayout = findViewById(R.id.categories);
        AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) textInputLayout.getEditText();
        CategoryListAdapter categoryAdapter = new CategoryListAdapter(CreatePackageActivity.this, android.R.layout.simple_dropdown_item_1line, categoriesFromDb);
        autoCompleteTextView.setAdapter(categoryAdapter);

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Category selectedCategory = (Category) parent.getItemAtPosition(position);
                categoryId = selectedCategory.getId();
                categoryName = selectedCategory.getName();
                subcategoriesFromDb.clear();

                getProducts();
                getServices();
            }
        });

        subcategoryTextInputLayout = findViewById(R.id.subcategories);
        eventTextInputLayout = findViewById(R.id.events);

        TextInputLayout priceTextInputLayout = findViewById(R.id.price);
        editPrice = findViewById(R.id.editPrice);
        editPrice.setFocusable(false);
        editPrice.setClickable(false);

        reservationDueTextInputLayout = findViewById(R.id.reservation_due);
        editReservationDue = findViewById(R.id.edit_reservation_due);

        cancelationDueTextInputLayout = findViewById(R.id.cancelation_due);
        editCancelationDue = findViewById(R.id.edit_cancelation_due);

        addProduct = findViewById(R.id.add_product);

        addProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = (LayoutInflater) CreatePackageActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View popUpView = inflater.inflate(R.layout.add_product, null);

                ListView listView = popUpView.findViewById(R.id.productsList);
                ProductListAddAdapter productListAddAdapter = new ProductListAddAdapter(CreatePackageActivity.this, new ProductListAddAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Product item) {
                        if(!products.contains(item)) {
                            products.add(item);
                            for(Uri productImage : item.getImages()){
                                packageImages.add(productImage);
                            }
                            imageAdapter = new ImageAdapter(CreatePackageActivity.this, R.layout.image_carousel_card_package, packageImages);
                            recyclerView.setAdapter(imageAdapter);

                            for(Long eventId : item.getEventTypeIds()){
                                getEventById(eventId);
                            }

                            eventAutoCompleteTextView = (AutoCompleteTextView) eventTextInputLayout.getEditText();
                            eventListAdapter = new EventListAdapter(CreatePackageActivity.this, R.layout.event_card_package, packageEvents);
                            eventAutoCompleteTextView.setAdapter(eventListAdapter);
                            eventAutoCompleteTextView.setEnabled(false);
                            eventAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                }
                            });

                            productListAdapter = new ProductListAdapter(CreatePackageActivity.this, R.layout.product_card_package, products);
                            productListAdapter.setOnItemRemovedListener(CreatePackageActivity.this);
                            listview = findViewById(R.id.product_list);
                            listview.setAdapter(productListAdapter);

                            getSubcategoryById(item.getSubcategoryId());
                            priceSum += item.getPrice();
                            editPrice.setText(priceSum.toString());
                        }
                    }
                }, productsFromDb);
                listView.setAdapter(productListAddAdapter);

                int width = ViewGroup.LayoutParams.MATCH_PARENT;
                int height = ViewGroup.LayoutParams.MATCH_PARENT;
                boolean focusable = true;
                PopupWindow popupWindow = new PopupWindow(popUpView, width, height, focusable);

                popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
            }
        });

        addService = findViewById(R.id.add_service);

        addService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = (LayoutInflater) CreatePackageActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View popUpView = inflater.inflate(R.layout.add_service, null);

                ListView listView = popUpView.findViewById(R.id.service_list);
                ServiceListAddAdapter serviceListAddAdapter = new ServiceListAddAdapter(CreatePackageActivity.this, new ServiceListAddAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Service item) {
                        if(!services.contains(item)) {
                            services.add(item);
                            for(Uri serviceImage : item.getImages()){
                                packageImages.add(serviceImage);
                            }
                            imageAdapter = new ImageAdapter(CreatePackageActivity.this, R.layout.image_carousel_card_package, packageImages);
                            recyclerView.setAdapter(imageAdapter);

                            for(Long eventId : item.getEventTypeIds()){
                                getEventById(eventId);
                            }

                            eventAutoCompleteTextView = (AutoCompleteTextView) eventTextInputLayout.getEditText();
                            eventListAdapter = new EventListAdapter(CreatePackageActivity.this, R.layout.event_card_package, packageEvents);
                            eventAutoCompleteTextView.setAdapter(eventListAdapter);
                            eventAutoCompleteTextView.setEnabled(false);
                            eventAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                }
                            });

                            serviceListAdapter = new ServiceListAdapter(CreatePackageActivity.this, R.layout.service_card_package, services);
                            serviceListAdapter.setOnItemRemovedListener(CreatePackageActivity.this);
                            serviceListview = findViewById(R.id.service_list);
                            serviceListview.setAdapter(serviceListAdapter);

                            getSubcategoryById(item.getSubcategoryId());
                            priceSum += item.getFullPrice();
                            editPrice.setText(priceSum.toString());

                            Integer min = !editReservationDue.getText().toString().isEmpty() ? Integer.parseInt(editReservationDue.getText().toString()) : -1;
                            Integer reservationDue = 0;
                            for(Service service : services){
                                reservationDue = Integer.parseInt(service.getReservationDue());
                                if(reservationDue < min)
                                    min = reservationDue;
                            }
                            editReservationDue.setText(reservationDue.toString());
                            editReservationDue.setFocusable(false);
                            editReservationDue.setClickable(false);

                            Integer minCancelation = !editCancelationDue.getText().toString().isEmpty() ? Integer.parseInt(editCancelationDue.getText().toString()) : -1;
                            Integer cancelationDue = 0;
                            for(Service service : services){
                                cancelationDue = Integer.parseInt(service.getCancelationDue());
                                if(cancelationDue < minCancelation)
                                    minCancelation = cancelationDue;
                            }
                            editCancelationDue.setText(cancelationDue.toString());
                            editCancelationDue.setFocusable(false);
                            editCancelationDue.setClickable(false);

                            automaticAffirmation = findViewById(R.id.automatic_affirmation);
                            automaticAffirmation.setChecked(services.stream().filter(s -> s.getAutomaticAffirmation()).count() != 0);
                            automaticAffirmation.setEnabled(false);
                        }
                    }
                }, servicesFromDb);
                listView.setAdapter(serviceListAddAdapter);

                int width = ViewGroup.LayoutParams.MATCH_PARENT;
                int height = ViewGroup.LayoutParams.MATCH_PARENT;
                boolean focusable = true;
                PopupWindow popupWindow = new PopupWindow(popUpView, width, height, focusable);

                popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
            }
        });

        name = findViewById(R.id.name);
        description = findViewById(R.id.description);
        discount = findViewById(R.id.discount);
        available = findViewById(R.id.availability);
        visible = findViewById(R.id.visibility);

        Button create = findViewById(R.id.create_button);

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Long id = new Random().nextLong();

                Map<String, Object> doc = new HashMap<>();

                doc.put("name", name.getEditText().getText().toString());
                doc.put("description", description.getEditText().getText().toString());
                doc.put("discount", Double.parseDouble(discount.getEditText().getText().toString()));
                doc.put("available", available.isChecked());
                doc.put("visible", visible.isChecked());
                doc.put("eventIds", eventIds);
                doc.put("categoryId", categoryId);
                for(Subcategory subcateg : packageSubcategories){
                    subcategoryIds.add(subcateg.getId());
                }
                doc.put("subcategoryIds", subcategoryIds);
                for(Product p : products){
                    productIds.add(p.getId());
                }
                doc.put("productIds", productIds);
                for(Service s: services){
                    serviceIds.add(s.getId());
                }
                doc.put("serviceIds", serviceIds);
                doc.put("price", priceSum);
                doc.put("reservationDue", reservationDueTextInputLayout.getEditText().getText().toString());
                doc.put("cancelationDue", cancelationDueTextInputLayout.getEditText().getText().toString());
                doc.put("automaticAffirmation", automaticAffirmation.isChecked());
                doc.put("deleted", false);
                for (Uri image : packageImages) {
                    String path = image.toString().substring(image.toString().indexOf("/o/") + 3, image.toString().indexOf("?"));
                    String decodedPath = path.replace("%2F", "/");
                    imageUrls.add(decodedPath);
                }
                doc.put("imageUrls", imageUrls);

                db.collection("Packages")
                        .document(id.toString())
                        .set(doc)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(CreatePackageActivity.this, "Package created", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(CreatePackageActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    @Override
    public void onProductRemoved(Product removedItem) {
        priceSum -= removedItem.getPrice();
        editPrice.setText(priceSum.toString());

        for(Uri img: removedItem.getImages()){
            packageImages.remove(img);
        }
        imageAdapter = new ImageAdapter(CreatePackageActivity.this, R.layout.image_carousel_card_package, packageImages);
        recyclerView.setAdapter(imageAdapter);

        for(Long eventId: removedItem.getEventTypeIds()){
            if(!(products.stream().filter(p -> p.getEventTypeIds().contains(eventId)).count() != 0
                || services.stream().filter(s -> s.getEventTypeIds().contains(eventId)).count() != 0)){
                packageEvents.removeIf(p -> p.getId().equals(eventId));
                eventIds.remove(eventId);
                eventListAdapter = new EventListAdapter(CreatePackageActivity.this, R.layout.event_card_package, packageEvents);
                eventAutoCompleteTextView.setAdapter(eventListAdapter);
            }
        }

        if(!(products.stream().filter(p -> p.getSubcategoryId().equals(removedItem.getSubcategoryId())).count() != 0
                || services.stream().filter(s -> s.getSubcategoryId().equals(removedItem.getSubcategoryId())).count() != 0)){
            packageSubcategories.removeIf(s -> s.getId().equals(removedItem.getSubcategoryId()));

            subcategoryAdapter = new SubcategoryAdapter(CreatePackageActivity.this, android.R.layout.simple_dropdown_item_1line, packageSubcategories);
            subcategoryAutoCompleteTextView.setAdapter(subcategoryAdapter);
        }
    }

    @Override
    public void onServiceRemoved(Service removedItem) {
        priceSum -= removedItem.getFullPrice();
        editPrice.setText(priceSum.toString());

        for(Uri img: removedItem.getImages()){
            packageImages.remove(img);
        }
        imageAdapter = new ImageAdapter(CreatePackageActivity.this, R.layout.image_carousel_card_package, packageImages);
        recyclerView.setAdapter(imageAdapter);

        for(Long eventId: removedItem.getEventTypeIds()){
            if(!(products.stream().filter(p -> p.getEventTypeIds().contains(eventId)).count() != 0
                    || services.stream().filter(s -> s.getEventTypeIds().contains(eventId)).count() != 0)){
                packageEvents.removeIf(p -> p.getId().equals(eventId));
                eventIds.remove(eventId);
                eventListAdapter = new EventListAdapter(CreatePackageActivity.this, R.layout.event_card_package, packageEvents);
                eventAutoCompleteTextView.setAdapter(eventListAdapter);
            }
        }

        if(!(products.stream().filter(p -> p.getSubcategoryId().equals(removedItem.getSubcategoryId())).count() != 0
                || services.stream().filter(s -> s.getSubcategoryId().equals(removedItem.getSubcategoryId())).count() != 0)){
            packageSubcategories.removeIf(s -> s.getId().equals(removedItem.getSubcategoryId()));

            subcategoryAdapter = new SubcategoryAdapter(CreatePackageActivity.this, android.R.layout.simple_dropdown_item_1line, packageSubcategories);
            subcategoryAutoCompleteTextView.setAdapter(subcategoryAdapter);
        }

        Integer min = !editReservationDue.getText().toString().isEmpty() ? Integer.parseInt(editReservationDue.getText().toString()) : -1;
        Integer reservationDue = 0;
        for(Service service : services){
            reservationDue = Integer.parseInt(service.getReservationDue());
            if(reservationDue < min)
                min = reservationDue;
        }
        editReservationDue.setText(reservationDue.toString());
        editReservationDue.setFocusable(false);
        editReservationDue.setClickable(false);

        Integer minCancelation = !editCancelationDue.getText().toString().isEmpty() ? Integer.parseInt(editCancelationDue.getText().toString()) : -1;
        Integer cancelationDue = 0;
        for(Service service : services){
            cancelationDue = Integer.parseInt(service.getCancelationDue());
            if(cancelationDue < minCancelation)
                minCancelation = cancelationDue;
        }
        editCancelationDue.setText(cancelationDue.toString());
        editCancelationDue.setFocusable(false);
        editCancelationDue.setClickable(false);

        automaticAffirmation = findViewById(R.id.automatic_affirmation);
        automaticAffirmation.setChecked(services.stream().filter(s -> s.getAutomaticAffirmation()).count() != 0);
        automaticAffirmation.setEnabled(false);
    }

    private void getEventById(Long id){
        db.collection("Events")
                .document(id.toString())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
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

                        if(!packageEvents.contains(event)) {
                            packageEvents.add(event);
                            eventIds.add(event.getId());
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CreatePackageActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void getSubcategoryById(Long id){
        db.collection("Subcategories")
                .document(id.toString())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long subcateogryId = Long.parseLong(documentSnapshot.getId());
                        String categoryName = documentSnapshot.getString("CategoryName");
                        String name = documentSnapshot.getString("Name");
                        String descirption = documentSnapshot.getString("Description");
                        int type =  ((Number)documentSnapshot.get("Type")).intValue();


                        if(!packageSubcategories.contains(new Subcategory(subcateogryId, categoryName, name, descirption, type)))
                            packageSubcategories.add(new Subcategory(subcateogryId, categoryName, name, descirption, type));

                        subcategoryAutoCompleteTextView = (AutoCompleteTextView) subcategoryTextInputLayout.getEditText();
                        subcategoryAdapter = new SubcategoryAdapter(CreatePackageActivity.this, android.R.layout.simple_dropdown_item_1line, packageSubcategories);
                        subcategoryAutoCompleteTextView.setAdapter(subcategoryAdapter);
                        subcategoryAutoCompleteTextView.setEnabled(false);
                        subcategoryAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CreatePackageActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void getCategories() {
        categoriesFromDb = new ArrayList<>();

        db.collection("Categories")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for (DocumentSnapshot doc : task.getResult()) {
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
                        Toast.makeText(CreatePackageActivity.this, e.getMessage(), Toast.LENGTH_SHORT);
                    }
                });
    }

    private void getProducts(){
        productsFromDb = new ArrayList<>();

        db.collection("Products")
                .whereEqualTo("pending", false)
                .whereEqualTo("deleted", false)
                .whereEqualTo("categoryId", categoryId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            final List<DocumentSnapshot> productDocs = task.getResult().getDocuments();
                            final int numProducts = productDocs.size();
                            final int[] productsProcessed = {0};

                            for (DocumentSnapshot doc : productDocs) {
                                Product product = new Product(
                                        /*Long.parseLong(doc.getId()),
                                        doc.getLong("categoryId"),
                                        doc.getLong("subcategoryId"),
                                        doc.getString("name"),
                                        doc.getString("description"),
                                        ((Number) doc.get("price")).doubleValue(),
                                        ((Number) doc.get("discount")).doubleValue(),
                                        new ArrayList<>(), //images
                                        (ArrayList<Long>) doc.get("eventIds"),
                                        doc.getBoolean("available"),
                                        doc.getBoolean("visible"),
                                        doc.getBoolean("pending"),
                                        doc.getBoolean("deleted")*/);

                                ArrayList<String> imageUrls = (ArrayList<String>) doc.get("imageUrls");
                                final int numImages = imageUrls.size();

                                for (String imageUrl : imageUrls) {
                                    StorageReference imageRef = storage.getReference().child(imageUrl);
                                    imageRef.getDownloadUrl()
                                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    product.getImages().add(uri);

                                                    if (product.getImages().size() == numImages) {
                                                        productsProcessed[0]++;

                                                        if (productsProcessed[0] == numProducts) {
                                                            ProductListPupvAdapter productListAdapter = new ProductListPupvAdapter(CreatePackageActivity.this, products);
                                                            ListView productsListView = findViewById(R.id.product_list);
                                                            productsListView.setAdapter(productListAdapter);
                                                            productsListView.setClickable(true);
                                                        }
                                                    }
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(CreatePackageActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                                }
                                            });
                                }

                                productsFromDb.add(product);
                            }
                        } else {
                            Toast.makeText(CreatePackageActivity.this, "Failed to fetch products: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CreatePackageActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void getServices(){
        servicesFromDb = new ArrayList<>();

        db.collection("Services")
                .whereEqualTo("pending", false)
                .whereEqualTo("deleted", false)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            final List<DocumentSnapshot> serviceDocs = task.getResult().getDocuments();
                            final int numServices = serviceDocs.size();
                            final int[] servicesProccessed = {0};

                            for (DocumentSnapshot doc : serviceDocs) {
                                Service service = new Service(
                                        /*Long.parseLong(doc.getId()),
                                        doc.getLong("categoryId"),
                                        doc.getLong("subcategoryId"),
                                        doc.getString("name"),
                                        doc.getString("description"),
                                        new ArrayList<>(), //images
                                        doc.getString("specific"),
                                        ((Number) doc.get("pricePerHour")).doubleValue(),
                                        ((Number) doc.get("fullPrice")).doubleValue(),
                                        ((Number) doc.get("duration")).doubleValue(),
                                        ((Number) doc.get("durationMin")).doubleValue(),
                                        ((Number) doc.get("durationMax")).doubleValue(),
                                        doc.getString("location"),
                                        ((Number) doc.get("discount")).doubleValue(),
                                        (ArrayList<String>) doc.get("providers"),
                                        (ArrayList<Long>) doc.get("eventIds"),
                                        doc.getString("reservationDue"),
                                        doc.getString("cancelationDue"),
                                        doc.getBoolean("automaticAffirmation"),
                                        doc.getBoolean("available"),
                                        doc.getBoolean("visible"),
                                        doc.getBoolean("pending"),
                                        doc.getBoolean("deleted")*/);

                                ArrayList<String> imageUrls = (ArrayList<String>) doc.get("imageUrls");
                                final int numImages = imageUrls.size();

                                for (String imageUrl : imageUrls) {
                                    StorageReference imageRef = storage.getReference().child(imageUrl);
                                    imageRef.getDownloadUrl()
                                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    service.getImages().add(uri);

                                                    if (service.getImages().size() == numImages) {
                                                        servicesProccessed[0]++;

                                                        if (servicesProccessed[0] == numServices) {
                                                            ServiceListPupvAdapter productListAdapter = new ServiceListPupvAdapter(CreatePackageActivity.this, services);
                                                            ListView serviceListView = findViewById(R.id.service_list);
                                                            serviceListView.setAdapter(productListAdapter);
                                                            serviceListView.setClickable(true);
                                                        }
                                                    }
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(CreatePackageActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                                }
                                            });
                                }

                                servicesFromDb.add(service);
                            }
                        } else {
                            Toast.makeText(CreatePackageActivity.this, "Failed to fetch services: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CreatePackageActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}