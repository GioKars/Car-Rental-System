package com.example.car_rent;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class CarAdapter extends RecyclerView.Adapter<CarAdapter.CarViewHolder> {

    private List<Car> carList;
    private OnCarClickListener listener;
    private boolean isClickable = true; // Default to true
    private boolean userIsBanned = false;
    private boolean userIsApproved = false;

    public interface OnCarClickListener {
        void onCarClick(Car car);
    }

    public CarAdapter(List<Car> carList, OnCarClickListener listener) {
        this.carList = carList;
        this.listener = listener;
    }

    // Set user status and update clickability
    public void setUserStatus(boolean isBanned, boolean isApproved) {
        this.userIsBanned = isBanned;
        this.userIsApproved = isApproved;
        this.isClickable = !userIsBanned && userIsApproved; // Clickable only if not banned and approved
        notifyDataSetChanged(); // Refresh the UI
    }

    @NonNull
    @Override
    public CarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.car_item, parent, false);
        return new CarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarViewHolder holder, int position) {
        Car car = carList.get(position);
        holder.bind(car);

        holder.itemView.setOnClickListener(v -> {
            if (isClickable) {
                listener.onCarClick(car);
            } else {
                String reason = getAccessDeniedReason(v.getContext());
                Toast.makeText(v.getContext(), reason, Toast.LENGTH_LONG).show();
            }
        });

        // Visually indicate unclickable items
        holder.itemView.setAlpha(isClickable ? 1.0f : 0.5f);
    }

    @Override
    public int getItemCount() {
        return carList.size();
    }

    public class CarViewHolder extends RecyclerView.ViewHolder {
        private ImageView carImage;
        private TextView carModel, carDescription, carYear, carSeats, carTransmission, carFuelType, carDailyPrice;

        public CarViewHolder(View itemView) {
            super(itemView);
            carImage = itemView.findViewById(R.id.carImage);
            carModel = itemView.findViewById(R.id.carModel);
            carDescription = itemView.findViewById(R.id.carDescription);
            carYear = itemView.findViewById(R.id.carYear);
            carSeats = itemView.findViewById(R.id.carSeats);
            carTransmission = itemView.findViewById(R.id.carTransmission);
            carFuelType = itemView.findViewById(R.id.carFuelType);
            carDailyPrice = itemView.findViewById(R.id.carDailyPrice);
        }

        public void bind(Car car) {
            carModel.setText(car.getModel());
            carDescription.setText(car.getDescription());
            carYear.setText("Year: " + car.getYear());
            carSeats.setText("Seats: " + car.getSeats());
            carTransmission.setText("Transmission: " + car.getTransmission());
            carFuelType.setText("Fuel Type: " + car.getFuelType());
            carDailyPrice.setText("Price: $" + car.getPrice() + " / day");

            Picasso.get().load(car.getImageUrl()).into(carImage);
        }
    }

    private String getAccessDeniedReason(Context context) {
        if (userIsBanned) {
            return "Access denied. Your account is banned. Contact support.";
        } else if (!userIsApproved) {
            return "Access denied. Your account is pending approval.";
        } else {
            return "Access denied. Please contact support.";
        }
    }
}
