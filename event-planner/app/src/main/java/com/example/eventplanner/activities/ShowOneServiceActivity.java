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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventplanner.R;
import com.example.eventplanner.adapters.EmployeeRecyclerViewAdapter;
import com.example.eventplanner.adapters.ImageAdapter;
import com.example.eventplanner.databinding.ActivityShowOneServiceBinding;
import com.example.eventplanner.fragments.ReserveServiceFragment;
import com.example.eventplanner.model.Category;
import com.example.eventplanner.model.Subcategory;
import com.example.eventplanner.model.UserOD;
import com.example.eventplanner.model.UserPUPV;
import com.example.eventplanner.model.UserPUPZ;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ShowOneServiceActivity extends AppCompatActivity {
    ActivityShowOneServiceBinding binding;

    RecyclerView recyclerView;

    RecyclerView recyclerViewForPupz;
    ImageAdapter imageAdapter;

    Long idService;
    String idPupv;

    Category category;
    Subcategory subcategory;

    UserPUPV userPUPV;

    String fullnameSender;
    ArrayList<UserPUPZ> pupzs;

    ArrayList<String> productIds = new ArrayList<>();

    ArrayList<String> serviceIds = new ArrayList<>();
    ArrayList<String> packageIds = new ArrayList<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    FirebaseUser user = mAuth.getCurrentUser();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShowOneServiceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        idService = getIntent().getLongExtra("serviceId", 0L);
        idPupv = getIntent().getStringExtra("pupvId");
        getUserPupv(idPupv).thenAccept(userPUPV -> {
            this.userPUPV = userPUPV;
        });
        Long idCategory = getIntent().getLongExtra("categoryId", 0L);
        Long idSubcategory = getIntent().getLongExtra("subcategoryId", 0L);
        ArrayList<Uri> images = getIntent().getParcelableArrayListExtra("images");
        getCategory(idCategory);
        getSubcategory(idSubcategory);

        recyclerView = findViewById(R.id.recycler);
        imageAdapter = new ImageAdapter(ShowOneServiceActivity.this, R.layout.image_carousel_card_without_button,images);
        recyclerView.setAdapter(imageAdapter);



        String name = getIntent().getStringExtra("name");
        String description = getIntent().getStringExtra("description");
        double price = getIntent().getDoubleExtra("price", 0.0);
        double discount = getIntent().getDoubleExtra("discount", 0.0);
        boolean available = getIntent().getBooleanExtra("available", false);
        String specific = getIntent().getStringExtra("specific");
        double pricePerHour = getIntent().getDoubleExtra("pricePerHour",0.0);
        double duration = getIntent().getDoubleExtra("duration", 0.0);
        double durationMin = getIntent().getDoubleExtra("durationMin", 0.0);
        double durationMax = getIntent().getDoubleExtra("durationMax", 0.0);
        String reservationDue = getIntent().getStringExtra("deadlineReservation");
        String cancellationDue = getIntent().getStringExtra("cancellationReservation");
        ArrayList<String> pupIds = getIntent().getStringArrayListExtra("pupIds");
        ArrayList<String> eventTypeIds = getIntent().getStringArrayListExtra("eventTypeIds");
        getEventTypesName(eventTypeIds);

        binding.nameService.setText(name);
        binding.descriptionSerivce.setText(description);
        binding.priceService.setText(String.valueOf(price) + " $");
        binding.discountPrice.setText(String.valueOf(discount) + " %");
        binding.pricePerHour.setText(String.valueOf(pricePerHour) + " $/hour");

        if(duration != 0.0){
            binding.durationService.setText(String.valueOf(duration) + " hour");
            binding.durationminmaxLayout.setVisibility(View.GONE);
        }else{
            binding.durationMin.setText(String.valueOf(durationMin));
            binding.durationMax.setText(String.valueOf(durationMax));
            binding.durationLayout.setVisibility(View.GONE);
        }
        binding.priceWithDiscount.setText(String.valueOf(price - (price * discount/100)) + " $");
        binding.specificOfService.setText(specific);
        binding.deadlineReservation.setText(reservationDue);
        binding.cancelationReservation.setText(cancellationDue);

        if(user==null){
            binding.bookService.setVisibility(View.GONE);
        }else if(!user.getDisplayName().equals("OD")){
            binding.bookService.setVisibility(View.GONE);
        }
        if(available){
            binding.availability.setChecked(true);
            binding.bookService.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ReserveServiceFragment fragment = new ReserveServiceFragment(idService,true,null);
                    fragment.show(getSupportFragmentManager(), "ReserveServiceFragment");
                }
            });
        }else{
            binding.bookService.setVisibility(View.GONE);
        }


        binding.showCompanyInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShowOneServiceActivity.this, CompanyViewActivity.class);
                intent.putExtra("pupvId", idPupv);
                startActivity(intent);
            }
        });

        if(user != null){
            binding.showCompanyInfo.setVisibility(View.VISIBLE);
        }


        if(user != null && user.getDisplayName().equals("OD")){
            getUserOd(user.getUid()).thenAccept(userOD -> {
                binding.pupzsEmployeForOd.setVisibility(View.VISIBLE);
                recyclerViewForPupz = binding.pupzListView;
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(ShowOneServiceActivity.this);
                recyclerViewForPupz.setLayoutManager(layoutManager);

                findAllPupz(pupIds);

                binding.likeUnlikeButtonService.setVisibility(View.VISIBLE);

                updateLikeButtonState(idService);
                binding.likeUnlikeButtonService.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(binding.likeUnlikeButtonService.getText().toString().equals("Like")){
                            serviceIds.add(idService.toString());
                            addServiceToFavourite(productIds, serviceIds, packageIds);
                            binding.likeUnlikeButtonService.setText(R.string.unlike);
                            binding.likeUnlikeButtonService.setIcon(getDrawable(R.drawable.ic_unlike));
                            binding.likeUnlikeButtonService.setBackgroundColor(getColor(R.color.purple_light));
                        }else if(binding.likeUnlikeButtonService.getText().toString().equals("Unlike")){
                            serviceIds.remove(idService.toString());
                            removeFromFavourite(serviceIds);
                            binding.likeUnlikeButtonService.setText(R.string.like);
                            binding.likeUnlikeButtonService.setIcon(getDrawable(R.drawable.ic_like));
                            binding.likeUnlikeButtonService.setBackgroundColor(getColor(R.color.yellow));
                        }
                    }
                });

            });

        }
    }

    private void updateLikeButtonState(Long idService) {
        DocumentReference userFavoritesRef = db.collection("FavouritesPsp").document(user.getUid());

        userFavoritesRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        productIds = (ArrayList<String>) document.get("productIds");
                        serviceIds = (ArrayList<String>) document.get("serviceIds");
                        packageIds = (ArrayList<String>) document.get("packageIds");

                        if (serviceIds != null && serviceIds.contains(idService.toString())) {
                            binding.likeUnlikeButtonService.setText(R.string.unlike);
                            binding.likeUnlikeButtonService.setIcon(getDrawable(R.drawable.ic_unlike));
                            binding.likeUnlikeButtonService.setBackgroundColor(getColor(R.color.purple_light));
                        } else {
                            binding.likeUnlikeButtonService.setText(R.string.like);
                            binding.likeUnlikeButtonService.setIcon(getDrawable(R.drawable.ic_like));
                            binding.likeUnlikeButtonService.setBackgroundColor(getColor(R.color.yellow));
                        }
                    }
                } else {
                    Log.e(TAG, "Error getting document", task.getException());
                }
            }
        });
    }

    private void removeFromFavourite(ArrayList<String> serviceIds) {
        DocumentReference userFavoritesRef = db.collection("FavouritesPsp").document(user.getUid());

        userFavoritesRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {


                        userFavoritesRef.update("serviceIds", serviceIds)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // Uspješno uklonjen proizvod iz liste omiljenih
                                        Toast.makeText(ShowOneServiceActivity.this, "Removed from favourites", Toast.LENGTH_SHORT).show();
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

    private void addServiceToFavourite(ArrayList<String> productIds,ArrayList<String> serviceIds,ArrayList<String> packageIds) {

        Map<String, Object> elememt = new HashMap<>();
        elememt.put("productIds", productIds);
        elememt.put("serviceIds", serviceIds);
        elememt.put("packageIds", packageIds);
        db.collection("FavouritesPsp").document(user.getUid())
                .set(elememt)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ShowOneServiceActivity.this, "Add to favourite", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error adding document", e);
                    }
                });

    }

    private void findAllPupz(ArrayList<String> pupIds) {
        pupzs = new ArrayList<>();

        if (pupIds.isEmpty()) {
            Log.d(TAG, "List of pupIds is empty, nothing to fetch.");
            return;
        }

        Map<String, UserPUPZ> pupzMap = new HashMap<>();
        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();

        for (String id : pupIds) {
            Task<DocumentSnapshot> task = db.collection("User").document(id).get();
            tasks.add(task);
        }

        Tasks.whenAllComplete(tasks)
                .addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
                    @Override
                    public void onComplete(@NonNull Task<List<Task<?>>> task) {
                        if (task.isSuccessful()) {
                            for (Task<?> t : tasks) {
                                DocumentSnapshot doc = (DocumentSnapshot) t.getResult();
                                if (doc.exists()) {
                                    UserPUPZ user = doc.toObject(UserPUPZ.class);
                                    pupzMap.put(doc.getId(), user);
                                } else {
                                    Log.d(TAG, "No such document: " + doc.getId());
                                }
                            }

                            for (String id : pupIds) {
                                UserPUPZ user = pupzMap.get(id);
                                if (user != null) {
                                    pupzs.add(user);
                                }
                            }

                            // Pretpostavljam da imate pristup user i fullnameSender varijablama
                            EmployeeRecyclerViewAdapter adapterEvents = new EmployeeRecyclerViewAdapter(pupzs, pupIds, user.getUid(), fullnameSender);
                            recyclerViewForPupz.setAdapter(adapterEvents);
                        } else {
                            Log.e(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }


    private CompletableFuture<UserOD> getUserOd(String uid) {
        CompletableFuture<UserOD> future = new CompletableFuture<>();

        db.collection("User").document(uid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d("HomeTwoActivity", "DocumentSnapshot data: " + document.getData());
                                UserOD userOdd = new UserOD();
                                userOdd.setFirstName((String) document.get("FirstName"));
                                userOdd.setLastName((String) document.get("LastName"));
                                userOdd.setEmail((String) document.get("E-mail"));
                                userOdd.setPassword((String) document.get("Password"));
                                userOdd.setPhone((String) document.get("Phone"));
                                userOdd.setAddress((String) document.get("Address"));
                                userOdd.setValid((Boolean) document.get("IsValid"));

                                fullnameSender = userOdd.getFirstName() + " " + userOdd.getLastName();
                                future.complete(userOdd);
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

    private void getSubcategory(Long idSubcategory) {
        db.collection("Subcategories").document(idSubcategory.toString()).
                get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String categoryName = document.getString("CategoryName");
                                String name = document.getString("Name");
                                String description = document.getString("Description");
                                Long type1 = document.getLong("Type");
                                subcategory = new Subcategory(idSubcategory, categoryName, name, description, type1.intValue());
                                binding.subcategoryService.setText(subcategory.getName());
                            } else {
                                Log.d("Category", "No such document");
                            }
                        } else {
                            Log.d("Category", "get failed with ", task.getException());
                        }
                    }
                });

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
                                binding.categoryService.setText(category.getName());
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