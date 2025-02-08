package com.example.eventplanner.activities;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.example.eventplanner.R;
import com.example.eventplanner.databinding.ActivityHomeTwoBinding;
import com.example.eventplanner.model.UserOD;
import com.example.eventplanner.model.UserPUPV;
import com.example.eventplanner.model.UserPUPZ;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class HomeTwoActivity extends AppCompatActivity {

    ActivityHomeTwoBinding binding;

    private DrawerLayout drawer;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ActionBar actionBar;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Set<Integer> topLevelDestinations = new HashSet<>();
    private AppBarConfiguration mAppBarConfiguration;
    private NavController navController;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth=FirebaseAuth.getInstance();

    TextView userName;
    ImageView userImage;
    private UserOD userOd;
    private UserPUPZ userPupz;
    private  UserPUPV userPupv;
    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHomeTwoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseUser user= mAuth.getCurrentUser();

        drawer = binding.drawerLayout;
        navigationView = binding.navView;
        toolbar = binding.activityHomeBase.toolbar;

        View headerView = navigationView.getHeaderView(0);

        // Pronađite elemente unutar zaglavlja
        userImage = headerView.findViewById(R.id.user_image);
        userName = headerView.findViewById(R.id.user_name);
        userImage.setVisibility(View.GONE);
        userName.setVisibility(View.GONE);



        if(user!= null){
            navigationView.getMenu().findItem(R.id.my_profile).setVisible(true);
            navigationView.getMenu().findItem(R.id.chats).setVisible(true);

        }
        if(user != null && (user.getDisplayName().equals("OD")|| user.getDisplayName().equals("ADMIN"))){
            if(user.getDisplayName().equals("OD")){
                navigationView.getMenu().findItem(R.id.favouritespsp).setVisible(true);
            }
            navigationView.getMenu().findItem(R.id.nav_events).setVisible(true);
            userImage.setVisibility(View.VISIBLE);
            userName.setVisibility(View.VISIBLE);
            loadImage(user.getUid(),userImage);
            getUserOd(user.getUid()).thenAccept(userOD -> {
                this.userOd = userOD;

                userName.setText(this.userOd.getFirstName() + " " + this.userOd.getLastName());

            });

        }else if(user != null && user.getDisplayName().equals("PUPZ")){
            userImage.setVisibility(View.VISIBLE);
            userName.setVisibility(View.VISIBLE);
            loadImage(user.getUid(),userImage);
            getUserPupz(user.getUid()).thenAccept(userPupz -> {
                this.userPupz = userPupz;

                userName.setText(this.userPupz.getFirstName() + " " + this.userPupz.getLastName());

            });

        }else if(user != null && user.getDisplayName().equals("PUPV")){
            userImage.setVisibility(View.VISIBLE);
            userName.setVisibility(View.VISIBLE);
            loadImage(user.getUid(),userImage);
            getUserPupv(user.getUid()).thenAccept(userPupv -> {
                this.userPupv = userPupv;

                userName.setText(this.userPupv.getFirstName() + " " + this.userPupv.getLastName());

            });

        }

        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_hamburger);
            actionBar.setHomeButtonEnabled(false);
        }

        actionBarDrawerToggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawer.addDrawerListener(actionBarDrawerToggle);

        actionBarDrawerToggle.syncState();

        navController = Navigation.findNavController(this, R.id.fragment_nav_content_main);
        navController.addOnDestinationChangedListener((navController, navDestination, bundle) -> {

            int id = navDestination.getId();
                /*switch (id) {
                    case R.id.nav_product_and_services: // Replace with your actual menu item ID
                        // Do something when this item is selected,
                        // such as navigating to a specific fragment
                        // For example:
                        // navController.navigate(R.id.nav_products);
                        // Replace with your destination fragment ID
                        Toast.makeText(HomeTwoActivity.this, "Products", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.nav_create_event:
                        Toast.makeText(HomeTwoActivity.this, "New product", Toast.LENGTH_SHORT).show();
                        break;
                }*/
                // Close the drawer if the destination is not a top level destination
                drawer.closeDrawers();




            drawer.closeDrawers();

        });

        mAppBarConfiguration = new AppBarConfiguration
                .Builder(R.id.nav_product_and_services)
                .setOpenableLayout(drawer)
                .build();

        NavigationUI.setupWithNavController(navigationView, navController);

        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);

    }


   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // menu.clear();
        // koristimo ako je nasa arhitekrura takva da imamo jednu aktivnost
        // i vise fragmentaa gde svaki od njih ima svoj menu unutar toolbar-a

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }*/

    /*@Override
    protected void onResume() {

        super.onResume();
        FirebaseUser user= mAuth.getCurrentUser();
        if(user==null){

        }else{
            if(user.getDisplayName().equals("OD")){
                navigationView.getMenu().findItem(R.id.nav_events).setVisible(true);
            }
        }


    }*/

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        navController = Navigation.findNavController(this, R.id.fragment_nav_content_main);

        return NavigationUI.onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        navController = Navigation.findNavController(this, R.id.fragment_nav_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }

    private CompletableFuture<UserPUPZ> getUserPupz(String uid) {
        CompletableFuture<UserPUPZ> future = new CompletableFuture<>();

        db.collection("User").document(uid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d("HomeTwoActivity", "DocumentSnapshot data: " + document.getData());
                                UserPUPZ userPupzz = new UserPUPZ();
                                userPupzz.setFirstName((String) document.get("firstName"));
                                userPupzz.setLastName((String) document.get("lastName"));
                                userPupzz.setEmail((String) document.get("email"));
                                userPupzz.setPassword((String) document.get("password"));
                                userPupzz.setPhone((String) document.get("phone"));
                                userPupzz.setAddress((String) document.get("address"));
                                userPupzz.setValid((Boolean) document.get("valid"));
                                userPupzz.setOwnerId((String) document.get("ownerId"));

                                userName.setText(userPupzz.getFirstName() + " " + userPupzz.getLastName());

                                future.complete(userPupzz);
                            } else {
                                Log.e("HomeTwoActivity", "No such document");
                                future.completeExceptionally(new Exception("No such document"));
                            }
                        } else {
                            Log.e("HomeTwoActivity", "Error getting document", task.getException());
                            future.completeExceptionally(task.getException());
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("HomeTwoActivity", "Error getting document", e);
                        future.completeExceptionally(e);
                    }
                });

        return future;
    }

    private CompletableFuture<UserPUPV> getUserPupv(String uid) {
        CompletableFuture<UserPUPV> future = new CompletableFuture<>();

        db.collection("User").document(uid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d("HomeTwoActivity", "DocumentSnapshot data: " + document.getData());
                                UserPUPV userPupvv = new UserPUPV();
                                userPupvv.setFirstName((String) document.get("FirstName"));
                                userPupvv.setLastName((String) document.get("LastName"));
                                userPupvv.setEmail((String) document.get("E-mail"));
                                userPupvv.setPassword((String) document.get("Password"));
                                userPupvv.setPhone((String) document.get("Phone"));
                                userPupvv.setAddress((String) document.get("Address"));
                                userPupvv.setValid((Boolean) document.get("IsValid"));
                                userPupvv.setCompanyName((String) document.get("CompanyName"));
                                userPupvv.setCompanyDescription((String) document.get("CompanyDescription"));
                                userPupvv.setCompanyAddress((String) document.get("CompanyAddress"));
                                userPupvv.setCompanyemail((String) document.get("CompanyEmail"));
                                userPupvv.setCompanyPhone((String) document.get("CompanyPhone"));
                                userPupvv.setWorkTime((String) document.get("WorkTime"));

                                userName.setText(userPupvv.getFirstName() + " " + userPupvv.getLastName());
                                future.complete(userPupvv);
                            } else {
                                Log.e("HomeTwoActivity", "No such document");
                                future.completeExceptionally(new Exception("No such document"));
                            }
                        } else {
                            Log.e("HomeTwoActivity", "Error getting document", task.getException());
                            future.completeExceptionally(task.getException());
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("HomeTwoActivity", "Error getting document", e);
                        future.completeExceptionally(e);
                    }
                });

        return future;
    }
    private CompletableFuture<UserOD> getUserOd(String uid) {
        CompletableFuture<UserOD> future = new CompletableFuture<>();

        db.collection("User").document(uid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d("HomeTwoActivity", "DocumentSnapshot data: " + document.getData());
                                UserOD userOd = new UserOD();
                                userOd.setFirstName((String) document.get("FirstName"));
                                userOd.setLastName((String) document.get("LastName"));
                                userOd.setEmail((String) document.get("E-mail"));
                                userOd.setPassword((String) document.get("Password"));
                                userOd.setPhone((String) document.get("Phone"));
                                userOd.setAddress((String) document.get("Address"));
                                userOd.setValid((Boolean) document.get("IsValid"));

                                userName.setText(userOd.getFirstName() + " " + userOd.getLastName());

                                future.complete(userOd);
                            } else {
                                Log.e("HomeTwoActivity", "No such document");
                                future.completeExceptionally(new Exception("No such document"));
                            }
                        } else {
                            Log.e("HomeTwoActivity", "Error getting document", task.getException());
                            future.completeExceptionally(task.getException());
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("HomeTwoActivity", "Error getting document", e);
                        future.completeExceptionally(e);
                    }
                });

        return future;
    }

    public void loadImage(String userId, ImageView imageView) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // Referenca na sliku u Firebase Storage
        StorageReference imageRef = storageRef.child("images/" + userId);

        // Korišćenje Glide za učitavanje slike i postavljanje u ImageView
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
                // Rukovanje greškom prilikom učitavanja slike
                Log.e("HomeTwoActivity", "Error loading image", exception);
                // Možete postaviti default sliku ili prikazati poruku o grešci
                imageView.setImageResource(R.drawable.defaultprofilepicture); // Pretpostavljamo da imate default_image u drawable resursima
            }
        });
    }

}