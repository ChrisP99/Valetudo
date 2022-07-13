package com.example.valetudo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;


public class SearchResultsActivity extends AppCompatActivity {

    private static final String TAG = "LOG:";
    private ImageView backArrow;
    private FirebaseFirestore firestore;

    private String city;
    private String startDate;
    private String endDate;
    private String roomType;

    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        firestore = FirebaseFirestore.getInstance();
        linearLayout = findViewById(R.id.scroll_layout);
        backArrow = findViewById(R.id.back_button);

        backArrow.setOnClickListener(v ->{
            Intent searchResultsToLogin = new Intent(SearchResultsActivity.this, HomeActivity.class);
            startActivity(searchResultsToLogin);
        });

        // Gets the query that the user entered in the search fragment
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            startDate = extras.getString("startDate");
            endDate = extras.getString("endDate");
            roomType = extras.getString("roomType");
            city = extras.getString("city");

            String results =
                    extras.getString("city") + "\t\t\t\t" + extras.getString("startDate") + " - " + extras.getString("endDate");

            TextView resultsText = findViewById(R.id.results);
            resultsText.setText(results);
        }
        executeSearchQuery();
    }

    private void searchRooms(QueryDocumentSnapshot venueDocument) {
        // From the rooms collection
        firestore.collection("rooms")
                // If venueID for the room matches venueID for the venue
                .whereEqualTo("venueId", venueDocument.getDouble("venueId"))
                // If room type matches the selected room type
                .whereEqualTo("roomType", roomType)
                // If venue is available
                .whereEqualTo("isAvailable", true)
                // Attempt to get the document(s)
                .get()
                // Add an on complete listener to the retrieval
                .addOnCompleteListener(roomSearchTask -> {
                    // If task is successful
                    if (roomSearchTask.isSuccessful()) {
                        // For each returned room document
                        for (QueryDocumentSnapshot roomDocument : roomSearchTask.getResult()) {
                            // Log a message
                            Log.d(TAG, roomDocument.getId() + " => " + roomDocument.getData());
                            // Display the details
                            displayDetails(venueDocument, roomDocument);
                        }
                    } else {
                        Log.d(TAG, "Error getting room documents: ",
                                roomSearchTask.getException());
                    }
                });
    }

    public void executeSearchQuery() {
        // From the venues collection
        firestore.collection("venues")
                // If city is equal to destination
                .whereEqualTo("city", city)
                // Attempt to get the document(s)
                .get()
                // Add an on complete listener to the retrieval
                .addOnCompleteListener(venueSearchTask -> {
                    // If task is successful
                    if (venueSearchTask.isSuccessful()) {
                        // For each returned venue
                        for (QueryDocumentSnapshot venueDocument : venueSearchTask.getResult()) {
                            // Log a message
                            Log.d(TAG, venueDocument.getId() + " => " +
                                    venueDocument.getData());
                            // Search rooms
                            searchRooms(venueDocument);
                        }
                    } else {
                        Log.d(TAG, "Error getting venue documents: ",
                                venueSearchTask.getException());
                    }
                });
    }


    // Display the details for the search result


    private void displayDetails(QueryDocumentSnapshot venueDocument,
                                QueryDocumentSnapshot roomDocument) {


        // Inflate a View which contains each search result
        View searchResult = SearchResultsActivity.this.getLayoutInflater()
                .inflate(R.layout.search_result_fragment,
                        new ConstraintLayout(SearchResultsActivity.this),
                        false);

        TextView hotelName = searchResult.findViewById(R.id.hotel_name_booking);
        TextView hotelRating = searchResult.findViewById(R.id.hotel_rating);
        TextView covidRating = searchResult.findViewById(R.id.covid_rating);
        TextView roomPrice = searchResult.findViewById(R.id.price_per_night);
        TextView location = searchResult.findViewById(R.id.city_booking);

        ImageView hotelImage = searchResult.findViewById(R.id.hotel_image);
        ImageView parking = searchResult.findViewById(R.id.parking);

        ImageView wifi = searchResult.findViewById(R.id.wifi);
        ImageView gym = searchResult.findViewById(R.id.gym);
        ImageView wheelchair = searchResult.findViewById(R.id.wheelchair_access);

        DecimalFormat priceFormat = new DecimalFormat("'£'0.00");


        // Set each component equal to the string of the record in the Firestore Database
        hotelName.setText(venueDocument.getString("venueName") + " - " + roomDocument.getString("roomType"));
        roomPrice.setText(priceFormat.format((roomDocument.getDouble("pricePerNight"))));
        hotelRating.setText(venueDocument.getDouble("venueRating") + " Stars");
        covidRating.setText(venueDocument.getDouble("covidRating") + " Shields");
        location.setText(venueDocument.getString("city"));

        // If any features are false make them invisible
        if(Boolean.FALSE.equals(venueDocument.getBoolean("parking"))){
            parking.setVisibility(View.GONE);
        }

        if(Boolean.TRUE.equals(venueDocument.getBoolean("wifiProvided"))){
            wifi.setVisibility(View.GONE);
        }

        if(Boolean.FALSE.equals(venueDocument.getBoolean("gym"))){
            gym.setVisibility(View.GONE);
        }

        if(Boolean.FALSE.equals(venueDocument.getBoolean("wheelchairAccess"))){
            wheelchair.setVisibility(View.GONE);
        }

        // Set the thumbnail image
        Picasso.get().load(venueDocument.getString("pictureUrl")).into(hotelImage);


        // Listener for when the usr clicks on a result

        // Pass the variables through to the hotel results screen
        searchResult.setOnClickListener(v -> {
            Intent searchIntent = new Intent(SearchResultsActivity.this,
                    SelectedHotelActivity.class);
            searchIntent.putExtra("city", city);
            searchIntent.putExtra("endDate", endDate);
            searchIntent.putExtra("roomType", roomType);
            searchIntent.putExtra("startDate", startDate);
            searchIntent.putExtra("venueId", venueDocument.getDouble("venueId")); // Get from selected venue
            searchIntent.putExtra("roomId", roomDocument.getDouble("roomId")); // Get from selected venue); // Get from selected venue
            // Start the intent
            startActivity(searchIntent);
        });

        linearLayout.addView(searchResult);
    }

    @Override
    public void onStart() {
        super.onStart();

    }
}