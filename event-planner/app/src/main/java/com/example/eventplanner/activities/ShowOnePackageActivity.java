package com.example.eventplanner.activities;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventplanner.R;
import com.example.eventplanner.adapters.ImageAdapter;
import com.example.eventplanner.adapters.ProductListAdapter;
import com.example.eventplanner.adapters.ServiceListAdapter;
import com.example.eventplanner.databinding.ActivityShowOnePackageBinding;
import com.example.eventplanner.model.Category;
import com.example.eventplanner.model.Product;
import com.example.eventplanner.model.Service;
import com.example.eventplanner.model.UserPUPV;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ShowOnePackageActivity extends AppCompatActivity {

    ActivityShowOnePackageBinding binding;

    RecyclerView recyclerView;
    ImageAdapter imageAdapter;

    Long idPackage;
    String idPupv;

    Category category;


    UserPUPV userPUPV;

    ArrayList<Product> products;
    ArrayList<Service> services;

    ArrayList<String> productIdsss = new ArrayList<>();

    ArrayList<String> serviceIdsss = new ArrayList<>();
    ArrayList<String> packageIdsss = new ArrayList<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    FirebaseStorage storage = FirebaseStorage.getInstance();

    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    FirebaseUser user = mAuth.getCurrentUser();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShowOnePackageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        idPackage = getIntent().getLongExtra("packageId", 0L);
        idPupv = getIntent().getStringExtra("pupvId");
        getUserPupv(idPupv).thenAccept(userPUPV -> {
            this.userPUPV = userPUPV;
        });
        Long idCategory = getIntent().getLongExtra("categoryId", 0L);
        getCategory(idCategory);

        ArrayList<Uri> images = getIntent().getParcelableArrayListExtra("images");

        recyclerView = findViewById(R.id.recycler);
        imageAdapter = new ImageAdapter(ShowOnePackageActivity.this, R.layout.image_carousel_card_package,images);
        recyclerView.setAdapter(imageAdapter);

        String name = getIntent().getStringExtra("name");
        String description = getIntent().getStringExtra("description");

        boolean available = getIntent().getBooleanExtra("available", false);
        double price = getIntent().getDoubleExtra("price", 0.0);
        double discount = getIntent().getDoubleExtra("discount", 0.0);
        String reservationDue = getIntent().getStringExtra("deadlineReservation");
        String cancellationDue = getIntent().getStringExtra("cancellationReservation");
        ArrayList<String> eventTypeIds = getIntent().getStringArrayListExtra("eventTypeIds");
        getEventTypesName(eventTypeIds);
        ArrayList<String> subcategoryIds = getIntent().getStringArrayListExtra("subcategoryIds");
        getSubcategoriesNames(subcategoryIds);
        ArrayList<String> productIds = getIntent().getStringArrayListExtra("productIds");
        getProducts(productIds);
        ArrayList<String> serviceIds = getIntent().getStringArrayListExtra("serviceIds");
        getServices(serviceIds);

        if(user!=null){
            binding.showCompanyInfo.setVisibility(View.VISIBLE);
        }


        binding.namePackage.setText(name);
        binding.descriptionPackage.setText(description);
        binding.pricePackage.setText(String.valueOf(price) + " $");
        binding.discountPricePackage.setText(String.valueOf(discount) + " %");
        binding.priceWithDiscountPackage.setText(String.valueOf(price - (price * discount/100)) + " $");
        binding.deadlineReservationPackage.setText(reservationDue);
        binding.cancelationReservationPackage.setText(cancellationDue);
        if(user==null){
            binding.bookPackage.setVisibility(View.GONE);
        }else  if(!user.getDisplayName().equals("OD")){
            binding.bookPackage.setVisibility(View.GONE);
        }
        if(available){
            binding.availability.setChecked(true);
            binding.bookPackage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ShowOnePackageActivity.this, ReservePackageActivity.class);
                    intent.putExtra("id",idPackage.toString());
                    startActivity(intent);
                }
            });
        }else{
            binding.bookPackage.setVisibility(View.GONE);
        }


        binding.showCompanyInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShowOnePackageActivity.this, CompanyViewActivity.class);
                intent.putExtra("pupvId", idPupv);
                startActivity(intent);
            }
        });

        if(user != null && user.getDisplayName().equals("OD")) {
            binding.likeUnlikeButtonPackage.setVisibility(View.VISIBLE);

            updateLikeButtonState(idPackage);
            binding.likeUnlikeButtonPackage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(binding.likeUnlikeButtonPackage.getText().toString().equals("Like")){
                        packageIdsss.add(idPackage.toString());
                        addPackageToFavourite(productIdsss, serviceIdsss, packageIdsss);
                        binding.likeUnlikeButtonPackage.setText(R.string.unlike);
                        binding.likeUnlikeButtonPackage.setIcon(getDrawable(R.drawable.ic_unlike));
                        binding.likeUnlikeButtonPackage.setBackgroundColor(getColor(R.color.purple_light));
                    }else if(binding.likeUnlikeButtonPackage.getText().toString().equals("Unlike")){
                        packageIdsss.remove(idPackage.toString());
                        removeFromFavourite(packageIdsss);
                        binding.likeUnlikeButtonPackage.setText(R.string.like);
                        binding.likeUnlikeButtonPackage.setIcon(getDrawable(R.drawable.ic_like));
                        binding.likeUnlikeButtonPackage.setBackgroundColor(getColor(R.color.yellow));
                    }
                }
            });
        }
    }

    private void updateLikeButtonState(Long idPackage) {
        DocumentReference userFavoritesRef = db.collection("FavouritesPsp").document(user.getUid());

        userFavoritesRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        productIdsss = (ArrayList<String>) document.get("productIds");
                        serviceIdsss = (ArrayList<String>) document.get("serviceIds");
                        packageIdsss = (ArrayList<String>) document.get("packageIds");

                        if (packageIdsss != null && packageIdsss.contains(idPackage.toString())) {
                            binding.likeUnlikeButtonPackage.setText(R.string.unlike);
                            binding.likeUnlikeButtonPackage.setIcon(getDrawable(R.drawable.ic_unlike));
                            binding.likeUnlikeButtonPackage.setBackgroundColor(getColor(R.color.purple_light));
                        } else {
                            binding.likeUnlikeButtonPackage.setText(R.string.like);
                            binding.likeUnlikeButtonPackage.setIcon(getDrawable(R.drawable.ic_like));
                            binding.likeUnlikeButtonPackage.setBackgroundColor(getColor(R.color.yellow));
                        }
                    }
                } else {
                    Log.e(TAG, "Error getting document", task.getException());
                }
            }
        });
    }

    private void removeFromFavourite(ArrayList<String> packageIdss) {
        DocumentReference userFavoritesRef = db.collection("FavouritesPsp").document(user.getUid());

        userFavoritesRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {


                        userFavoritesRef.update("packageIds", packageIdss)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // Uspješno uklonjen proizvod iz liste omiljenih
                                        Toast.makeText(ShowOnePackageActivity.this, "Removed from favourites", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e(TAG, "Error updating document", e);
                                    }
                                });

                    }
                } else {
                    Log.e(TAG, "Error getting document", task.getException());
                }
            }
        });
    }

    private void addPackageToFavourite(ArrayList<String> productIdss,ArrayList<String> serviceIdss,ArrayList<String> packageIdss) {

        Map<String, Object> elememt = new HashMap<>();
        elememt.put("productIds", productIdss);
        elememt.put("serviceIds", serviceIdss);
        elememt.put("packageIds", packageIdss);
        db.collection("FavouritesPsp").document(user.getUid())
                .set(elememt)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ShowOnePackageActivity.this, "Add to favourite", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error adding document", e);
                    }
                });

    }

    private void getServices(ArrayList<String> serviceIds) {
        services = new ArrayList<>();
        db.collection("Services")
                .whereIn(FieldPath.documentId(), serviceIds)
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

                                ArrayList<String> imageUrls = (ArrayList<String>) doc.get("imageIds");
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
                                                            ServiceListAdapter serviceListAdapter = new ServiceListAdapter(ShowOnePackageActivity.this, R.layout.service_card, services);
                                                            binding.serviceList.setAdapter(serviceListAdapter);
                                                            binding.serviceList.setClickable(true);
                                                        }
                                                    }
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(ShowOnePackageActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                                }
                                            });
                                }

                                services.add(service);
                            }


                        } else {
                            Toast.makeText(ShowOnePackageActivity.this, "Failed to fetch services: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ShowOnePackageActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }


    private void getProducts(ArrayList<String> productIds) {
        products = new ArrayList<>();

        db.collection("Products")
                .whereIn(FieldPath.documentId(), productIds)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){

                            final List<DocumentSnapshot> productDocs = task.getResult().getDocuments();
                            final int numProducts = productDocs.size();
                            final int[] productsProcessed = {0};

                            for(DocumentSnapshot doc: task.getResult()){
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
                                        convertStringArrayToLong((ArrayList<String>) doc.get("eventTypeIds")),
                                        doc.getBoolean("available"),
                                        doc.getBoolean("visible"),
                                        doc.getBoolean("pending"),
                                        doc.getBoolean("deleted")
                                );

                                ArrayList<String> imageUrls = (ArrayList<String>) doc.get("imageIds");
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
                                                            ProductListAdapter productListAdapter = new ProductListAdapter(ShowOnePackageActivity.this, R.layout.product_card, products);
                                                            binding.productList.setAdapter(productListAdapter);
                                                            binding.productList.setClickable(true);
                                                        }
                                                    }
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(ShowOnePackageActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                                }
                                            });
                                }

                                products.add(product);
                            }

                        } else {
                            Toast.makeText(ShowOnePackageActivity.this, "Failed to fetch products: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ShowOnePackageActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private ArrayList<Long> convertStringArrayToLong(ArrayList<String> list){
        ArrayList<Long> ids = new ArrayList<>();

        for(String item: list){
            ids.add(Long.parseLong(item));
        }

        return ids;
    }

    private void getSubcategoriesNames(ArrayList<String> subcategoryIds) {
        if(!subcategoryIds.isEmpty()){
            db.collection("Subcategories")
                    .whereIn(FieldPath.documentId(), subcategoryIds)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            ArrayList<String> subcategoriyNames = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String name = document.getString("Name");
                                subcategoriyNames.add(name);
                            }

                            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                    this,
                                    R.layout.list_item_layout_white,
                                    subcategoriyNames
                            );


                            binding.subcategoriesList.setAdapter(adapter);
                        } else {
                            Log.d("Firestore", "Error getting documents: ", task.getException());
                        }
                    });

        }
    }

    private void getEventTypesName(ArrayList<String> eventTypeIds) {
        if(!eventTypeIds.isEmpty()){
            db.collection("EventTypes")
                    .whereIn(FieldPath.documentId(), eventTypeIds)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            ArrayList<String> eventTypeName = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String name = document.getString("Name");
                                eventTypeName.add(name);
                            }

                            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                    this,
                                    R.layout.list_item_layout_white,
                                    eventTypeName
                            );


                            binding.eventTypesList.setAdapter(adapter);
                        } else {
                            Log.d("Firestore", "Error getting documents: ", task.getException());
                        }
                    });
        }
    }

    private CompletableFuture<UserPUPV> getUserPupv(String uid) {
        CompletableFuture<UserPUPV> future = new CompletableFuture<>();

        db.collection("User").document(uid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                UserPUPV userPupvv = new UserPUPV();
                                userPupvv.setFirstName((String) document.get("FirstName"));
                                userPupvv.setLastName((String) document.get("LastName"));
                                userPupvv.setEmail((String) document.get("E-mail"));
                                userPupvv.setPassword((String) document.get("Password"));
                                userPupvv.setPhone((String) document.get("Phone"));
                                userPupvv.setAddress((String) document.get("Address"));
                                userPupvv.setValid((Boolean) document.get("IsValid"));
                                userPupvv.setCompanyName((String) document.get("CompanyName"));
                                userPupvv.setCompanyDescription((String) document.get("CompanyDescription"));
                                userPupvv.setCompanyAddress((String) document.get("CompanyAddress"));
                                userPupvv.setCompanyemail((String) document.get("CompanyEmail"));
                                userPupvv.setCompanyPhone((String) document.get("CompanyPhone"));
                                userPupvv.setWorkTime((String) document.get("WorkTime"));

                                userPUPV = userPupvv;

                                future.complete(userPupvv);
                            } else {
                                Log.e("HomeTwoActivity", "No such document");
                                future.completeExceptionally(new Exception("No such document"));
                            }
                        } else {
                            Log.e("HomeTwoActivity", "Error getting document", task.getException());
                            future.completeExceptionally(task.getException());
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("HomeTwoActivity", "Error getting document", e);
                        future.completeExceptionally(e);
                    }
                });

        return future;
    }

    private void getCategory(Long idCategory) {
        db.collection("Categories").document(idCategory.toString()).
                get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String name = document.getString("Name");
                                String description = document.getString("Description");
                                category = new Category(idCategory, name, description);
                                binding.categoryPackage.setText(category.getName());
                            } else {
                                Log.d("Category", "No such document");
                            }
                        } else {
                            Log.d("Category", "get failed with ", task.getException());
                        }
                    }
                });


    }
}