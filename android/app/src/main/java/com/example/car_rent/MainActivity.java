package com.example.car_rent;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.widget.PopupMenu;

import android.widget.ArrayAdapter;
import androidx.appcompat.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CarAdapter.OnCarClickListener {

    private FirebaseAuth mAuth;
    private Button registerLoginButton ;
    private ImageView userIcon;
    private RecyclerView recyclerViewCars;
    private CarAdapter carAdapter;
    private List<Car> carList = new ArrayList<>();
    private List<Car> filteredCarList = new ArrayList<>();
    private FirebaseFirestore db;
    private Spinner carFilterSpinner;
    private SearchView carSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerViewCars = findViewById(R.id.recyclerViewCars);
        carFilterSpinner = findViewById(R.id.carFilterSpinner);
        carSearchView = findViewById(R.id.carSearchView);

        mAuth = FirebaseAuth.getInstance();

        registerLoginButton = findViewById(R.id.loginRegisterButton);
        userIcon = findViewById(R.id.userIcon);

        recyclerViewCars.setLayoutManager(new LinearLayoutManager(this));
        db = FirebaseFirestore.getInstance();

        // Check if the user is logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User is logged in, show user icon
            setupLoggedInUI();
        } else {
            // User is not logged in, show Register/Login button
            setupLoggedOutUI();
        }

        setupSpinner();
        setupSearchView();

        fetchCarData();
    }

    private void setupLoggedOutUI() {
        // Show the Register/Login button
        registerLoginButton.setVisibility(View.VISIBLE);
        userIcon.setVisibility(View.GONE);

        // Set click listener to navigate to LoginActivity
        registerLoginButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void setupLoggedInUI() {
        // Hide the Register/Login button and show user icon
        registerLoginButton.setVisibility(View.GONE);
        userIcon.setVisibility(View.VISIBLE);

        checkUserStatus(); // check user status

        // Set up a click listener for the user icon to open the popup menu
        userIcon.setOnClickListener(v -> showUserMenu(v));
    }

    private void checkUserStatus() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            db.collection("Users").document(userId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        boolean isBanned = Boolean.TRUE.equals(document.getBoolean("banned"));
                        String status = document.getString("status");
                        boolean isApproved = "approved".equalsIgnoreCase(status);

                        if (isBanned) {
                            // Redirect to the banned account screen
                            Intent intent = new Intent(MainActivity.this, BanActivity.class);
                            startActivity(intent);
                            finish(); // Close the MainActivity so user can't return back
                        } else if (!isApproved) {
                            Toast.makeText(MainActivity.this, "Your account is pending approval.", Toast.LENGTH_LONG).show();
                        }

                        carAdapter.setUserStatus(isBanned, isApproved);
                        recyclerViewCars.setAdapter(carAdapter);
                    } else {
                        Toast.makeText(MainActivity.this, "User details not found.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Error fetching user status.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setupSpinner() {
        // Define car categories
        String[] categories = {"All", "SUV", "Sedan", "Pickup"};

        // Create an adapter for the spinner
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item, // Layout for individual items
                categories // List of categories
        );

        // Set the dropdown view style for the spinner
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set the adapter to the spinner
        carFilterSpinner.setAdapter(spinnerAdapter);

        // Set an item selected listener for the spinner
        carFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedCategory = parentView.getItemAtPosition(position).toString();
                filterCars(selectedCategory, carSearchView.getQuery().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing when no item is selected
            }
        });
    }
    private void setupSearchView() {
        // Set the query text listener for the SearchView
        carSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Perform filtering when search text is submitted
                filterCars(carFilterSpinner.getSelectedItem().toString(), query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Perform filtering when the query text is changed
                filterCars(carFilterSpinner.getSelectedItem().toString(), newText);
                return true;
            }
        });
    }
    private void showUserMenu(View view) {
        // Create a PopupMenu that will be shown when the user icon is clicked
        PopupMenu popupMenu = new PopupMenu(this, view);

        // Inflate the menu from the menu resource file
        popupMenu.getMenuInflater().inflate(R.menu.user_menu, popupMenu.getMenu());

        // Set the menu item click listener
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_dashboard:
                    // Navigate to the Dashboard activity
                    startActivity(new Intent(MainActivity.this, DashboardActivity.class));
                    return true;
                case R.id.menu_logout:
                    // Log out the user
                    mAuth.signOut();
                    Toast.makeText(MainActivity.this, "Logged out", Toast.LENGTH_SHORT).show();

                    // Update the UI to show Register/Login button
                    setupLoggedOutUI();
                    return true;
                default:
                    return false;
            }
        });

        // Show the popup menu
        popupMenu.show();
    }


    private void fetchCarData() {
        db.collection("cars")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        carList.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            Car car = document.toObject(Car.class);
                            if (car != null) {
                                carList.add(car);
                            }
                        }
                        filteredCarList.addAll(carList);
                        carAdapter = new CarAdapter(filteredCarList, this); // `this` refers to MainActivity as OnCarClickListener
                        recyclerViewCars.setAdapter(carAdapter);

                        if (carList.isEmpty()) {
                            Toast.makeText(MainActivity.this, "No cars available.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Unable to fetch car data. Please check your connection.", Toast.LENGTH_LONG).show();
                    }
                });
    }


    private void filterCars(String category, String query) {
        filteredCarList.clear();

        for (Car car : carList) {
            boolean matchesCategory = category.equals("All") || car.getType().equalsIgnoreCase(category);

            // Ensure car.getName() is not null before performing any operations on it
            boolean matchesQuery = TextUtils.isEmpty(query) || (car.getModel() != null && car.getModel().toLowerCase().contains(query.toLowerCase()));

            if (matchesCategory && matchesQuery) {
                filteredCarList.add(car);
            }
        }

        // Only notify the adapter if it has been initialized
        if (carAdapter != null) {
            carAdapter.notifyDataSetChanged();
        } else {
            // If the adapter is not initialized, initialize it
            carAdapter = new CarAdapter(filteredCarList, this);
            recyclerViewCars.setAdapter(carAdapter);
        }
    }

    @Override
    public void onCarClick(Car car) {
        // Handle car click: Navigate to a rental details screen
        Intent intent = new Intent(this, RentalDetailsActivity.class);
        intent.putExtra("car", car); // Assuming Car is Serializable or Parcelable
        startActivity(intent);
    }

    // Method to open email client with predefined support email
    private void sendSupportEmail() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:"));  // Only email apps should handle this intent
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"support@yourcompany.com"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Support Request");

        // Check if an email client is available
        if (emailIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(Intent.createChooser(emailIntent, "Contact Support"));
        } else {
            // If no email app is available, you can show a Toast or open a webpage.
            // For example, opening a support webpage in a browser:
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.yourcompany.com/support"));
            startActivity(browserIntent);
        }
    }
}
