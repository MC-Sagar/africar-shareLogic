package com.mahanthesh.africar.model;

import java.io.Serializable;

public class JobRequest implements Serializable {

    private String id;
    private String OTP;
    private String accepted;
    private String acceptedTime;
    private String isVerified;
    private LocationInfo drop;
    private LocationInfo pickup;
    private String duration;
    private String distance;
    private double ride_cost;
    private String isCompleted;
    private String driverId;
    private String type;
    private int required_seats;

    public int getRequired_seats() {
        return required_seats;
    }

    public void setRequired_seats(int required_seats) {
        this.required_seats = required_seats;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOTP() {
        return OTP;
    }

    public void setOTP(String OTP) {
        this.OTP = OTP;
    }

    public String getAccepted() {
        return accepted;
    }

    public void setAccepted(String accepted) {
        this.accepted = accepted;
    }

    public String getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(String isVerified) {
        this.isVerified = isVerified;
    }

    public LocationInfo getDrop() {
        return drop;
    }

    public void setDrop(LocationInfo drop) {
        this.drop = drop;
    }

    public LocationInfo getPickup() {
        return pickup;
    }

    public void setPickup(LocationInfo pickup) {
        this.pickup = pickup;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public double getRide_cost() {
        return ride_cost;
    }

    public void setRide_cost(double ride_cost) {
        this.ride_cost = ride_cost;
    }

    public String getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(String isCompleted) {
        this.isCompleted = isCompleted;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getAcceptedTime() {
        return acceptedTime;
    }

    public void setAcceptedTime(String acceptedTime) {
        this.acceptedTime = acceptedTime;
    }
}
