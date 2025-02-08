package com.example.eventplanner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventplanner.R;
import com.example.eventplanner.model.EventType;
import com.example.eventplanner.model.Subcategory;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class AddEventTypesActivity extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    List<Subcategory> subcategories=new ArrayList<>();
    Long eventTypeId;
    TextInputEditText typeNameTextField;
    TextInputEditText typeDescriptionTextField;
    boolean inUse;
   List<String> subcategoriesList=new ArrayList<>();
    boolean editFlag;
    ListView subcategoriesView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_event_types);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        Intent intent = getIntent();
        editFlag=intent.getBooleanExtra("editButtonFlag",false);
        eventTypeId = intent.getLongExtra("eventTypeId",0);
        String typeName = intent.getStringExtra("typeName");
        String typeDecription = intent.getStringExtra("typeDecription");
        inUse = intent.getBooleanExtra("inUse",true);
        typeNameTextField= findViewById(R.id.typeName);
        typeNameTextField.setText(typeName);
        typeDescriptionTextField= findViewById(R.id.typeDescription);
        typeDescriptionTextField.setText(typeDecription);
        if(editFlag){
            typeNameTextField.setClickable(false);
            typeNameTextField.setFocusable(false);
        }
        getSubcategories();
        findViewById(R.id.addType).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                putEventType();

            }
        });


    }
    private void setupListViewForCategories() {
        subcategoriesView = findViewById(R.id.subcategoriesView);
        ArrayAdapter<String> adapterSubategories = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, subcategoriesList);
        subcategoriesView.setAdapter(adapterSubategories);

    }

    private void getSubcategories(){
        db.collection("Subcategories")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> taskSubcategories) {
                        if (taskSubcategories.isSuccessful()) {
                            for(DocumentSnapshot doc: taskSubcategories.getResult()){
                                Subcategory subcategory = new Subcategory(
                                        Long.parseLong(doc.getId()),
                                        doc.getString("CategoryName"),
                                        doc.getString("Name"),
                                        doc.getString("Description"),
                                        doc.getLong("Type").intValue()
                                );
                                subcategories.add(subcategory);
                                subcategoriesList.add(subcategory.getName());
                            }
                            setupListViewForCategories();
                            List<String> itemList =  getIntent().getStringArrayListExtra("recomendedSubcategories");
                            if(itemList!=null){
                                for (String item:itemList) {
                                    int position=subcategoriesList.indexOf(item);
                                    if (position != -1) {
                                        subcategoriesView.setItemChecked(position, true);
                                    }
                                }
                            }

                        } else {
                            Toast.makeText(AddEventTypesActivity.this, "Error fetching subcategories: " + taskSubcategories.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void putEventType(){
        Long id;
        if(!editFlag){
            id = new Random().nextLong();
        }
        else{
            id=eventTypeId;
        }
        SparseBooleanArray checkedPositions = subcategoriesView.getCheckedItemPositions();
        List<String> ids=new ArrayList<>();
        for (int i=0;i<subcategories.size();i++) {
            if(checkedPositions.get(i)){
                ids.add(subcategories.get(i).getId().toString());
            }

        }

        Map<String, Object> item = new HashMap<>();
        item.put("Name", typeNameTextField.getText().toString());
        item.put("Description", typeDescriptionTextField.getText().toString());
        item.put("InUse", inUse);
        item.put("Subcategories", ids);

        db.collection("EventTypes")
                .document(id.toString())
                .set(item)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(AddEventTypesActivity.this, "Product created", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }
}