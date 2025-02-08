package com.example.eventplanner.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventplanner.R;
import com.example.eventplanner.model.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;


public class ChatRecyclerAdapter extends RecyclerView.Adapter<ChatRecyclerAdapter.ChatModelViewHolder> {

    private ArrayList<Message> messages;

    public ChatRecyclerAdapter(ArrayList<Message> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public ChatRecyclerAdapter.ChatModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_message_recycler_row, parent, false);
        return new ChatRecyclerAdapter.ChatModelViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ChatRecyclerAdapter.ChatModelViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class ChatModelViewHolder extends RecyclerView.ViewHolder{

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        LinearLayout leftChatLayout,rightChatLayout;
        TextView leftChatTextview,rightChatTextview;

        public ChatModelViewHolder(@NonNull View itemView) {
            super(itemView);

            leftChatLayout = itemView.findViewById(R.id.left_chat_layout);
            rightChatLayout = itemView.findViewById(R.id.right_chat_layout);
            leftChatTextview = itemView.findViewById(R.id.left_chat_textview);
            rightChatTextview = itemView.findViewById(R.id.right_chat_textview);
        }

        @SuppressLint("SetTextI18n")
        public void bind(Message message) {

            if(user.getUid().equals(message.getSenderId())){
                rightChatLayout.setVisibility(View.GONE);
                leftChatLayout.setVisibility(View.VISIBLE);
                leftChatTextview.setText(message.getContent());
            }else{
                leftChatLayout.setVisibility(View.GONE);
                rightChatLayout.setVisibility(View.VISIBLE);
                rightChatTextview.setText(message.getContent());
            }

        }
    }
}
