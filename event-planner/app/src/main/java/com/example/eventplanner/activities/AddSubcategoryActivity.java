package com.example.eventplanner.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class AddSubcategoryActivity extends AppCompatActivity {
    Long subcategoryId;
    boolean editButtonFlag;

    TextInputEditText categoryNameTextField;
    TextInputEditText subcategoryNameTextField;
    TextInputEditText descriptionNameTextField;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_subcategory);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        String categoryName = intent.getStringExtra("categoryName");
        String subcategoryName = intent.getStringExtra("name");
        String description = intent.getStringExtra("description");
        subcategoryId = intent.getLongExtra("subcategoryId",0);

        int type = intent.getIntExtra("type",-1);
        if(type==0){
            RadioButton radioButtonService = findViewById(R.id.serviceRadio);
            radioButtonService.setChecked(true);
        }else if(type==1){
            RadioButton radioButtonProduct = findViewById(R.id.productRadio);
            radioButtonProduct.setChecked(true);
        }else{
            Log.i("NEKITAG","Error with radio");
        }
        editButtonFlag = intent.getBooleanExtra("editButtonFlag",false);

        if(editButtonFlag){
            Button button = findViewById(R.id.addSubcategory);
            button.setText("Edit");
        }

        categoryNameTextField= findViewById(R.id.categoryName);
        categoryNameTextField.setText(categoryName);
        subcategoryNameTextField= findViewById(R.id.subcategoryName);
        subcategoryNameTextField.setText(subcategoryName);
        descriptionNameTextField= findViewById(R.id.subcategoryDescription);
        descriptionNameTextField.setText(description);
        findViewById(R.id.addSubcategory).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addEditSubcategory(v);
            }
        });

    }
    String serverKey="AAAA8GYmoZ8:APA91bHsjyzOSa2JtO_cQWFO-X1p9nMuHRO8DTfD1zhcY4mnqZ-2EZmIn8tMf1ISmnM31WB68Mzn2soeUgEISXlSc9WjRvcRhyYbmBgi7whJuYXX-24wkODByasquofLaMZydpg78esK";
    public static void sendMessage(String serverKey, String jsonPayload) {
        FCMHttpClient httpClient = new FCMHttpClient();
        httpClient.sendMessageToTopic(serverKey, "PUPV", jsonPayload);
    }
    private void addEditSubcategory(View v){
        Long id;
        if(!editButtonFlag){
            id = new Random().nextLong();
        }
        else{
            id=subcategoryId;
        }


        Map<String, Object> item = new HashMap<>();
        item.put("CategoryName", categoryNameTextField.getText().toString());
        item.put("Name", subcategoryNameTextField.getText().toString());
        item.put("Description", descriptionNameTextField.getText().toString());

        RadioButton radioButtonService = findViewById(R.id.serviceRadio);
        RadioButton radioButtonProduct = findViewById(R.id.productRadio);

        if(radioButtonProduct.isChecked()){
            item.put("Type", 1);
        }else if(radioButtonService.isChecked()){
            item.put("Type", 0);
        }
        else {
            item.put("Type", 2);
        }

        db.collection("Subcategories")
                .document(id.toString())
                .set(item)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(v.getContext(), "Subcategory created", Toast.LENGTH_SHORT).show();
                        db.collection("SuggestedSubcategories").document(id.toString())
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot document = task.getResult();
                                            document.getReference().delete();
                                            String jsonPayload = "{\"data\":{\"title\":\"New subcategory!\",\"body\":\""+item.get("Name").toString()+"\"},\"to\":\"/topics/" + "PUPV" + "\"}";
                                            sendMessage(serverKey,jsonPayload);
                                        }
                                        finish();
                                    }
                                });


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(v.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });



    }
}