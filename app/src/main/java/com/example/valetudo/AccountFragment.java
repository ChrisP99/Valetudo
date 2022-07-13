package com.example.valetudo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class AccountFragment extends Fragment {

    private static final String TAG = "LOG";
    private FirebaseAuth mAuth;
    public FirebaseFirestore db = FirebaseFirestore.getInstance();

    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View accountView = inflater.inflate(R.layout.fragment_account, container, false);
        // Inflate the layout for this fragment
        // Get the current user from Firebase
        updateAccountDetails(accountView);

        // Initialise variables
        Button managePersonalInformation = accountView.findViewById(R.id.personal_information);
        Button vaccineCard = accountView.findViewById(R.id.vaccine_card);

        Button logOut = accountView.findViewById(R.id.log_out);

        // If the usr clicks on Personal Information bring them to the Personal Information page
        managePersonalInformation.setOnClickListener(v -> {
           Fragment personalInformationFragment = new PersonalInformationFragment();

           getParentFragmentManager()
                   .beginTransaction()
                   .replace(R.id.fragment_container, personalInformationFragment)
                   .commit();
        });


        // If the user clicks on the Vaccine Card link bring them to the Vaccine card page
        vaccineCard.setOnClickListener(v -> {
            Fragment vaccineCardFragment = new VaccineFragment();

            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, vaccineCardFragment)
                    .commit();
        });


        // Safely logs the user out of Firebase and any account provider
        logOut.setOnClickListener(v -> {
            LoginManager.getInstance().logOut();
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getActivity(), LoginActivity.class));
        });

        return accountView;
    }

    // Function to check if the user is a firebase user
    private boolean isFirebase() {
        boolean isFirebase = true;
        // Get the provider data of the user who is logged in
        for (UserInfo userInfo : Objects.requireNonNull(mAuth.getCurrentUser()).getProviderData()) {
            // If their login is via Google or Facebook
            if (userInfo.getProviderId().equals("facebook.com") || userInfo.getProviderId().equals("google.com")) {
                // Set is Firebase to false
                isFirebase = false;
            }
        }
        return isFirebase;
    }

    public void setPersonalInformation(View accountView, String photoValue, String name) {

        // If Google or Facebook auth is used (i.e. photoValue is not blank)
        if (!isFirebase()) {
            // Set accountImage
            ImageView accountImage = accountView.findViewById(R.id.account_photo);
            Picasso.get().load(photoValue).into(accountImage);
        }

        // Set accountName
        TextView accountName = accountView.findViewById(R.id.account_name);
        accountName.setText(name);
    }

    private void updateAccountDetails(View accountView) {
            FirebaseUser currentUser = mAuth.getCurrentUser();

            // Assert that current user is not null
            assert currentUser != null;

            // Initialise account interface values
            AtomicReference<String> photoValue = new AtomicReference<>("");

            // Get the document reference for the current user
            DocumentReference userRef = db.collection("users")
                    .document(mAuth.getCurrentUser().getUid());

            userRef.get().addOnCompleteListener(task -> {
                // If the document is successfully retrieved
                if (task.isSuccessful()) {
                    // Assign the result to the document
                    DocumentSnapshot userSnapshot = task.getResult();
                    // If the document exists
                    if (userSnapshot.exists()) {
                        // Log a message
                        Log.d(TAG, "DocumentSnapshot data: " + userSnapshot.getData());
                        // Get access token
                        String accessToken = AccessToken.getCurrentAccessToken() != null
                                ? AccessToken.getCurrentAccessToken().getToken() : "";
                        // Get photo url from current user using access token
                        photoValue.set(currentUser.getPhotoUrl() + "?access_token=" + accessToken
                                + "&type=large");
                        setPersonalInformation(accountView,
                                photoValue.get(),
                                userSnapshot.getString("name"));
                        // If the document does not exist
                    } else {
                        // Log an error
                        Log.d(TAG, "No such document");
                    }
                    // If the document is not successfully retrieved
                } else {
                    // Log an error
                    Log.d(TAG, "get failed with ", task.getException());
                }
            });
        }
    }