package com.ocr.termocel;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.ocr.termocel.model.Microlog;
import com.ocr.termocel.model.Telephone;
import com.ocr.termocel.receivers.MessageReceiver;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class TelephoneChangeActivity extends AppCompatActivity {

    private final String TAG = TelephoneChangeActivity.class.getSimpleName();

    private final String PHONE_CONSULT = "T?";
    private final String PHONES_TO_REPORT_CONSULT = "S?1";
    private final String PHONES_TO_REPORT_CHANGE = "S#";
    private final String PHONE_MODIFY = "T";

    private Microlog mMicrolog;


    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @InjectView(R.id.editTextPhone1)
    EditText editTextPhone1;
    @InjectView(R.id.editTextPhone2)
    EditText editTextPhone2;
    @InjectView(R.id.editTextPhone3)
    EditText editTextPhone3;

    @InjectView(R.id.buttonModify1)
    Button buttonModify1;
    @InjectView(R.id.buttonModify2)
    Button buttonModify2;
    @InjectView(R.id.buttonModify3)
    Button buttonModify3;
    @InjectView(R.id.buttonModifySpinner)
    Button buttonModifySpinner;

    @OnClick(R.id.buttonModify1)
    public void modifyButtonClicked1() {
        String dialogTitle;
        if (consultMode) {
            dialogTitle = getString(R.string.dialog_title_telephone_consult);
        } else {
            dialogTitle = getString(R.string.dialog_title_telephone);
        }
        showAlertDialog(dialogTitle + " 1", getString(R.string.dialog_message_confirm_sms), 0);
    }

    @OnClick(R.id.buttonModify2)
    public void modifyButtonClicked2() {
        String dialogTitle;
        if (consultMode) {
            dialogTitle = getString(R.string.dialog_title_telephone_consult);
        } else {
            dialogTitle = getString(R.string.dialog_title_telephone);
        }
        showAlertDialog(dialogTitle + " 2", getString(R.string.dialog_message_confirm_sms), 1);
    }

    @OnClick(R.id.buttonModify3)
    public void modifyButtonClicked3() {
        String dialogTitle;
        if (consultMode) {
            dialogTitle = getString(R.string.dialog_title_telephone_consult);
        } else {
            dialogTitle = getString(R.string.dialog_title_telephone);
        }
        showAlertDialog(dialogTitle + " 3", getString(R.string.dialog_message_confirm_sms), 2);
    }

    @OnClick(R.id.buttonModifySpinner)
    public void modifyButtonSpinner() {
        String dialogTitle;
        if (consultMode) {
            dialogTitle = getString(R.string.dialog_title_telephone_consult_spinner);

        } else {
            dialogTitle = getString(R.string.dialog_title_telephone_spinner);
        }
        new AlertDialog.Builder(this)
                .setTitle(dialogTitle)
                .setMessage(getString(R.string.dialog_message_confirm_sms))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.dialog_yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        saveNumberOfPhonesToReport();
                    }
                })
                .setNegativeButton(getString(R.string.dialog_no), null)
                .show();

    }


    @InjectView(R.id.textViewUpdateDate1)
    TextView textViewUpdateDate1;
    @InjectView(R.id.textViewUpdateDate2)
    TextView textViewUpdateDate2;
    @InjectView(R.id.textViewUpdateDate3)
    TextView textViewUpdateDate3;

    @InjectView(R.id.switchConsult)
    Switch switchConsult;

    @InjectView(R.id.spinner)
    Spinner phoneSpinner;

    private boolean consultMode = false;

    SmsManager smsManager;

    List<Telephone> mTelephones;

    private String sensorTelephoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_telephone_change);
        ButterKnife.inject(this);

        /** toolBar **/
        setUpToolBar();

        Intent intent = getIntent();
        sensorTelephoneNumber = intent.getStringExtra(MainActivity.EXTRA_TELEPHONE_NUMBER);
        if (sensorTelephoneNumber == null || sensorTelephoneNumber.isEmpty()) {
            sensorTelephoneNumber = intent.getStringExtra(MessageReceiver.EXTRA_PHONE_NUMBER);
        }

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.telephone_items_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        phoneSpinner.setAdapter(adapter);

        phoneSpinner.getSelectedItemId();
