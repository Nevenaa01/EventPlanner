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

import com.example.eventplanner.R;

import java.util.ArrayList;

public class ProviderListAdapter extends ArrayAdapter<String> {
    private ArrayList<String> providers;
    Button remove;

    public ProviderListAdapter(Context context, ArrayList<String> providers) {
        super(context, R.layout.event_card, providers);
        this.providers = providers;
    }

    @Override
    public int getCount() {
        return this.providers.size();
    }

    @Nullable
    @Override
    public String getItem(int position) {
        return this.providers.get(position);
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String provider = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.event_card, parent, false);
        }

        TextView name = convertView.findViewById(R.id.event_name);

        if (provider != null) {
            name.setText(provider);
        }

        remove = convertView.findViewById(R.id.remove);

        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                providers.remove(position);
                notifyDataSetChanged();
            }
        });

        return convertView;
    }
}
