package com.example.eventplanner.adapters;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventplanner.R;
import com.example.eventplanner.model.UserPUPZ;
import com.example.eventplanner.services.FCMHttpClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class EmployeeRecyclerViewAdapter extends RecyclerView.Adapter<EmployeeRecyclerViewAdapter.EmployeeViewHolder>{

    private ArrayList<UserPUPZ> pupzs;
    private String senderId;
    private String senderFullname;

    private ArrayList<String> pupIds;

    public EmployeeRecyclerViewAdapter(ArrayList<UserPUPZ> pupzs, ArrayList<String> pupIds,  String senderId, String senderFullname) {

        this.pupzs = pupzs;
        this.senderId = senderId;
        this.senderFullname = senderFullname;
        this.pupIds = pupIds;
    }

    @NonNull
    @Override
    public EmployeeRecyclerViewAdapter.EmployeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.employee_card, parent, false);
        return new EmployeeRecyclerViewAdapter.EmployeeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmployeeRecyclerViewAdapter.EmployeeViewHolder holder, int position) {
        UserPUPZ pupz = pupzs.get(position);
        String pupId = pupIds.get(position);
        holder.bind(pupz, senderId, senderFullname, pupId);
    }

    @Override
    public int getItemCount() {
        return pupzs.size();
    }

    public static class EmployeeViewHolder extends RecyclerView.ViewHolder {

        TextView pupzFullname;
        Button sendMessage;
        private String senderId;
        private String senderFullname;
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        public EmployeeViewHolder(@NonNull View itemView) {
            super(itemView);
            pupzFullname = itemView.findViewById(R.id.employee_fullname);
            sendMessage = itemView.findViewById(R.id.send_message_button);
        }

        @SuppressLint("SetTextI18n")
        public void bind(UserPUPZ pupz, String senderId, String senderFullname, String pupId) {
            this.senderId = senderId;
            this.senderFullname = senderFullname;
            pupzFullname.setText(pupz.getFirstName() + " " + pupz.getLastName());

            sendMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());

                    LayoutInflater inflater = (LayoutInflater) itemView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View dialogView = inflater.inflate(R.layout.dialog_send_message, null);

                    EditText messageInput = dialogView.findViewById(R.id.messageInput);
                    Button buttonSend = dialogView.findViewById(R.id.buttonSend);
                    Button buttonClose = dialogView.findViewById(R.id.buttonClose);

                    builder.setView(dialogView);

                    AlertDialog dialog = builder.create();
                    dialog.show();

                    buttonSend.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String message = messageInput.getText().toString().trim();
                            if (!message.isEmpty()) {
                                sendMessageToPupz(message, pupId, pupz.getFirstName() + " " + pupz.getLastName(), senderId, senderFullname);
                                dialog.dismiss();
                            } else {
                                Toast.makeText(itemView.getContext(), "Please enter a message", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    buttonClose.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                }
            });
        }


        private void sendMessageToPupz(String message,String recipientId, String recipientFullname, String senderId, String senderFullname) {
            Map<String, Object> elememt = new HashMap<>();
            elememt.put("senderId", senderId);
            elememt.put("senderFullName", senderFullname);
            elememt.put("recipientId", recipientId);
            elememt.put("fullnameRecipientId", recipientFullname);
            elememt.put("dateOfSending", new Date());
            elememt.put("content", message);
            elememt.put("status", false);
            elememt.put("participants", Arrays.asList(senderId, recipientId));

            // Dodajte novi dokument sa generisanim ID-om
            db.collection("Messages").document()
                    .set(elememt)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(itemView.getContext(), "Send message successfully", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(itemView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        String serverKey="AAAA8GYmoZ8:APA91bHsjyzOSa2JtO_cQWFO-X1p9nMuHRO8DTfD1zhcY4mnqZ-2EZmIn8tMf1ISmnM31WB68Mzn2soeUgEISXlSc9WjRvcRhyYbmBgi7whJuYXX-24wkODByasquofLaMZydpg78esK";
        public static void sendMessage(String serverKey, String jsonPayload) {
            FCMHttpClient httpClient = new FCMHttpClient();
            httpClient.sendMessageToTopic(serverKey, "PUPV", jsonPayload);
        }


    }
}
