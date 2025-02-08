package com.example.eventplanner.fragments;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.eventplanner.R;
import com.example.eventplanner.databinding.FragmentSearchPspBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.textfield.TextInputEditText;

import androidx.core.util.Pair;

import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SearchPspFragment extends Fragment {

    FragmentSearchPspBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        binding = FragmentSearchPspBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        return root;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d("EventPlanner", "SearchPspFragment onAttach()");
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}