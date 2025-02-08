package com.example.eventplanner.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventplanner.R;
import com.example.eventplanner.adapters.SubcategoriesCardAdapter;
import com.example.eventplanner.model.Subcategory;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class SuggestedSubcategoriesActivity extends AppCompatActivity {
    List<Subcategory> dataList = new ArrayList<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onResume() {

        super.onResume();
        getSuggestedSubcategories();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_suggested_subcategories);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //getSuggestedSubcategories();


    }

    public void getSuggestedSubcategories(){
        dataList = new ArrayList<>();
        db.collection("SuggestedSubcategories")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for(DocumentSnapshot doc: task.getResult()) {
                                Subcategory subcategory = new Subcategory(
                                        Long.parseLong(doc.getId()),
                                        doc.getString("categoryName"),
                                        doc.getString("name"),
                                        doc.getString("description"),
                                        doc.getLong("type").intValue()
                                );
                                dataList.add(subcategory);
                            }
                            RecyclerView recyclerView = findViewById(R.id.recyclerView);
                            recyclerView.setLayoutManager(new LinearLayoutManager(SuggestedSubcategoriesActivity.this));
                            recyclerView.setAdapter(new SubcategoriesCardAdapter(dataList));

                        } else {
                            Toast.makeText(SuggestedSubcategoriesActivity.this, "Getting data failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}