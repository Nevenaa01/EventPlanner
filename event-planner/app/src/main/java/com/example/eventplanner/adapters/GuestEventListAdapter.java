package com.example.eventplanner.adapters;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventplanner.R;
import com.example.eventplanner.activities.ShowOneEventActivity;
import com.example.eventplanner.model.GuestEvent;
import com.example.eventplanner.model.SubcategoryPlanner;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GuestEventListAdapter extends  RecyclerView.Adapter<GuestEventListAdapter.GuestEventViewHolder>{
    private static ArrayList<GuestEvent> guestsEvent;

    public GuestEventListAdapter(ArrayList<GuestEvent> guestsEvent){
        this.guestsEvent = guestsEvent;
    }

    @NonNull
    @Override
    public GuestEventListAdapter.GuestEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.guest_row_table, parent, false);
        return new GuestEventListAdapter.GuestEventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GuestEventListAdapter.GuestEventViewHolder holder, int position) {
        GuestEvent guestEvent = guestsEvent.get(position);
        holder.bind(guestEvent,position);
    }

    @Override
    public int getItemCount() {
        return guestsEvent.size();
    }




    public static class GuestEventViewHolder extends RecyclerView.ViewHolder {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        BottomSheetDialog bottomSheetDialog;
        TextView serialNumber;
        TextView idGuest;
        TextView eventIdGuest;
        TextView fullname;

        TextView age;

        TextView invite;
        TextView accept;
        TextView specialRequests;

        ShowOneEventActivity activity;

        ImageView deleteGuestButton;

        ImageView editGuestButton;
        public GuestEventViewHolder(@NonNull View itemView) {
            super(itemView);
            activity = (ShowOneEventActivity) itemView.getContext();
            fullname = itemView.findViewById(R.id.fullNameGuest);
            age = itemView.findViewById(R.id.ageGuest);
            invite = itemView.findViewById(R.id.invited);
            accept = itemView.findViewById(R.id.acceptedInvite);
            specialRequests = itemView.findViewById(R.id.specialRequest);
            serialNumber = itemView.findViewById(R.id.serialNumberGuest);
            idGuest = itemView.findViewById(R.id.idGuest);
            eventIdGuest = itemView.findViewById(R.id.eventIdGuest);

            deleteGuestButton = itemView.findViewById(R.id.deleteGuest);
            editGuestButton = itemView.findViewById(R.id.editGuest);

            deleteGuestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LayoutInflater inflater = (LayoutInflater) itemView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View popUpView = inflater.inflate(R.layout.confirmation_popup, null);



                    int width = ViewGroup.LayoutParams.MATCH_PARENT;
                    int height = ViewGroup.LayoutParams.MATCH_PARENT;
                    boolean focusable = true;
                    PopupWindow popupWindow = new PopupWindow(popUpView, width, height, focusable);

                    popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);

                    Button cancelButton = popUpView.findViewById(R.id.cancel_button);
                    Button deleteButton = popUpView.findViewById(R.id.delete_button);

                    cancelButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            popupWindow.dismiss();
                        }
                    });

                    deleteButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int position = getAdapterPosition();


                            if (position != RecyclerView.NO_POSITION) {
                                // Pristupanje elementu u listi podataka na odgovarajućoj poziciji
                                GuestEvent guest = guestsEvent.get(position);

                                // Sada možete izvršiti željene akcije sa tim elementom
                                // Na primjer, možete prikazati dijalog za potvrdu brisanja ili izvršiti brisanje direktno
                                //showDeleteConfirmationDialog(subcategory);
                                deleteGuest(guest);
                                popupWindow.dismiss();
                            }

                        }
                    });


                }
            });


            editGuestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Context context = itemView.getContext();

                    LayoutInflater inflater = LayoutInflater.from(context);

                    bottomSheetDialog = new BottomSheetDialog( itemView.getContext(), R.style.FullScreenBottomSheetDialog);
                    View dialogView =inflater.inflate(R.layout.fragment_add_guest, null);



                    TextInputEditText editFullname = dialogView.findViewById(R.id.fullNameGuest);
                    RadioGroup ageGroup = dialogView.findViewById(R.id.ageGroup);

                    RadioGroup invited = dialogView.findViewById(R.id.inviteGroup);
                    RadioGroup accepted = dialogView.findViewById(R.id.acceptedInviteGroup);
                    TextInputEditText editSpecialRequest = dialogView.findViewById(R.id.specialRequestGuest);

                    editFullname.setText(fullname.getText());
                    CharSequence text = age.getText();
                    if (text.equals("0-3")) {
                        ageGroup.check(R.id.toddler);
                    } else if (text.equals("3-10")) {
                        ageGroup.check(R.id.child);
                    } else if (text.equals("10-18")) {
                        ageGroup.check(R.id.teenager);
                    } else if (text.equals("18-30")) {
                        ageGroup.check(R.id.youngAdult);
                    } else if (text.equals("30-50")) {
                        ageGroup.check(R.id.adult);
                    } else if (text.equals("50-70")) {
                        ageGroup.check(R.id.middleAgeAdult);
                    } else if (text.equals("70+")) {
                        ageGroup.check(R.id.elderly);
                    }

                    CharSequence inviteText = invite.getText();
                    if(inviteText.equals("Yes")){
                        invited.check(R.id.guestInvite);
                    }else if(inviteText.equals("No")){
                        invited.check(R.id.guestDontInvite);
                    }

                    CharSequence acceptText = accept.getText();
                    if(acceptText.equals("Yes")){
                        accepted.check(R.id.acceptInvite);
                    }else if(acceptText.equals("No")){
                        accepted.check(R.id.unacceptInvite);
                    }
                    editSpecialRequest.setText(specialRequests.getText());



                    MaterialButton buttonEditGuest = dialogView.findViewById(R.id.createGuestForEvent);

                    buttonEditGuest.setText("Edit guest");

                    buttonEditGuest.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            /*if (validateCreteSubcategoryPlanner(editPrice))
                                return;*/
                            String fullname = editFullname.getText().toString();
                            RadioButton ageButton =  dialogView.findViewById(ageGroup.getCheckedRadioButtonId());
                            String age = ageButton.getText().toString();
                            RadioButton inviteButton = dialogView.findViewById(invited.getCheckedRadioButtonId());
                            String invite = inviteButton.getText().toString();
                            RadioButton acceptInv = dialogView.findViewById(accepted.getCheckedRadioButtonId());
                            String acceptInvite = acceptInv.getText().toString();
                            String specialRequsts = editSpecialRequest.getText().toString();

                            updateEventGuest(new GuestEvent(Long.parseLong(String.valueOf(idGuest.getText())),
                                    Long.parseLong(String.valueOf(eventIdGuest.getText())) , fullname ,age, invite, acceptInvite, specialRequsts));
                            bottomSheetDialog.dismiss();
                        }

                    });



                    bottomSheetDialog.setContentView(dialogView);
                    bottomSheetDialog.show();
                }
            });
        }

        private void updateEventGuest(GuestEvent guestEvent) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("fullname", guestEvent.getFullname());
            updates.put("age", guestEvent.getAge());
            updates.put("invited", guestEvent.getInvite());
            updates.put("accepted", guestEvent.getAcceptInvite());
            updates.put("specialRequests", guestEvent.getSpecialRequests());

            db.collection("GuestsEvent").document(guestEvent.getId().toString()).update(updates)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "Document successfully updated!");
                            activity.getEventGuests(guestEvent.getId());

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error updating document", e);
                        }
                    });


        }

        private void deleteGuest(GuestEvent guest) {
            db.collection("GuestsEvent").document(guest.getId().toString()).delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "Document successfully deleted!");
                            activity.getEventGuests(guest.getEventId());

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error updating document", e);
                        }
                    });
        }

        public void bind(GuestEvent guestEvent, int position) {
            fullname.setText(guestEvent.getFullname());
            age.setText(guestEvent.getAge());
            invite.setText(guestEvent.getInvite());
            accept.setText(guestEvent.getAcceptInvite());
            specialRequests.setText(guestEvent.getSpecialRequests());
            serialNumber.setText(String.valueOf(position + 1));
            idGuest.setText(guestEvent.getId().toString());
            eventIdGuest.setText(guestEvent.getEventId().toString());

        }
    }
}
