package com.example.eventplanner.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.eventplanner.R;
import com.example.eventplanner.model.Event;

import java.util.ArrayList;

public class EventListAdapter  extends ArrayAdapter<Event> {
    private ArrayList<Event> events;
    Button remove;
    private int resource;

    public EventListAdapter(Context context, int resource, ArrayList<Event> events){
        super(context, resource, events);
        this.events = events;
        this.resource = resource;
    }

    @Override
    public int getCount() {
        return this.events.size();
    }

    @Nullable
    @Override
    public Event getItem(int position) {
        return this.events.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            view = inflater.inflate(resource, parent, false);
        }

        TextView eventNameTextView = resource == android.R.layout.simple_dropdown_item_1line ? view.findViewById(android.R.id.text1) : view.findViewById(R.id.event_name);
        eventNameTextView.setText(getItem(position).getName());


        if(resource != android.R.layout.simple_dropdown_item_1line && resource != R.layout.event_card_package) {
            remove = view.findViewById(R.id.remove);

            remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    events.remove(position);
                    notifyDataSetChanged();
                }
            });
        }

        return view;
    }
}
