package com.example.eventplanner.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.eventplanner.R;
import com.example.eventplanner.activities.ShowOneServiceActivity;
import com.example.eventplanner.model.Service;

import java.util.ArrayList;

public class ServiceListAdapter extends ArrayAdapter<Service> {
    private ArrayList<Service> services;
    Button remove;
    int resource;
    private OnItemRemovedListener onItemRemovedListener;
    public interface OnItemRemovedListener {
        void onServiceRemoved(Service removedItem);
    }
    public void setOnItemRemovedListener(OnItemRemovedListener listener) {
        this.onItemRemovedListener = listener;
    }

    public ServiceListAdapter(Context context, int resource, ArrayList<Service> services){
        super(context, resource, services);
        this.services = services;
        this.resource = resource;
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
            convertView = LayoutInflater.from(getContext()).inflate(resource, parent, false);
        }

        ImageView productImage = convertView.findViewById(R.id.image);
        TextView productName = convertView.findViewById(R.id.name);
        TextView productDescription = convertView.findViewById(R.id.description);
        TextView productFullPrice = convertView.findViewById(R.id.price);
        TextView productPricePerHour = convertView.findViewById(R.id.pricePerHour);

        if(service != null){
            Glide.with(getContext())
                    .load(service.getImages().get(0))
                    .into(productImage);
            productName.setText(service.getName());
            productDescription.setText(service.getDescription());
            productFullPrice.setText(service.getFullPrice().toString() + "$");
            productPricePerHour.setText(service.getPricePerHour().toString() + "$/h");
        }

        RelativeLayout serviceCard = convertView.findViewById(R.id.service_card);
        serviceCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serviceCard.setAlpha(0.3f);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        serviceCard.setAlpha(1.0f);
                    }
                }, 100);

                Intent intent = new Intent(getContext(), ShowOneServiceActivity.class);
                intent.putExtra("serviceId", service.getId());
                intent.putExtra("pupvId", service.getPupvId());
                intent.putExtra("categoryId", service.getCategoryId());
                intent.putExtra("subcategoryId", service.getSubcategoryId());
                intent.putExtra("name", service.getName());
                intent.putExtra("description", service.getDescription());
                intent.putExtra("price", service.getFullPrice());
                intent.putExtra("discount", service.getDiscount());
                intent.putExtra("available", service.getAvailable());
                intent.putParcelableArrayListExtra("images", service.getImages());
                intent.putExtra("specific",service.getSpecific());
                intent.putExtra("duration", service.getDuration());
                intent.putExtra("pricePerHour",service.getPricePerHour());
                intent.putExtra("duration", service.getDuration());
                intent.putExtra("durationMin", service.getDurationMin());
                intent.putExtra("durationMax",service.getDurationMax());
                intent.putExtra("deadlineReservation", service.getReservationDue());
                intent.putExtra("cancellationReservation", service.getCancelationDue());
                intent.putStringArrayListExtra("pupIds", service.getPupIds());
                intent.putStringArrayListExtra("eventTypeIds", convertLongListToStringList(service.getEventTypeIds()));

                getContext().startActivity(intent);

            }
        });

        if(resource == R.layout.service_card_package) {

            remove = convertView.findViewById(R.id.remove);

            remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Service removedItem = services.get(position);
                    services.remove(position);
                    notifyDataSetChanged();

                    if (onItemRemovedListener != null) {
                        onItemRemovedListener.onServiceRemoved(removedItem);
                    }
                }

            });
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
