package com.example.eventplanner.activities;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eventplanner.R;
import com.example.eventplanner.adapters.ChatRecyclerAdapter;
import com.example.eventplanner.databinding.ActivityShowOneChatBinding;
import com.example.eventplanner.model.Message;
import com.example.eventplanner.services.FCMHttpClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ShowOneChatActivity extends AppCompatActivity {

    ActivityShowOneChatBinding binding;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user;
    String recipientId;
    String senderId;
    String recipientFullname;
    String senderFullname;
    ArrayList<Message> messages;
    RecyclerView recyclerView;
    private ListenerRegistration listenerRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShowOneChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        user = mAuth.getCurrentUser();

        recipientId = getIntent().getStringExtra("recipientId");
        loadImage(recipientId, binding.imageRecipientProfile);
        senderId = getIntent().getStringExtra("senderId");

        recipientFullname = getIntent().getStringExtra("recipientFullname");
        senderFullname = getIntent().getStringExtra("senderFullname");

        binding.recipcientFullName.setText(recipientFullname);

        recyclerView = binding.chatMessageRecyclerRow;
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(ShowOneChatActivity.this);
        recyclerView.setLayoutManager(layoutManager);

        // Postavljanje snapshot listener-a
        setupSnapshotListener(senderId, recipientId);

        binding.sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(binding.inputMessage.getText().toString());
                binding.inputMessage.setText("");
            }
        });

        binding.imageRecipientProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShowOneChatActivity.this, UserInfoActivity.class);
                intent.putExtra("userId", recipientId);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }

    private void sendMessage(String message) {
        Map<String, Object> element = new HashMap<>();
        element.put("senderId", senderId);
        element.put("senderFullName", senderFullname);
        element.put("recipientId", recipientId);
        element.put("fullnameRecipientId", recipientFullname);
        element.put("dateOfSending", new Date());
        element.put("content", message);
        element.put("status", false);
        element.put("participants", Arrays.asList(senderId, recipientId));

        db.collection("Messages").document()
                .set(element)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("ShowOneChatActivity", "Message sent successfully");
                        createNotification(recipientId, message, senderFullname);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error adding document", e);
                    }
                });
    }

    private void createNotification(String userId, String message, String fullnameSender){
        Long id = new Random().nextLong();
        Map<String, Object> doc = new HashMap<>();

        doc.put("title", "Message from " + fullnameSender);
        doc.put("body", message);
        doc.put("read", false);
        doc.put("userId", userId);

        db.collection("Notifications")
                .document(id.toString())
                .set(doc)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        String jsonPayload = "{\"data\":{" +
                                "\"title\":\"Message from " + fullnameSender + "\"," +
                                "\"body\":\"" + message + "\"," +
                                "\"topic\":\"Message\"" +
                                "}," +
                                "\"to\":\"/topics/" + userId + "Message" + "\"}";
                        sendMessage(serverKey,jsonPayload);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ShowOneChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    String serverKey="AAAA8GYmoZ8:APA91bHsjyzOSa2JtO_cQWFO-X1p9nMuHRO8DTfD1zhcY4mnqZ-2EZmIn8tMf1ISmnM31WB68Mzn2soeUgEISXlSc9WjRvcRhyYbmBgi7whJuYXX-24wkODByasquofLaMZydpg78esK";
    public static void sendMessage(String serverKey, String jsonPayload) {
        FCMHttpClient httpClient = new FCMHttpClient();
        httpClient.sendMessageToTopic(serverKey, "PUPV", jsonPayload);
    }

    private void setupSnapshotListener(String senderId, String recipientId) {
        messages = new ArrayList<>();

        List<List<String>> participantsCombinations = new ArrayList<>();
        participantsCombinations.add(Arrays.asList(senderId, recipientId));
        participantsCombinations.add(Arrays.asList(recipientId, senderId));

        listenerRegistration = db.collection("Messages")
                .whereIn("participants", participantsCombinations)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.w(TAG, "Listen failed.", error);
                            return;
                        }

                        messages.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            String senderId = doc.getString("senderId");
                            String senderFullaname = doc.getString("senderFullName");
                            String recipientId = doc.getString("recipientId");
                            String recipientFullname = doc.getString("fullnameRecipientId");
                            Date dateOfSending = doc.getDate("dateOfSending");
                            String content = doc.getString("content");
                            boolean status = doc.getBoolean("status");

                            Message message = new Message(senderId, senderFullaname, recipientId, recipientFullname, dateOfSending, content, status);
                            messages.add(message);
                        }

                        Collections.sort(messages, new Comparator<Message>() {
                            @Override
                            public int compare(Message m1, Message m2) {
                                return m1.getDateOfSending().compareTo(m2.getDateOfSending());
                            }
                        });

                        ChatRecyclerAdapter adapterChats = new ChatRecyclerAdapter(messages);
                        recyclerView.setAdapter(adapterChats);

                        recyclerView.scrollToPosition(messages.size() - 1);
                    }
                });
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
