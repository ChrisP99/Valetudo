package com.example.valetudo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;

import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class SearchFragment extends Fragment {

    private final String DATE_FORMAT = "dd/MM/yyyy";

    private EditText location;
    private Spinner roomType;

    private TextView stat;
    private TextView dates;
    private TextView alert1;
    private TextView alert2;
    private TextView alert3;


    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View searchView =  inflater.inflate(R.layout.fragment_search, container, false);

        Button cases = searchView.findViewById(R.id.cases_button);
        Button deaths = searchView.findViewById(R.id.deaths_button);

        location = searchView.findViewById(R.id.location);
        roomType = searchView.findViewById(R.id.room_type);

        // Initialise elements and make them invisible until the user chooses a country
        stat = searchView.findViewById(R.id.stat);
        stat.setText(R.string.select_stat);

        dates = searchView.findViewById(R.id.dates);
        Button search = searchView.findViewById(R.id.search_button);

        alert1 = searchView.findViewById(R.id.alert_text);
        alert2 = searchView.findViewById(R.id.alert_text2);
        alert3 = searchView.findViewById(R.id.alert_text3);

        alert1.setVisibility(View.INVISIBLE);
        alert2.setVisibility(View.INVISIBLE);
        alert3.setVisibility(View.INVISIBLE);

        // If dobText is clicked
        dates.setOnClickListener(view -> {

            // Creates a builder to display the dates to the user
            MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();

            // Creates a builder for the user to pik the dates
            MaterialDatePicker<Pair<Long, Long>> picker = builder.build();

            // Shows the picker to the user
            picker.show(requireActivity().getSupportFragmentManager(), picker.toString());


            // When the user has made a selection
            picker.addOnPositiveButtonClickListener(selection -> {

                // Store both the dates
                Long startDate = selection.first;
                Long endDate = selection.second;

                // Offset the timezones

                // Create a date format, then a date object with an offset
                SimpleDateFormat simpleFormat = new SimpleDateFormat(DATE_FORMAT, Locale.UK);

                // Update the view to display the dates the user picked
                dates.setText(simpleFormat.format(startDate) + " - " + simpleFormat.format(endDate));
            });
        });



        deaths.setOnClickListener(view ->  {
            if(location.getText().toString().equals("United Kingdom") ||
                    location.getText().toString().equals("Birmingham") ||
                    location.getText().toString().equals("Sheffield")){
                stat.setText("177,410 Deaths");
                alert1.setVisibility(View.VISIBLE);
                alert2.setVisibility(View.VISIBLE);
                alert3.setVisibility(View.VISIBLE);
            }
        });

        cases.setOnClickListener(view ->  {
            if(location.getText().toString().equals("United Kingdom") ||
               location.getText().toString().equals("Birmingham") ||
               location.getText().toString().equals("Sheffield")){

                stat.setText("2,203,799 Cases");
                alert1.setVisibility(View.VISIBLE);
                alert2.setVisibility(View.VISIBLE);
                alert3.setVisibility(View.VISIBLE);
            }
        });


        // When the user clicks on the search button
        search.setOnClickListener(view -> {
            // Make a new intent containing the search results
            Intent searchToResults = new Intent(getActivity(), SearchResultsActivity.class);

            // Pass through their selected location and dates
            searchToResults.putExtra("city", location.getText().toString());
            String[] selectedDates = dates.getText().toString().split(" - ");
            searchToResults.putExtra("roomType", roomType.getSelectedItem().toString());
            searchToResults.putExtra("startDate", selectedDates[0]);
            searchToResults.putExtra("endDate", selectedDates[1]);

            // Show the results
            startActivity(searchToResults);
        });


        location.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                // TODO Auto-generated method stub
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {

                EditText countryEntry = searchView.findViewById(R.id.location);
                TextView stat = searchView.findViewById(R.id.stat);

                TextView alert1 = searchView.findViewById(R.id.alert_text);
                TextView alert2 = searchView.findViewById(R.id.alert_text2);
                TextView alert3 = searchView.findViewById(R.id.alert_text3);

                if(location.getText().toString().equals("United Kingdom") ||
                        location.getText().toString().equals("Birmingham") ||
                        location.getText().toString().equals("Sheffield")){
                    stat.setText("22,203,799 Cases");
                    alert1.setVisibility(View.VISIBLE);
                    alert2.setVisibility(View.VISIBLE);
                    alert3.setVisibility(View.VISIBLE);
                }
            }
        });

        setUpRoomTypeDropdown(searchView);

        return searchView;
    }

    private void setUpRoomTypeDropdown(View searchView) {
        // Get the venue type dropdown
        Spinner roomType = searchView.findViewById(R.id.room_type);
        // Create a list of venue types
        String[] roomTypes = new String[]{"Single", "Double", "Twin"};
        // Create an adapter to describe how the items are displayed
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, roomTypes);
        // Set the venue types within the dropdown
        roomType.setAdapter(adapter);
        // Set the default selected value for the dropdown
        roomType.setSelection(0);
    }
}