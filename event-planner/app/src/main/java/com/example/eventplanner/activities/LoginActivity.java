package com.example.eventplanner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventplanner.R;
import com.example.eventplanner.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class LoginActivity extends AppCompatActivity {
    FirebaseAuth mAuth= FirebaseAuth.getInstance();
    ActivityLoginBinding binding;
    private FirebaseFirestore db;
    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        if(currentUser != null){
            finish();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



        binding= ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.registerButton.setOnClickListener(v->{
            Intent intent = new Intent(LoginActivity.this, OD_RegisterActivity.class);
            startActivity(intent);
        });

        binding.loginButton.setOnClickListener(v->{
            if(!validateInput()){
                Toast.makeText(this, "Please fill in required fields!", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(binding.emailTextField.getText().toString(), binding.passwordTextField.getText().toString())
                    .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                getUser(user);
                            } else {

                                Toast.makeText(LoginActivity.this, "Wrong email or password",
                                        Toast.LENGTH_SHORT).show();

                            }
                        }
                    });

        });

    }

    private void logIn(FirebaseUser user) {
        if (user.isEmailVerified()) {
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            String s=mAuth.getCurrentUser().getDisplayName();
            Toast.makeText(LoginActivity.this,s,Toast.LENGTH_SHORT).show();

            if(mAuth.getCurrentUser()!=null && mAuth.getCurrentUser().getDisplayName().equals("PUPV")){
                FirebaseMessaging.getInstance().subscribeToTopic("PUPV");
                FirebaseMessaging.getInstance().subscribeToTopic(mAuth.getCurrentUser().getUid() + "Topic");
            }

            if(mAuth.getCurrentUser()!=null && mAuth.getCurrentUser().getDisplayName().equals("ADMIN")){
                FirebaseMessaging.getInstance().subscribeToTopic("AdminTopic");
            }

            if(mAuth.getCurrentUser()!=null && mAuth.getCurrentUser().getDisplayName().equals("OD")) {
                FirebaseMessaging.getInstance().subscribeToTopic(mAuth.getCurrentUser().getUid() + "Topic");
            }
            if(mAuth.getCurrentUser()!=null && mAuth.getCurrentUser().getDisplayName().equals("PUPZ")) {
                FirebaseMessaging.getInstance().subscribeToTopic(mAuth.getCurrentUser().getUid() + "PUPZTopic");
                FirebaseMessaging.getInstance().subscribeToTopic("PUPZ");
            }

            if(mAuth.getCurrentUser()!= null){
                FirebaseMessaging.getInstance().subscribeToTopic(mAuth.getCurrentUser().getUid() + "Message");
            }


        } else {
            mAuth.signOut();
            Toast.makeText(LoginActivity.this, "Please verify your email before logging in", Toast.LENGTH_SHORT).show();
        }
    }

    private void getUser(FirebaseUser user){
        db.collection("User")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        //pupv, od i admin
                        if(documentSnapshot.getString("UserType") != null){
                            if(documentSnapshot.getBoolean("IsValid")){
                                logIn(user);
                            }
                            else{
                                Toast.makeText(LoginActivity.this, "You cannot log in on this account", Toast.LENGTH_SHORT).show();
                            }
                        }
                        //pupz
                        else{
                            if(documentSnapshot.getBoolean("valid")){
                                logIn(user);
                            }
                            else{
                                Toast.makeText(LoginActivity.this, "You cannot log in on this account", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private boolean validateInput(){
        TextInputEditText emailTextField= binding.emailTextField;
        TextInputEditText passwordTextField= binding.passwordTextField;
        if(TextUtils.isEmpty(passwordTextField.getText())) {
            return false;
        }
        return !TextUtils.isEmpty(emailTextField.getText()) && android.util.Patterns.EMAIL_ADDRESS.matcher(emailTextField.getText()).matches();
    }
}