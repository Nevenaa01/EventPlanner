package com.example.eventplanner.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventplanner.adapters.ChatsRecyclerViewAdapter;
import com.example.eventplanner.databinding.FragmentChatsBinding;
import com.example.eventplanner.model.Message;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ChatsFragment extends Fragment {
    FragmentChatsBinding binding;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    ArrayList<Message> messages;

    RecyclerView recyclerView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentChatsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        FirebaseUser user = mAuth.getCurrentUser();

        recyclerView = binding.chatsRecyclerView;
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        findAllChats(user.getUid());

        return root;
    }

    private void findAllChats(String uid) {
        messages = new ArrayList<>();
        db.collection("Messages")
                .whereArrayContains("participants", uid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            for (DocumentSnapshot doc : task.getResult()) {
                                String senderId = doc.getString("senderId");
                                String senderFullaname = doc.getString("senderFullName");
                                String recipientId = doc.getString("recipientId");
                                String recipientFullname = doc.getString("fullnameRecipientId");
                                Date dateOfSending = doc.getDate("dateOfSending");
                                String content = doc.getString("content");
                                boolean status = doc.getBoolean("status");


                                if (recipientId.equals(uid)) {
                                    String temp = recipientId;
                                    String temp1 = recipientFullname;
                                    recipientId = senderId;
                                    recipientFullname = senderFullaname;
                                    senderId = temp;
                                    senderFullaname = temp1;
                                }

                                Message message = new Message(senderId, senderFullaname, recipientId, recipientFullname, dateOfSending, content, status);

                                messages.add(message);
                            }

                            // Grupisanje poruka po paru senderId i recipientId
                            Map<String, Message> messageMap = new HashMap<>();
                            for (Message message : messages) {
                                String key = message.getSenderId() + "-" + message.getRecipientId();
                                if (messageMap.containsKey(key)) {

                                    Message existingMessage = messageMap.get(key);
                                    if (message.getDateOfSending().after(existingMessage.getDateOfSending())) {
                                        messageMap.put(key, message);
                                    }
                                } else {
                                    messageMap.put(key, message);
                                }
                            }

                            ArrayList<Message> filteredMessages = new ArrayList<>(messageMap.values());

                            for(Message m : filteredMessages){

                            }

                            ChatsRecyclerViewAdapter adapterChats = new ChatsRecyclerViewAdapter(filteredMessages);
                            recyclerView.setAdapter(adapterChats);

                        } else {
                            Toast.makeText(requireContext(), "Error getting documents: " + task.getException(), Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}