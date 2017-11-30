package com.ocr.termocel.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Micrologs
 */
@Table(name = "Micrologs")
public class Microlog extends Model {

    @Column
    public String sensorPhoneNumber;

    @Column
    public String sensorId;

    @Column
    public String name;

    @Column
    public String lastState;

    @Column
    public int numberOfPhonesToReport;

    @Column
    public double latitude;

    @Column
    public double longitude;

    public Microlog(String sensorPhoneNumber, String sensorId, String name, String lastState, int numberOfPhonesToReport) {
        this.sensorPhoneNumber = sensorPhoneNumber;
        this.sensorId = sensorId;
        this.name = name;
        this.lastState = lastState;
        this.numberOfPhonesToReport = numberOfPhonesToReport;
    }

    public Microlog() {
        super();
    }

    public String getSensorPhoneNumber() {
        return sensorPhoneNumber;
    }

    public void setSensorPhoneNumber(String sensorPhoneNumber) {
        this.sensorPhoneNumber = sensorPhoneNumber;
    }

    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastState() {
        return lastState;
    }

    public void setLastState(String lastState) {
        this.lastState = lastState;
    }

    public int getNumberOfPhonesToReport() {
        return numberOfPhonesToReport;
    }

    public void setNumberOfPhonesToReport(int numberOfPhonesToReport) {
        this.numberOfPhonesToReport = numberOfPhonesToReport;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
