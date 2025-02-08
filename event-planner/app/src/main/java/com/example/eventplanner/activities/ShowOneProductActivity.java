package com.example.eventplanner.activities;


import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventplanner.R;
import com.example.eventplanner.adapters.ImageAdapter;
import com.example.eventplanner.databinding.ActivityShowOneProductBinding;
import com.example.eventplanner.model.Category;
import com.example.eventplanner.model.Product;
import com.example.eventplanner.model.Subcategory;
import com.example.eventplanner.model.UserOD;
import com.example.eventplanner.model.UserPUPV;
import com.example.eventplanner.services.FCMHttpClient;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class ShowOneProductActivity extends AppCompatActivity {

    ActivityShowOneProductBinding binding;
    RecyclerView recyclerView;
    ImageAdapter imageAdapter;

    Long idProduct;
    String idPupv;

    Category category;
    Subcategory subcategory;

    UserPUPV userPUPV;
    String fullnamePupv;
    String fullnameSender;

    ArrayList<String> productIds = new ArrayList<>();
    ArrayList<String> serviceIds = new ArrayList<>();
    ArrayList<String> packageIds = new ArrayList<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShowOneProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        user = mAuth.getCurrentUser();

        idProduct = getIntent().getLongExtra("productId", 0L);
        idPupv = getIntent().getStringExtra("pupvId");
        getUserPupv(idPupv).thenAccept(userPupv -> {
            this.userPUPV = userPupv;
        });
        Long idCategory = getIntent().getLongExtra("categoryId", 0L);
        Long idSubcategory = getIntent().getLongExtra("subcategoryId", 0L);
        ArrayList<Uri> images = getIntent().getParcelableArrayListExtra("images");
        getCategory(idCategory);
        getSubcategory(idSubcategory);

        recyclerView = findViewById(R.id.recycler);
        imageAdapter = new ImageAdapter(ShowOneProductActivity.this, R.layout.image_carousel_card_without_button,images);
        recyclerView.setAdapter(imageAdapter);



        String name = getIntent().getStringExtra("name");
        String description = getIntent().getStringExtra("description");
        double price = getIntent().getDoubleExtra("price", 0.0);
        double discount = getIntent().getDoubleExtra("discount", 0.0);
        boolean available = getIntent().getBooleanExtra("available", false);
        ArrayList<String> eventTypeIds = getIntent().getStringArrayListExtra("eventTypeIds");
        getEventTypesName(eventTypeIds);




        binding.nameProductt.setText(name);
        binding.descriptionProduct.setText(description);
        binding.priceProduct.setText(String.valueOf(price) + " $");
        binding.discountPrice.setText(String.valueOf(discount) + " %");
        binding.priceWithDiscount.setText(String.valueOf(price - (price * discount/100)) + " $");

        if(user==null){
            binding.buyProduct.setVisibility(View.GONE);
        }else  if(!user.getDisplayName().equals("OD")){
            binding.buyProduct.setVisibility(View.GONE);
        }
        if(available){
            binding.availability.setChecked(true);

            binding.buyProduct.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getProduct();
                }
            });
        }else{
            binding.buyProduct.setVisibility(View.GONE);
        }



        binding.showCompanyInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(ShowOneProductActivity.this, CompanyViewActivity.class);
                intent.putExtra("pupvId", idPupv);
                startActivity(intent);
            }
        });

        if(user!= null){
            binding.showCompanyInfo.setVisibility(View.VISIBLE);
            if(!user.getDisplayName().equals("OD")){
                binding.sendMessagePupv.setVisibility(View.GONE);
                binding.likeUnlikeButton.setVisibility(View.GONE);
            }else {
                getUserOd(user.getUid()).thenAccept(userOD -> {
                    binding.sendMessagePupv.setVisibility(View.VISIBLE);
                    binding.likeUnlikeButton.setVisibility(View.VISIBLE);
                    binding.sendMessagePupv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            AlertDialog.Builder builder = new AlertDialog.Builder(ShowOneProductActivity.this);

                            LayoutInflater inflater = getLayoutInflater();
                            View dialogView = inflater.inflate(R.layout.dialog_send_message, null);

                            EditText messageInput = dialogView.findViewById(R.id.messageInput);
                            Button buttonSend = dialogView.findViewById(R.id.buttonSend);
                            Button buttonClose = dialogView.findViewById(R.id.buttonClose);

                            builder.setView(dialogView);

                            AlertDialog dialog = builder.create();
                            dialog.show();

                            buttonSend.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // Handle sending the message
                                    String message = messageInput.getText().toString().trim();
                                    if (!message.isEmpty()) {
                                        // Send the message to Pupv (implement your own logic here)
                                        sendMessageToPupv(message);
                                        // Dismiss the dialog
                                        dialog.dismiss();
                                    } else {
                                        Toast.makeText(ShowOneProductActivity.this, "Please enter a message", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                            buttonClose.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // Close the dialog
                                    dialog.dismiss();
                                }
                            });

                        }
                    });

                    updateLikeButtonState(idProduct);
                    binding.likeUnlikeButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(binding.likeUnlikeButton.getText().toString().equals("Like")){
                                productIds.add(idProduct.toString());
                                addProductToFavourite(productIds,serviceIds,packageIds);
                                binding.likeUnlikeButton.setText(R.string.unlike);
                                binding.likeUnlikeButton.setIcon(getDrawable(R.drawable.ic_unlike));
                                binding.likeUnlikeButton.setBackgroundColor(getColor(R.color.purple_light));
                            }else if(binding.likeUnlikeButton.getText().toString().equals("Unlike")){
                                productIds.remove(idProduct.toString());
                                removeFromFavourite(productIds);
                                binding.likeUnlikeButton.setText(R.string.like);
                                binding.likeUnlikeButton.setIcon(getDrawable(R.drawable.ic_like));
                                binding.likeUnlikeButton.setBackgroundColor(getColor(R.color.yellow));
                            }
                        }
                    });


                });

            }
        }



    }

    private void updateLikeButtonState(Long idProduct) {
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

                        if (productIds != null && productIds.contains(idProduct.toString())) {
                            binding.likeUnlikeButton.setText(R.string.unlike);
                            binding.likeUnlikeButton.setIcon(getDrawable(R.drawable.ic_unlike));
                            binding.likeUnlikeButton.setBackgroundColor(getColor(R.color.purple_light));
                        } else {
                            binding.likeUnlikeButton.setText(R.string.like);
                            binding.likeUnlikeButton.setIcon(getDrawable(R.drawable.ic_like));
                            binding.likeUnlikeButton.setBackgroundColor(getColor(R.color.yellow));
                        }
                    }
                } else {
                    Log.e(TAG, "Error getting document", task.getException());
                }
            }
        });
    }

    private void removeFromFavourite(ArrayList<String> productIds) {
        DocumentReference userFavoritesRef = db.collection("FavouritesPsp").document(user.getUid());

        userFavoritesRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {


                        userFavoritesRef.update("productIds", productIds)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // Uspješno uklonjen proizvod iz liste omiljenih
                                        Toast.makeText(ShowOneProductActivity.this, "Removed from favourites", Toast.LENGTH_SHORT).show();
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

    private void addProductToFavourite(ArrayList<String> productIds,ArrayList<String> serviceIds,ArrayList<String> packageIds) {

        Map<String, Object> elememt = new HashMap<>();
        elememt.put("productIds", productIds);
        elememt.put("serviceIds", serviceIds);
        elememt.put("packageIds", packageIds);
        db.collection("FavouritesPsp").document(user.getUid())
                .set(elememt)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ShowOneProductActivity.this, "Add to favourite", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error adding document", e);
                    }
                });

    }

    private void sendMessageToPupv(String message) {
        Map<String, Object> elememt = new HashMap<>();
        elememt.put("senderId", user.getUid());
        elememt.put("senderFullName", fullnameSender);
        elememt.put("recipientId", idPupv);
        elememt.put("fullnameRecipientId", fullnamePupv);
        elememt.put("dateOfSending", new Date());
        elememt.put("content", message);
        elememt.put("status", false);
        elememt.put("participants", Arrays.asList(user.getUid(), idPupv));

        // Dodajte novi dokument sa generisanim ID-om
        db.collection("Messages").document()
                .set(elememt)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ShowOneProductActivity.this, "Send message successfully", Toast.LENGTH_SHORT).show();
                        createNotification(idPupv, message, fullnameSender);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error adding document", e);
                    }
                });

    }

    private void createNotification(String userId, String message, String fullnameSender){
        Long id = new Random().nextLong();
        Map<String, Object> doc = new HashMap<>();

        doc.put("title", "Message from " + fullnameSender);
        doc.put("body", message);
        doc.put("read", false);
        doc.put("userId", userId);

        db.collection("Notifications")
                .document(id.toString())
                .set(doc)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        String jsonPayload = "{\"data\":{" +
                                "\"title\":\"Message from " + fullnameSender + "\"," +
                                "\"body\":\"" + message + "\"," +
                                "\"topic\":\"Message\"" +
                                "}," +
                                "\"to\":\"/topics/" + userId + "Message" + "\"}";
                        sendMessage(serverKey,jsonPayload);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ShowOneProductActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    String serverKey="AAAA8GYmoZ8:APA91bHsjyzOSa2JtO_cQWFO-X1p9nMuHRO8DTfD1zhcY4mnqZ-2EZmIn8tMf1ISmnM31WB68Mzn2soeUgEISXlSc9WjRvcRhyYbmBgi7whJuYXX-24wkODByasquofLaMZydpg78esK";
    public static void sendMessage(String serverKey, String jsonPayload) {
        FCMHttpClient httpClient = new FCMHttpClient();
        httpClient.sendMessageToTopic(serverKey, "PUPV", jsonPayload);
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
    private void getProduct(){
        db.collection("Products")
                .document(idProduct.toString())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot doc = task.getResult();
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
                        reserveProduct(product);
                    } else {
                        Toast.makeText(this, "Failed to get data", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void reserveProduct(Product product){
        Map<String,Object> map= new HashMap<>();
        map.put("product",product);
        map.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());


        db.collection("ProductReservation").add(map)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Data added successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to add data", Toast.LENGTH_SHORT).show();
                    }
                });
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

                                fullnamePupv = userPupvv.getFirstName() + " " + userPupvv.getLastName();

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
                                binding.subcategoryProduct.setText(subcategory.getName());
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
                                binding.categoryProduct.setText(category.getName());
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