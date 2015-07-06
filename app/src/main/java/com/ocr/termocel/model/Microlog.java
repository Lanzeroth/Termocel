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
}
