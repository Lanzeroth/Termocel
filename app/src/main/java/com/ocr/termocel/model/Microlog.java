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

    public Microlog(String sensorPhoneNumber, String sensorId, String name, String lastState) {
        this.sensorPhoneNumber = sensorPhoneNumber;
        this.sensorId = sensorId;
        this.name = name;
        this.lastState = lastState;
    }

    public Microlog() {
        super();
    }
}
