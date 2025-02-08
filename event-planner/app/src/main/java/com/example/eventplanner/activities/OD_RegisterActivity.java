package com.example.eventplanner.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventplanner.R;
import com.example.eventplanner.model.UserPUPV;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import com.google.firebase.auth.FirebaseUser;
public class OD_RegisterActivity extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth= FirebaseAuth.getInstance();
    Button buttonSelectImage;
    ImageView imageViewProfile;
    Uri selectedImage;


    static final int REQUEST_IMAGE_PICK = 1;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            finish();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_od_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



        RadioGroup radioGroup = findViewById(R.id.radioGroup);
        LinearLayout PUPVLayout=findViewById(R.id.PUPVLayout);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.ODRadio) {
                    PUPVLayout.setVisibility(View.GONE);
                } else if (checkedId == R.id.PUPVRadio) {
                    PUPVLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        buttonSelectImage = findViewById(R.id.addImageButton);
        ImageView imageViewProfile = findViewById(R.id.imageViewProfile);
        buttonSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 3);
            }
        });
        findViewById(R.id.registerUser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(radioGroup.getCheckedRadioButtonId()==R.id.PUPVRadio) {
                    Map<String, Object> item=createUserPUPV();
                    if(item==null) return;

                    Intent intent = new Intent(OD_RegisterActivity.this, PUPV_RegisterCategoryActivity.class);
                    intent.putExtra("object", (Serializable) item);
                    if(selectedImage!=null)intent.putExtra("pathImage",selectedImage.toString());
                    else intent.putExtra("pathImage","");
                    startActivity(intent);
                }else{
                    createUserOd();
                }
            }
        });


    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            selectedImage = data.getData();
            ImageView imageView=findViewById(R.id.imageViewProfile);
            imageView.setImageURI(selectedImage);

        }
    }
    private void uploadImage(String userId){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        Uri fileUri = selectedImage;
        StorageReference imageRef = storageRef.child("images/" + userId);

        UploadTask uploadTask = imageRef.putFile(fileUri);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Image uploaded successfully
                Log.d("TAG", "Image uploaded successfully");

                // Get the download URL for the uploaded image
                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri downloadUri) {
                        // Handle the download URL (e.g., save it to a database)
                        String imageUrl = downloadUri.toString();
                        Log.d("TAG", "Download URL: " + imageUrl);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle unsuccessful uploads
                Log.e("TAG", "Error uploading image", e);
            }
        });
    }
    private void  createUserOd(){
        TextInputEditText firstNameTextField=findViewById(R.id.firstNameTextbox);
        TextInputEditText lastNameTextField=findViewById(R.id.lastNameTextbox);
        TextInputEditText addressTextField=findViewById(R.id.addressTextbox);
        TextInputEditText emailTextField=findViewById(R.id.emailTextbox);
        TextInputEditText passwordTextField=findViewById(R.id.passwordTextbox);
        TextInputEditText confirmPasswordTextField=findViewById(R.id.confirmPasswordTextbox);
        TextInputEditText phoneNumberTextField=findViewById(R.id.phoneNumberTextbox);

        if (validateOdInput(firstNameTextField, lastNameTextField, addressTextField, emailTextField, phoneNumberTextField, passwordTextField, confirmPasswordTextField))
            return;

        Map<String, Object> item = new HashMap<>();
        item.put("FirstName", firstNameTextField.getText().toString());
        item.put("LastName", lastNameTextField.getText().toString());
        item.put("E-mail", emailTextField.getText().toString());
        item.put("Address", addressTextField.getText().toString());
        item.put("Phone", phoneNumberTextField.getText().toString());
        item.put("Password", passwordTextField.getText().toString());
        item.put("IsValid", false);
        item.put("UserType", "OD");

        mAuth.createUserWithEmailAndPassword(item.get("E-mail").toString(),passwordTextField.getText().toString() )
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            sendVerificationEmail();
                            updateUsersRole(item);
                            mAuth.signOut();
                        } else {
                            Exception exception = task.getException();
                            if (exception instanceof FirebaseAuthException) {
                                FirebaseAuthException firebaseAuthException = (FirebaseAuthException) exception;
                                String errorCode = firebaseAuthException.getErrorCode();
                                String errorMessage = firebaseAuthException.getMessage();
                                Log.e("JovoFirebaseAuth", "Authentication failed with error code: " + errorCode + ", message: " + errorMessage);
                            }
                            Toast.makeText(OD_RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private boolean validateOdInput(TextInputEditText firstNameTextField, TextInputEditText lastNameTextField, TextInputEditText addressTextField, TextInputEditText emailTextField, TextInputEditText phoneNumberTextField, TextInputEditText passwordTextField, TextInputEditText confirmPasswordTextField) {
        boolean error=false;
        if(TextUtils.isEmpty(firstNameTextField.getText())){
            firstNameTextField.setError("Fill textfield!");
            error=true;
        }
        if(TextUtils.isEmpty(lastNameTextField.getText())){
            lastNameTextField.setError("Fill textfield!");
            error=true;
        }
        if(TextUtils.isEmpty(addressTextField.getText())){
            addressTextField.setError("Fill textfield!");
            error=true;
        }
        if(TextUtils.isEmpty(emailTextField.getText())){
            emailTextField.setError("Fill textfield!");
            error=true;
        }
        else if(!android.util.Patterns.EMAIL_ADDRESS.matcher(emailTextField.getText()).matches()){
            emailTextField.setError("E-Mail is not valid!");
            error=true;
        }
        if(TextUtils.isEmpty(phoneNumberTextField.getText())){
            phoneNumberTextField.setError("Fill textfield!");
            error=true;
        }
        if(TextUtils.isEmpty(passwordTextField.getText())){
            passwordTextField.setError("Fill textfield!");
            error=true;
        }
        if(TextUtils.isEmpty(confirmPasswordTextField.getText())){
            confirmPasswordTextField.setError("Fill textfield!");
            error=true;
        }

        if(error) return true;


        if(!passwordTextField.getText().toString().equals(confirmPasswordTextField.getText().toString())){
            passwordTextField.setError("Passwords dont match!");
            return true;
        }
        else if(passwordTextField.getText().toString().length()<6){
            passwordTextField.setError("Passwords should be 6 or more characters!");
            return true;
        }
        return false;
    }

    private void sendVerificationEmail() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> emailTask) {
                            if (emailTask.isSuccessful()) {
                                // Email sent successfully
                                Toast.makeText(OD_RegisterActivity.this, "Verification email sent", Toast.LENGTH_SHORT).show();
                            } else {
                                // Failed to send email
                                Log.e("EmailFailedToSend", "sendEmailVerification", emailTask.getException());
                                Toast.makeText(OD_RegisterActivity.this, "Failed to send verification email", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
    private void updateUsersRole(Map<String, Object> item){
        FirebaseUser user = mAuth.getCurrentUser();
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName("OD").build();
        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            if(selectedImage!=null){
                                uploadImage(user.getUid());
                            }
                            addUserToCollection(item,user.getUid());
                        }
                    }
                });
    }

    private void addUserToCollection(Map<String, Object> item,String id){
        db.collection("User")
                .document(id)
                .set(item)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(OD_RegisterActivity.this, "User created", Toast.LENGTH_SHORT).show();

                        finish();
                    }
                });
    }
    private boolean validateCompanyInput(TextInputEditText companyNameTextField, TextInputEditText companyAddressTextField, TextInputEditText companyEmailTextField, TextInputEditText companyDescriptionTextField, TextInputEditText companyPhoneNumberTextField) {
        boolean error=false;
        if(TextUtils.isEmpty(companyNameTextField.getText())){
            companyNameTextField.setError("Fill textfield!");
            error=true;
        }
        if(TextUtils.isEmpty(companyAddressTextField.getText())){
            companyAddressTextField.setError("Fill textfield!");
            error=true;
        }
        if(TextUtils.isEmpty(companyDescriptionTextField.getText())){
            companyDescriptionTextField.setError("Fill textfield!");
            error=true;
        }
        if(TextUtils.isEmpty(companyEmailTextField.getText())){
            companyEmailTextField.setError("Fill textfield!");
            error=true;
        }
        else if(!android.util.Patterns.EMAIL_ADDRESS.matcher(companyEmailTextField.getText()).matches()){
            companyEmailTextField.setError("E-Mail is not valid!");
            error=true;
        }
        if(TextUtils.isEmpty(companyPhoneNumberTextField.getText())){
            companyPhoneNumberTextField.setError("Fill textfield!");
            error=true;
        }
        return error;
    }
    private Map<String, Object>  createUserPUPV() {

        TextInputEditText firstNameTextField=findViewById(R.id.firstNameTextbox);
        TextInputEditText lastNameTextField=findViewById(R.id.lastNameTextbox);
        TextInputEditText addressTextField=findViewById(R.id.addressTextbox);
        TextInputEditText emailTextField=findViewById(R.id.emailTextbox);
        TextInputEditText passwordTextField=findViewById(R.id.passwordTextbox);
        TextInputEditText confirmPasswordTextField=findViewById(R.id.passwordTextbox);
        TextInputEditText phoneNumberTextField=findViewById(R.id.phoneNumberTextbox);

        if (validateOdInput(firstNameTextField, lastNameTextField, addressTextField, emailTextField, phoneNumberTextField, passwordTextField, confirmPasswordTextField))
            return null;

        TextInputEditText companyNameTextField=findViewById(R.id.companyNameTextBox);
        TextInputEditText companyAddressTextField=findViewById(R.id.companyAddressTextBox);
        TextInputEditText companyEmailTextField=findViewById(R.id.companyEmailTextBox);
        TextInputEditText companyDescriptionTextField=findViewById(R.id.companyDescriptionTextBox);
        TextInputEditText companyPhoneNumberTextField=findViewById(R.id.companyPhoneTextBox);

        if(validateCompanyInput(companyNameTextField,companyAddressTextField,companyEmailTextField,companyDescriptionTextField,companyPhoneNumberTextField))
            return null;

        TextInputEditText startMon=findViewById(R.id.startMonday);
        TextInputEditText endMon=findViewById(R.id.endMonday);
        TextInputEditText startTue=findViewById(R.id.startTuesday);
        TextInputEditText endTue=findViewById(R.id.endTuesday);
        TextInputEditText startWed=findViewById(R.id.startWednesday);
        TextInputEditText endWed=findViewById(R.id.endWednesday);
        TextInputEditText startThu=findViewById(R.id.startThursday);
        TextInputEditText endThu=findViewById(R.id.endThursday);
        TextInputEditText startFri=findViewById(R.id.startFriday);
        TextInputEditText endFri=findViewById(R.id.endFriday);
        TextInputEditText startSat=findViewById(R.id.startSaturday);
        TextInputEditText endSat=findViewById(R.id.endSaturday);
        TextInputEditText startSun=findViewById(R.id.startSunday);
        TextInputEditText endSun=findViewById(R.id.endSunday);

        if(validateDates(startMon, endMon, startTue, endTue, startWed, endWed, startThu, endThu, startFri, endFri, startSat, endSat, startSun, endSun)){
            return null;
        }

        CheckBox monCheckbox=findViewById(R.id.mondayCheckbox);
        CheckBox tueCheckbox=findViewById(R.id.tuesdayCheckbox);
        CheckBox wedCheckbox=findViewById(R.id.wednesdayCheckbox);
        CheckBox thuCheckbox=findViewById(R.id.thursdayCheckbox);
        CheckBox friCheckbox=findViewById(R.id.fridayCheckbox);
        CheckBox satCheckbox=findViewById(R.id.saturdayCheckbox);
        CheckBox sunCheckbox=findViewById(R.id.sundayCheckbox);

        String workTime;
        workTime=monCheckbox.isChecked() ?"free" : startMon.getText().toString()+"-"+endMon.getText().toString();
        workTime+="?"+(tueCheckbox.isChecked() ?"free" :startTue.getText().toString()+"-"+endTue.getText().toString());
        workTime+="?"+(wedCheckbox.isChecked() ?"free" :startWed.getText().toString()+"-"+endWed.getText().toString());
        workTime+="?"+(thuCheckbox.isChecked() ?"free" :startThu.getText().toString()+"-"+endThu.getText().toString());
        workTime+="?"+(friCheckbox.isChecked() ?"free" :startFri.getText().toString()+"-"+endFri.getText().toString());
        workTime+="?"+(satCheckbox.isChecked() ?"free" :startSat.getText().toString()+"-"+endSat.getText().toString());
        workTime+="?"+(sunCheckbox.isChecked() ?"free" :startSun.getText().toString()+"-"+endSun.getText().toString());

        Map<String, Object> item = new HashMap<>();
        item.put("FirstName", firstNameTextField.getText().toString());
        item.put("LastName", lastNameTextField.getText().toString());
        item.put("E-mail", emailTextField.getText().toString());
        item.put("Address", addressTextField.getText().toString());
        item.put("Phone", phoneNumberTextField.getText().toString());
        item.put("Password", passwordTextField.getText().toString());
        item.put("IsValid", false);
        item.put("CompanyName", companyNameTextField.getText().toString());
        item.put("CompanyAddress", companyAddressTextField.getText().toString());
        item.put("CompanyEmail", companyEmailTextField.getText().toString());
        item.put("CompanyDescription", companyDescriptionTextField.getText().toString());
        item.put("CompanyPhone", companyPhoneNumberTextField.getText().toString());
        item.put("WorkTime", workTime);

        return item;

    }

    private boolean validateDates(TextInputEditText startMon, TextInputEditText endMon, TextInputEditText startTue, TextInputEditText endTue, TextInputEditText startWed, TextInputEditText endWed, TextInputEditText startThu, TextInputEditText endThu, TextInputEditText startFri, TextInputEditText endFri, TextInputEditText startSat, TextInputEditText endSat, TextInputEditText startSun, TextInputEditText endSun) {
        boolean error=false;
        if(TextUtils.isEmpty(startMon.getText())){
            startMon.setError("Fill textfield!");
            error=true;
        }
        if(TextUtils.isEmpty(endMon.getText())){
            endMon.setError("Fill textfield!");
            error=true;
        }
        if(TextUtils.isEmpty(startTue.getText())){
            startTue.setError("Fill textfield!");
            error=true;
        }
        if(TextUtils.isEmpty(endTue.getText())){
            endTue.setError("Fill textfield!");
            error=true;
        }
        if(TextUtils.isEmpty(startWed.getText())){
            startWed.setError("Fill textfield!");
            error=true;
        }
        if(TextUtils.isEmpty(endWed.getText())){
            endWed.setError("Fill textfield!");
            error=true;
        }
        if(TextUtils.isEmpty(startThu.getText())){
            startThu.setError("Fill textfield!");
            error=true;
        }
        if(TextUtils.isEmpty(endThu.getText())){
            endThu.setError("Fill textfield!");
            error=true;
        }
        if(TextUtils.isEmpty(startFri.getText())){
            startFri.setError("Fill textfield!");
            error=true;
        }
        if(TextUtils.isEmpty(endFri.getText())){
            endFri.setError("Fill textfield!");
            error=true;
        }
        if(TextUtils.isEmpty(startSat.getText())){
            startSat.setError("Fill textfield!");
            error=true;
        }
        if(TextUtils.isEmpty(endSat.getText())){
            endSat.setError("Fill textfield!");
            error=true;
        }
        if(TextUtils.isEmpty(startSun.getText())){
            startSun.setError("Fill textfield!");
            error=true;
        }
        if(TextUtils.isEmpty(endSun.getText())){
            endSun.setError("Fill textfield!");
            error=true;
        }
        return error;
    }


}