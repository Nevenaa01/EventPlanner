package com.example.eventplanner.fragments;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.eventplanner.R;
import com.example.eventplanner.adapters.ProductListPupvAdapter;
import com.example.eventplanner.databinding.FragmentProductListPupzBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.example.eventplanner.adapters.ProductListAdapter;
import com.example.eventplanner.model.Product;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class ProductListPupzFragment extends Fragment {
    View view;
    FragmentProductListPupzBinding binding;
    ArrayList<Product> products;
    FirebaseFirestore db;
    FirebaseStorage storage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_product_list_pupz, container,false);
        binding = FragmentProductListPupzBinding.inflate(getLayoutInflater());

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        getProducts();

        return binding.getRoot();
    }

    private void getProducts() {
        products = new ArrayList<>();

        db.collection("Products")
                .whereEqualTo("pending", false)
                .whereEqualTo("visible", true)
                .whereEqualTo("deleted", false)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            final List<DocumentSnapshot> productDocs = task.getResult().getDocuments();
                            final int numProducts = productDocs.size();
                            final int[] productsProcessed = {0};

                            for (DocumentSnapshot doc : productDocs) {
                                Product product = new Product(/*
                                        Long.parseLong(doc.getId()),
                                        doc.getLong("categoryId"),
                                        doc.getLong("subcategoryId"),
                                        doc.getString("name"),
                                        doc.getString("description"),
                                        ((Number) doc.get("price")).doubleValue(),
                                        ((Number) doc.get("discount")).doubleValue(),
                                        new ArrayList<>(), //images
                                        (ArrayList<Long>) doc.get("eventIds"),
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
                                                    product.getImages().add(uri);

                                                    if (product.getImages().size() == numImages) {
                                                        productsProcessed[0]++;

                                                        if (productsProcessed[0] == numProducts) {
                                                            ProductListAdapter productListAdapter = new ProductListAdapter(requireContext(), R.layout.product_card, products);
                                                            binding.productsListPupz.setAdapter(productListAdapter);
                                                            binding.productsListPupz.setClickable(true);
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

                                products.add(product);
                            }
                        } else {
                            Toast.makeText(requireContext(), "Failed to fetch products: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
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