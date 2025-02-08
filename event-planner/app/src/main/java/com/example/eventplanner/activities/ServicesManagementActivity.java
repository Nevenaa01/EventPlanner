package com.example.eventplanner.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventplanner.R;
import com.example.eventplanner.fragments.ServiceListPupvFragment;
import com.example.eventplanner.fragments.ServiceListPupzFragment;
import com.example.eventplanner.databinding.ActivityServicesManagementBinding;
import com.google.android.material.textfield.TextInputLayout;

public class ServicesManagementActivity extends AppCompatActivity {

    ActivityServicesManagementBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityServicesManagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String[] categories = {"Category 1", "Category 2", "Category 3", "Category 4", "Category 5"};
        String[] subcategories = {"Subcategory 1", "Subcategory 2", "Subcategory 3", "Subcategory 4", "Subcategory 5"};
        String[] providers = {"Provider 1", "Provider 2", "Provider 3", "Provider 4", "Provider 5"};

        TextInputLayout textInputLayout = findViewById(R.id.category_filter);
        AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) textInputLayout.getEditText();
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(ServicesManagementActivity.this, android.R.layout.simple_dropdown_item_1line, categories);
        autoCompleteTextView.setAdapter(categoryAdapter);

        TextInputLayout subcategoryTextInputLayout = findViewById(R.id.subcategory_filter);
        AutoCompleteTextView subcategoryAutoCompleteTextView = (AutoCompleteTextView) subcategoryTextInputLayout.getEditText();
        ArrayAdapter<String> subcategoryAdapter = new ArrayAdapter<>(ServicesManagementActivity.this, android.R.layout.simple_dropdown_item_1line, subcategories);
        subcategoryAutoCompleteTextView.setAdapter(subcategoryAdapter);

        TextInputLayout eventTypeTextInputLayout = findViewById(R.id.event_type_filter);
        AutoCompleteTextView eventTypeAutoCompleteTextView = (AutoCompleteTextView) eventTypeTextInputLayout.getEditText();
        String[] events = {"Event 1", "Event 2", "Event 3", "Event 4", "Event 5"};
        ArrayAdapter<String> eventTypeAdapter = new ArrayAdapter<>(ServicesManagementActivity.this, android.R.layout.simple_dropdown_item_1line, events);
        eventTypeAutoCompleteTextView.setAdapter(eventTypeAdapter);

        TextInputLayout providerTextInputLayout = findViewById(R.id.providers_filter);
        AutoCompleteTextView providerAutoCompleteTextView = (AutoCompleteTextView) providerTextInputLayout.getEditText();
        ArrayAdapter<String> providerAdapter = new ArrayAdapter<>(ServicesManagementActivity.this, android.R.layout.simple_dropdown_item_1line, providers);
        providerAutoCompleteTextView.setAdapter(providerAdapter);

        String usedFragment = getIntent().getStringExtra("used_fragment");

        if(savedInstanceState == null){
            if(usedFragment.equals("service_list_pupv")){
                ServiceListPupvFragment fragment = new ServiceListPupvFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.service_list_framelayout, fragment)
                        .commit();
            }
            else{
                ServiceListPupzFragment fragment = new ServiceListPupzFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.service_list_framelayout, fragment)
                        .commit();
            }
        }
    }
}