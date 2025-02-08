package com.example.eventplanner.adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventplanner.R;

import java.util.ArrayList;

import com.example.eventplanner.model.Subcategory;

public class SubcategoryListAdapter extends RecyclerView.Adapter<SubcategoryListAdapter.SubcategoryViewHolder> {

    private ArrayList<Subcategory> subcategories;

    public SubcategoryListAdapter(ArrayList<Subcategory> subcategories){
        this.subcategories = subcategories;
    }

    @NonNull
    @Override
    public SubcategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.subcategories_of_servecis_products, parent, false);
        return new SubcategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubcategoryViewHolder holder, int position) {
        Subcategory subcategory = subcategories.get(position);
        holder.bind(subcategory);
    }

    @Override
    public int getItemCount() {
        return subcategories.size();
    }

    public static class SubcategoryViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName;
        TextView nameSubcategory;
        TextView descriptionSubcategory;
        TextView typeSubcategory;

        public SubcategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.nameCategory);
            nameSubcategory = itemView.findViewById(R.id.nameSubcategory);
            descriptionSubcategory = itemView.findViewById(R.id.descriptionSubcategory);
            typeSubcategory = itemView.findViewById(R.id.typeSubcategory);
        }

        public void bind(Subcategory subcategory) {
            categoryName.setText(subcategory.getCategoryName());
            nameSubcategory.setText(subcategory.getName());
            descriptionSubcategory.setText(subcategory.getDescription());
            typeSubcategory.setText(subcategory.getType() == 1 ? "Product" : "Service");

        }
    }
}
