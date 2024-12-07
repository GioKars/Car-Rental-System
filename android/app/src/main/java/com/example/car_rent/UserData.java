package com.example.car_rent;

public class UserData {
    private String name;
    private String surname;
    private String phone;
    private String email;
    private String driverLicenseFrontUri;
    private String driverLicenseBackUri;
    private String idFrontUri;
    private String idBackUri;

    // Required empty constructor for Firebase
    public UserData() {}

//    // Full constructor
//    public UserData(String name, String surname, String phone, String email,
//                    String driverLicenseFrontUri, String driverLicenseBackUri,
//                    String idFrontUri, String idBackUri) {
//        this.name = name;
//        this.surname = surname;
//        this.phone = phone;
//        this.email = email;
//        this.driverLicenseFrontUri = driverLicenseFrontUri;
//        this.driverLicenseBackUri = driverLicenseBackUri;
//        this.idFrontUri = idFrontUri;
//        this.idBackUri = idBackUri;
//    }
    // Constructor with parameters
    public UserData(String name, String surname, String phone, String email) {
        this.name = name;
        this.surname = surname;
        this.phone = phone;
        this.email = email;
    }

    // Getters and setters for each field
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDriverLicenseFrontUri() {
        return driverLicenseFrontUri;
    }

    public void setDriverLicenseFrontUri(String driverLicenseFrontUri) {
        this.driverLicenseFrontUri = driverLicenseFrontUri;
    }

    public String getDriverLicenseBackUri() {
        return driverLicenseBackUri;
    }

    public void setDriverLicenseBackUri(String driverLicenseBackUri) {
        this.driverLicenseBackUri = driverLicenseBackUri;
    }

    public String getIdFrontUri() {
        return idFrontUri;
    }

    public void setIdFrontUri(String idFrontUri) {
        this.idFrontUri = idFrontUri;
    }

    public String getIdBackUri() {
        return idBackUri;
    }

    public void setIdBackUri(String idBackUri) {
        this.idBackUri = idBackUri;
    }
}
