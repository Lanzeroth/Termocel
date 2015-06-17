package com.ocr.termocel.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.ocr.termocel.model.Temperature;

/**
 * Receiver
 */
public class MessageReceiver extends BroadcastReceiver {
    private final String TAG = MessageReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle pudsBundle = intent.getExtras();
        Object[] pdus = (Object[]) pudsBundle.get("pdus");
        SmsMessage messages = SmsMessage.createFromPdu((byte[]) pdus[0]);
        Log.i(TAG, messages.getMessageBody());
        if (messages.getMessageBody().contains("MICROLOG")) {
            formatMessage(messages);
            Log.i(TAG, "tried to abort");
            abortBroadcast();
        }
    }

    private void formatMessage(SmsMessage smsMessage) {
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

        Temperature temperature = new Temperature(
                smsMessage.getOriginatingAddress(),
                micrologId,
                stateString,
                Double.parseDouble(temperatureString),
                Double.parseDouble(relativeHumidityString),
                smsMessage.getTimestampMillis()
        );
        temperature.save();
    }
}
