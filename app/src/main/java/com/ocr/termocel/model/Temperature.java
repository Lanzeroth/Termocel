package com.ocr.termocel.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Temperatures
 */
@Table(name = "Temperatures")
public class Temperature extends Model {

    @Column
    public String number;

    @Column
    public double tempInFahrenheit;


    public Temperature(String number, double tempInFahrenheit) {
        this.number = number;
        this.tempInFahrenheit = tempInFahrenheit;
    }

    public Temperature() {
        super();
    }
}
