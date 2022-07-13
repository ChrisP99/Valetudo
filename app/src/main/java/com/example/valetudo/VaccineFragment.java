package com.example.valetudo;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

public class VaccineFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public VaccineFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View vaccineFragment =  inflater.inflate(R.layout.fragment_vaccine, container, false);

        Button back = vaccineFragment.findViewById(R.id.back_vaccine);
        Fragment accountFragment = new AccountFragment();

        back.setOnClickListener(v -> getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, accountFragment)
                .commit());

        return vaccineFragment;
    }
}