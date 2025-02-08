package com.example.eventplanner.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.eventplanner.activities.EditPackageActivity;
import com.example.eventplanner.R;

import java.util.ArrayList;

import com.example.eventplanner.model.Package;
import com.example.eventplanner.model.Product;
import com.example.eventplanner.model.Service;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

public class PackageListPupvAdapter extends ArrayAdapter<Package> {

    private ArrayList<Package> packages;
    private FirebaseFirestore db;

    public PackageListPupvAdapter(Context context, ArrayList<Package> packages){
        super(context, R.layout.package_card_pupv, packages);
        this.packages = packages;
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public int getCount() {
        return this.packages.size();
    }

    @Nullable
    @Override
    public Package getItem(int position) {
        return this.packages.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Package pckage = getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.package_card_pupv, parent, false);
        }

        TextView name = convertView.findViewById(R.id.name);
        TextView description = convertView.findViewById(R.id.description);
        TextView price = convertView.findViewById(R.id.price);
        TextView products = convertView.findViewById(R.id.products);
        TextView services = convertView.findViewById(R.id.services);


        if(pckage != null){
            name.setText(pckage.getName());
            description.setText(pckage.getDescription());
            price.setText(pckage.getPrice().toString() + "$");
            products.setText(String.valueOf(pckage.getProductIds().size()));
            services.setText(String.valueOf(pckage.getServiceIds().size()));
        }

        Button editButton = convertView.findViewById(R.id.edit);
        Button deleteButton = convertView.findViewById(R.id.delete);

        final View finalConvertView = convertView;
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(finalConvertView.getContext(), EditPackageActivity.class);
                intent.putExtra("Id", pckage.getId());

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
                        db.collection("Packages")
                                .document(packages.get(position).getId().toString())
                                .update("deleted", true)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        packages.remove(position);
                                        notifyDataSetChanged();
                                        Toast.makeText(finalConvertView.getContext(), "Product deleted", Toast.LENGTH_LONG).show();
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
