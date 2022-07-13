package com.example.valetudo;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class EmailLoginActivity extends AppCompatActivity {

    // Email log in variables
    private FirebaseAuth mAuth;
    private TextView email;
    private TextView password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.email_login_screen);

        AlertDialog.Builder resetPasswordBuilder = new AlertDialog.Builder(EmailLoginActivity.this);

        setUpResetPassword(resetPasswordBuilder);

        // Email log in variable initialisation
        mAuth = FirebaseAuth.getInstance();
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        TextView createAccount = findViewById(R.id.create_account_text);
        Button login = findViewById(R.id.email_login);
        Button resetPassword = findViewById(R.id.reset_password);

        // When the user presses the log in button, pass it to Firebase to check that the information exists
        login.setOnClickListener(v -> mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
            .addOnCompleteListener(EmailLoginActivity.this, task -> {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Intent intent = new Intent(EmailLoginActivity.this, HomeActivity.class);
                    startActivity(intent);
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(EmailLoginActivity.this, "Your username or password is incorrect.",
                            Toast.LENGTH_SHORT).show();
                }
            }));

        createAccount.setOnClickListener(v -> {
            Intent intent = new Intent(EmailLoginActivity.this, CreateAccountActivity.class);
            startActivity(intent);
        });

        resetPassword.setOnClickListener(v ->  {
            AlertDialog dialog = resetPasswordBuilder.create();
            dialog.show();
        });
    }

    private void resetPassword(final String email) {
        mAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    startActivity(new Intent(EmailLoginActivity.this, LoginActivity.class));
                    Toast.makeText(EmailLoginActivity.this, "Reset email instructions sent to " + email, Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(EmailLoginActivity.this, email + " does not exist", Toast.LENGTH_LONG).show();
                }
            });
    }

    private void setUpResetPassword(AlertDialog.Builder builder) {

            // Make alert dialogue cancellable
            builder.setCancelable(true);

            // Set alert dialogue title
            builder.setTitle(R.string.reset_password);
            final EditText resetPasswordInput = new EditText(EmailLoginActivity.this);
            resetPasswordInput.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
            builder.setView(resetPasswordInput);

            // Set alert dialogue message
            builder.setMessage(R.string.reset_password_text);

            // Set up the buttons
            builder.setPositiveButton(R.string.yes, (dialog, which) -> {
                String resetEmail = resetPasswordInput.getText().toString();
                resetPassword(resetEmail);
            });
            builder.setNegativeButton(R.string.no, (dialog, which) -> dialog.cancel());
        }

    @Override
    public void onStart() {
        super.onStart();
    }
}