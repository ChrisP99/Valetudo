package com.example.valetudo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;

import java.util.Objects;

public class PersonalInformationFragment extends Fragment {

    private static final String TAG = "LOG";
    private FirebaseAuth mAuth;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private TextView managedByProvider;

    private EditText name;
    private EditText email;
    private EditText mobile;
    private Button resetPassword;

    public PersonalInformationFragment() {
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
        // Inflate the layout for this fragment
        View personalInformationView = inflater.inflate(R.layout.fragment_personal_information, container, false);

        Fragment accountFragment = new AccountFragment();

        Button back = personalInformationView.findViewById(R.id.back);
        Button save = personalInformationView.findViewById(R.id.save);
        Button deleteAccount = personalInformationView.findViewById(R.id.delete_account);
        resetPassword = personalInformationView.findViewById(R.id.reset_password);

        name = personalInformationView.findViewById(R.id.name);
        email = personalInformationView.findViewById(R.id.email);
        mobile = personalInformationView.findViewById(R.id.mobile);

        // Create an alert dialogue builder to confirm account deletion
        AlertDialog.Builder deleteBuilder = new AlertDialog.Builder(requireActivity());
        AlertDialog.Builder resetPasswordBuilder = new AlertDialog.Builder(requireActivity());

        setUpDeleteAccount(deleteBuilder);
        setUpResetPassword(personalInformationView, resetPasswordBuilder);

        getPersonalInformation();

        back.setOnClickListener(v -> getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, accountFragment)
                .commit());

        save.setOnClickListener(v -> updatePersonalInformation(personalInformationView));

        resetPassword.setOnClickListener(v ->  {
           AlertDialog dialog = resetPasswordBuilder.create();
           dialog.show();
        });

        // If deleteAccountButton is clicked
        deleteAccount.setOnClickListener(v -> {
            AlertDialog dialog = deleteBuilder.create();
            dialog.show();
        });

        return personalInformationView;
    }

    public void setUpDeleteAccount(AlertDialog.Builder builder) {

        // Make alert dialogue cancellable
        builder.setCancelable(true);

        // Set alert dialogue title
        builder.setTitle(R.string.delete_account);

        // Set alert dialogue message
        builder.setMessage(R.string.delete_confirmation);

        // Set alert dialogue confirmation behaviour
        builder.setPositiveButton(R.string.yes,
                // Attempt to delete the current user
            (dialog, which) ->
                // Get the current user document
                db.collection("users")
                    .document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                    // Attempt to delete the current user document
                    .delete()
                    // Add an on success listener for deleting the current user document
                    .addOnSuccessListener(aVoid -> {
                        // Log a message
                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                        // Attempt to delete the user Firebase auth
                        mAuth.getCurrentUser().delete()
                            // Add an on complete listener for deleting the current user
                            .addOnCompleteListener(task -> {
                                // If the current user is successfully deleted
                                if (task.isSuccessful()) {
                                    // Log a message
                                    Log.d(TAG, "User account deleted.");
                                    // Redirect to HomeScreen
                                    startActivity(new Intent(getActivity(), LoginActivity.class));
                                }
                            });
                    })
                        // Add an on failure listener for deleting the current user document
                        .addOnFailureListener(e ->
                                // Log an error
                                Log.w(TAG, "Error deleting document", e)));

        // Set alert dialogue cancellation behaviour
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {

        });
    }

    public void getPersonalInformation() {
        DocumentReference userRef = db.collection("users")
                .document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid());

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                DocumentSnapshot userSnapshot = task.getResult();
                if(userSnapshot.exists()) {
                    setElements(userSnapshot.getString("email"),
                    userSnapshot.getString("name"),
                    userSnapshot.getString("phoneNumber"));
                } else {
                    Log.e(TAG, "No such document");
                }
            } else {
                Log.e(TAG, "Task failed", task.getException());
            }
        });
    }

    private void resetPassword(final String email) {
        mAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                    Toast.makeText(getActivity(), "Reset email instructions sent to " + email, Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(getActivity(), email + " does not exist", Toast.LENGTH_LONG).show();
                }
            });
    }

    private void setElements(String emailValue, String nameValue, String phoneNumberValue) {
        // If Firebase auth is used
        if (isFirebase()) {
            // Set the email element value from the user document
            email.setText(emailValue);
            // Enable the element
            email.setEnabled(true);

            // Make the rest password label and button visible
            resetPassword.setVisibility(View.VISIBLE);

            // If Google or Facebook auth is used
        } else {
            // Set the email element value to "This is managed by your provider"
            email.setText(R.string.managed_by_provider);
            // Disable the element
            email.setEnabled(false);
            // Make the rest password label and button invisible
            resetPassword.setVisibility(View.GONE);
        }
        // Set the interface element values from the user document
        // If the value is "Prefer not to say" the text field is filled as blank
        name.setText(nameValue);
        mobile.setText(phoneNumberValue);
    }

    private boolean isFirebase() {
        boolean isFirebase = true;
        for (UserInfo userInfo : Objects.requireNonNull(mAuth.getCurrentUser()).getProviderData()) {
            if (userInfo.getProviderId().equals("facebook.com") || userInfo.getProviderId().equals("google.com")) {
                isFirebase = false;
            }
        }
        return isFirebase;
      }

    private void setUpResetPassword(View personalInformationView, AlertDialog.Builder builder) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        Button resetPassword = personalInformationView.findViewById(R.id.reset_password);

        if (!isFirebase()) {
            resetPassword.setVisibility(View.GONE);
        } else {

            // Make alert dialogue cancellable
            builder.setCancelable(true);

            // Set alert dialogue title
            builder.setTitle(R.string.reset_password);

            // Set alert dialogue message
            builder.setMessage(R.string.reset_password_text);

            // Set alert dialogue confirmation behaviour
            builder.setPositiveButton(R.string.yes,
                    // Attempt to delete the current user
                    (dialog, which) ->
                    {
                        assert currentUser != null;
                        resetPassword(currentUser.getEmail());
                    });
            // Set alert dialogue cancellation behaviour
            builder.setNegativeButton(android.R.string.no, (dialog, which) -> {
            });
        }
    }


        private void updatePersonalInformation(View personalInformationView){
        FirebaseUser currentUser = mAuth.getCurrentUser();

        EditText email = personalInformationView.findViewById(R.id.email);
        EditText name = personalInformationView.findViewById(R.id.name);
        EditText mobile = personalInformationView.findViewById(R.id.mobile);

            // Get the document references
        DocumentReference documentReference = db
                .collection("users")
                .document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid());

        // Run a Firestore transaction to update the document
        db.runTransaction((Transaction.Function<Void>) transaction -> {
            // Get the document snapshot from the document references
            // Get the updated user values

            String updateEmail = email.getText().toString();
            String updateName = name.getText().toString();
            String updatePhoneNumber = mobile.getText().toString();

            // Update the document
            transaction.update(documentReference, "name", updateName);
            transaction.update(documentReference, "phoneNumber", updatePhoneNumber);
            assert currentUser != null;
            currentUser.updateEmail(updateEmail);

            // End the transaction
            return null;

            // If the transaction is successful
        }).addOnSuccessListener(v -> {
            // Log a message
            Log.d(TAG, "Transaction success!");
            // Display "Details saved." to the user
            Toast.makeText(getActivity(), "Details saved.", Toast.LENGTH_SHORT).show();
            // If the transaction fails
        }).addOnFailureListener(e ->
                // Log an error
                Log.w(TAG, "Transaction failure.", e));
    }
}