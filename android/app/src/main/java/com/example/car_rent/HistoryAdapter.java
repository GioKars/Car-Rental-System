package com.example.car_rent;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;


public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
    private List<Rental> rentalHistoryList;

    public HistoryAdapter(List<Rental> rentalHistoryList) {
        this.rentalHistoryList = rentalHistoryList;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        Rental rental = rentalHistoryList.get(position);
        holder.bind(rental);
    }

    @Override
    public int getItemCount() {
        return rentalHistoryList.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        private ImageView carImage;
        private TextView carModel, startDate, endDate, totalPrice;

        public HistoryViewHolder(View itemView) {
            super(itemView);

            carImage = itemView.findViewById(R.id.carImageHistory);
            carModel = itemView.findViewById(R.id.carModelHistory);
            startDate = itemView.findViewById(R.id.startDateHistory);
            endDate = itemView.findViewById(R.id.endDateHistory);
            totalPrice = itemView.findViewById(R.id.totalPriceHistory);
        }

        public void bind(Rental rental) {
            // Set car model
            carModel.setText(rental.getCar().getModel());

            // Format and set start and end dates
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
            String formattedStartDate = dateFormat.format(rental.getStartDate());
            String formattedEndDate = dateFormat.format(rental.getEndDate());
            startDate.setText("Start: " + formattedStartDate);
            endDate.setText("End: " + formattedEndDate);

            // Calculate total price
            long diffInMillis = rental.getEndDate().getTime() - rental.getStartDate().getTime();
            int days = (int) (diffInMillis / (1000 * 60 * 60 * 24)) + 1; // Add 1 for inclusive rental period
            double totalPriceValue = days * rental.getCar().getPrice();

            // Display total price
            totalPrice.setText("Total: $" + totalPriceValue + " for " + days + " days");

            // Load car image
            String carImageUrl = rental.getCar().getImageUrl();
            Glide.with(itemView.getContext())
                    .load(carImageUrl)
//                    .placeholder(R.drawable.placeholder) // Optional placeholder image
//                    .error(R.drawable.error_image)       // Optional error image
                    .into(carImage);
        }

    }
}
