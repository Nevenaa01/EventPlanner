package com.example.eventplanner.adapters;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventplanner.R;
import com.example.eventplanner.activities.ShowOneEventActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import com.example.eventplanner.model.Event;

public class EventRecyclerViewAdapter  extends RecyclerView.Adapter<EventRecyclerViewAdapter.EventsViewHolder> {

    private ArrayList<Event> events;

    public EventRecyclerViewAdapter(ArrayList<Event> events) {
        this.events = events;
    }

    @NonNull
    @Override
    public EventsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_card_purple_light, parent, false);
        return new EventsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventsViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public static class EventsViewHolder extends RecyclerView.ViewHolder {
        TextView eventName;

        TextView eventType;
        TextView eventDescription;
        TextView eventLocation;
        TextView eventDistanceLocation;
        TextView eventAvailable;
        TextView eventDate;
        TextView idEvent;

        LinearLayout openEvent;


        public EventsViewHolder(@NonNull View itemView) {
            super(itemView);
            eventName = itemView.findViewById(R.id.nameEvent);
            eventType = itemView.findViewById(R.id.typeEvent);
            eventDescription = itemView.findViewById(R.id.descriptionEvent);
            eventLocation = itemView.findViewById(R.id.locationEventShow);
            eventDistanceLocation = itemView.findViewById(R.id.doo);
            eventAvailable = itemView.findViewById(R.id.availableEvent);
            eventDate = itemView.findViewById(R.id.dateEvent);
            idEvent = itemView.findViewById(R.id.eventId);

            openEvent = itemView.findViewById(R.id.eventCard);
            openEvent.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    openEvent.setAlpha(0.3f);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            openEvent.setAlpha(1.0f);
                        }
                    }, 100);



                    Intent intent = new Intent(itemView.getContext(), ShowOneEventActivity.class);
                    intent.putExtra("eventId", idEvent.getText());
                    intent.putExtra("eventName", eventName.getText());
                    intent.putExtra("eventDescription", eventDescription.getText());
                    intent.putExtra("eventLocation", eventLocation.getText());
                    intent.putExtra("eventDistanceLocation", eventDistanceLocation.getText());
                    intent.putExtra("eventType", eventType.getText());
                    intent.putExtra("eventDate", eventDate.getText());
                    itemView.getContext().startActivity(intent);

                }
            });
        }

        @SuppressLint("SetTextI18n")
        public void bind(Event event) {
            eventName.setText(event.getName());
            eventType.setText(event.getTypeEvent());
            eventDescription.setText(event.getDescription());
            eventLocation.setText(event.getLocationPlace());
            idEvent.setText(Long.toString(event.getId()));

            eventDistanceLocation.setText(Integer.toString(event.getMaxDistance()));
            eventAvailable.setText(event.isAvailble() ? "Da" : "Ne");
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

            eventDate.setText((sdf.format(event.getDateEvent()).toString()));
        }
    }
}