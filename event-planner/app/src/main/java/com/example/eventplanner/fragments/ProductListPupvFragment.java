package com.example.eventplanner.fragments;

import android.app.ProgressDialog;
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
import com.example.eventplanner.activities.CreateProductActivity;
import com.example.eventplanner.databinding.FragmentProductListPupvBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.example.eventplanner.adapters.ProductListPupvAdapter;
import com.example.eventplanner.model.Product;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProductListPupvFragment extends Fragment{
    View view;
    FragmentProductListPupvBinding binding;
    ArrayList<Product> products;
    FirebaseFirestore db;
    FirebaseStorage storage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_product_list_pupv, container, false);
        binding = FragmentProductListPupvBinding.inflate(getLayoutInflater());

        products = new ArrayList<>();

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

        products.clear();
        getProducts();

        binding.addProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), CreateProductActivity.class);
                startActivity(intent);
            }
        });

    }
    private void getProducts() {
        db.collection("Products")
                .whereEqualTo("pending", false)
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
                                                            ProductListPupvAdapter productListAdapter = new ProductListPupvAdapter(requireContext(), products);
                                                            binding.productsListPupv.setAdapter(productListAdapter);
                                                            binding.productsListPupv.setClickable(true);
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