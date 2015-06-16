package com.ocr.termocel.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Sensors
 */
@Table(name = "Sensors")
public class Sensor extends Model {

    @Column
    public String telephoneNumber;

    @Column
    public String name;

    @Column
    public String lastState;

    public Sensor(String telephoneNumber, String name, String lastState) {
        this.telephoneNumber = telephoneNumber;
        this.name = name;
        this.lastState = lastState;
    }

    public Sensor() {
        super();
    }
}
