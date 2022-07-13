package com.example.valetudo;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.valetudo.databinding.ActivityHotelResultsBinding;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SelectedHotelActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap map;

    private static final String TAG = "LOG:";
    private final String DATE_FORMAT = "dd/MM/yyyy";

    private FirebaseFirestore firestore;

    private String roomType;
    private String city;
    double roomId;
    private double venueId;
    private String bookingRef;
    private String startDate;
    private String endDate;

    private FirebaseAuth mAuth;

    private Integer stays;
    private Double staysPrice;
    private Double roomPrice;

    private Button book;
    private final String CHANNEL_ID = "1";
    private final int NOTIFICATION_ID = 1;

    private final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle("Booking confirmed.")
            .setContentText("Your booking has been confirmed!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT);

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_results);

        createNotificationChannel();

        mAuth = FirebaseAuth.getInstance();
        mAuth.getCurrentUser();

        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(SelectedHotelActivity.this);

        firestore = FirebaseFirestore.getInstance();
        ImageView backArrow = findViewById(R.id.back_button);



        backArrow.setOnClickListener(v ->{
            Intent searchResultsToLogin = new Intent(SelectedHotelActivity.this, HomeActivity.class);
            startActivity(searchResultsToLogin);
        });


        // Gets the query that the user entered in the search fragment
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            startDate = extras.getString("startDate");
            endDate = extras.getString("endDate");
            roomType = extras.getString("roomType");

            venueId = extras.getDouble("venueId");
            roomId = extras.getDouble("roomId");

            city = extras.getString("city");

            TextView checkInDate = findViewById(R.id.check_in);
            checkInDate.setText(startDate);

            TextView checkOutDate = findViewById(R.id.check_out);
            checkOutDate.setText(endDate);

            // Converts the start and end date back to date variables to calculate the number of nights
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT, Locale.UK);
            LocalDate startDateNew = LocalDate.parse(startDate, formatter);
            LocalDate endDateNew = LocalDate.parse(endDate, formatter);

            long daysBetween = ChronoUnit.DAYS.between(startDateNew, endDateNew);
            stays = Math.toIntExact(daysBetween);

        }
        executeSearchQuery();
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Booking notification";
            String description = "A notification after a user makes a booking";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

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
                            map = googleMap;
                            map.getUiSettings().setMyLocationButtonEnabled(false);

                            // Updates the location and zoom of the MapView
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(venueDocument.getDouble("latitude"), venueDocument.getDouble("longtitude")), 16.0F);
                            map.animateCamera(cameraUpdate);
                            map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(venueDocument.getDouble("latitude"), venueDocument.getDouble("longtitude"))));
                        }
                    } else {
                        Log.d(TAG, "Error getting venue documents: ",
                                venueSearchTask.getException());
                    }
                });
    }

    public void searchRooms(QueryDocumentSnapshot venueDocument) {
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

    private void displayDetails(QueryDocumentSnapshot venueDocument,
                                QueryDocumentSnapshot roomDocument) {

        TextView hotelNameTop = findViewById(R.id.hotel_name_top);
        TextView hotelName = findViewById(R.id.hotel_name_booking);
        TextView hotelRating = findViewById(R.id.hotel_rating);
        TextView covidRating = findViewById(R.id.covid_rating);

        TextView address = findViewById(R.id.address);

        ImageView hotel_selected = findViewById(R.id.hotel_selected);

        book = findViewById(R.id.book);

        // Create the Views...
        DecimalFormat priceFormat = new DecimalFormat("'£'0.00");

        roomPrice = roomDocument.getDouble("pricePerNight");
        staysPrice = stays * roomPrice;

        String staysText = "Book for " + staysPrice;

        hotelNameTop.setText(venueDocument.getString("venueName") + " - " + roomDocument.getString("roomType"));
        hotelName.setText(venueDocument.getString("venueName"));
        hotelRating.setText(venueDocument.getDouble("venueRating") + " Stars");
        covidRating.setText(venueDocument.getDouble("covidRating") + " Shields");
        address.setText(venueDocument.getString("venueAddress"));
        book.setText(staysText);

        // Set the thumbnail image
        Picasso.get().load(venueDocument.getString("selectedUrl")).into(hotel_selected);

        book.setOnClickListener(v -> {

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            // notificationId is a unique int for each notification that you must define
            notificationManager.notify(NOTIFICATION_ID, builder.build());

            DocumentReference key = firestore.collection("bookings").document();
            Map<String, Object> data = new HashMap<>();
            data.put("amountPaid",  staysPrice);
            data.put("bookingReference", key.getId());
            data.put("checkInDate", startDate);
            data.put("checkOutDate", endDate);
            data.put("roomId", roomId);
            data.put("userId", mAuth.getUid());
            data.put("venueId", venueId);

            firestore.collection("bookings").document()
                    .set(data)
                    .addOnSuccessListener(aVoid -> {
                    });

            Intent bookingIntent = new Intent(SelectedHotelActivity.this, BookingActivity.class);

            // Start the intent
            startActivity(bookingIntent);
        });
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }


    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}