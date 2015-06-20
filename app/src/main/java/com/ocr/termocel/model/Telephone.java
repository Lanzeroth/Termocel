package com.ocr.termocel.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Sensors
 */
@Table(name = "Telephones")
public class Telephone extends Model {

    @Column
    public String sensorNumber;

    @Column
    public int phoneIndex;

    @Column
    public String phoneNumber;

    @Column
    public long date;

    @Column
    public boolean verified;

    public Telephone(String sensorNumber, int phoneIndex, String phoneNumber, long date, boolean verified) {
        this.sensorNumber = sensorNumber;
        this.phoneIndex = phoneIndex;
        this.phoneNumber = phoneNumber;
        this.date = date;
        this.verified = verified;
    }

    public Telephone() {
        super();
    }
}
