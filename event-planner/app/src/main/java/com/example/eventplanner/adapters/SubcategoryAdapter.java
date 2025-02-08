package com.example.eventplanner.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.eventplanner.model.Event;
import com.example.eventplanner.model.Subcategory;

import java.util.ArrayList;

public class SubcategoryAdapter extends ArrayAdapter<Subcategory> {
    private ArrayList<Subcategory> subcategories;
    private int resource;

    public SubcategoryAdapter(Context context, int resource, ArrayList<Subcategory> subcategories){
        super(context, resource, subcategories);
        this.subcategories = subcategories;
        this.resource = resource;
    }

    @Override
    public int getCount() {
        return this.subcategories.size();
    }

    @Nullable
    @Override
    public Subcategory getItem(int position) {
        return this.subcategories.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        TextView textView = (TextView) view.findViewById(android.R.id.text1);
        textView.setText(getItem(position).getName());
        return view;
    }

}
