package com.example.eventplanner.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.eventplanner.R;
import com.example.eventplanner.activities.ApproveRegistrationActivity;
import com.example.eventplanner.activities.HomeActivity;
import com.example.eventplanner.activities.OwnerDashboard;
import com.example.eventplanner.model.UserPUPV;
import com.example.eventplanner.utils.MailSender;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import javax.mail.MessagingException;


public class ReasonFragment extends BottomSheetDialogFragment {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    UserPUPV user;
    View view;
    public ReasonFragment(UserPUPV user) {
        this.user=user;
    }

    private FragmentCloseListener listener;

    public interface FragmentCloseListener {
        void onFragmentClosed();
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentCloseListener) {
            listener = (FragmentCloseListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement FragmentCloseListener");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.removeUser).setOnClickListener(v->{
            removeUser();
        });
    }
    private static class SendMailTask extends AsyncTask<Void, Void, Void> {
        private String senderEmail;
        private String senderPassword;
        private String recipientEmail;
        private String subject;
        private String body;

        public SendMailTask(String senderEmail, String senderPassword, String recipientEmail, String subject, String body) {
            this.senderEmail = senderEmail;
            this.senderPassword = senderPassword;
            this.recipientEmail = recipientEmail;
            this.subject = subject;
            this.body = body;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                MailSender mailSender = new MailSender(senderEmail, senderPassword);
                mailSender.sendMail(recipientEmail, subject, body);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_reason, container, false);
        return view;
    }

    private void removeUser(){
        TextInputEditText text=view.findViewById(R.id.reason);
        if(text.getText()==null){
            Toast.makeText(view.getContext(), "Reason is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, Object> updates = new HashMap<>();
        updates.put("Reason", text.getText().toString());
        db.collection("User").document(user.getId()).update(updates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firestore", "DocumentSnapshot successfully updated!");
                        sendEmail();
                        listener.onFragmentClosed();
                        getActivity().getSupportFragmentManager().beginTransaction().remove(ReasonFragment.this).commit();

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
    private void sendEmail() {
        TextInputEditText text=view.findViewById(R.id.reason);
        if(text.getText()==null){
            Toast.makeText(view.getContext(), "Reason is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        String recipientEmail = user.getEmail();
        String subject = "Declined account";
        String body ="Your account registration has been denied! \n Reason: " + text.getText().toString();
        String senderEmail = "markoradetic67@gmail.com";
        String senderPassword = "zjhr jitq xent roob";

        new SendMailTask(senderEmail, senderPassword, recipientEmail, subject, body).execute();
    }
}