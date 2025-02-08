package com.example.eventplanner.fragments;

import static android.content.ContentValues.TAG;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.eventplanner.R;
import com.example.eventplanner.adapters.PackageListAdapter;
import com.example.eventplanner.adapters.ProductListAdapter;
import com.example.eventplanner.adapters.ServiceListAdapter;
import com.example.eventplanner.databinding.FragmentShowFavouritesPspBinding;
import com.example.eventplanner.model.Package;
import com.example.eventplanner.model.Product;
import com.example.eventplanner.model.Service;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ShowFavouritesPspFragment extends Fragment {

    FragmentShowFavouritesPspBinding binding;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    FirebaseStorage storage = FirebaseStorage.getInstance();

    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    FirebaseUser user = mAuth.getCurrentUser();

    ArrayList<String> productIds = new ArrayList<>();
    ArrayList<String> serviceIds = new ArrayList<>();
    ArrayList<String> packageIds = new ArrayList<>();
    ArrayList<Product> products;
    ArrayList<Service> services;
    ArrayList<Package> packages;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentShowFavouritesPspBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        getFavouritesPsp(user.getUid());

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                switch (position) {
                    case 0:
                        // Prikazati listu proizvoda, sakriti ostale
                        binding.productListt.setVisibility(View.VISIBLE);
                        binding.serviceListt.setVisibility(View.GONE);
                        binding.packageListt.setVisibility(View.GONE);
                        break;
                    case 1:
                        // Prikazati listu usluga, sakriti ostale
                        binding.productListt.setVisibility(View.GONE);
                        binding.serviceListt.setVisibility(View.VISIBLE);
                        binding.packageListt.setVisibility(View.GONE);
                        break;
                    case 2:
                        // Prikazati listu paketa, sakriti ostale
                        binding.productListt.setVisibility(View.GONE);
                        binding.serviceListt.setVisibility(View.GONE);
                        binding.packageListt.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Ovdje ne trebate ništa raditi, jer se ne treba reagirati na odabir drugih tabova
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Ovdje ne trebate ništa raditi, jer se ne treba reagirati na ponovni odabir taba
            }
        });


        return root;
    }





    private void getFavouritesPsp(String uid) {
        DocumentReference userFavoritesRef = db.collection("FavouritesPsp").document(user.getUid());

        userFavoritesRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        productIds = (ArrayList<String>) document.get("productIds");
                        serviceIds = (ArrayList<String>) document.get("serviceIds");
                        packageIds = (ArrayList<String>) document.get("packageIds");


                        getProducts(productIds);
                        getServices(serviceIds);
                    }
                } else {
                    Log.e(TAG, "Error getting document", task.getException());
                }
            }
        });
    }

    private void getServices(ArrayList<String> serviceIds) {
        services = new ArrayList<>();
        if(serviceIds.isEmpty()){
            return;
        }
        db.collection("Services")
                .whereIn(FieldPath.documentId(), serviceIds)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            final List<DocumentSnapshot> serviceDocs = task.getResult().getDocuments();
                            final int numServices = serviceDocs.size();
                            final int[] servicesProccessed = {0};

                            for (DocumentSnapshot doc : serviceDocs) {
                                Service service = new Service(
                                        Long.parseLong(doc.getId()),
                                        doc.getString("pupvId"),
                                        Long.parseLong(doc.getString("categoryId")),
                                        Long.parseLong(doc.getString("subcategoryId")),
                                        doc.getString("name"),
                                        doc.getString("description"),
                                        new ArrayList<>(), //images
                                        doc.getString("specific"),
                                        ((Number) doc.get("pricePerHour")).doubleValue(),
                                        ((Number) doc.get("fullPrice")).doubleValue(),
                                        doc.get("duration") != null ? ((Number) doc.get("duration")).doubleValue() : null,
                                        doc.get("durationMin") != null ? ((Number) doc.get("durationMin")).doubleValue() : null,
                                        doc.get("durationMax") != null ? ((Number) doc.get("durationMax")).doubleValue() : null,
                                        doc.getString("location"),
                                        ((Number) doc.get("discount")).doubleValue(),
                                        (ArrayList<String>) doc.get("pupIds"),
                                        convertStringArrayToLong((ArrayList<String>) doc.get("eventTypeIds")),
                                        doc.getString("reservationDue"),
                                        doc.getString("cancelationDue"),
                                        doc.getBoolean("automaticAffirmation"),
                                        doc.getBoolean("available"),
                                        doc.getBoolean("visible"),
                                        doc.getBoolean("pending"),
                                        doc.getBoolean("deleted"));

                                ArrayList<String> imageUrls = (ArrayList<String>) doc.get("imageIds");
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
                                                            ServiceListAdapter serviceListAdapter = new ServiceListAdapter(requireContext(), R.layout.service_card, services);
                                                            binding.serviceListt.setAdapter(serviceListAdapter);
                                                            binding.serviceListt.setClickable(true);
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

                            getPackages(packageIds);
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

    private void getPackages(ArrayList<String> packageIds) {
        packages = new ArrayList<>();
        if(packageIds.isEmpty()){
            return;
        }
        db.collection("Packages")
                .whereIn(FieldPath.documentId(), packageIds)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if(task.isSuccessful()){
                            final List<DocumentSnapshot> packageDocs = task.getResult().getDocuments();
                            final int numProducts = packageDocs.size();
                            final int[] productsProcessed = {0};
                            for(DocumentSnapshot doc: task.getResult()) {
                                Package packagee = new Package(
                                        Long.parseLong(doc.getId()),
                                        doc.getString("pupvId"),
                                        doc.getString("name"),
                                        doc.getString("description"),
                                        ((Number) doc.get("discount")).doubleValue(),
                                        doc.getBoolean("available"),
                                        doc.getBoolean("visible"),
                                        Long.parseLong(doc.getString("categoryId")),
                                        convertStringArrayToLong((ArrayList<String>) doc.get("subcategoryIds")),
                                        convertStringArrayToLong((ArrayList<String>) doc.get("productIds")),
                                        convertStringArrayToLong((ArrayList<String>) doc.get("serviceIds")),
                                        convertStringArrayToLong((ArrayList<String>) doc.get("eventTypeIds")),
                                        ((Number) doc.get("price")).doubleValue(),
                                        new ArrayList<>(), //images
                                        doc.getString("reservationDue"),
                                        doc.getString("cancelationDue"),
                                        doc.getBoolean("automaticAffirmation"),
                                        doc.getBoolean("deleted"));

                                ArrayList<String> imageUrls = (ArrayList<String>) doc.get("imageIds");
                                final int numImages = imageUrls.size();

                                for (String imageUrl : imageUrls) {
                                    StorageReference imageRef = storage.getReference().child(imageUrl);
                                    imageRef.getDownloadUrl()
                                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    packagee.getImages().add(uri);

                                                    if (packagee.getImages().size() == numImages) {
                                                        productsProcessed[0]++;

                                                        if (productsProcessed[0] == numProducts) {
                                                            PackageListAdapter packageListAdapter = new PackageListAdapter(requireContext(), packages);
                                                            binding.packageListt.setAdapter(packageListAdapter);
                                                            binding.packageListt.setClickable(true);
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

                                packages.add(packagee);
                            }

                        }else{
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

    private void getProducts(ArrayList<String> productIds) {
        products = new ArrayList<>();
        if(productIds.isEmpty()){
            return;
        }
        db.collection("Products")
                .whereIn(FieldPath.documentId(), productIds)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){

                            final List<DocumentSnapshot> productDocs = task.getResult().getDocuments();
                            final int numProducts = productDocs.size();
                            final int[] productsProcessed = {0};

                            for(DocumentSnapshot doc: task.getResult()){
                                Product product = new Product(
                                        Long.parseLong(doc.getId()),
                                        doc.getString("pupvId"),
                                        Long.parseLong(doc.getString("categoryId")),
                                        Long.parseLong(doc.getString("subcategoryId")),
                                        doc.getString("name"),
                                        doc.getString("description"),
                                        doc.getDouble("price"),
                                        doc.getDouble("discount"),
                                        new ArrayList<>(),
                                        convertStringArrayToLong((ArrayList<String>) doc.get("eventTypeIds")),
                                        doc.getBoolean("available"),
                                        doc.getBoolean("visible"),
                                        doc.getBoolean("pending"),
                                        doc.getBoolean("deleted")
                                );

                                ArrayList<String> imageUrls = (ArrayList<String>) doc.get("imageIds");
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
                                                            binding.productListt.setAdapter(productListAdapter);
                                                            binding.productListt.setClickable(true);
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

    private ArrayList<Long> convertStringArrayToLong(ArrayList<String> list){
        ArrayList<Long> ids = new ArrayList<>();

        for(String item: list){
            ids.add(Long.parseLong(item));
        }

        return ids;
    }
}