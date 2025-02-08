package com.example.eventplanner.adapters;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eventplanner.R;
import com.example.eventplanner.activities.ShowOneChatActivity;
import com.example.eventplanner.model.Message;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ChatsRecyclerViewAdapter extends RecyclerView.Adapter<ChatsRecyclerViewAdapter.ChatsViewHolder>{
    private static ArrayList<Message> messages;

    public ChatsRecyclerViewAdapter(ArrayList<Message> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public ChatsRecyclerViewAdapter.ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_card, parent, false);
        return new ChatsRecyclerViewAdapter.ChatsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatsRecyclerViewAdapter.ChatsViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public interface OnDataLoadedListener {
        void onDataLoaded(String firstName, String lastName);
    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder {

        ImageView imageUser;
        TextView firstAndLastName;
        TextView dateOfSending;
        TextView lastMessage;
        FirebaseFirestore db;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);
            imageUser = itemView.findViewById(R.id.imageUser);
            firstAndLastName = itemView.findViewById(R.id.nameandSurnameUser);
            dateOfSending = itemView.findViewById(R.id.dateOfSending);
            lastMessage = itemView.findViewById(R.id.lastMessage);
            db = FirebaseFirestore.getInstance();

            LinearLayout openChat = itemView.findViewById(R.id.chatCard);
            openChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openChat.setAlpha(0.3f);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            openChat.setAlpha(1.0f);
                        }
                    }, 100);

                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Message message = messages.get(position);

                        Intent intent = new Intent(itemView.getContext(), ShowOneChatActivity.class);
                        intent.putExtra("senderId", message.getSenderId());
                        intent.putExtra("senderFullname", message.getSenderFullName());
                        intent.putExtra("recipientId", message.getRecipientId());
                        intent.putExtra("recipientFullname", message.getRecipientFullName());

                        itemView.getContext().startActivity(intent);
                    }

                }
            });
        }

        @SuppressLint("SetTextI18n")
        public void bind(Message message) {
            lastMessage.setText(message.getContent());
            //SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            //dateOfSending.setText((sdf.format(message.getDateOfSending().toString())));'

            System.out.println(message.getDateOfSending().toString());

            SimpleDateFormat inputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");

            try {
                Date date = inputFormat.parse(message.getDateOfSending().toString());
                String formattedDate = outputFormat.format(date);
                dateOfSending.setText(formattedDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            loadImage(message.getRecipientId(),imageUser);

            firstAndLastName.setText(message.getRecipientFullName());
        }

        private void loadImage(String userId, ImageView imageView) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();

            StorageReference imageRef = storageRef.child("images/" + userId);

            imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    // Koristimo Glide za učitavanje slike
                    Glide.with(imageView.getContext())
                            .load(uri)
                            .into(imageView);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.e("HomeTwoActivity", "Error loading image", exception);
                    imageView.setImageResource(R.drawable.defaultprofilepicture);
                }
            });
        }

    }
}
