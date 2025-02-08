package com.example.eventplanner.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventplanner.fragments.PackageListPupvFragment;
import com.example.eventplanner.fragments.PackageListPupzFragment;
import com.example.eventplanner.R;
import com.example.eventplanner.databinding.ActivityPackagesManagementBinding;
import com.google.android.material.textfield.TextInputLayout;

public class PackagesManagementActivity extends AppCompatActivity {

    ActivityPackagesManagementBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPackagesManagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String[] categories = {"Category 1", "Category 2", "Category 3", "Category 4", "Category 5"};
        String[] subcategories = {"Subcategory 1", "Subcategory 2", "Subcategory 3", "Subcategory 4", "Subcategory 5"};

        TextInputLayout textInputLayout = findViewById(R.id.category_filter);
        AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) textInputLayout.getEditText();
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(PackagesManagementActivity.this, android.R.layout.simple_dropdown_item_1line, categories);
        autoCompleteTextView.setAdapter(categoryAdapter);

        TextInputLayout subcategoryTextInputLayout = findViewById(R.id.subcategory_filter);
        AutoCompleteTextView subcategoryAutoCompleteTextView = (AutoCompleteTextView) subcategoryTextInputLayout.getEditText();
        ArrayAdapter<String> subcategoryAdapter = new ArrayAdapter<>(PackagesManagementActivity.this, android.R.layout.simple_dropdown_item_1line, subcategories);
        subcategoryAutoCompleteTextView.setAdapter(subcategoryAdapter);

        TextInputLayout eventTypeTextInputLayout = findViewById(R.id.event_type_filter);
        AutoCompleteTextView eventTypeAutoCompleteTextView = (AutoCompleteTextView) eventTypeTextInputLayout.getEditText();
        String[] events = {"Event 1", "Event 2", "Event 3", "Event 4", "Event 5"};
        ArrayAdapter<String> eventTypeAdapter = new ArrayAdapter<>(PackagesManagementActivity.this, android.R.layout.simple_dropdown_item_1line, events);
        eventTypeAutoCompleteTextView.setAdapter(eventTypeAdapter);

        String usedFragment = getIntent().getStringExtra("used_fragment");

        if(savedInstanceState == null){
            if(usedFragment.equals("package_list_pupv")){
                PackageListPupvFragment fragment = new PackageListPupvFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.package_list_framelayout, fragment)
                        .commit();
            }
            else{
                PackageListPupzFragment fragment = new PackageListPupzFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.package_list_framelayout, fragment)
                        .commit();
            }
        }
    }
}