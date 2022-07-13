package com.example.valetudo;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    // Google Sign in variables
    SignInButton loginGoogle;
    private static final String TAG = "LOG";
    private FirebaseAuth mAuth;
    private static final int REQ_ONE_TAP = 2;  // Can be any integer unique to the Activity.
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;
    public FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Facebook Sign in variables
    LoginButton loginFacebook;
    private static final String EMAIL = "email";
    CallbackManager callbackManager = CallbackManager.Factory.create();


    // Email Sign in variables
    Button login;
    Button createAccount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);

        // Google sign in initialisation
        loginGoogle = findViewById(R.id.login_google);
        oneTapClient = Identity.getSignInClient(LoginActivity.this);

        // Facebook sign in initialisation
        callbackManager = CallbackManager.Factory.create();
        loginFacebook = findViewById(R.id.login_facebook);
        loginFacebook.setPermissions(Collections.singletonList(EMAIL));
        loginFacebook.setPermissions("email", "public_profile");

        // Email sign in initialisation
        login = findViewById(R.id.login_email);
        createAccount = findViewById(R.id.logout);

        mAuth = FirebaseAuth.getInstance();

        setGooglePlusButtonText(loginGoogle);

        // Google sign in setup
        signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        // Your server's client ID, not your Android client ID.
                        .setServerClientId(getString(R.string.webapp_clientid))
                        // Only show accounts previously used to sign in.
                        .setFilterByAuthorizedAccounts(false)
                        .build())
                .build();


        // Facebook sign in setup
        loginFacebook.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            //If the login is correct, pass the login token to handleFacebookAccessToken
            // (Dealt with by Firebase)
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            // If the login is cancelled do nothing
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
            }

            @Override
            // If there is an error with the login, display an error message
            public void onError(@NonNull FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                Toast.makeText(LoginActivity.this, "Sorry, an error occurred. Please try again.",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Facebook sign in button
        loginFacebook.setOnClickListener(v -> LoginManager.getInstance()
                    .logInWithReadPermissions(LoginActivity.this, Collections.singletonList("public_profile")));

        // Google sign in button
        // First, begin the sign in process with the One Tap client
        loginGoogle.setOnClickListener(v -> oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener(LoginActivity.this, result -> {
                // If the one tap pop-up appears successfully
                try {
                    startIntentSenderForResult(
                            // Move to onActivity Result for Google log in
                            result.getPendingIntent().getIntentSender(), REQ_ONE_TAP,
                            null, 0, 0, 0);
                } catch (IntentSender.SendIntentException e) {
                    Log.e(TAG, "Couldn't start One Tap UI: " + e.getLocalizedMessage());
                    Toast.makeText(LoginActivity.this, "Sorry, an error occurred. Please try again.",
                            Toast.LENGTH_SHORT).show();
                }
            })
                // If it fails
            .addOnFailureListener(LoginActivity.this, e -> {
                // No saved credentials found. Launch the One Tap sign-up flow, or
                // do nothing and continue presenting the signed-out UI.
                Log.d(TAG, e.getLocalizedMessage());
                Toast.makeText(LoginActivity.this, "Sorry, an error occurred. Please try again.",
                        Toast.LENGTH_SHORT).show();
            }));

        // If the user presses the Create account button
        createAccount.setOnClickListener(v -> {
            // Bring them to the create account page
            Intent intent = new Intent(LoginActivity.this, CreateAccountActivity.class);
            startActivity(intent);
        });

        // If the user press log in with email and password button
        login.setOnClickListener(v -> {
            // Bring them to the email log in page
            Intent intent = new Intent(LoginActivity.this, EmailLoginActivity.class);
            startActivity(intent);
        });
    }

    // Function to handle Facebook log in
    private void handleFacebookAccessToken(AccessToken token) {
        // Get the token from Facebook setup function
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        // Get a credential from the token to access the user's Facebook page
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        // Use Firebase to sign in with their credentials
        mAuth.signInWithCredential(credential)
                // If the sign in is successful
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");
                        insertUserIntoFirestore();
                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        startActivity(intent);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    protected void setGooglePlusButtonText(SignInButton signInButton) {
        // Find the TextView that is inside of the SignInButton and set its text
        for (int i = 0; i < signInButton.getChildCount(); i++) {
            View v = signInButton.getChildAt(i);

            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setText(R.string.login_google);
                return;
            }
        }
    }

    @Override
    // Google log in function
    // Facebook log in function
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

        // If one tap has appeared
        if (requestCode == REQ_ONE_TAP) {
            try {
                // Attempt to grab the credentials from Google after has user has signed in
                SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(data);
                String idToken = credential.getGoogleIdToken();
                String password = credential.getPassword();

                // If the id token generated from the log in isn't null
                if (idToken != null) {
                    // Use Firebase to get a token from the user's Google profile
                    AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
                    // Sign in with Google, using Firebase
                    mAuth.signInWithCredential(firebaseCredential)
                            .addOnCompleteListener(this, task -> {
                                // If this is successful
                                if (task.isSuccessful()) {
                                    insertUserIntoFirestore();
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "signInWithCredential:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    assert user != null;
                                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                    startActivity(intent);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "signInWithCredential:failure", task.getException());
                                }
                            });
                } else if (password != null) {
                    // Got a saved username and password. Use them to authenticate
                    // with your backend.
                    Log.d(TAG, "Got password.");
                }
            } catch (ApiException e) {
                // ...
            }
        }
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
                String email = mAuth.getCurrentUser().getEmail();
                String name = mAuth.getCurrentUser().getDisplayName();

                // Create a new user with a first and last name
                Map<String, Object> user = new HashMap<>();
                user.put("email", email);
                user.put("name", name);
                user.put("phoneNumber", null);
                user.put("userId", mAuth.getCurrentUser().getUid());
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
        LoginManager.getInstance().logOut();
        FirebaseAuth.getInstance().signOut();

    }
}