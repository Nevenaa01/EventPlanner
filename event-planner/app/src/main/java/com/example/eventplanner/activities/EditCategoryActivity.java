package com.example.eventplanner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventplanner.R;
import com.example.eventplanner.services.FCMHttpClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class EditCategoryActivity extends AppCompatActivity {
    TextInputEditText descriptionInput;
    TextInputEditText nameInput;
    boolean isCategoryActive;
    Long categoryId;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth=FirebaseAuth.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_category);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Intent intent = getIntent();
        String categoryName = intent.getStringExtra("categoryName");
        String categoryDescription = intent.getStringExtra("categoryDescription");
        categoryId = intent.getLongExtra("categoryId",0);

        nameInput= findViewById(R.id.categoryName);
        nameInput.setText(categoryName);
        descriptionInput= findViewById(R.id.categoryDescription);
        descriptionInput.setText(categoryDescription);

        isCategoryActive = intent.getBooleanExtra("isAdd", false);
        if(isCategoryActive){
            Button button=findViewById(R.id.editCategory);
            button.setText("Add");
        }

        findViewById(R.id.editCategory).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addEditCategory(v);
            }
        });
    }

    String serverKey="AAAA8GYmoZ8:APA91bHsjyzOSa2JtO_cQWFO-X1p9nMuHRO8DTfD1zhcY4mnqZ-2EZmIn8tMf1ISmnM31WB68Mzn2soeUgEISXlSc9WjRvcRhyYbmBgi7whJuYXX-24wkODByasquofLaMZydpg78esK";
    public static void sendMessage(String serverKey, String jsonPayload) {
        FCMHttpClient httpClient = new FCMHttpClient();
        httpClient.sendMessageToTopic(serverKey, "PUPV", jsonPayload);
    }


    private void addEditCategory(View v){
        Long id;
        if(isCategoryActive){
            id = new Random().nextLong();
        }
        else{
            id=categoryId;
        }


        Map<String, Object> item = new HashMap<>();
        item.put("Name", nameInput.getText().toString());
        item.put("Description", descriptionInput.getText().toString());

        db.collection("Categories")
                .document(id.toString())
                .set(item)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(v.getContext(), "Product created", Toast.LENGTH_SHORT).show();
                        String jsonPayload = "{\"data\":{" +
                                "\"title\":\"New category!\"," +
                                "\"body\":\"" + item.get("Name").toString() + "\"," +
                                "\"topic\":\"PUPV_Category\"" +
                                "}," +
                                "\"to\":\"/topics/" + "PUPV" + "\"}";
                        sendMessage(serverKey,jsonPayload);
                        addNotification();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(v.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });



    }
    private void addNotification(){


        db.collection("User").whereEqualTo("UserType","PUPV").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    // Task was successful, process the documents
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot != null) {
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            Long id = new Random().nextLong();
                            Map<String,Object> map=new HashMap<>();
                            map.put("body",nameInput.getText().toString());
                            map.put("title","New category!");
                            map.put("read",false);
                            map.put("userId",document.getId());

                            db.collection("Notifications")
                                    .document(id.toString())
                                    .set(map);
                        }
                    }
                } else {
                    // Task failed, handle the error
                    System.out.println("Error getting documents: " + task.getException());
                }
            }
        });



    }
}