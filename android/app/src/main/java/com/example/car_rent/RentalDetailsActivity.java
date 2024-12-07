package com.example.car_rent;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class RentalDetailsActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private TextView carModelTextView, carPriceTextView, selectedDatesTextView, totalPriceTextView;
    private Button selectDatesButton, confirmRentalButton;

    private Car car; // Car object passed from MainActivity
    private Calendar startDate, endDate;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rental_details);

        // Check if the user is logged in
//        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            // User not logged in, redirect to LoginActivity
            Toast.makeText(this, "Please log in to rent a car.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish(); // Close RentalDetailsActivity
            return;
        }

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Bind views
        carModelTextView = findViewById(R.id.carModelTextView);
        carPriceTextView = findViewById(R.id.carPriceTextView);
        selectedDatesTextView = findViewById(R.id.selectedDatesTextView);
        totalPriceTextView = findViewById(R.id.totalPriceTextView);
        selectDatesButton = findViewById(R.id.selectDatesButton);
        confirmRentalButton = findViewById(R.id.confirmRentalButton);

        // Get the Car object passed from MainActivity
        car = (Car) getIntent().getSerializableExtra("car");
        if (car != null) {
            carModelTextView.setText(car.getModel());
            carPriceTextView.setText("Price: $" + car.getPrice() + " / day");
        }

        // Set up date picker for selecting dates
        selectDatesButton.setOnClickListener(v -> fetchRentalHistoryAndOpenDatePicker());

        // Confirm rental and save booking details to Firestore
        confirmRentalButton.setOnClickListener(v -> confirmRental());
    }

    private void fetchRentalHistoryAndOpenDatePicker() {
        db.collection("rentals")
                .whereEqualTo("car.model", car.getModel()) // Query rentals for the specific car
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Rental> rentals = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        rentals.add(document.toObject(Rental.class));
                    }
                    openDatePicker(rentals); // Pass the rentals to the date picker
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch rental history", Toast.LENGTH_SHORT).show();
                    openDatePicker(new ArrayList<>()); // Open date picker with no restrictions
                });
    }

    private void openDatePicker(List<Rental> rentalHistory) {
        Calendar now = Calendar.getInstance(); // Current date

        // Clear previous date selections if user wants to reselect
        startDate = null;
        endDate = null;

        DatePickerDialog dpd = DatePickerDialog.newInstance(
                this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );

        dpd.setAccentColor(getResources().getColor(R.color.purple_500));
        dpd.setVersion(DatePickerDialog.Version.VERSION_2);
        dpd.setMinDate(now); // Disable all dates before today

        // Disable dates based on rental history (already rented dates)
        for (Rental rental : rentalHistory) {
            Calendar rentedStart = Calendar.getInstance();
            Calendar rentedEnd = Calendar.getInstance();
            rentedStart.setTime(rental.getStartDate());
            rentedEnd.setTime(rental.getEndDate());

            Calendar[] disabledDates = getDatesBetween(rentedStart, rentedEnd);
            dpd.setDisabledDays(disabledDates);
        }

        dpd.setOkText("Select Start Date");
        dpd.show(getSupportFragmentManager(), "StartDatePicker"); // Tag as "StartDatePicker"
    }


    private Calendar[] getDatesBetween(Calendar startDate, Calendar endDate) {
        List<Calendar> dates = new ArrayList<>();
        Calendar current = (Calendar) startDate.clone();

        while (!current.after(endDate)) {
            Calendar date = (Calendar) current.clone();
            dates.add(date);
            current.add(Calendar.DATE, 1);
        }

        return dates.toArray(new Calendar[0]);
    }
    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        if (view.getTag().equals("StartDatePicker")) {
            // User is selecting a start date
            startDate = Calendar.getInstance();
            startDate.set(year, monthOfYear, dayOfMonth);

            // Show the date picker for the end date
            DatePickerDialog endDatePicker = DatePickerDialog.newInstance(
                    this,
                    year, monthOfYear, dayOfMonth
            );
            endDatePicker.setAccentColor(getResources().getColor(R.color.purple_500));
            endDatePicker.setVersion(DatePickerDialog.Version.VERSION_2);
            endDatePicker.setMinDate(startDate); // End date cannot be before the start date
            endDatePicker.setOkText("Select End Date");
            endDatePicker.show(getSupportFragmentManager(), "EndDatePicker");

        } else if (view.getTag().equals("EndDatePicker")) {
            // User is selecting an end date
            endDate = Calendar.getInstance();
            endDate.set(year, monthOfYear, dayOfMonth);

            if (endDate.before(startDate)) {
                Toast.makeText(this, "End date cannot be before the start date!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Calculate total days and update the UI
            calculateAndUpdateTotalPrice();
        }
    }

    @SuppressLint("SetTextI18n")
    private void calculateAndUpdateTotalPrice() {
        if (startDate != null && endDate != null) {
            long diff = endDate.getTimeInMillis() - startDate.getTimeInMillis();
            int days = (int) (diff / (1000 * 60 * 60 * 24)) + 1;

            if (days <= 0) {
                Toast.makeText(this, "Invalid date range!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (days > 31) {
                Toast.makeText(this, "Rental period cannot exceed 31 days!", Toast.LENGTH_LONG).show();
                // Clear the date selection to force user to reselect dates
                startDate = null;
                endDate = null;
                selectedDatesTextView.setText("Please select a valid date range.");
                totalPriceTextView.setText("");
                return;
            }

            selectedDatesTextView.setText("Selected Dates: " +
                    new SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(startDate.getTime()) +
                    " to " +
                    new SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(endDate.getTime()));

            double totalPrice = days * car.getPrice();
            totalPriceTextView.setText("Total Price: $" + totalPrice + " for " + days + " days");
        }
    }

    private void confirmRental() {
        if (startDate == null || endDate == null) {
            Toast.makeText(this, "Please select rental dates.", Toast.LENGTH_SHORT).show();
            return;
        }

        long diff = endDate.getTimeInMillis() - startDate.getTimeInMillis();
        int days = (int) (diff / (1000 * 60 * 60 * 24)) + 1;

        if (days > 31) {
            Toast.makeText(this, "You cannot rent a car for more than 31 days.", Toast.LENGTH_LONG).show();
            return;
        }

        // Ensure the user is logged in
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "You must be logged in to rent a car.", Toast.LENGTH_SHORT).show();
            return;
        }

        double totalPrice = days * car.getPrice();

        // Save booking to Firestore
        String userId = auth.getCurrentUser().getUid();
        Rental rental = new Rental(userId, car, startDate.getTime(), endDate.getTime(), totalPrice);

        // Save the rental to Firestore
        db.collection("rentals")
                .add(rental)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(RentalDetailsActivity.this, "Rental saved successfully!", Toast.LENGTH_SHORT).show();

                    // Optionally, redirect to another activity (e.g., history or main activity)
                    Intent intent = new Intent(RentalDetailsActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(RentalDetailsActivity.this, "Failed to save rental: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
