package com.example.eventplanner.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.eventplanner.R;

import java.util.ArrayList;

public class EventListPackageAdapter extends ArrayAdapter<String> {
    private ArrayList<String> events;

    public EventListPackageAdapter(Context context, ArrayList<String> events){
        super(context, R.layout.event_card_package, events);
        this.events = events;
    }

    @Override
    public int getCount() {
        return this.events.size();
    }

    @Nullable
    @Override
    public String getItem(int position) {
        return this.events.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String event = getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.event_card_package, parent, false);
        }

        TextView eventName = convertView.findViewById(R.id.event_name);

        if(event != null){
            eventName.setText(event);
        }

        return convertView;
    }
}
