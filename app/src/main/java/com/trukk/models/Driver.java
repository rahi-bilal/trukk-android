package com.trukk.models;

public class Driver {
    private int driverId;
    private String mobile;
    private String first_name;
    private String last_name;
    private String email;
    private int vehicle_type_id;
    private int admin_id;
    private String vehical_document;
    private String idcard;
    private String driving_licence;
    private String driver_image;
    private String account_status;
    private Pivot pivot;

    public int getDriverId() {
        return driverId;
    }

    public String getMobile() {
        return mobile;
    }

    public String getFirst_name() {
        return first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public String getEmail() {
        return email;
    }

    public int getVehicle_type_id() {
        return vehicle_type_id;
    }

    public int getAdmin_id() {
        return admin_id;
    }

    public String getVehical_document() {
        return vehical_document;
    }

    public String getIdcard() {
        return idcard;
    }

    public String getDriving_licence() {
        return driving_licence;
    }

    public String getDriver_image() {
        return driver_image;
    }

    public String getAccount_status() {
        return account_status;
    }

    public Pivot getPivot() {
        return pivot;
    }

    public void setDriverId(int driverId) {
        this.driverId = driverId;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setVehicle_type_id(int vehicle_type_id) {
        this.vehicle_type_id = vehicle_type_id;
    }

    public void setAdmin_id(int admin_id) {
        this.admin_id = admin_id;
    }

    public void setVehical_document(String vehical_document) {
        this.vehical_document = vehical_document;
    }

    public void setIdcard(String idcard) {
        this.idcard = idcard;
    }

    public void setDriving_licence(String driving_licence) {
        this.driving_licence = driving_licence;
    }

    public void setDriver_image(String driver_image) {
        this.driver_image = driver_image;
    }

    public void setAccount_status(String account_status) {
        this.account_status = account_status;
    }

    public void setPivot(Pivot pivot) {
        this.pivot = pivot;
    }
}
