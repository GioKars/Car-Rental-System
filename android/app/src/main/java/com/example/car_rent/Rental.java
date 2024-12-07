package com.example.car_rent;

import java.util.Date;

public class Rental {
    private String userId;
    private String carModel;
    private Car car;
    private Date startDate;
    private Date endDate;

    private double totalPrice;

    public Rental() {
        // Required for Firestore
    }

    public Rental(String userId, Car car, Date startDate, Date endDate, double totalPrice) {
        this.userId = userId;
        this.car = car;
        this.startDate = startDate;
        this.endDate = endDate;
        this.carModel = car.getModel();
        this.totalPrice = totalPrice;

    }

    public String getCarModel() {
        return carModel;
    }
    public String getUserId() {
        return userId;
    }

    public Car getCar() {
        return car;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }
}
