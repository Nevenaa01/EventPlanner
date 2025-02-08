package com.example.eventplanner.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.eventplanner.R;
import com.example.eventplanner.activities.AddSubcategoryActivity;
import com.example.eventplanner.activities.CategoryActivity;
import com.example.eventplanner.activities.EditCategoryActivity;
import com.example.eventplanner.model.Category;
import com.example.eventplanner.model.Subcategory;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class ExpandableListAdapter extends BaseExpandableListAdapter {
    private Context context;
    private List<Category> listDataHeader;
    private HashMap<Category, List<Subcategory>> listHashMap;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public ExpandableListAdapter(Context context, List<Category> listDataHeader, HashMap<Category, List<Subcategory>> listHashMap) {
        this.context = context;
        this.listDataHeader = listDataHeader;
        this.listHashMap = listHashMap;
    }

    @Override
    public int getGroupCount() {
        return listDataHeader.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        List<Subcategory> list=listHashMap.get(listDataHeader.get(groupPosition));
        if(list==null)return 0;
        return list.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return listDataHeader.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        //if(listHashMap.isEmpty())return null;
        return listHashMap.get(listDataHeader.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

//    private void getProductsAndServices(Long categoryId,Long subcategoryId){
//        db.collection("Products").whereEqualTo("categoryId", categoryId)
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(Task<QuerySnapshot> task) {
//                        if (task.isSuccessful()) {
//
//                            db.collection("Products").whereEqualTo("categoryId", categoryId)
//                                    .get()
//                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                                        @Override
//                                        public void onComplete(Task<QuerySnapshot> task2) {
//                                            if (task2.isSuccessful()) {
//                                                for (DocumentSnapshot document : task.getResult()) {
//                                                    Log.d("NestoSeDesilo2", document.getString("name"));
//                                                    return;
//
//                                                }
//                                                for (DocumentSnapshot document : task2.getResult()) {
//                                                    Log.d("NestoSeDesilo2", document.getString("name"));
//                                                    return;
//                                                }
//
//
//                                            } else {
//                                                Toast.makeText(context, "Deleting failed", Toast.LENGTH_SHORT).show();
//                                            }
//                                        }
//                                    });
//
//                        } else {
//                            Toast.makeText(context, "Deleting failed", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//    }
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        Category headerTitle =(Category) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_group, null);
        }
        TextView lblListHeader = convertView.findViewById(R.id.lblListHeader);
        lblListHeader.setText(headerTitle.getName());
        TextView lblListDescription = convertView.findViewById(R.id.lblListDescription);
        lblListDescription.setText(headerTitle.getDescription());

        ImageView iconEdit = convertView.findViewById(R.id.iconEdit);
        iconEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EditCategoryActivity.class);
                intent.putExtra("categoryName", headerTitle.getName());
                intent.putExtra("categoryDescription", headerTitle.getDescription());
                intent.putExtra("categoryId", headerTitle.getId());
                context.startActivity(intent);
            }
        });

        ImageView iconDelete = convertView.findViewById(R.id.iconDelete);

        iconDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //getProductsAndServices(headerTitle.getId(),headerTitle.getId());
                db.collection("Subcategories").whereEqualTo("CategoryName", headerTitle.getName())
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    // Iterate through the query result and delete each document
                                    for (DocumentSnapshot document : task.getResult()) {
                                        // Delete the document
                                        document.getReference().delete();

                                    }

                                } else {
                                    Toast.makeText(context, "Deleting failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                db.collection("Categories")
                        .document(headerTitle.getId().toString()).delete()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(context, "Category deleted", Toast.LENGTH_SHORT).show();
                            ((CategoryActivity) context).getCategories();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "error while deleting", Toast.LENGTH_SHORT).show();
                        });



            }
        });

        ImageView iconAddSubcategory = convertView.findViewById(R.id.iconAddSubcategory);
        iconAddSubcategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, AddSubcategoryActivity.class);
                intent.putExtra("categoryName", headerTitle.getName());
                context.startActivity(intent);
            }
        });
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final Subcategory childText = (Subcategory) getChild(groupPosition, childPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item, null);
        }
        TextView txtListChild = convertView.findViewById(R.id.lblListItem);
        txtListChild.setText(childText.getName());

        ImageView iconEditSub = convertView.findViewById(R.id.iconEditSub);
        iconEditSub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, AddSubcategoryActivity.class);
                intent.putExtra("subcategoryId", childText.getId());
                intent.putExtra("editButtonFlag", true);
                intent.putExtra("categoryName", childText.getCategoryName());
                intent.putExtra("name", childText.getName());
                intent.putExtra("description", childText.getDescription());
                intent.putExtra("type", childText.getType());
                context.startActivity(intent);
            }
        });
        ImageView iconDeleteSub = convertView.findViewById(R.id.iconDeleteSub);

        iconDeleteSub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteSubcategory(childText.getId());
                ((CategoryActivity) context).getCategories();
            }
        });
        return convertView;
    }
    private void deleteSubcategory(Long id){
        db.collection("Subcategories")
                .document(id.toString()).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Subcategory deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "error while deleting", Toast.LENGTH_SHORT).show();
                });
    }
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
