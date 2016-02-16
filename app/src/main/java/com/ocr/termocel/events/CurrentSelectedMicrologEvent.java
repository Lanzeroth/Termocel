package com.ocr.termocel.events;

/**
 * Created by carlos on 2/16/16.
 */
public class CurrentSelectedMicrologEvent {

    public CurrentSelectedMicrologEvent(String phoneNumber, boolean isEmpty) {
        this.phoneNumber = phoneNumber;
        this.isEmpty = isEmpty;
    }

    public String getPhoneNumber() {
        return phoneNumber;

    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public void setEmpty(boolean empty) {
        isEmpty = empty;
    }

    String phoneNumber;
    boolean isEmpty;
}
