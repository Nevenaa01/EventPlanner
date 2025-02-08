package com.example.eventplanner.activities;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventplanner.R;
import com.example.eventplanner.databinding.ActivityHomeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

public class HomeActivity extends AppCompatActivity {

    private Button buttonCreateEventOD;

    private Button butonSearchAndFilter;

    private Button buttonHome;
    ActivityHomeBinding binding;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Notifications permission granted", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    Toast.makeText(this, "FCM can't post notifications without POST_NOTIFICATIONS permission",
                            Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelId = "123";
            String channelName = "Ime";
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_DEFAULT));
        }
        askNotificationPermission();


        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        /*binding.productsManagmentPUPV.setOnClickListener(v ->{
            Intent intent = new Intent(HomeActivity.this, ProductsManegementActivity.class);
            intent.putExtra("used_fragment", "product_list_pupv");
            startActivity(intent);
        });

        binding.productsManagmentPUPZ.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProductsManegementActivity.class);
            intent.putExtra("used_fragment", "product_list_pupz");
            startActivity(intent);
        });

        binding.serviceManagmentPUPZ.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ServicesManagementActivity.class);
            intent.putExtra("used_fragment", "service_list_pupz");
            startActivity(intent);
        });

        binding.serviceManagmentPUPV.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ServicesManagementActivity.class);
            intent.putExtra("used_fragment", "service_list_pupv");
            startActivity(intent);
        });

        binding.packageManagmentPUPZ.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, PackagesManagementActivity.class);
            intent.putExtra("used_fragment", "package_list_pupz");
            startActivity(intent);
        });

        binding.packageManagmentPUPV.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, PackagesManagementActivity.class);
            intent.putExtra("used_fragment", "package_list_pupv");
            startActivity(intent);
        });*/

        binding.addCommentBtn.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AddCommentActivity.class);
            startActivity(intent);
        });

        binding.pricelist.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, PricelistActivity.class);
            startActivity(intent);
        });

        binding.notifications.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, NotificationsViewActivity.class);
            startActivity(intent);
        });

        binding.userReports.setOnClickListener(v ->{
            Intent intent = new Intent(HomeActivity.this, UserReportsViewActivity.class);
            startActivity(intent);
        });

        binding.registerButton.setOnClickListener(v->{
            Intent intent = new Intent(HomeActivity.this, OD_RegisterActivity.class);
            startActivity(intent);
        });

        binding.loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        binding.categoriesButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CategoryActivity.class);
            startActivity(intent);
        });

        binding.typesOfEventsButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, EventTypesActivity.class);
            startActivity(intent);

        });

        binding.homeact.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, HomeTwoActivity.class);
            startActivity(intent);
        });
        binding.ownerDashboardBtn.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, OwnerDashboard.class);
            startActivity(intent);
        });
        binding.approveRegistration.setOnClickListener(v->{
            Intent intent = new Intent(HomeActivity.this, ApproveRegistrationActivity.class);
            startActivity(intent);
        });

        binding.viewCompanyComments.setOnClickListener(v->{
            Intent intent = new Intent(HomeActivity.this, CommentPreviewActivity.class);
            startActivity(intent);
        });

        binding.signOut.setOnClickListener(v->{
            FirebaseMessaging.getInstance().unsubscribeFromTopic("PUPV");
            FirebaseMessaging.getInstance().unsubscribeFromTopic("AdminTopic");
            FirebaseMessaging.getInstance().unsubscribeFromTopic(mAuth.getCurrentUser().getUid() + "Topic");
            FirebaseMessaging.getInstance().unsubscribeFromTopic(mAuth.getCurrentUser().getUid() + "PUPZTopic");
            FirebaseMessaging.getInstance().unsubscribeFromTopic(mAuth.getCurrentUser().getUid() + "Message");
            FirebaseMessaging.getInstance().unsubscribeFromTopic("PUPZ");
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(this, "SingedOut", Toast.LENGTH_SHORT).show();
            this.onResume();
        });

        binding.reservationViewId.setOnClickListener(v->{
            Intent intent = new Intent(HomeActivity.this, ReservationView.class);
            startActivity(intent);
        });

    }


    @Override
    protected void onResume() {

        super.onResume();
        FirebaseUser user = mAuth.getCurrentUser();

        binding.signOut.setVisibility(View.GONE);
        binding.pricelist.setVisibility(View.GONE);
        binding.userReports.setVisibility(View.GONE);
        binding.registerButton.setVisibility(View.GONE);
        binding.loginButton.setVisibility(View.GONE);
        binding.categoriesButton.setVisibility(View.GONE);
        binding.typesOfEventsButton.setVisibility(View.GONE);
        binding.approveRegistration.setVisibility(View.GONE);
        binding.notifications.setVisibility(View.GONE);
        binding.addCommentBtn.setVisibility(View.GONE);
        binding.viewCompanyComments.setVisibility(View.GONE);

        if(user==null){
            binding.registerButton.setVisibility(View.VISIBLE);
            binding.loginButton.setVisibility(View.VISIBLE);
            return;
        }else{
            binding.signOut.setVisibility(View.VISIBLE);
            binding.notifications.setVisibility(View.VISIBLE);
        }

        if(user.getDisplayName().equals("OD")){
            binding.addCommentBtn.setVisibility(View.VISIBLE);
            binding.homeact.setVisibility(View.VISIBLE);
            binding.pricelist.setVisibility(View.VISIBLE);
            binding.reservationViewId.setVisibility(View.VISIBLE);
        }else if(user.getDisplayName().equals("ADMIN")){
            binding.categoriesButton.setVisibility(View.VISIBLE);
            binding.typesOfEventsButton.setVisibility(View.VISIBLE);
            binding.userReports.setVisibility(View.VISIBLE);
            binding.approveRegistration.setVisibility(View.VISIBLE);
        }else if(user.getDisplayName().equals("PUPV")){
            binding.pricelist.setVisibility(View.VISIBLE);
            binding.reservationViewId.setVisibility(View.VISIBLE);
            binding.viewCompanyComments.setVisibility(View.VISIBLE);
        }else if(user.getDisplayName().equals("PUPZ")){

        }


    }

    private void askNotificationPermission () {
        // This is only necessary for API Level > 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                // FCM SDK (and your app) can post notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
}