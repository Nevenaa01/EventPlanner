package com.example.eventplanner.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventplanner.R;
import com.example.eventplanner.activities.AddSubcategoryActivity;
import com.example.eventplanner.activities.ApproveRegistrationActivity;
import com.example.eventplanner.activities.HomeActivity;
import com.example.eventplanner.activities.PUPV_RegisterCategoryActivity;
import com.example.eventplanner.activities.SuggestedSubcategoriesActivity;
import com.example.eventplanner.activities.UserDetailsActivity;
import com.example.eventplanner.fragments.ReasonFragment;
import com.example.eventplanner.model.Subcategory;
import com.example.eventplanner.model.UserPUPV;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class PupvUserCardAdapter extends RecyclerView.Adapter<PupvUserCardAdapter.ViewHolder> {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<UserPUPV> dataList;
    private Context context;
    private ApproveRegistrationActivity activity;

    public PupvUserCardAdapter(List<UserPUPV> dataList,Context context,ApproveRegistrationActivity activity) {
        this.dataList = dataList;
        this.context=context;
        this.activity = activity;
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameText;
        public TextView emailText;
        public TextView comapanyNameText;
        public TextView comapanyEmailText;

        public ImageView editIcon;
        public ImageView addIcon;
        public ImageView deleteIcon;

        public ImageView detailsIcon;
        public ViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.nameText);
            emailText = itemView.findViewById(R.id.emailText);
            comapanyNameText = itemView.findViewById(R.id.comapanyNameText);
            comapanyEmailText = itemView.findViewById(R.id.comapanyEmailText);

            editIcon = itemView.findViewById(R.id.iconEditSub);
            addIcon = itemView.findViewById(R.id.iconAddSub);
            deleteIcon = itemView.findViewById(R.id.iconDeleteSub);
            detailsIcon= itemView.findViewById(R.id.detailsIconSub);
        }
    }
    @Override
    public PupvUserCardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_register, parent, false);
        return new PupvUserCardAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PupvUserCardAdapter.ViewHolder holder, int position) {
        UserPUPV data = dataList.get(position);
        holder.nameText.setText(data.getFirstName()+ " " + data.getLastName());
        holder.emailText.setText(data.getEmail());
        holder.comapanyNameText.setText(data.getCompanyName());
        holder.comapanyEmailText.setText(data.getCompanyemail());

        holder.addIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> updates = new HashMap<>();
                updates.put("IsValid", true);
                db.collection("User").document(data.getId()).update(updates)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("Firestore", "DocumentSnapshot successfully updated!");
                                activity.getUsers();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Handle the error
                                Log.w("Firestore", "Error updating document", e);
                            }
                        });

            }
        });

        holder.deleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReasonFragment fragment = new ReasonFragment(data);
                fragment.show(((FragmentActivity) context).getSupportFragmentManager(), "ReasonFragment");
            }
        });

        holder.detailsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, UserDetailsActivity.class);
                intent.putExtra("user", data);
                context.startActivity(intent);
            }
        });

    }


    @Override
    public int getItemCount() {
        return dataList.size();
    }
}
