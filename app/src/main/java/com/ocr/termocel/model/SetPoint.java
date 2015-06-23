package com.ocr.termocel.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Temperatures
 */
@Table(name = "SetPoints")
public class SetPoint extends Model {

    @Column
    public String phoneNumber;

    @Column
    public String micrologId;

    @Column
    public int setPointNumber;

    @Column
    public double tempInFahrenheit;

    @Column
    public long timestamp;

    @Column
    public boolean verified;

    public SetPoint(String phoneNumber, String micrologId, int setPointNumber, double tempInFahrenheit, long timestamp, boolean verified) {
        this.phoneNumber = phoneNumber;
        this.micrologId = micrologId;
        this.setPointNumber = setPointNumber;
        this.tempInFahrenheit = tempInFahrenheit;
        this.timestamp = timestamp;
        this.verified = verified;
    }

    public SetPoint() {
        super();
    }
}