//        Log.d(TAG, String.valueOf(phoneSpinner.getSelectedItemId()));

        smsManager = SmsManager.getDefault();

        switchConsult.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                consultMode = !consultMode;
                if (consultMode) {
                    buttonModify1.setText("Consultar");
                    buttonModify2.setText("Consultar");
                    buttonModify3.setText("Consultar");
                    buttonModifySpinner.setText("Consultar");
                    editTextPhone1.setEnabled(false);
                    editTextPhone2.setEnabled(false);
                    editTextPhone3.setEnabled(false);
                    phoneSpinner.setEnabled(false);
                } else {
                    buttonModify1.setText("Modificar");
                    buttonModify2.setText("Modificar");
                    buttonModify3.setText("Modificar");
                    buttonModifySpinner.setText("Modificar");
                    editTextPhone1.setEnabled(true);
                    editTextPhone2.setEnabled(true);
                    editTextPhone3.setEnabled(true);
                    phoneSpinner.setEnabled(true);
                }
            }
        });

        mMicrolog = getMicrolog();

        if (mMicrolog.numberOfPhonesToReport > 0) {
            phoneSpinner.setSelection(mMicrolog.numberOfPhonesToReport - 1, true);
        } else {
            phoneSpinner.setSelection(2, true);
        }

        mTelephones = getTelephonesFromDB();
        // if there is info in the database, we populate the views, otherwise we prepopulate the
        // database so we always have 3 "spaces" that we can use
        if (mTelephones != null && !mTelephones.isEmpty()) {
            editTextPhone1.setText(mTelephones.get(0).phoneNumber);
            editTextPhone2.setText(mTelephones.get(1).phoneNumber);
            editTextPhone3.setText(mTelephones.get(2).phoneNumber);

            Calendar calendar = new GregorianCalendar();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", java.util.Locale.getDefault());

            calendar.setTimeInMillis(mTelephones.get(0).date);
            textViewUpdateDate1.setText(simpleDateFormat.format(calendar.getTime()));
            calendar.setTimeInMillis(mTelephones.get(1).date);
            textViewUpdateDate2.setText(simpleDateFormat.format(calendar.getTime()));
            calendar.setTimeInMillis(mTelephones.get(2).date);
            textViewUpdateDate3.setText(simpleDateFormat.format(calendar.getTime()));
        } else {
            Calendar c = Calendar.getInstance();
            for (int i = 0; i < 3; i++) {
                Telephone telephone = new Telephone(
                        sensorTelephoneNumber,
                        0,
                        "",
                        c.getTimeInMillis(),
                        false
                );
                telephone.save();
            }
        }
    }

    /**
     * Show the alert dialog and then clicks on the index
     *
     * @param title         title to show
     * @param messageToShow message
     * @param indexClicked  for the saveToDB
     */
    private void showAlertDialog(String title, String messageToShow, final int indexClicked) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(messageToShow)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.dialog_yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        saveToDB(indexClicked);
                    }
                })
                .setNegativeButton(getString(R.string.dialog_no), null)
                .show();
    }


    /**
     * If is not on consult mode we try to save the new phone to an existing database index and
     * then send an SMS to be saved on the microlog
     *
     * @param i the index number
     */
    private void saveToDB(int i) {
        try {
            if (!consultMode) {
                Telephone tempTelephone = new Select().
                        from(Telephone.class).
                        where("sensorPhoneNumber = ?", sensorTelephoneNumber).
                        and("phoneIndex = ?", i).
                        executeSingle();
                if (tempTelephone != null) {
                    switch (i) {
                        case 0:
                            tempTelephone.phoneNumber = editTextPhone1.getText().toString();
                            break;
                        case 1:
                            tempTelephone.phoneNumber = editTextPhone2.getText().toString();
                            break;
                        case 2:
                            tempTelephone.phoneNumber = editTextPhone3.getText().toString();
                            break;
                    }
                    Calendar calendar = Calendar.getInstance();
                    tempTelephone.date = calendar.getTimeInMillis();
                    tempTelephone.verified = false;
                    tempTelephone.save();
//                    Log.d(TAG, "telephone saved to db");
                }
            }

            sendSMS(i);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveNumberOfPhonesToReport() {
        try {
            if (consultMode) {
                smsManager.sendTextMessage(sensorTelephoneNumber, null, mMicrolog.sensorId + PHONES_TO_REPORT_CONSULT, null, null);
                Toast.makeText(this, "Mensaje de consulta enviado al " + sensorTelephoneNumber, Toast.LENGTH_SHORT).show();
            } else {
                String numberOfPhonesToReport = null;
                if (phoneSpinner.getSelectedItemPosition() == 0) {
                    numberOfPhonesToReport = "01";
                } else if (phoneSpinner.getSelectedItemPosition() == 1) {
                    numberOfPhonesToReport = "02";
                } else if (phoneSpinner.getSelectedItemPosition() == 2) {
                    numberOfPhonesToReport = "03";
                }
                mMicrolog.numberOfPhonesToReport = phoneSpinner.getSelectedItemPosition() + 1;
                mMicrolog.save();

                smsManager.sendTextMessage(sensorTelephoneNumber, null, mMicrolog.sensorId + PHONES_TO_REPORT_CHANGE + numberOfPhonesToReport, null, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendSMS(int i) throws Exception {
        if (consultMode) {
            switch (i) {
                case 0:
                    smsManager.sendTextMessage(sensorTelephoneNumber, null, mMicrolog.sensorId + PHONE_CONSULT + 2, null, null);
                    break;
                case 1:
                    smsManager.sendTextMessage(sensorTelephoneNumber, null, mMicrolog.sensorId + PHONE_CONSULT + 3, null, null);
                    break;
                case 2:
                    smsManager.sendTextMessage(sensorTelephoneNumber, null, mMicrolog.sensorId + PHONE_CONSULT + 4, null, null);
                    break;
            }
            Toast.makeText(this, "Mensaje de consulta enviado al " + sensorTelephoneNumber, Toast.LENGTH_SHORT).show();
        } else {
            switch (i) {
                case 0:
                    smsManager.sendTextMessage(sensorTelephoneNumber, null, mMicrolog.sensorId + PHONE_MODIFY + 2, null, null);
                    break;
                case 1:
                    smsManager.sendTextMessage(sensorTelephoneNumber, null, mMicrolog.sensorId + PHONE_MODIFY + 3, null, null);
                    break;
                case 2:
                    smsManager.sendTextMessage(sensorTelephoneNumber, null, mMicrolog.sensorId + PHONE_MODIFY + 4, null, null);
                    break;
            }
            Toast.makeText(this, "Numero" + i + " actualizado en el " + sensorTelephoneNumber, Toast.LENGTH_SHORT).show();
        }

    }

    public List<Telephone> getTelephonesFromDB() {
        return new Select().from(Telephone.class).where("sensorPhoneNumber = ?", sensorTelephoneNumber).orderBy("phoneIndex ASC").execute();
    }

    private Microlog getMicrolog() {
        return new Select().from(Microlog.class).where("sensorPhoneNumber = ?", sensorTelephoneNumber).executeSingle();
    }

    /**
     * sets up the top bar
     */
    private void setUpToolBar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.telephone_activity_title));
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        // enabling action bar app icon and behaving it as toggle button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }
}
