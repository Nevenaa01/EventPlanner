package com.example.eventplanner.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eventplanner.R;

import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Uri> arrayList;
    private int resource;

    public ImageAdapter(Context context, int resource, ArrayList<Uri> arrayList){
        this.context = context;
        this.arrayList = arrayList;
        this.resource = resource;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(resource, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Glide.with(context).load(arrayList.get(position)).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public  class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        Button remove;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.list_item_image);

            if(resource == R.layout.image_carousel_card) {
                remove = itemView.findViewById(R.id.remove_image);

                remove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            arrayList.remove(position);
                            notifyDataSetChanged();
                        }
                    }
                });
            }
        }
    }
}
