package com.example.eventplanner.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.eventplanner.R;
import com.example.eventplanner.activities.CreateServiceActivity;
import com.example.eventplanner.adapters.ServiceListPupvAdapter;
import com.example.eventplanner.databinding.FragmentServiceListPupzBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.example.eventplanner.adapters.ServiceListAdapter;

import com.example.eventplanner.model.Service;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ServiceListPupzFragment extends Fragment {

    View view;
    FragmentServiceListPupzBinding binding;
    ArrayList<Service> services;
    FirebaseFirestore db;
    FirebaseStorage storage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_service_list_pupv, container,false);
        binding = FragmentServiceListPupzBinding.inflate(getLayoutInflater());

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        services = new ArrayList<>();

        getServices();

        ServiceListAdapter serviceListAdapter = new ServiceListAdapter(requireContext(), R.layout.service_card, services);

        binding.serviceListPupz.setAdapter(serviceListAdapter);
        binding.serviceListPupz.setClickable(true);

        return binding.getRoot();
    }

    @NonNull
    private void getServices() {
        db.collection("Services")
                .whereEqualTo("pending", false)
                .whereEqualTo("deleted", false)
                .whereEqualTo("visible", true)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            final List<DocumentSnapshot> serviceDocs = task.getResult().getDocuments();
                            final int numServices = serviceDocs.size();
                            final int[] servicesProccessed = {0};

                            for (DocumentSnapshot doc : serviceDocs) {
                                Service service = new Service(/*
                                        Long.parseLong(doc.getId()),
                                        doc.getLong("categoryId"),
                                        doc.getLong("subcategoryId"),
                                        doc.getString("name"),
                                        doc.getString("description"),
                                        new ArrayList<>(), //images
                                        doc.getString("specific"),
                                        ((Number) doc.get("pricePerHour")).doubleValue(),
                                        ((Number) doc.get("fullPrice")).doubleValue(),
                                        ((Number) doc.get("duration")).doubleValue(),
                                        ((Number) doc.get("durationMin")).doubleValue(),
                                        ((Number) doc.get("durationMax")).doubleValue(),
                                        doc.getString("location"),
                                        ((Number) doc.get("discount")).doubleValue(),
                                        (ArrayList<String>) doc.get("providers"),
                                        (ArrayList<Long>) doc.get("eventIds"),
                                        doc.getString("reservationDue"),
                                        doc.getString("cancelationDue"),
                                        doc.getBoolean("automaticAffirmation"),
                                        doc.getBoolean("available"),
                                        doc.getBoolean("visible"),
                                        doc.getBoolean("pending"),
                                        doc.getBoolean("deleted")*/);

                                ArrayList<String> imageUrls = (ArrayList<String>) doc.get("imageUrls");
                                final int numImages = imageUrls.size();

                                for (String imageUrl : imageUrls) {
                                    StorageReference imageRef = storage.getReference().child(imageUrl);
                                    imageRef.getDownloadUrl()
                                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    service.getImages().add(uri);

                                                    if (service.getImages().size() == numImages) {
                                                        servicesProccessed[0]++;

                                                        if (servicesProccessed[0] == numServices) {
                                                            ServiceListAdapter productListAdapter = new ServiceListAdapter(requireContext(), R.layout.service_card, services);
                                                            binding.serviceListPupz.setAdapter(productListAdapter);
                                                            binding.serviceListPupz.setClickable(true);
                                                        }
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

                                services.add(service);
                            }
                        } else {
                            Toast.makeText(requireContext(), "Failed to fetch services: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
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