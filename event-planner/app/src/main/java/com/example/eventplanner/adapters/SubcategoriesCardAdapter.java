package com.example.eventplanner.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.eventplanner.R;
import com.example.eventplanner.activities.AddSubcategoryActivity;

import com.example.eventplanner.activities.CategoryActivity;
import com.example.eventplanner.activities.SuggestedSubcategoriesActivity;
import com.example.eventplanner.model.Subcategory;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class SubcategoriesCardAdapter extends RecyclerView.Adapter<SubcategoriesCardAdapter.ViewHolder> {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<Subcategory> dataList;

    public SubcategoriesCardAdapter(List<Subcategory> dataList) {
        this.dataList = dataList;
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public TextView textView2;
        public ImageView editIcon;
        public ImageView addIcon;
        public ImageView deleteIcon;
        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.lblListItem);
            textView2 = itemView.findViewById(R.id.lblListItemUnder);
            editIcon = itemView.findViewById(R.id.iconEditSub);
            addIcon = itemView.findViewById(R.id.iconAddSub);
            deleteIcon = itemView.findViewById(R.id.iconDeleteSub);
        }
    }
    @Override
    public SubcategoriesCardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SubcategoriesCardAdapter.ViewHolder holder, int position) {
        Subcategory data = dataList.get(position);
        holder.textView.setText(data.getCategoryName());
        holder.textView2.setText(data.getName());

        holder.addIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Context context = holder.itemView.getContext();
                Intent intent = new Intent(context, AddSubcategoryActivity.class);
                intent.putExtra("editButtonFlag", true);
                intent.putExtra("subcategoryId", data.getId());
                intent.putExtra("categoryName", data.getCategoryName());
                intent.putExtra("name", data.getName());
                intent.putExtra("description", data.getDescription());
                intent.putExtra("type", data.getType());
                context.startActivity(intent);
            }
        });

        holder.deleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Context context = holder.itemView.getContext();
                db.collection("SuggestedSubcategories")
                        .document(data.getId().toString()).delete()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(context, "Category deleted", Toast.LENGTH_SHORT).show();
                            ((SuggestedSubcategoriesActivity) context).getSuggestedSubcategories();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "error while deleting", Toast.LENGTH_SHORT).show();
                        });

            }
        });


    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }
}