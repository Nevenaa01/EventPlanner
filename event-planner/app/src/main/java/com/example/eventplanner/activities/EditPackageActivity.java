package com.example.eventplanner.activities;

import android.content.Context;
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
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventplanner.R;
import com.example.eventplanner.adapters.EventListAdapter;
import com.example.eventplanner.adapters.ProductListAdapter;
import com.example.eventplanner.adapters.ProductListPupvAdapter;
import com.example.eventplanner.adapters.ServiceListAdapter;
import com.example.eventplanner.adapters.ServiceListPupvAdapter;
import com.example.eventplanner.adapters.SubcategoryAdapter;
import com.example.eventplanner.databinding.ActivityEditPackageBinding;
import com.example.eventplanner.model.Event;
import com.example.eventplanner.model.Package;
import com.example.eventplanner.model.Subcategory;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.example.eventplanner.adapters.EventListPackageAdapter;
import com.example.eventplanner.adapters.ImageAdapter;
import com.example.eventplanner.adapters.PackageProductListAdapter;
import com.example.eventplanner.adapters.PackageServiceListAdapter;
import com.example.eventplanner.adapters.ProductListAddAdapter;
import com.example.eventplanner.adapters.ServiceListAddAdapter;
import com.example.eventplanner.model.Product;
import com.example.eventplanner.model.Service;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class EditPackageActivity extends AppCompatActivity implements ProductListAdapter.OnItemRemovedListener, ServiceListAdapter.OnItemRemovedListener{

    ActivityEditPackageBinding binding;
    Button addProduct, addService;
    TextInputLayout subcategoryTextInputLayout, eventTextInputLayout, reservationDueTextInputLayout, cancelationDueTextInputLayout;

    AutoCompleteTextView subcategoryAutoCompleteTextView, eventAutoCompleteTextView;

    ArrayList<Package> packages;
    Package pckg;

    ArrayList<String> imagesFromDb;
    ArrayList<Uri> imagesFromDbUri;
    ArrayList<String> imagesFromDbProduct;
    ArrayList<Uri> imagesFromDbUriProduct;
    ArrayList<String> imagesFromDbService;
    ArrayList<Uri> imagesFromDbUriService;
    ArrayList<Long> eventIds;

    ArrayList<Event> packageEvents;
    ArrayList<Product> products;
    ArrayList<Service> services;

    ArrayList<Product> productsFromDb;
    ArrayList<Service> servicesFromDb;

    ArrayList<Subcategory> subcategories;

    Double priceSum;

    FirebaseFirestore db;
    FirebaseStorage storage;
    StorageReference storageRootReference;

    ProductListAdapter productListAdapter;
    ServiceListAdapter serviceListAdapter;
    EventListAdapter eventListAdapter;
    ImageAdapter imageAdapter;
    SubcategoryAdapter subcategoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditPackageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        packages = new ArrayList<>();
        products = new ArrayList<>();
        services = new ArrayList<>();
        packageEvents = new ArrayList<>();
        eventIds = new ArrayList<>();
        subcategories = new ArrayList<>();

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRootReference  = FirebaseStorage.getInstance().getReference();

        subcategoryTextInputLayout = findViewById(R.id.subcategories);

        Long packageId = getIntent().getLongExtra("Id", -1);
        getPackageById(packageId);
    }

    private void initilazeComponents(){
        RecyclerView recyclerView = findViewById(R.id.recycler);
        ImageAdapter adapter = new ImageAdapter(EditPackageActivity.this, R.layout.image_carousel_card_package, pckg.getImages());
        recyclerView.setAdapter(adapter);

        TextInputLayout name = findViewById(R.id.name);
        TextInputEditText nameAutoComplete = (TextInputEditText) name.getEditText();
        nameAutoComplete.setText(pckg.getName());

        TextInputLayout description = findViewById(R.id.description);
        TextInputEditText descriptionAutoComplete = (TextInputEditText) description.getEditText();
        descriptionAutoComplete.setText(pckg.getDescription());

        TextInputLayout price = findViewById(R.id.price);
        TextInputEditText fullPriceAutoComplete = (TextInputEditText) price.getEditText();
        fullPriceAutoComplete.setText(pckg.getPrice().toString());

        TextInputLayout discount = findViewById(R.id.discount);
        TextInputEditText discountAutoComplete = (TextInputEditText) discount.getEditText();
        discountAutoComplete.setText(pckg.getDiscount().toString());

        eventListAdapter = new EventListAdapter(this, R.layout.event_card_package ,packageEvents);
        binding.events.setAdapter(eventListAdapter);

        productListAdapter = new ProductListAdapter(this, R.layout.product_card_package ,products);
        binding.productList.setAdapter(productListAdapter);

        serviceListAdapter = new ServiceListAdapter(this, R.layout.service_card_package, services);
        binding.serviceList.setAdapter(serviceListAdapter);

        TextInputLayout reservationDue = findViewById(R.id.reservation_due);
        TextInputEditText reservationDueAutoComplete = (TextInputEditText) reservationDue.getEditText();
        reservationDueAutoComplete.setText(pckg.getReservationDue());

        TextInputLayout cancelationDue = findViewById(R.id.cancelation_due);
        TextInputEditText cancelationDueAutoComplete = (TextInputEditText) cancelationDue.getEditText();
        cancelationDueAutoComplete.setText(pckg.getCancelationDue());

        CheckBox automaticAffirmation = findViewById(R.id.automatic_affirmation);
        automaticAffirmation.setChecked(pckg.getAutomaticAffirmation());

        CheckBox available = findViewById(R.id.availability);
        available.setChecked(pckg.getAvailable());

        CheckBox visibility = findViewById(R.id.visibility);
        visibility.setChecked(pckg.getVisible());


        addProduct = findViewById(R.id.add_product);

        addProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = (LayoutInflater) EditPackageActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View popUpView = inflater.inflate(R.layout.add_product, null);

                ListView listView = popUpView.findViewById(R.id.productsList);
                ProductListAddAdapter productListAddAdapter = new ProductListAddAdapter(EditPackageActivity.this, new ProductListAddAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Product item) {
                        if(!products.contains(item)) {
                            products.add(item);
                            for(Uri productImage : item.getImages()){
                                pckg.getImages().add(productImage);
                            }
                            imageAdapter = new ImageAdapter(EditPackageActivity.this, R.layout.image_carousel_card_package, pckg.getImages());
                            recyclerView.setAdapter(imageAdapter);

                            for(Long eventId : item.getEventTypeIds()){
                                getEventById(eventId);
                            }

                            eventListAdapter = new EventListAdapter(EditPackageActivity.this, R.layout.event_card_package, packageEvents);
                            binding.events.setAdapter(eventListAdapter);

                            productListAdapter = new ProductListAdapter(EditPackageActivity.this, R.layout.product_card_package, products);
                            productListAdapter.setOnItemRemovedListener(EditPackageActivity.this);
                            binding.productList.setAdapter(productListAdapter);

                            getSubcategoryById(item.getSubcategoryId());
                            priceSum += item.getPrice();
                            binding.editPrice.setText(priceSum.toString());
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
                LayoutInflater inflater = (LayoutInflater) EditPackageActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View popUpView = inflater.inflate(R.layout.add_service, null);

                ListView listView = popUpView.findViewById(R.id.service_list);
                ServiceListAddAdapter serviceListAddAdapter = new ServiceListAddAdapter(EditPackageActivity.this, new ServiceListAddAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Service item) {
                        if(!services.contains(item)) {
                            services.add(item);
                            for(Uri serviceImage : item.getImages()){
                                pckg.getImages().add(serviceImage);
                            }
                            imageAdapter = new ImageAdapter(EditPackageActivity.this, R.layout.image_carousel_card_package, pckg.getImages());
                            recyclerView.setAdapter(imageAdapter);

                            for(Long eventId : item.getEventTypeIds()){
                                getEventById(eventId);
                            }

                            eventListAdapter = new EventListAdapter(EditPackageActivity.this, R.layout.event_card_package, packageEvents);
                            binding.events.setAdapter(eventListAdapter);

                            serviceListAdapter = new ServiceListAdapter(EditPackageActivity.this, R.layout.service_card_package, services);
                            serviceListAdapter.setOnItemRemovedListener(EditPackageActivity.this);
                            binding.serviceList.setAdapter(serviceListAdapter);

                            getSubcategoryById(item.getSubcategoryId());
                            priceSum += item.getFullPrice();
                            binding.editPrice.setText(priceSum.toString());

                            Integer min = !binding.editReservationDue.getText().toString().isEmpty() ? Integer.parseInt(binding.editReservationDue.getText().toString()) : -1;
                            Integer reservationDue = 0;
                            for(Service service : services){
                                reservationDue = Integer.parseInt(service.getReservationDue());
                                if(reservationDue < min)
                                    min = reservationDue;
                            }
                            binding.editReservationDue.setText(reservationDue.toString());
                            binding.editReservationDue.setFocusable(false);
                            binding.editReservationDue.setClickable(false);

                            Integer minCancelation = !binding.editCancelationDue.getText().toString().isEmpty() ? Integer.parseInt(binding.editCancelationDue.getText().toString()) : -1;
                            Integer cancelationDue = 0;
                            for(Service service : services){
                                cancelationDue = Integer.parseInt(service.getCancelationDue());
                                if(cancelationDue < minCancelation)
                                    minCancelation = cancelationDue;
                            }
                            binding.editCancelationDue.setText(cancelationDue.toString());
                            binding.editCancelationDue.setFocusable(false);
                            binding.editCancelationDue.setClickable(false);

                            binding.automaticAffirmation.setChecked(services.stream().filter(s -> s.getAutomaticAffirmation()).count() != 0);
                            binding.automaticAffirmation.setEnabled(false);
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

        binding.cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DocumentReference packageRef = db.collection("Packages").document(pckg.getId().toString());

                ArrayList<Long> updatedEventIds = new ArrayList<>();

                for (Event event : packageEvents) {
                    updatedEventIds.add(event.getId());
                }
                ArrayList<Long> subcategoryIds = new ArrayList<>();
                for(Subcategory subcateg : subcategories){
                    subcategoryIds.add(subcateg.getId());
                }
                ArrayList<Long> productIds = new ArrayList<>();
                for(Product p : products){
                    productIds.add(p.getId());
                }
                ArrayList<Long> serviceIds = new ArrayList<>();
                for(Service s: services){
                    serviceIds.add(s.getId());
                }
                ArrayList<String> imageUrls = new ArrayList<>();
                for (Uri image : pckg.getImages()) {
                    String path = image.toString().substring(image.toString().indexOf("/o/") + 3, image.toString().indexOf("?"));
                    String decodedPath = path.replace("%2F", "/");
                    imageUrls.add(decodedPath);
                }

                packageRef.update(
                                "name", binding.name.getEditText().getText().toString(),
                                "description", binding.description.getEditText().getText().toString(),
                                "discount", Double.parseDouble(binding.discount.getEditText().getText().toString()),
                                "available", binding.availability.isChecked(),
                                "visible", binding.visibility.isChecked(),
                                "subcategoryIds", subcategoryIds,
                                "productIds", productIds,
                                "serviceIds", serviceIds,
                                "eventIds", updatedEventIds,
                                "price", Double.parseDouble(binding.price.getEditText().getText().toString()),
                                "reservationDue", binding.reservationDue.getEditText().getText().toString(),
                                "cancelationDue", binding.cancelationDue.getEditText().getText().toString(),
                                "imageUrls", imageUrls,
                                "automaticAffirmation", binding.automaticAffirmation.isChecked())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(EditPackageActivity.this, "Package updated", Toast.LENGTH_LONG).show();

                                finish();
                            }
                        })

                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(EditPackageActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });
    }

    @Override
    public void onProductRemoved(Product removedItem) {
        priceSum -= removedItem.getPrice();
        binding.editPrice.setText(priceSum.toString());

        for(Uri img: removedItem.getImages()){
            pckg.getImages().remove(img);
        }
        imageAdapter = new ImageAdapter(EditPackageActivity.this, R.layout.image_carousel_card_package, pckg.getImages());
        binding.recycler.setAdapter(imageAdapter);

        for(Long eventId: removedItem.getEventTypeIds()){
            if(!(products.stream().filter(p -> p.getEventTypeIds().contains(eventId)).count() != 0
                    || services.stream().filter(s -> s.getEventTypeIds().contains(eventId)).count() != 0)){
                packageEvents.removeIf(p -> p.getId().equals(eventId));
                eventIds.remove(eventId);
                eventListAdapter = new EventListAdapter(EditPackageActivity.this, R.layout.event_card_package, packageEvents);
                binding.events.setAdapter(eventListAdapter);
            }
        }

        if(!(products.stream().filter(p -> p.getSubcategoryId().equals(removedItem.getSubcategoryId())).count() != 0
                || services.stream().filter(s -> s.getSubcategoryId().equals(removedItem.getSubcategoryId())).count() != 0)){
            subcategories.removeIf(s -> s.getId().equals(removedItem.getSubcategoryId()));

            subcategoryAdapter = new SubcategoryAdapter(EditPackageActivity.this, android.R.layout.simple_dropdown_item_1line, subcategories);
            subcategoryAutoCompleteTextView.setAdapter(subcategoryAdapter);
        }
    }

    @Override
    public void onServiceRemoved(Service removedItem) {
        priceSum -= removedItem.getFullPrice();
        binding.editPrice.setText(priceSum.toString());

        for(Uri img: removedItem.getImages()){
            pckg.getImages().remove(img);
        }
        imageAdapter = new ImageAdapter(EditPackageActivity.this, R.layout.image_carousel_card_package, pckg.getImages());
        binding.recycler.setAdapter(imageAdapter);

        for(Long eventId: removedItem.getEventTypeIds()){
            if(!(products.stream().filter(p -> p.getEventTypeIds().contains(eventId)).count() != 0
                    || services.stream().filter(s -> s.getEventTypeIds().contains(eventId)).count() != 0)){
                packageEvents.removeIf(p -> p.getId().equals(eventId));
                eventIds.remove(eventId);
                eventListAdapter = new EventListAdapter(EditPackageActivity.this, R.layout.event_card_package, packageEvents);
                binding.events.setAdapter(eventListAdapter);
            }
        }

        if(!(products.stream().filter(p -> p.getSubcategoryId().equals(removedItem.getSubcategoryId())).count() != 0
                || services.stream().filter(s -> s.getSubcategoryId().equals(removedItem.getSubcategoryId())).count() != 0)){
            subcategories.removeIf(s -> s.getId().equals(removedItem.getSubcategoryId()));

            subcategoryAdapter = new SubcategoryAdapter(EditPackageActivity.this, android.R.layout.simple_dropdown_item_1line, subcategories);
            subcategoryAutoCompleteTextView.setAdapter(subcategoryAdapter);
        }

        Integer min = !binding.editReservationDue.getText().toString().isEmpty() ? Integer.parseInt(binding.editReservationDue.getText().toString()) : -1;
        Integer reservationDue = 0;
        for(Service service : services){
            reservationDue = Integer.parseInt(service.getReservationDue());
            if(reservationDue < min)
                min = reservationDue;
        }
        binding.editReservationDue.setText(reservationDue.toString());
        binding.editReservationDue.setFocusable(false);
        binding.editReservationDue.setClickable(false);

        Integer minCancelation = !binding.editCancelationDue.getText().toString().isEmpty() ? Integer.parseInt(binding.editCancelationDue.getText().toString()) : -1;
        Integer cancelationDue = 0;
        for(Service service : services){
            cancelationDue = Integer.parseInt(service.getCancelationDue());
            if(cancelationDue < minCancelation)
                minCancelation = cancelationDue;
        }
        binding.editCancelationDue.setText(cancelationDue.toString());
        binding.editCancelationDue.setFocusable(false);
        binding.editCancelationDue.setClickable(false);

        binding.automaticAffirmation.setChecked(services.stream().filter(s -> s.getAutomaticAffirmation()).count() != 0);
        binding.automaticAffirmation.setEnabled(false);
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


                        subcategories.add(new Subcategory(subcateogryId, categoryName, name, descirption, type));

                        subcategoryAutoCompleteTextView = (AutoCompleteTextView) subcategoryTextInputLayout.getEditText();
                        subcategoryAdapter = new SubcategoryAdapter(EditPackageActivity.this, android.R.layout.simple_dropdown_item_1line, subcategories);
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
                    Toast.makeText(EditPackageActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void getCategoryById(Long serviceCategory) {
        db.collection("Categories")
                .document(serviceCategory.toString())
                .get()
                .addOnSuccessListener(documentSnapshot ->  {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("Name");
                        TextInputLayout category = findViewById(R.id.category);
                        TextInputEditText categoryAutoComplete = (TextInputEditText) category.getEditText();
                        categoryAutoComplete.setText(name);
                    }
                })
                .addOnFailureListener(e ->  {
                    Toast.makeText(EditPackageActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void getSubcategories(){
        ArrayList<Long> subcategoryIds = pckg.getSubCategoryId();
        for(Long id : subcategoryIds) {
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


                            subcategories.add(new Subcategory(subcateogryId, categoryName, name, descirption, type));
                            TextInputLayout subcategory = findViewById(R.id.subcategories);
                            AutoCompleteTextView subcategoryAutoComplete = (AutoCompleteTextView) subcategory.getEditText();
                            SubcategoryAdapter subcategoryAdapter = new SubcategoryAdapter(EditPackageActivity.this, android.R.layout.simple_dropdown_item_1line, subcategories);
                            subcategoryAutoComplete.setAdapter(subcategoryAdapter);
                            subcategoryAutoComplete.setSelection(subcategoryAutoComplete.getText().length());
                            subcategoryAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                }
                            });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(EditPackageActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }
    private void getPackageById(Long id){
        db.collection("Packages")
                .document(id.toString())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){
                            pckg = new Package(/*
                                    id,
                                    documentSnapshot.getString("name"),
                                    documentSnapshot.getString("description"),
                                    documentSnapshot.getDouble("discount"),
                                    documentSnapshot.getBoolean("available"),
                                    documentSnapshot.getBoolean("visible"),
                                    documentSnapshot.getLong("categoryId"),
                                    (ArrayList<Long>) documentSnapshot.get("subcategoryIds"),
                                    (ArrayList<Long>) documentSnapshot.get("productIds"),
                                    (ArrayList<Long>) documentSnapshot.get("serviceIds"),
                                    (ArrayList<Long>) documentSnapshot.get("eventIds"),
                                    documentSnapshot.getDouble("price"),
                                    new ArrayList<>(), //images
                                    documentSnapshot.getString("reservationDue"),
                                    documentSnapshot.getString("cancelationDue"),
                                    documentSnapshot.getBoolean("automaticAffirmation"),
                                    documentSnapshot.getBoolean("deleted")*/);

                            imagesFromDb = (ArrayList<String>) documentSnapshot.get("imageUrls");
                            final int numImages = imagesFromDb.size();
                            final int numEvents = pckg.getEventTypeIds().size();

                            for (String imageUrl : imagesFromDb) {
                                StorageReference imageRef = storage.getReference().child(imageUrl);
                                imageRef.getDownloadUrl()
                                        .addOnSuccessListener(uri ->  {
                                            pckg.getImages().add(uri);

                                            if (pckg.getImages().size() == numImages) {
                                                getEvents(numImages, numEvents);
                                                getProductsFromDb();
                                                getServicesFromDb();
                                                getCategoryById(pckg.getCategoryId());
                                                getSubcategories();
                                                priceSum = pckg.getPrice();
                                                imagesFromDbUri = new ArrayList<>(pckg.getImages());
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(EditPackageActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                        });
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditPackageActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void getProductsFromDb(){
        productsFromDb = new ArrayList<>();

        db.collection("Products")
                .whereEqualTo("pending", false)
                .whereEqualTo("deleted", false)
                .whereEqualTo("categoryId", pckg.getCategoryId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            final List<DocumentSnapshot> productDocs = task.getResult().getDocuments();
                            final int numProducts = productDocs.size();
                            final int[] productsProcessed = {0};

                            for (DocumentSnapshot doc : productDocs) {
                                Product product = new Product(/*
                                        Long.parseLong(doc.getId()),
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
                                                            ProductListPupvAdapter productListAdapter = new ProductListPupvAdapter(EditPackageActivity.this, products);
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
                                                    Toast.makeText(EditPackageActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                                }
                                            });
                                }

                                productsFromDb.add(product);
                            }
                        } else {
                            Toast.makeText(EditPackageActivity.this, "Failed to fetch products: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditPackageActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void getServicesFromDb(){
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
                                Service service = new Service(/*
                                        Long.parseLong(doc.getId()),
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
                                                            ServiceListPupvAdapter productListAdapter = new ServiceListPupvAdapter(EditPackageActivity.this, services);
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
                                                    Toast.makeText(EditPackageActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                                }
                                            });
                                }

                                servicesFromDb.add(service);
                            }
                        } else {
                            Toast.makeText(EditPackageActivity.this, "Failed to fetch services: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditPackageActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void getEvents(int numImages, int numEvents){
        ArrayList<Long> eventIds = pckg.getEventTypeIds();
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

                        packageEvents.add(event);

                        if(packageEvents.size() == numEvents) {
                            getProducts();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(EditPackageActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
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
                    Toast.makeText(EditPackageActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void getProducts(){
        ArrayList<Long> productIds = pckg.getProductIds();

        for(Long id : productIds){
            db.collection("Products")
                    .document(id.toString())
                    .get()
                    .addOnSuccessListener(documentSnapshot ->  {
                        if(documentSnapshot.exists()){
                            Product product = new Product(/*id,
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

                            imagesFromDbProduct = (ArrayList<String>) documentSnapshot.get("imageUrls");
                            final int numImages = imagesFromDbProduct.size();
                            final int numEvents = product.getEventTypeIds().size();

                            for (String imageUrl : imagesFromDbProduct) {
                                StorageReference imageRef = storage.getReference().child(imageUrl);
                                imageRef.getDownloadUrl()
                                        .addOnSuccessListener(uri ->  {
                                            product.getImages().add(uri);

                                            if (product.getImages().size() == numImages) {
                                                imagesFromDbUriProduct = new ArrayList<>(product.getImages());
                                                products.add(product);

                                                getServices();
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(EditPackageActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                        });
                            }


                        }
                    })
                    .addOnFailureListener(e ->  {
                        Toast.makeText(EditPackageActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }

    private void getServices(){
        ArrayList<Long> serviceIds = pckg.getServiceIds();

        for(Long id: serviceIds){
            db.collection("Services")
                    .document(id.toString())
                    .get()
                    .addOnSuccessListener(documentSnapshot ->  {
                        if(documentSnapshot.exists()){
                            Service service = new Service(/*id,
                                    documentSnapshot.getLong("categoryId"),
                                    documentSnapshot.getLong("subcategoryId"),
                                    documentSnapshot.getString("name"),
                                    documentSnapshot.getString("description"),
                                    new ArrayList<>(),
                                    documentSnapshot.getString("specific"),
                                    documentSnapshot.getDouble("pricePerHour"),
                                    documentSnapshot.getDouble("fullPrice"),
                                    documentSnapshot.getDouble("duration"),
                                    documentSnapshot.getDouble("durationMin"),
                                    documentSnapshot.getDouble("durationMax"),
                                    documentSnapshot.getString("location"),
                                    documentSnapshot.getDouble("discount"),
                                    (ArrayList<String>) documentSnapshot.get("providers"),
                                    (ArrayList<Long>) documentSnapshot.get("eventIds"),
                                    documentSnapshot.getString("reservationDue"),
                                    documentSnapshot.getString("cancelationDue"),
                                    documentSnapshot.getBoolean("automaticAffirmation"),
                                    documentSnapshot.getBoolean("available"),
                                    documentSnapshot.getBoolean("visible"),
                                    documentSnapshot.getBoolean("pending"),
                                    documentSnapshot.getBoolean("deleted")*/);

                            imagesFromDbService = (ArrayList<String>) documentSnapshot.get("imageUrls");
                            final int numImages = imagesFromDbService.size();
                            final int numEvents = service.getEventTypeIds().size();

                            for (String imageUrl : imagesFromDbService) {
                                StorageReference imageRef = storage.getReference().child(imageUrl);
                                imageRef.getDownloadUrl()
                                        .addOnSuccessListener(uri ->  {
                                            service.getImages().add(uri);

                                            if (service.getImages().size() == numImages) {
                                                imagesFromDbUriService = new ArrayList<>(service.getImages());
                                                services.add(service);

                                                initilazeComponents();
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(EditPackageActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                        });
                            }


                        }
                    })
                    .addOnFailureListener(e ->  {
                        Toast.makeText(EditPackageActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }
}