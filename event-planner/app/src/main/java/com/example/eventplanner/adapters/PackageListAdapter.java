package com.example.eventplanner.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.eventplanner.R;
import com.example.eventplanner.activities.ShowOnePackageActivity;
import com.example.eventplanner.model.Package;

import java.util.ArrayList;

public class PackageListAdapter extends ArrayAdapter<Package> {

    private ArrayList<Package> packages;

    public PackageListAdapter(Context context, ArrayList<Package> packages){
        super(context, R.layout.package_card, packages);
        this.packages = packages;
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
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.package_card, parent, false);
        }

        TextView name = convertView.findViewById(R.id.name);
        TextView description = convertView.findViewById(R.id.description);
        TextView price = convertView.findViewById(R.id.price);
        TextView products = convertView.findViewById(R.id.products);
        TextView services = convertView.findViewById(R.id.services);

        RelativeLayout packageCard = convertView.findViewById(R.id.package_card);
        packageCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                packageCard.setAlpha(0.3f);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        packageCard.setAlpha(1.0f);
                    }
                }, 100);

                Intent intent = new Intent(getContext(), ShowOnePackageActivity.class);
                intent.putExtra("packageId", pckage.getId());
                intent.putExtra("pupvId", pckage.getPupvId());
                intent.putExtra("categoryId", pckage.getCategoryId());
                intent.putExtra("name", pckage.getName());
                intent.putExtra("description", pckage.getDescription());
                intent.putExtra("available", pckage.getAvailable());
                intent.putExtra("price",pckage.getPrice());
                intent.putExtra("discount",pckage.getDiscount());
                intent.putStringArrayListExtra("subcategoryIds", convertLongListToStringList(pckage.getSubCategoryId()));
                intent.putStringArrayListExtra("productIds", convertLongListToStringList(pckage.getProductIds()));
                intent.putStringArrayListExtra("serviceIds",convertLongListToStringList(pckage.getServiceIds()));
                intent.putStringArrayListExtra("eventTypeIds", convertLongListToStringList(pckage.getEventTypeIds()));
                intent.putExtra("deadlineReservation", pckage.getReservationDue());
                intent.putExtra("cancellationReservation", pckage.getCancelationDue());

                intent.putParcelableArrayListExtra("images",pckage.getImages());

                getContext().startActivity(intent);
            }
        });


        if(pckage != null){
            name.setText(pckage.getName());
            description.setText(pckage.getDescription());
            price.setText(pckage.getPrice().toString() + "$");
            products.setText(String.valueOf(pckage.getProductIds().size()));
            services.setText(String.valueOf(pckage.getServiceIds().size()));
        }

        return convertView;
    }

    public static ArrayList<String> convertLongListToStringList(ArrayList<Long> longList) {
        ArrayList<String> stringList = new ArrayList<>();
        for (Long number : longList) {
            stringList.add(String.valueOf(number));
        }
        return stringList;
    }
}
