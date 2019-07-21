package com.trukk.models;

public class Vehicle {
    private int id, status;
    private String vehicle_name;

    public int getId() {
        return id;
    }

    public int getStatus() {
        return status;
    }

    public String getVehicle_name() {
        return vehicle_name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setVehicle_name(String vehicle_name) {
        this.vehicle_name = vehicle_name;
    }
}
