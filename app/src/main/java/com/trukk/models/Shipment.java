package com.trukk.models;

public class Shipment {

    //Fields
    private int shipment_id;
    private int vehicle_type_id;
    private String drop_off_lat;
    private String drop_off_long;
    private String drop_off_address;
    private String shipment_type;
    private String description;
    private String packaging;
    private String packaging_dimensions;
    private String pricing_type;

    private String driverName;
    private String driverPhone;
    private String status;
    public void setShipment_id(int shipment_id) {
        this.shipment_id = shipment_id;
    }

    //Setters
    public void setVehicle_type_id(int vehicle_type_id) {
        this.vehicle_type_id = vehicle_type_id;
    }

    public void setDrop_off_lat(String drop_off_lat) {
        this.drop_off_lat = drop_off_lat;
    }

    public void setDrop_off_long(String drop_off_long) {
        this.drop_off_long = drop_off_long;
    }

    public void setDrop_off_address(String drop_off_address) {
        this.drop_off_address = drop_off_address;
    }

    public void setShipment_type(String shipment_type) {
        this.shipment_type = shipment_type;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPackaging(String packaging) {
        this.packaging = packaging;
    }

    public void setPackaging_dimensions(String packaging_dimensions) {
        this.packaging_dimensions = packaging_dimensions;
    }

    public void setPricing_type(String pricing_type) {
        this.pricing_type = pricing_type;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public void setDriverPhone(String driverPhone) {
        this.driverPhone = driverPhone;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    //getters
    public int getShipment_id() {
        return shipment_id;
    }

    public int getVehicle_type_id() {
        return vehicle_type_id;
    }

    public String getDrop_off_lat() {
        return drop_off_lat;
    }

    public String getDrop_off_long() {
        return drop_off_long;
    }

    public String getDrop_off_address() {
        return drop_off_address;
    }

    public String getShipment_type() {
        return shipment_type;
    }

    public String getDescription() {
        return description;
    }

    public String getPackaging() {
        return packaging;
    }

    public String getPackaging_dimensions() {
        return packaging_dimensions;
    }

    public String getPricing_type() {
        return pricing_type;
    }

    public String getDriverName() {
        return driverName;
    }

    public String getDriverPhone() {
        return driverPhone;
    }

    public String getStatus() {
        return status;
    }


}
