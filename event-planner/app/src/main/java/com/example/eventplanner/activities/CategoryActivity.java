package com.example.eventplanner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventplanner.R;
import com.example.eventplanner.adapters.ExpandableListAdapter;

import com.example.eventplanner.model.Category;
import com.example.eventplanner.model.Subcategory;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CategoryActivity extends AppCompatActivity {
    ExpandableListView expandableListView;
    ExpandableListAdapter expandableListAdapter;
    List<Category> listDataHeader;
    HashMap<Category, List<Subcategory>> listDataChild;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_category);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        findViewById(R.id.addRecomendedCategoryButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CategoryActivity.this, SuggestedSubcategoriesActivity.class);
                startActivity(intent);
            }
        });


        expandableListView = findViewById(R.id.expandableListView);

        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            int previousGroup = -1;

            @Override
            public void onGroupExpand(int groupPosition) {
                if (groupPosition != previousGroup)
                    expandableListView.collapseGroup(previousGroup);
                previousGroup = groupPosition;

            }
        });

        Button addCategory = findViewById(R.id.addCategoryButton);
        addCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CategoryActivity.this, EditCategoryActivity.class);
                intent.putExtra("categoryName", "");
                intent.putExtra("categoryDescription", "");
                intent.putExtra("categoryId", 0);
                intent.putExtra("isAdd", true);
                startActivity(intent);
            }
        });

    }
    @Override
    protected void onResume() {
        super.onResume();

        getCategories();
    }

    public void getCategories(){
        listDataHeader = new ArrayList<>();
        HashMap<String, List<Subcategory>> map = new HashMap<>();
        listDataChild = new HashMap<>();


        db.collection("Subcategories")
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
                                    doc.getLong("Type").intValue()
                            );
                            if (map.containsKey(subcategory.getCategoryName())) {
                                List<Subcategory> list = map.get(subcategory.getCategoryName());
                                list.add(subcategory);
                            } else {
                                List<Subcategory> newList = new ArrayList<>();
                                newList.add(subcategory);
                                // Put the ArrayList into the map with the key "More"
                                map.put(subcategory.getCategoryName(), newList);
                            }
                        }
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
                                            if (map.containsKey(category.getName())) {
                                                listDataChild.put(category,map.get(category.getName()));
                                            }
                                            listDataHeader.add(category);
                                        }

                                        expandableListAdapter = new ExpandableListAdapter(CategoryActivity.this, listDataHeader, listDataChild);
                                        expandableListView.setAdapter(expandableListAdapter);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(CategoryActivity.this, e.getMessage(), Toast.LENGTH_SHORT);
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CategoryActivity.this, e.getMessage(), Toast.LENGTH_SHORT);
                    }
                });



    }




}