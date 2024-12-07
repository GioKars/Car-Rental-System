package com.example.car_rent;
import java.io.Serializable;

public class Car implements Serializable {
    private String model;
    private String type; // e.g., "SUV", "Sedan"

    private String description;

    private String imageUrl;

    private int year;
    private int seats;
    private String transmission;
    private String fuelType;
    private double price;

    public Car() {
        // Required for Firestore or default initialization
    }

    public Car(String model, String type, String description, String imageUrl,int year, int seats, String transmission, String fuelType, double price) {
        this.model = model;
        this.type = type;
        this.description = description;
        this.imageUrl = imageUrl;
        this.year = year;
        this.seats = seats;
        this.transmission = transmission;
        this.fuelType = fuelType;
        this.price = price;
    }

    public String getModel() {
        return model;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getYear() {
        return year;
    }
    public void setYear(int year) {
        this.year = year;
    }


    public int getSeats() {
        return seats;
    }

    public String getTransmission() {
        return transmission;
    }

    public String getFuelType() {
        return fuelType;
    }

    public double getPrice() {
        return price;
    }
}
