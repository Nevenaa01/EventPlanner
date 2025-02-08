package com.example.eventplanner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventplanner.R;
import com.example.eventplanner.model.Category;
import com.example.eventplanner.model.EventType;
import com.example.eventplanner.model.UserPUPV;

public class UserDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        UserPUPV user = (UserPUPV)intent.getSerializableExtra("user");
        setTextBoxes(user);

    }

    void setTextBoxes(UserPUPV user){
        TextView addressText=findViewById(R.id.address);
        addressText.setText(user.getAddress());

        TextView categoriesText=findViewById(R.id.categories);
        String categories="";
        for(Category cat : user.getCategories()){
            categories+=cat.getName()+", ";
        }
        categoriesText.setText(categories);

        TextView compayAddressText=findViewById(R.id.companyAddress);
        compayAddressText.setText(user.getCompanyAddress());

        TextView companyDescriptionText=findViewById(R.id.description);
        companyDescriptionText.setText(user.getCompanyDescription());

        TextView companyEmailText=findViewById(R.id.companyEmail);
        companyEmailText.setText(user.getCompanyemail());

        TextView companyNameText=findViewById(R.id.companyName);
        companyNameText.setText(user.getCompanyName());

        TextView companyPhoneText=findViewById(R.id.companyPhone);
        companyPhoneText.setText(user.getCompanyPhone());


        TextView emailText=findViewById(R.id.email);
        emailText.setText(user.getEmail());

        TextView eventTypesText=findViewById(R.id.eventTypes);
        String eventTypes="";
        for(EventType cat : user.getEventTypes()){
            eventTypes+=cat.getTypeName()+", ";
        }
        eventTypesText.setText(eventTypes);

        TextView firstNameText=findViewById(R.id.firstName);
        firstNameText.setText(user.getFirstName());

        TextView lastNameText=findViewById(R.id.lastName);
        lastNameText.setText(user.getLastName());

        TextView phoneText=findViewById(R.id.phone);
        phoneText.setText(user.getPhone());



    }
}