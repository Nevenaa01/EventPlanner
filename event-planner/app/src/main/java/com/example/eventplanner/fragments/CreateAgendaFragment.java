package com.example.eventplanner.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.eventplanner.R;
import com.example.eventplanner.databinding.FragmentAddSubcategoryOnBudgetPlannerBinding;
import com.example.eventplanner.databinding.FragmentCreateEventBinding;

public class CreateAgendaFragment extends Fragment {

   FragmentCreateEventBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentCreateEventBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        return root;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d("EventPlanner", "CreateAgendaFragment onAttach()");
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}