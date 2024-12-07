package com.example.car_rent;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerViewHistory;
    private HistoryAdapter historyAdapter;
    private List<Rental> rentalHistoryList = new ArrayList<>();
    private FirebaseFirestore db;
    private TextView noRentalsTextView; // TextView for the "no rentals" message

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        db = FirebaseFirestore.getInstance();

        recyclerViewHistory = findViewById(R.id.recyclerViewHistory);
        noRentalsTextView = findViewById(R.id.noRentalsTextView); // Initialize "no rentals" TextView

        // Initialize RecyclerView
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new HistoryAdapter(rentalHistoryList);
        recyclerViewHistory.setAdapter(historyAdapter);

        // Fetch rental history for the logged-in user
        fetchRentalHistory();
    }

    private void fetchRentalHistory() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("rentals")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    rentalHistoryList.clear();
                    rentalHistoryList.addAll(queryDocumentSnapshots.toObjects(Rental.class));

                    // Check if rental history is empty
                    if (rentalHistoryList.isEmpty()) {
                        // Show "no rentals" message
                        noRentalsTextView.setVisibility(View.VISIBLE);
                        recyclerViewHistory.setVisibility(View.GONE);
                    } else {
                        // Show the RecyclerView and hide "no rentals" message
                        noRentalsTextView.setVisibility(View.GONE);
                        recyclerViewHistory.setVisibility(View.VISIBLE);
                    }

                    historyAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(HistoryActivity.this, "Failed to load history.", Toast.LENGTH_SHORT).show()
                );
    }
}
