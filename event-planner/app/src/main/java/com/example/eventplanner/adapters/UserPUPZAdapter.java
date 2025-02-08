package com.example.eventplanner.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventplanner.R;
import com.example.eventplanner.model.UserPUPZ;

import java.util.List;

public class UserPUPZAdapter extends RecyclerView.Adapter<UserPUPZAdapter.ViewHolder> {

    private List<UserPUPZ> userList;
    private Context context;
    private OnUserClickListener onUserClickListener;

    public UserPUPZAdapter(List<UserPUPZ> userList, Context context, OnUserClickListener onUserClickListener) {
        this.userList = userList;
        this.context = context;
        this.onUserClickListener = onUserClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_worker_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserPUPZ user = userList.get(position);

        holder.workerNameSurname.setText(user.getFirstName() + " " + user.getLastName());
        holder.workerPhoneNumber.setText(user.getPhone());
        holder.workerEmail.setText(user.getEmail());
        holder.workerLocation.setText(user.getAddress());

        if (user.isValid()) {
            holder.workerIsVerifiedWrapper.setVisibility(View.VISIBLE);
        } else {
            holder.workerIsVerifiedWrapper.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (onUserClickListener != null) {
                onUserClickListener.onUserClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView workerNameSurname, workerPhoneNumber, workerEmail, workerLocation;
        LinearLayout workerIsVerifiedWrapper;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            workerNameSurname = itemView.findViewById(R.id.worker_name_surname);
            workerPhoneNumber = itemView.findViewById(R.id.worker_phone_number);
            workerEmail = itemView.findViewById(R.id.worker_email);
            workerLocation = itemView.findViewById(R.id.worker_location);
            workerIsVerifiedWrapper = itemView.findViewById(R.id.worker_is_verified_wrapper);
        }
    }

    public interface OnUserClickListener {
        void onUserClick(UserPUPZ user);
    }
}

