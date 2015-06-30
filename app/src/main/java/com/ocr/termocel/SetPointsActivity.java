package com.ocr.termocel;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.EditText;
import android.widget.SeekBar;

import com.activeandroid.query.Select;
import com.ocr.termocel.model.SetPoint;
import com.ocr.termocel.model.Telephone;

import java.util.Calendar;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class SetPointsActivity extends AppCompatActivity {

    private final String TAG = SetPointsActivity.class.getSimpleName();

    private final int MAX_TEMP = 100;
    private final int MIN_TEMP = 0;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @InjectView(R.id.editTextMicrologId)
    EditText editTextMicrologId;

    @InjectView(R.id.seekBarSetPoint1)
    SeekBar seekBarSetPoint1;
    @InjectView(R.id.seekBarSetPoint2)
    SeekBar seekBarSetPoint2;
    @InjectView(R.id.seekBarSetPoint3)
    SeekBar seekBarSetPoint3;

    @InjectView(R.id.editTextSetPoint1)
    EditText editTextSetPoint1;
    @InjectView(R.id.editTextSetPoint2)
    EditText editTextSetPoint2;
    @InjectView(R.id.editTextSetPoint3)
    EditText editTextSetPoint3;

    @OnClick(R.id.buttonSetPoint1)
    public void setPointClicked1() {
        setPointClick(0);
    }

    @OnClick(R.id.buttonSetPoint2)
    public void setPointClicked2() {
        setPointClick(1);
    }

    @OnClick(R.id.buttonSetPoint3)
    public void setPointClicked3() {
        setPointClick(2);
    }


    SmsManager smsManager;

    private String sensorTelephoneNumber;

    private List<SetPoint> mSetPointList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_points);
        ButterKnife.inject(this);

        Intent intent = getIntent();
        sensorTelephoneNumber = intent.getStringExtra(MainActivity.EXTRA_TELEPHONE_NUMBER);

        smsManager = SmsManager.getDefault();

        editTextMicrologId.setText("01");
        editTextMicrologId.setSelectAllOnFocus(true);

        seekBarSetPoint1.setProgress(36);
        seekBarSetPoint2.setProgress(33);
        seekBarSetPoint3.setProgress(28);

        editTextSetPoint1.setText("36");
        editTextSetPoint1.setSelectAllOnFocus(true);
        editTextSetPoint2.setText("33");
        editTextSetPoint2.setSelectAllOnFocus(true);
        editTextSetPoint3.setText("38");
        editTextSetPoint3.setSelectAllOnFocus(true);


        seekBarSetPoint1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                editTextSetPoint1.setText(String.valueOf(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBarSetPoint2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                editTextSetPoint2.setText(String.valueOf(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBarSetPoint3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                editTextSetPoint3.setText(String.valueOf(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        /** toolBar **/
        setUpToolBar();
    }

    private void setPointClick(int i) {
        SetPoint setPoint = new Select().
                from(SetPoint.class).
                where("phoneNumber = ?", sensorTelephoneNumber).
                and("setPointNumber = ?", i).
                executeSingle();
        if (setPoint != null) {
            switch (i) {
                case 0:
                    setPoint.tempInFahrenheit = Double.parseDouble(editTextSetPoint1.getText().toString());
                    break;
                case 1:
                    setPoint.tempInFahrenheit = Double.parseDouble(editTextSetPoint2.getText().toString());
                    break;
                case 2:
                    setPoint.tempInFahrenheit = Double.parseDouble(editTextSetPoint3.getText().toString());
                    break;
            }
            Calendar calendar = Calendar.getInstance();
            setPoint.timestamp = calendar.getTimeInMillis();
            setPoint.verified = false;
            setPoint.save();
        }
        Log.d(TAG, "set point saved to db");

        sendSMS(i);


    }

    private void sendSMS(int i) {
        String micrologId = formatMicrologId();
        String hex;
        switch (i) {
            case 0:
                hex = Integer.toHexString(Integer.parseInt(editTextSetPoint1.getText().toString()));
                smsManager.sendTextMessage(sensorTelephoneNumber, null, micrologId + "SX100" + hex, null, null);
                break;
            case 1:
                hex = Integer.toHexString(Integer.parseInt(editTextSetPoint2.getText().toString()));
                smsManager.sendTextMessage(sensorTelephoneNumber, null, micrologId + "SX200" + hex, null, null);
                break;
            case 2:
                hex = Integer.toHexString(Integer.parseInt(editTextSetPoint3.getText().toString()));
                smsManager.sendTextMessage(sensorTelephoneNumber, null, micrologId + "SX300" + hex, null, null);
                break;
        }
    }

    private String formatMicrologId() {
        String tempId = editTextMicrologId.getText().toString();
        int intId = Integer.parseInt(tempId);

        return tempId;
    }

    public List<Telephone> getSetPointsFromDB() {
        return new Select().from(SetPoint.class).where("phoneNumber = ?", sensorTelephoneNumber).orderBy("setPointNumber ASC").execute();
    }


    /**
     * sets up the top bar
     */
    private void setUpToolBar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.set_points_title));
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        // enabling action bar app icon and behaving it as toggle button
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }
}
