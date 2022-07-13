package com.example.valetudo;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "LOG";
    private FirebaseAuth mAuth;
    public FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(navListener);

        // as soon as the application opens the first
        // fragment should be shown to the user
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SearchFragment()).commit();
    }

    private final BottomNavigationView.OnItemSelectedListener  navListener =
            (BottomNavigationView.OnItemSelectedListener) item -> {
        // By using switch we can easily get the selected fragment by using its id.
        Fragment selectedFragment = null;
        switch (item.getItemId()) {
            case R.id.search:
                selectedFragment = new SearchFragment();
                break;
            case R.id.map:
                selectedFragment = new MapFragment();
                break;
            case R.id.saved:
                selectedFragment = new SavedFragment();
                break;
            case R.id.bookings:
                selectedFragment = new BookingsFragment();
                break;
            case R.id.account:
                selectedFragment = new AccountFragment();
                break;
        }
        // This will help replace one fragment to other.
                assert selectedFragment != null;
                getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, selectedFragment)
                .commit();
        return true;
    };
}
