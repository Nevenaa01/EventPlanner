package com.example.eventplanner.fragments;

import static android.content.ContentValues.TAG;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.eventplanner.R;
import com.example.eventplanner.databinding.FragmentShowEventBinding;

import java.io.Console;
import java.util.ArrayList;
import java.util.Date;

import com.example.eventplanner.adapters.EventRecyclerViewAdapter;

import com.example.eventplanner.model.Event;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


public class ShowEventFragment extends Fragment {
    private FragmentShowEventBinding binding;
    FirebaseFirestore db;
    ArrayList<Event> events;

    RecyclerView recyclerView;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public static ShowEventFragment newInstance() {
        return new ShowEventFragment();
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentShowEventBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        FirebaseUser user = mAuth.getCurrentUser();

        recyclerView = binding.eventsRecyclerView;
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        binding.floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.nav_create_event);

            }
        });

        //ArrayList<Event> events = createEvents();
        db = FirebaseFirestore.getInstance();
        getEvents(user.getUid());



        return root;
    }

    public void getEvents(String userOdId) {
        //ArrayList<Event> events = new ArrayList<>();

        events = new ArrayList<>();

        db.collection("Events")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for(DocumentSnapshot doc: task.getResult()){
                            Event event = new Event(doc.getLong("id"),
                                    doc.getString("userOdId"),
                                    doc.getString("typeEvent"),
                                    doc.getString("name"),
                                    doc.getString("description"),
                                    Integer.parseInt(String.valueOf(doc.getLong("maxPeople"))),
                                    doc.getString("locationPlace"),
                                    Integer.parseInt(String.valueOf(doc.getLong("maxDistance"))),
                                    doc.getDate("dateEvent"),
                                    doc.getBoolean("available"));

                            System.out.println(event.getId());


                            events.add(event);
                        }
                        ArrayList<Event> filteredEvents = new ArrayList<>();

                        for (Event event : events) {
                            if (event.getUserODId().equals(userOdId)) {
                                filteredEvents.add(event);
                            }
                        }
                        EventRecyclerViewAdapter adapterEvents = new EventRecyclerViewAdapter(filteredEvents);
                        recyclerView.setAdapter(adapterEvents);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}