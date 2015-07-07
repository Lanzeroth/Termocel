package com.ocr.termocel;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.ocr.termocel.model.Microlog;
import com.ocr.termocel.model.SetPoint;
import com.ocr.termocel.receivers.MessageReceiver;

import java.util.Calendar;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class SetPointsActivity extends AppCompatActivity {

    private final String TAG = SetPointsActivity.class.getSimpleName();

    private int mTemp1 = 36;
    private int mTemp2 = 33;
    private int mTemp3 = 28;

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

    @OnClick(R.id.buttonSetPointAll)
    public void setAllSetPointsClicked() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_title_get_all_set_points))
                .setMessage(getString(R.string.dialog_message_confirm_sms))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.dialog_yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        smsManager.sendTextMessage(sensorTelephoneNumber, null, mMicrolog.sensorId + "X01S?2", null, null);
                        Toast.makeText(getApplicationContext(), getString(R.string.toast_message_send), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(getString(R.string.dialog_no), null)
                .show();

    }

    @OnClick(R.id.buttonSetPoint1)
    public void setPointClicked1() {
        if (mTemp1 == Double.parseDouble(editTextSetPoint1.getText().toString())) {
            showAlertDialog(getString(R.string.dialog_title_set_point) + " 1", getString(R.string.dialog_message_temperature_is_the_same), 0);
        } else {
            showAlertDialog(getString(R.string.dialog_title_set_point) + " 1", getString(R.string.dialog_message_confirm_sms), 0);
        }
    }

    @OnClick(R.id.buttonSetPoint2)
    public void setPointClicked2() {
        if (mTemp2 == Double.parseDouble(editTextSetPoint2.getText().toString())) {
            showAlertDialog(getString(R.string.dialog_title_set_point) + " 2", getString(R.string.dialog_message_temperature_is_the_same), 1);
        } else {
            showAlertDialog(getString(R.string.dialog_title_set_point) + " 2", getString(R.string.dialog_message_confirm_sms), 1);
        }
    }

    @OnClick(R.id.buttonSetPoint3)
    public void setPointClicked3() {
        if (mTemp3 == Double.parseDouble(editTextSetPoint3.getText().toString())) {
            showAlertDialog(getString(R.string.dialog_title_set_point) + " 3", getString(R.string.dialog_message_temperature_is_the_same), 2);
        } else {
            showAlertDialog(getString(R.string.dialog_title_set_point) + " 3", getString(R.string.dialog_message_confirm_sms), 2);
        }
    }


    SmsManager smsManager;

    private String sensorTelephoneNumber;

    private List<SetPoint> mSetPointList;

    private Microlog mMicrolog;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_points);
        ButterKnife.inject(this);


        Intent intent = getIntent();
        sensorTelephoneNumber = intent.getStringExtra(MainActivity.EXTRA_TELEPHONE_NUMBER);
        if (sensorTelephoneNumber == null || sensorTelephoneNumber.isEmpty()) {
            sensorTelephoneNumber = intent.getStringExtra(MessageReceiver.EXTRA_PHONE_NUMBER);
        }
        smsManager = SmsManager.getDefault();

        /** toolBar **/
        setUpToolBar();


        drawSetPoints();


    }

    /**
     * Handles the setPoints drawing, is separated cause it will be called when refreshing
     */
    private void drawSetPoints() {
        mMicrolog = getMicrolog();


        if (!mMicrolog.sensorId.equalsIgnoreCase("")) {
            editTextMicrologId.setText(mMicrolog.sensorId);
        } else {
            editTextMicrologId.setText("00");
        }

        mSetPointList = getSetPointsFromDB();

        if (mSetPointList != null && !mSetPointList.isEmpty()) {

            mTemp1 = (int) mSetPointList.get(0).tempInFahrenheit;
            mTemp2 = (int) mSetPointList.get(1).tempInFahrenheit;
            mTemp3 = (int) mSetPointList.get(2).tempInFahrenheit;

            seekBarSetPoint1.setProgress(mTemp1);
            seekBarSetPoint2.setProgress(mTemp2);
            seekBarSetPoint3.setProgress(mTemp3);

            seekBarSetPoint1.setThumb(writeOnDrawable(R.drawable.thumb, String.valueOf(mTemp1)));
            seekBarSetPoint2.setThumb(writeOnDrawable(R.drawable.thumb, String.valueOf(mTemp2)));
            seekBarSetPoint3.setThumb(writeOnDrawable(R.drawable.thumb, String.valueOf(mTemp3)));

            editTextSetPoint1.setText(String.valueOf(mTemp1));
            editTextSetPoint2.setText(String.valueOf(mTemp2));
            editTextSetPoint3.setText(String.valueOf(mTemp3));
        } else {
            seekBarSetPoint1.setProgress(mTemp1);
            seekBarSetPoint2.setProgress(mTemp2);
            seekBarSetPoint3.setProgress(mTemp3);

            seekBarSetPoint1.setThumb(writeOnDrawable(R.drawable.thumb, String.valueOf(mTemp1)));
            seekBarSetPoint2.setThumb(writeOnDrawable(R.drawable.thumb, String.valueOf(mTemp2)));
            seekBarSetPoint3.setThumb(writeOnDrawable(R.drawable.thumb, String.valueOf(mTemp3)));


            editTextSetPoint1.setText(String.valueOf(mTemp1));
            editTextSetPoint2.setText(String.valueOf(mTemp2));
            editTextSetPoint3.setText(String.valueOf(mTemp3));
        }

        editTextSetPoint1.setSelectAllOnFocus(true);
        editTextSetPoint2.setSelectAllOnFocus(true);
        editTextSetPoint3.setSelectAllOnFocus(true);


        seekBarSetPoint1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                editTextSetPoint1.setText(String.valueOf(i));
                seekBarSetPoint1.setThumb(writeOnDrawable(R.drawable.thumb, String.valueOf(i)));
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
                seekBarSetPoint2.setThumb(writeOnDrawable(R.drawable.thumb, String.valueOf(i)));
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
                seekBarSetPoint3.setThumb(writeOnDrawable(R.drawable.thumb, String.valueOf(i)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    /**
     * Writes text on the Thumb drawable
     *
     * @param drawableId the resource Id
     * @param text       text to be drawn
     * @return bitmap
     */
    public BitmapDrawable writeOnDrawable(int drawableId, String text) {

        Bitmap bm = BitmapFactory.decodeResource(getResources(), drawableId).copy(Bitmap.Config.ARGB_8888, true);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setTextSize(40);

        Canvas canvas = new Canvas(bm);
        canvas.drawText(text, 7, 40, paint);

        return new BitmapDrawable(getResources(), bm);
    }

    private Microlog getMicrolog() {
        return new Select().from(Microlog.class).where("sensorPhoneNumber = ?", sensorTelephoneNumber).executeSingle();
    }

    /**
     * Show the alert dialog and then clicks on the index
     *
     * @param title         title to show
     * @param messageToShow message
     * @param indexClicked  for the setPointClick
     */
    private void showAlertDialog(String title, String messageToShow, final int indexClicked) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(messageToShow)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.dialog_yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        setPointClick(indexClicked);
                    }
                })
                .setNegativeButton(getString(R.string.dialog_no), null)
                .show();
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
        String micrologId = mMicrolog.sensorId;
        String hex;
        switch (i) {
            case 0:
                hex = Integer.toHexString(Integer.parseInt(editTextSetPoint1.getText().toString()));
                smsManager.sendTextMessage(sensorTelephoneNumber, null, micrologId + "SX100" + hex, null, null);
                Toast.makeText(this, getString(R.string.toast_message_send), Toast.LENGTH_SHORT).show();
                break;
            case 1:
                hex = Integer.toHexString(Integer.parseInt(editTextSetPoint2.getText().toString()));
                smsManager.sendTextMessage(sensorTelephoneNumber, null, micrologId + "SX200" + hex, null, null);
                Toast.makeText(this, getString(R.string.toast_message_send), Toast.LENGTH_SHORT).show();
                break;
            case 2:
                hex = Integer.toHexString(Integer.parseInt(editTextSetPoint3.getText().toString()));
                smsManager.sendTextMessage(sensorTelephoneNumber, null, micrologId + "SX300" + hex, null, null);
                Toast.makeText(this, getString(R.string.toast_message_send), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public List<SetPoint> getSetPointsFromDB() {
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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }


}
