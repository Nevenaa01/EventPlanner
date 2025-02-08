package com.example.eventplanner.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.eventplanner.R;
import com.example.eventplanner.model.Notification;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class NotificationAdapter extends ArrayAdapter<Notification> {
    private ArrayList<Notification> notifications;
    private int resource;
    private Context context;
    FirebaseFirestore db;

    public NotificationAdapter(Context context, int resource, ArrayList<Notification> notifications){
        super(context, resource, notifications);
        this.notifications = notifications;
        this.resource = resource;
        this.context = context;

        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(resource, parent, false);
        }

        Notification notification = notifications.get(position);

        TextView title = convertView.findViewById(R.id.title);
        TextView body = convertView.findViewById(R.id.body);

        title.setText(notification.getTitle());
        body.setText(notification.getBody());

        Button read = convertView.findViewById(R.id.read);
        if(notification.getRead()){
            read.setVisibility(View.GONE);
        }

        RelativeLayout notificationCard = convertView.findViewById(R.id.notification_card);
        notificationCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                read.setVisibility(View.GONE);
                notification.setRead(true);
                notifyDataSetChanged();

                updateInDb(notification.getId());
            }
        });
        return convertView;
    }

    private void updateInDb(Long id){
        db.collection("Notifications")
                .document(id.toString())
                .update("read", true);
    }
}
