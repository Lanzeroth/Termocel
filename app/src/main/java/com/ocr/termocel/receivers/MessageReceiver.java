package com.ocr.termocel.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.activeandroid.query.Select;
import com.ocr.termocel.MainActivity;
import com.ocr.termocel.SensorSelectorActivity;
import com.ocr.termocel.SetPointsActivity;
import com.ocr.termocel.TelephoneChangeActivity;
import com.ocr.termocel.model.Microlog;
import com.ocr.termocel.model.SetPoint;
import com.ocr.termocel.model.Telephone;
import com.ocr.termocel.model.Temperature;

/**
 * Receiver that listens to SMS and then formats the result and saves into our database
 */
public class MessageReceiver extends BroadcastReceiver {
    private final String TAG = MessageReceiver.class.getSimpleName();

    public static final String EXTRA_PHONE_NUMBER = "EXTRA_PHONE_NUMBER";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle pudsBundle = intent.getExtras();
        Object[] pdus = (Object[]) pudsBundle.get("pdus");
        SmsMessage messages = SmsMessage.createFromPdu((byte[]) pdus[0]);
//        Log.i(TAG, messages.getMessageBody());
        if (messages.getMessageBody().contains("MICROLOG")) {
//            Log.i(TAG, "tried to abort");
            abortBroadcast();
            String phoneNumber = formatMessage(messages);
            Intent newIntent = new Intent(context, MainActivity.class);
            startNewActivityOnTop(context, newIntent, phoneNumber);
        } else if (messages.getMessageBody().contains("01S?2")) {
//            Log.i(TAG, "SetPoints received");
            String phoneNumber = formatMessageSetPoints(messages);
            Intent newIntent = new Intent(context, SetPointsActivity.class);
            startNewActivityOnTop(context, newIntent, phoneNumber);
        } else if (messages.getMessageBody().contains("T2")) { // these are the immediate response when you change a phone
//            Log.i(TAG, "Telephone 1 Received");
            String phoneNumber = formatMessageTelephoneNumberSingle(messages, 4, 0);
            Intent newIntent = new Intent(context, TelephoneChangeActivity.class);
            startNewActivityOnTop(context, newIntent, phoneNumber);
        } else if (messages.getMessageBody().contains("T3")) {
//            Log.i(TAG, "Telephone 2 Received");
            String phoneNumber = formatMessageTelephoneNumberSingle(messages, 4, 1);
            Intent newIntent = new Intent(context, TelephoneChangeActivity.class);
            startNewActivityOnTop(context, newIntent, phoneNumber);
        } else if (messages.getMessageBody().contains("T4")) {
//            Log.i(TAG, "Telephone 3 Received");
            String phoneNumber = formatMessageTelephoneNumberSingle(messages, 4, 2);
            Intent newIntent = new Intent(context, TelephoneChangeActivity.class);
            startNewActivityOnTop(context, newIntent, phoneNumber);
        } else if (messages.getMessageBody().contains("T?2")) { // these are the normal consult phone response
//            Log.i(TAG, "Telephone 1 Received");
            String phoneNumber = formatMessageTelephoneNumberSingle(messages, 5, 0);
            Intent newIntent = new Intent(context, TelephoneChangeActivity.class);
            startNewActivityOnTop(context, newIntent, phoneNumber);
        } else if (messages.getMessageBody().contains("T?3")) {
//            Log.i(TAG, "Telephone 2 Received");
            String phoneNumber = formatMessageTelephoneNumberSingle(messages, 5, 1);
            Intent newIntent = new Intent(context, TelephoneChangeActivity.class);
            startNewActivityOnTop(context, newIntent, phoneNumber);
        } else if (messages.getMessageBody().contains("T?4")) {
//            Log.i(TAG, "Telephone 3 Received");
            String phoneNumber = formatMessageTelephoneNumberSingle(messages, 5, 2);
            Intent newIntent = new Intent(context, TelephoneChangeActivity.class);
            startNewActivityOnTop(context, newIntent, phoneNumber);
        } else if (messages.getMessageBody().contains("S#01")) {
//            Log.i(TAG, "report to 1 telephone");
            String phoneNumber = formatMessageNumberOfTelephonesToReport(messages, 1);
            Intent newIntent = new Intent(context, TelephoneChangeActivity.class);
            startNewActivityOnTop(context, newIntent, phoneNumber);
        } else if (messages.getMessageBody().contains("S#02")) {
//            Log.i(TAG, "report to 1 telephone");
            String phoneNumber = formatMessageNumberOfTelephonesToReport(messages, 2);
            Intent newIntent = new Intent(context, TelephoneChangeActivity.class);
            startNewActivityOnTop(context, newIntent, phoneNumber);
        } else if (messages.getMessageBody().contains("S#03")) {
//            Log.i(TAG, "report to 1 telephone");
            String phoneNumber = formatMessageNumberOfTelephonesToReport(messages, 3);
            Intent newIntent = new Intent(context, TelephoneChangeActivity.class);
            startNewActivityOnTop(context, newIntent, phoneNumber);
        } else if (messages.getMessageBody().contains("S?1")) {
//            Log.i(TAG, "report to 1 telephone");
            String phoneNumber = formatMessageNumberOfTelephonesToReportAll(messages);
            Intent newIntent = new Intent(context, TelephoneChangeActivity.class);
            startNewActivityOnTop(context, newIntent, phoneNumber);
        }
    }

    private void startNewActivityOnTop(Context context, Intent intent, String phoneNumber) {
        intent.putExtra(SensorSelectorActivity.EXTRA_COMES_FROM_RECEIVER, true);
        intent.putExtra(EXTRA_PHONE_NUMBER, phoneNumber);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * Formats an status message and saves it into the database
     *
     * @param smsMessage unformatted message
     * @return phoneNumber from where the messages comes
     */
    private String formatMessage(SmsMessage smsMessage) {
        String sms = smsMessage.getMessageBody();
        String[] spitedMessage = sms.split(" ");
        String temporalAddress = null;
        try {
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
//            Log.d(TAG, micrologId + " " + stateString + " " + temperatureString + " " + relativeHumidityString);

            temporalAddress = smsMessage.getOriginatingAddress();
            if (temporalAddress.length() > 10) {
                temporalAddress = smsMessage.getOriginatingAddress().substring(3, 13);
            }

            saveTemperature(smsMessage, micrologId, stateString, temperatureString, relativeHumidityString, temporalAddress);

            saveMicrologID(temporalAddress, micrologId, stateString);
        } catch (Exception e) {
            e.printStackTrace();
//            Log.e(TAG, "sms must not be well formatted");
        }

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

    /**
     * Formats an setPoints special sms message that contains all the temperatures
     *
     * @param smsMessage unformatted sms message
     * @return phone number from the sms
     */
    private String formatMessageSetPoints(SmsMessage smsMessage) {
        String sms = smsMessage.getMessageBody();
        String temporalAddress = null;
        Double[] setPoints = new Double[3];
        try {
            setPoints[0] = (double) Integer.parseInt(sms.substring(5, 9), 16);
            setPoints[1] = (double) Integer.parseInt(sms.substring(9, 13), 16);
            setPoints[2] = (double) Integer.parseInt(sms.substring(13, 17), 16);

            temporalAddress = smsMessage.getOriginatingAddress();
            if (temporalAddress.length() > 10) {
                temporalAddress = smsMessage.getOriginatingAddress().substring(3, 13);
            }

            saveSetPoints(temporalAddress, setPoints, smsMessage.getTimestampMillis());

        } catch (Exception e) {
            e.printStackTrace();
//            Log.e(TAG, "sms must not be well formatted");
        }

        return temporalAddress;
    }

    private void saveSetPoints(String phoneNumber, Double[] setPoints, long timestampMillis) {
        Microlog microlog = new Select().
                from(Microlog.class).
                where("sensorPhoneNumber = ?", phoneNumber).executeSingle();
        for (int i = 0; i < setPoints.length; i++) {
            SetPoint existingSetPoint = new Select().
                    from(SetPoint.class).
                    where("phoneNumber = ?", phoneNumber).
                    and("setPointNumber = ?", i).executeSingle();
            if (existingSetPoint != null) {
                existingSetPoint.micrologId = microlog.sensorId;
                existingSetPoint.tempInFahrenheit = setPoints[i];
                existingSetPoint.timestamp = timestampMillis;
                existingSetPoint.verified = true;
                existingSetPoint.save();
            } else {
                SetPoint setPoint = new SetPoint(
                        phoneNumber,
                        microlog.sensorId,
                        i,
                        setPoints[i],
                        timestampMillis,
                        true
                );
                setPoint.save();
            }
        }
    }

    /**
     * Formats a sms that contains a single phone number to report
     *
     * @param smsMessage unformatted sms
     * @param subString is the number of spaces from the beginning that we have to "ignore" to
     *                  create our phone
     * @param phoneIndex index to save in the database 1, 2, 3
     * @return phone number from sms
     */
    private String formatMessageTelephoneNumberSingle(SmsMessage smsMessage, int subString, int phoneIndex) {
        String sms = smsMessage.getMessageBody();
        String temporalAddress = null;
        try {
            String phoneToReport = sms.substring(subString, sms.length());

            temporalAddress = smsMessage.getOriginatingAddress();
            if (temporalAddress.length() > 10) {
                temporalAddress = smsMessage.getOriginatingAddress().substring(3, 13);
            }

            saveReportingTelephoneNumber(temporalAddress, phoneIndex, phoneToReport, smsMessage.getTimestampMillis());

        } catch (Exception e) {
            e.printStackTrace();
//            Log.e(TAG, "sms must not be well formatted");
        }

        return temporalAddress;
    }

    public void saveReportingTelephoneNumber(String micrologPhone, int phoneIndex, String phoneToReport, long date) {
        Telephone telephone = new Select().
                from(Telephone.class).
                where("sensorPhoneNumber = ?", micrologPhone).
                and("phoneIndex = ?", phoneIndex).executeSingle();

        if (telephone != null) {
            telephone.phoneNumber = phoneToReport;
            telephone.date = date;
            telephone.verified = true;
            telephone.save();
        } else {
            Telephone newTelephone = new Telephone(
                    micrologPhone,
                    phoneIndex,
                    phoneToReport,
                    date,
                    true
            );
            newTelephone.save();
        }
    }

    /**
     * Formats an sms that contains the number of phones that the sensor will report to
     *
     * @param smsMessage     sms
     * @param numberOfPhones number of phones that the sensor will report to
     * @return sensor phone number
     */
    private String formatMessageNumberOfTelephonesToReport(SmsMessage smsMessage, int numberOfPhones) {
        String temporalAddress = null;
        try {
            temporalAddress = smsMessage.getOriginatingAddress();
            if (temporalAddress.length() > 10) {
                temporalAddress = smsMessage.getOriginatingAddress().substring(3, 13);
            }

            saveNumberOfPhonesToReport(temporalAddress, numberOfPhones);

        } catch (Exception e) {
            e.printStackTrace();
//            Log.e(TAG, "sms must not be well formatted");
        }

        return temporalAddress;
    }

    public void saveNumberOfPhonesToReport(String temporalAddress, int numberOfPhones) {
        Microlog microlog = new Select().from(Microlog.class).
                where("sensorPhoneNumber = ?", temporalAddress).
                executeSingle();

        if (microlog != null) {
            microlog.numberOfPhonesToReport = numberOfPhones;
            microlog.save();
        }

    }

    /**
     * Formats an sms that contains the number of phones that the sensor will report to
     *
     * @param smsMessage sms
     * @return sensor phone number
     */
    private String formatMessageNumberOfTelephonesToReportAll(SmsMessage smsMessage) {
        String temporalAddress = null;
        int numberOfPhones;
        try {
            String tempNumber = smsMessage.getMessageBody().substring(31, 33);
            numberOfPhones = Integer.parseInt(tempNumber);
            temporalAddress = smsMessage.getOriginatingAddress();
            if (temporalAddress.length() > 10) {
                temporalAddress = smsMessage.getOriginatingAddress().substring(3, 13);
            }

            saveNumberOfPhonesToReport(temporalAddress, numberOfPhones);

        } catch (Exception e) {
            e.printStackTrace();
//            Log.e(TAG, "sms must not be well formatted");
        }

        return temporalAddress;
    }

}
