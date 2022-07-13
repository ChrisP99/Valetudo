package com.example.valetudo;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.Objects;

public class BookingActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;

    // Declare constants
    private static final String TAG = "LOG";
    private static final DecimalFormat priceFormat = new DecimalFormat("'£'0.00");

    private ImageView venueImage;

    private TextView hotelNameTop;
    private TextView hotelName;
    private TextView bookingReference;
    private TextView checkInDate;
    private TextView checkOutDate;
    private TextView amountPaid;

    // Declare Firebase auth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        // Gets an instance of Firestore
        firestore = FirebaseFirestore.getInstance();

        // Gets an instance of the logged in user
        mAuth = FirebaseAuth.getInstance();
        mAuth.getCurrentUser();

        Button bookings = this.findViewById(R.id.go_to_bookings);
        ImageView backArrow = findViewById(R.id.back);

        backArrow.setOnClickListener(v ->{
            Intent searchResultsToLogin = new Intent(BookingActivity.this, HomeActivity.class);
            startActivity(searchResultsToLogin);
        });

        bookings.setOnClickListener(v ->{
            Intent searchResultsToLogin = new Intent(BookingActivity.this, HomeActivity.class);
            startActivity(searchResultsToLogin);
        });

        displayUserBookings();
    }

    private void displayUserBookings() {
        // Search bookings
        searchBookings();
    }

    private void setBookingsThumbnailValues(QueryDocumentSnapshot bookingDocument,
                                            QueryDocumentSnapshot venueDocument) {
        // Set the text elements to their respective values
        hotelNameTop.setText(venueDocument.getString("venueName"));
        hotelName.setText(venueDocument.getString("venueName"));
        bookingReference.setText(bookingDocument.getString("bookingReference"));
        checkInDate.setText(bookingDocument.getString("checkInDate"));
        checkOutDate.setText(bookingDocument.getString("checkOutDate"));
        amountPaid.setText(priceFormat.format(bookingDocument.getDouble("amountPaid")));
        // Set the thumbnail image
        Picasso.get().load(venueDocument.getString("selectedUrl")).into(venueImage);
    }
    private void getBookingsThumbnailElements() {
        // Get the elements of the bookings thumbnail view
        hotelNameTop = this.findViewById(R.id.hotel_name_top);
        hotelName = this.findViewById(R.id.hotel_name_booking);
        bookingReference = this.findViewById(R.id.booking_reference);
        venueImage = this.findViewById(R.id.booking_hotel_image);
        checkInDate = this.findViewById(R.id.check_in_booking);
        checkOutDate = this.findViewById(R.id.check_out_booking);
        amountPaid = this.findViewById(R.id.total_amount);
    }

    private void searchBookings() {
        // From the bookings collection
        firestore.collection("bookings")
                // If userID for the booking matches the user
                .whereEqualTo("userId", Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                // Attempt to get the document(s)
                .get()
                // Add an on complete listener to the retrieval
                .addOnCompleteListener(bookingsSearchTask -> {
                    // If task is successful
                    if (bookingsSearchTask.isSuccessful()) {
                        // Get the task results
                        QuerySnapshot bookingDocuments = bookingsSearchTask.getResult();
                        // If the task result is not empty
                        if (!bookingDocuments.getDocuments().isEmpty()) {
                            // For each returned booking document
                            for (QueryDocumentSnapshot bookingDocument : bookingDocuments) {
                                // Log a message
                                Log.d(TAG, bookingDocument.getId() + " => " +
                                        bookingDocument.getData());
                                // Search venues
                                searchVenues(bookingDocument);
                            }
                        }
                    } else {
                        Log.d(TAG, "Error getting booking documents: ",
                                bookingsSearchTask.getException());
                    }
                });
    }

    private void searchRooms(QueryDocumentSnapshot bookingDocument,
                             QueryDocumentSnapshot venueDocument) {
        // From the rooms collection
        firestore.collection("rooms")
                // If the room ID matches
                .whereEqualTo("roomId", bookingDocument.getDouble("roomId"))
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
                            displayDetails(bookingDocument, venueDocument);
                        }
                    } else {
                        Log.d(TAG, "Error getting room documents: ",
                                roomSearchTask.getException());
                    }
                });
    }

    private void searchVenues(QueryDocumentSnapshot bookingDocument) {
        // From the venues collection
        firestore.collection("venues")
                // If the venueID matches
                .whereEqualTo("venueId", bookingDocument.getDouble("venueId"))
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
                            searchRooms(bookingDocument, venueDocument);
                        }
                    } else {
                        Log.d(TAG, "Error getting venue documents: ",
                                venueSearchTask.getException());
                    }
                });
    }

    private void displayDetails(QueryDocumentSnapshot bookingDocument,
                                QueryDocumentSnapshot venueDocument) {

        // Initialise the booking thumbnail interface elements
        getBookingsThumbnailElements();
        // Set the thumbnail interface element values
        setBookingsThumbnailValues(bookingDocument, venueDocument);
    }

}