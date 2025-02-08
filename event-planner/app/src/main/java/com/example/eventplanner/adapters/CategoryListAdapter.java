package com.example.eventplanner.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.eventplanner.model.Category;
import com.example.eventplanner.model.Event;

import java.util.ArrayList;

public class CategoryListAdapter extends ArrayAdapter<Category> {
    private ArrayList<Category> categories;
    private int resource;

    public CategoryListAdapter(Context context, int resource, ArrayList<Category> categories){
        super(context, resource, categories);
        this.categories = categories;
        this.resource = resource;
    }

    @Override
    public int getCount() {
        return this.categories.size();
    }

    @Nullable
    @Override
    public Category getItem(int position) {
        return this.categories.get(position);
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
