package com.trukk.models;

public class Pivot {

    private int shipment_id;
    private int driver_id;
    private String bid_amount;
    private String shipment_amount;
    private int status;
    private String description;
    private String created_at;

    public String getDescription() {
        return description;
    }

    public int getShipment_id() {
        return shipment_id;
    }

    public int getDriver_id() {
        return driver_id;
    }

    public String getBid_amount() {
        return bid_amount;
    }

    public String getCreated_at() {
        return created_at;
    }

    public String getShipment_amount() {
        return shipment_amount;
    }

    public int getStatus() {
        return status;
    }

    public void setShipment_id(int shipment_id) {
        this.shipment_id = shipment_id;
    }

    public void setDriver_id(int driver_id) {
        this.driver_id = driver_id;
    }

    public void setBid_amount(String bid_amount) {
        this.bid_amount = bid_amount;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public void setShipment_amount(String shipment_amount) {
        this.shipment_amount = shipment_amount;
    }

}
