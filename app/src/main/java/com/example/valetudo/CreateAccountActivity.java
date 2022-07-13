package com.example.valetudo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class CreateAccountActivity extends AppCompatActivity {
    // Create account screen sign in variables
    private FirebaseAuth mAuth;
    private EditText nameText;
    private EditText emailText;
    private EditText mobileNumberText;
    private EditText password;
    private final String TAG = "LOG";
    private FirebaseFirestore db;
    private CheckBox termsAndConditions;

    // Regular Expression for password
    public final Pattern TEXT_PATTERN =
            Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!£$%-+@#^&=_])(?=\\S+$).{8,}$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_account_screen);

        // Variable initialisation
        mAuth = FirebaseAuth.getInstance();
        Button createAccount = findViewById(R.id.back);
        nameText = findViewById(R.id.name);
        emailText = findViewById(R.id.email);
        password = findViewById(R.id.password);
        mobileNumberText = findViewById(R.id.mobile);
        db = FirebaseFirestore.getInstance();
        termsAndConditions = findViewById(R.id.terms);


        // When the user presses create account
        createAccount.setOnClickListener(view -> {
            // Check that all the fields have been filled in correctly
            boolean isValid = validateText(
                nameText,
                mobileNumberText,
                emailText,
                password) && validatePassword(password.getText().toString()) && validateEmail(emailText.getText().toString());

                // If it is valid
                if (isValid) {
                    // Check if the account exists or not
                    mAuth.fetchSignInMethodsForEmail(emailText.getText().toString()).addOnCompleteListener(task -> {
                        // If the account does not exist
                        if (Objects.requireNonNull(task.getResult().getSignInMethods()).size() == 0){
                            // Create a user
                            createUser(emailText.getText().toString(), password.getText().toString());
                            // If the account already exists
                        }else {
                            Toast.makeText(CreateAccountActivity.this, "Email already in use.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
           });
    }


    // Validate that the fields contain the correct information and are filled in
    private boolean validateText(EditText email, EditText name, EditText phoneNo, EditText password) {
        boolean validText =
                !email.getText().toString().trim().isEmpty()
                && !name.getText().toString().trim().isEmpty()
                && !phoneNo.getText().toString().trim().isEmpty()
                && !password.getText().toString().trim().isEmpty();
        if (!validText) {
            // If the fields are empty, tell the user to fill them in
            Toast.makeText(CreateAccountActivity.this, "Please complete all of the fields.",
                    Toast.LENGTH_SHORT).show();
        }
        return validText;
    }

    public boolean validateEmail(CharSequence target) {
        boolean validEmail = !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches();
        if(!validEmail){
            Toast.makeText(CreateAccountActivity.this, "Please enter a valid email.",
                    Toast.LENGTH_SHORT).show();
        } else {
            return validEmail;
        }

        return validEmail;
    }

    // Password validation
    private boolean validatePassword(String password) {
        // If the password is not secure
        boolean validPassword = TEXT_PATTERN.matcher(password).matches();
        if (!validPassword) {
            // Tell the user the password is not secure
            Toast.makeText(CreateAccountActivity.this, "Password is not secure.",
                    Toast.LENGTH_SHORT).show();
        }
        // Return the valid password
        return validPassword;
    }


    // Function to create a user
    private void createUser(String email, String password){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(CreateAccountActivity.this, task -> {
                    if (task.isSuccessful()) {
                        insertUserIntoFirestore();
                        Intent intent = new Intent(CreateAccountActivity.this, HomeActivity.class);
                        startActivity(intent);
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(CreateAccountActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void insertUserIntoFirestore() {
        // Attempt to get the document reference for the account
        DocumentReference documentReference = db.collection("users")
                .document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid());
        // Begin a transaction
        db.runTransaction((Transaction.Function<Void>) transaction -> {
            // Get the document snapshot from the document references
            DocumentSnapshot snapshot = transaction.get(documentReference);
            // If the snapshot does not already exist
            if (!snapshot.exists()) {
                // Get the user variables
                emailText = findViewById(R.id.email);
                String email = emailText.getText().toString();

                nameText = findViewById(R.id.name);
                String name = nameText.getText().toString();

                mobileNumberText = findViewById(R.id.mobile);
                String phoneNumber = mobileNumberText.getText().toString();
                // Create a new user with a first and last name
                Map<String, Object> user = new HashMap<>();
                user.put("userId", mAuth.getCurrentUser().getUid());
                user.put("email", email);
                user.put("name", name);
                user.put("phoneNumber", phoneNumber);
                // Add a new document with a generated ID
                db.collection("users")
                        .document(mAuth.getCurrentUser().getUid())
                        .set(user)
                        .addOnSuccessListener(aVoid -> Log.d(TAG,
                                "DocumentSnapshot successfully written!"))
                        .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
            }
            // End the transaction
            return null;
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseAuth.getInstance().signOut();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            currentUser.reload();
        }
    }
}