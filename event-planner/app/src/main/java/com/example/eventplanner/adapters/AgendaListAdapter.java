package com.example.eventplanner.adapters;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventplanner.R;
import com.example.eventplanner.activities.ShowOneEventActivity;
import com.example.eventplanner.model.AgendaActivity;
import com.example.eventplanner.model.EventType;
import com.example.eventplanner.model.SubcategoryPlanner;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AgendaListAdapter extends RecyclerView.Adapter<AgendaListAdapter.AgendaListViewHolder>{

    private static ArrayList<AgendaActivity> agendaActivities;

    public AgendaListAdapter(ArrayList<AgendaActivity> agendaActivities){
        this.agendaActivities = agendaActivities;
    }

    @NonNull
    @Override
    public AgendaListAdapter.AgendaListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.agenda_activity, parent, false);
        return new AgendaListAdapter.AgendaListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AgendaListAdapter.AgendaListViewHolder holder, int position) {
        AgendaActivity agendaActivity = agendaActivities.get(position);
        holder.bind(agendaActivity,position);
    }

    @Override
    public int getItemCount() {
        return agendaActivities.size();
    }

    public static class AgendaListViewHolder extends RecyclerView.ViewHolder {
        TextView nameActivity;
        TextView descriptionActivity;
        TextView durationFrom;
        TextView durationTo;
        TextView serialNum;

        TextView address;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        BottomSheetDialog bottomSheetDialog;

        ShowOneEventActivity activity;


        public AgendaListViewHolder(@NonNull View itemView) {
            super(itemView);
            activity = (ShowOneEventActivity) itemView.getContext();
            nameActivity = itemView.findViewById(R.id.nameActivity);
            descriptionActivity = itemView.findViewById(R.id.descriptionActivity);
            durationFrom = itemView.findViewById(R.id.durationFrom);
            durationTo = itemView.findViewById(R.id.durationTo);
            address = itemView.findViewById(R.id.addressAgendaActivity);
            serialNum = itemView.findViewById(R.id.serialNumber);


        }

        public void bind(AgendaActivity agendaActivity, int position) {
            nameActivity.setText(agendaActivity.getName());
            descriptionActivity.setText(agendaActivity.getDescription());
            durationFrom.setText(agendaActivity.getDurationFrom());
            durationTo.setText(agendaActivity.getDurationTo());
            address.setText(agendaActivity.getAddress());
            serialNum.setText(String.valueOf(position + 1) + ":");

        }
    }
}
