package com.example.valetudo;

import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.Objects;

public class BookingsFragment extends Fragment {

    // Declare constants
    private static final String TAG = "LOG";
    private static final DecimalFormat priceFormat = new DecimalFormat("'£'0.00");

    // Declare interface elements
    private LinearLayout bookingFragmentLayout;
    private ImageView venueImage;
    private TextView bookingReference;
    private TextView checkInDate;
    private TextView checkOutDate;
    private TextView amountPaid;
    private TextView venueName;
    private TextView venueRating;
    private TextView city;
    private TextView covidRating;

    // Declare Firebase auth
    private FirebaseAuth mAuth;

    // Declare Firebase Firestore
    FirebaseFirestore firestore;

    public BookingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialise Firebase auth
        mAuth = FirebaseAuth.getInstance();

        // Initialise Firebase Firestore
        firestore = FirebaseFirestore.getInstance();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View bookingsView =  inflater.inflate(R.layout.fragment_bookings, container, false);

        // Initialise the interface elements
        bookingFragmentLayout = bookingsView.findViewById(R.id.scroll_layout);
        // Display the bookings belonging to the user
        displayUserBookings();
        // Return the fragment
        return bookingsView;
    }

    private void displayDetails(QueryDocumentSnapshot bookingDocument,
                                QueryDocumentSnapshot venueDocument) {
        // Create the booking thumbnail view element
        View bookingsThumbnail = getActivity().getLayoutInflater()
                .inflate(R.layout.fragment_booking_holder,
                        new ConstraintLayout(getActivity()), false);

        // Initialise the booking thumbnail interface elements
        getBookingsHolderElements(bookingsThumbnail);
        // Set the thumbnail interface element values
        setBookingsHolderValues(bookingDocument, venueDocument);
//        // Setup the on click listener for the booking thumbnail
//        setBookingsThumbnailOnClickListener(bookingsThumbnail);
        // Add the booking thumbnail to the layout
        bookingFragmentLayout.addView(bookingsThumbnail);
    }


    private void setBookingsHolderValues(QueryDocumentSnapshot bookingDocument,
                                         QueryDocumentSnapshot venueDocument) {
        // Set the text elements to their respective values
        venueName.setText(venueDocument.getString("venueName"));
        city.setText(venueDocument.getString("city"));
        venueRating.setText(venueDocument.getDouble("venueRating").toString());
        covidRating.setText(venueDocument.getDouble("covidRating").toString());
        bookingReference.setText(bookingDocument.getString("bookingReference"));
        checkInDate.setText(bookingDocument.getString("checkInDate"));
        checkOutDate.setText(bookingDocument.getString("checkOutDate"));
        amountPaid.setText(priceFormat.format(bookingDocument.getDouble("amountPaid")));
        // Set the thumbnail image
        Picasso.get().load(venueDocument.getString("pictureUrl")).into(venueImage);
    }
    private void getBookingsHolderElements(View bookingHolder) {
        // Get the elements of the bookings thumbnail view
        venueName = bookingHolder.findViewById(R.id.hotel_name_booking);
        bookingReference = bookingHolder.findViewById(R.id.booking_ref);
        city = bookingHolder.findViewById(R.id.city_booking);
        venueImage = bookingHolder.findViewById(R.id.hotel_image);
        checkInDate = bookingHolder.findViewById(R.id.check_in_fragment);
        checkOutDate = bookingHolder.findViewById(R.id.check_out_fragment);
        amountPaid = bookingHolder.findViewById(R.id.amount_paid);
        venueRating = bookingHolder.findViewById(R.id.hotel_rating);
        covidRating = bookingHolder.findViewById(R.id.covid_rating);
    }

    private void displayUserBookings() {
        // Search bookings
        searchBookings();
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

}