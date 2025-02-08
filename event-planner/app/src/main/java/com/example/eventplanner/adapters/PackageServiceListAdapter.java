package com.example.eventplanner.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.eventplanner.R;

import java.util.ArrayList;

import com.example.eventplanner.model.Service;

public class PackageServiceListAdapter extends ArrayAdapter<Service> {

    private ArrayList<Service> services;

    public PackageServiceListAdapter(Context context, ArrayList<Service> services){
        super(context, R.layout.service_card_package, services);
        this.services = services;
    }

    @Override
    public int getCount() {
        return this.services.size();
    }

    @Nullable
    @Override
    public Service getItem(int position) {
        return this.services.get(position);
    }

    @Override
    public long getItemId(int position) {
        return this.getItem(position).getId();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Service service = getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.service_card_package, parent, false);
        }

        ImageView productImage = convertView.findViewById(R.id.image);
        TextView productName = convertView.findViewById(R.id.name);
        TextView productDescription = convertView.findViewById(R.id.description);
        TextView productFullPrice = convertView.findViewById(R.id.price);
        TextView productPricePerHour = convertView.findViewById(R.id.pricePerHour);

        if(service != null){
            productImage.setImageURI(service.getImages().get(0));
            productName.setText(service.getName());
            productDescription.setText(service.getDescription());
            productFullPrice.setText(service.getFullPrice().toString() + "$");
            productPricePerHour.setText(service.getPricePerHour().toString() + "$/h");
        }

        return convertView;
    }
}
