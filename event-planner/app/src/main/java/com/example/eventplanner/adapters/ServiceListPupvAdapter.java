package com.example.eventplanner.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.eventplanner.activities.EditServiceActivity;
import com.example.eventplanner.R;

import java.util.ArrayList;

import com.example.eventplanner.model.Service;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

public class ServiceListPupvAdapter extends ArrayAdapter<Service> {
    private ArrayList<Service> services;
    private FirebaseFirestore db;

    public ServiceListPupvAdapter(Context context, ArrayList<Service> services){
        super(context, R.layout.service_card_pupv, services);
        this.services = services;
        db = FirebaseFirestore.getInstance();
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
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.service_card_pupv, parent, false);
        }

        ImageView image = convertView.findViewById(R.id.image);
        TextView name = convertView.findViewById(R.id.name);
        TextView description = convertView.findViewById(R.id.description);
        TextView fullPrice = convertView.findViewById(R.id.price);
        TextView pricePerHour = convertView.findViewById(R.id.pricePerHour);

        if(service != null){
            Glide.with(getContext())
                    .load(service.getImages().get(0))
                    .into(image);
            name.setText(service.getName());
            description.setText(service.getDescription());
            fullPrice.setText(service.getFullPrice().toString() + "$");
            pricePerHour.setText(service.getPricePerHour().toString() + "$/h");
        }

        Button editButton = convertView.findViewById(R.id.edit);
        Button deleteButton = convertView.findViewById(R.id.delete);

        final View finalConvertView = convertView;
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(finalConvertView.getContext(), EditServiceActivity.class);
                intent.putExtra("Id", service.getId());

                finalConvertView.getContext().startActivity(intent);
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View popUpView = inflater.inflate(R.layout.confirmation_popup, null);

                int width = ViewGroup.LayoutParams.MATCH_PARENT;
                int height = ViewGroup.LayoutParams.MATCH_PARENT;
                boolean focusable = true;
                PopupWindow popupWindow = new PopupWindow(popUpView, width, height, focusable);

                popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);

                Button confrim = popUpView.findViewById(R.id.delete_button);
                confrim.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        db.collection("Services")
                                .document(services.get(position).getId().toString())
                                .update("deleted", true)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        services.remove(position);
                                        notifyDataSetChanged();
                                        Toast.makeText(finalConvertView.getContext(), "Service deleted", Toast.LENGTH_LONG).show();
                                        popupWindow.dismiss();
                                    }
                                })

                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(finalConvertView.getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    }
                });

                Button cancel = popUpView.findViewById(R.id.cancel_button);

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                    }
                });
            }
        });


        return convertView;
    }
}
