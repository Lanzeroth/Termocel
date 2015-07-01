package com.ocr.termocel.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.activeandroid.query.Select;
import com.ocr.termocel.MainActivity;
import com.ocr.termocel.SensorSelectorActivity;
import com.ocr.termocel.model.Microlog;
import com.ocr.termocel.model.Temperature;

/**
 * Receiver
 */
public class MessageReceiver extends BroadcastReceiver {
    private final String TAG = MessageReceiver.class.getSimpleName();

    public static final String EXTRA_PHONE_NUMBER = "EXTRA_PHONE_NUMBER";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle pudsBundle = intent.getExtras();
        Object[] pdus = (Object[]) pudsBundle.get("pdus");
        SmsMessage messages = SmsMessage.createFromPdu((byte[]) pdus[0]);
        Log.i(TAG, messages.getMessageBody());
        if (messages.getMessageBody().contains("MICROLOG")) {
            Log.i(TAG, "tried to abort");
            abortBroadcast();
            String phoneNumber = formatMessage(messages);
            startNewActivityOnTop(context, phoneNumber);
        }
    }

    private void startNewActivityOnTop(Context context, String phoneNumber) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(SensorSelectorActivity.EXTRA_COMES_FROM_RECEIVER, true);
        intent.putExtra(EXTRA_PHONE_NUMBER, phoneNumber);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private String formatMessage(SmsMessage smsMessage) {
        String sms = smsMessage.getMessageBody();
        String[] spitedMessage = sms.split(" ");
        String micrologId = spitedMessage[1].substring(1, 3);
        String stateTemp = spitedMessage[2].substring(0, 3);
        String stateString = "";
        if (stateTemp.equalsIgnoreCase("NOR")) {
            stateString = "Normal";
        } else if (stateTemp.equalsIgnoreCase("ATN")) {
            stateString = "Atencion";
        } else if (stateTemp.equalsIgnoreCase("ADV")) {
            stateString = "Advertencia";
        } else if (stateTemp.equalsIgnoreCase("ALA")) {
            stateString = "Alarma";
        }
        String temperatureString = spitedMessage[5].substring(0, 2);
        String relativeHumidityString = spitedMessage[8];
        Log.d(TAG, micrologId + " " + stateString + " " + temperatureString + " " + relativeHumidityString);

        String temporalAddress = smsMessage.getOriginatingAddress();
        if (temporalAddress.length() > 10) {
            temporalAddress = smsMessage.getOriginatingAddress().substring(3, 13);
        }

        saveTemperature(smsMessage, micrologId, stateString, temperatureString, relativeHumidityString, temporalAddress);

        saveMicrologID(temporalAddress, micrologId, stateString);

        return temporalAddress;
    }

    private void saveTemperature(SmsMessage smsMessage, String micrologId, String stateString, String temperatureString, String relativeHumidityString, String temporalAddress) {
        Temperature temperature = new Temperature(
                temporalAddress,
                micrologId,
                stateString,
                Double.parseDouble(temperatureString),
                Double.parseDouble(relativeHumidityString),
                smsMessage.getTimestampMillis()
        );
        temperature.save();
    }

    private void saveMicrologID(String temporalAddress, String micrologId, String stateString) {
        Microlog microlog = new Select().
                from(Microlog.class).
                where("sensorPhoneNumber = ?", temporalAddress).
                executeSingle();

        if (microlog != null) {
            microlog.sensorId = micrologId;
            microlog.lastState = stateString;
            microlog.save();
        }
    }
}
