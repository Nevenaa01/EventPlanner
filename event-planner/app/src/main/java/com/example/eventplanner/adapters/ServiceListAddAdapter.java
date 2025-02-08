package com.example.eventplanner.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.eventplanner.R;

import java.util.ArrayList;

import com.example.eventplanner.model.Service;

public class ServiceListAddAdapter extends ArrayAdapter<Service> {
    private ArrayList<Service> services;
    private OnItemClickListener listener;

    public ServiceListAddAdapter(Context context, OnItemClickListener listener, ArrayList<Service> services){
        super(context, R.layout.service_card_add, services);
        this.services = services;
        this.listener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Service service = getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.service_card_add, parent, false);
        }

        ImageView productImage = convertView.findViewById(R.id.image);
        TextView productName = convertView.findViewById(R.id.name);
        TextView productDescription = convertView.findViewById(R.id.description);
        TextView productFullPrice = convertView.findViewById(R.id.price);
        TextView productPricePerHour = convertView.findViewById(R.id.pricePerHour);

        Button addButton = convertView.findViewById(R.id.add_service);

        if(service != null){
            Glide.with(getContext())
                    .load(service.getImages().get(0))
                    .into(productImage);            productName.setText(service.getName());
            productDescription.setText(service.getDescription());
            productFullPrice.setText(service.getFullPrice().toString() + "$");
            productPricePerHour.setText(service.getPricePerHour().toString() + "$/h");
        }

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(service);
            }
        });

        return convertView;
    }

    public interface OnItemClickListener {
        void onItemClick(Service item);
    }
}
