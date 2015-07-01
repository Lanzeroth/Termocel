package com.ocr.termocel;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.ocr.termocel.model.Microlog;
import com.ocr.termocel.model.Temperature;
import com.ocr.termocel.receivers.MessageReceiver;
import com.ocr.termocel.utilities.AndroidBus;
import com.squareup.otto.Bus;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();

    public static final String EXTRA_TELEPHONE_NUMBER = "EXTRA_TELEPHONE_NUMBER";

    public static Bus bus;

    private int selectedId;
    private Microlog microlog;

    private String mContactName;
    private String mTelephoneNumber;

    private final String STATUS = "S";

    SmsManager smsManager;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @InjectView(R.id.textViewContactName)
    TextView textViewContactName;

    @InjectView(R.id.textViewTelephone)
    TextView textViewTelephone;

    @InjectView(R.id.textViewLastKnownTemp)
    TextView textViewLastKnownTemp;

    @InjectView(R.id.textViewStatus)
    TextView textViewStatus;

    @InjectView(R.id.textViewHumidity)
    TextView textViewHumidity;

    @InjectView(R.id.textViewNoInfo)
    TextView textViewNoInfo;

    @InjectView(R.id.textViewLastUpdateDate)
    TextView textViewLastUpdateDate;

    @InjectView(R.id.lastDataContainer)
    LinearLayout lastDataContainer;

    @InjectView(R.id.seekbar_font)
    SeekBar seekBarThermometer;

    @OnClick(R.id.button)
    public void buttonClicked() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_title_state))
                .setMessage(getString(R.string.dialog_message_confirm_sms))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.dialog_yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getUpdatedSensorInfo(mTelephoneNumber);
                    }
                })
                .setNegativeButton(getString(R.string.dialog_no), null)
                .show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.inject(this);

        drawThermometer();

        lastDataContainer.setVisibility(View.GONE);

        Intent intent = getIntent();
        boolean comesFromReceiver = intent.getBooleanExtra(SensorSelectorActivity.EXTRA_COMES_FROM_RECEIVER, false);
        if (comesFromReceiver) {
            mTelephoneNumber = intent.getStringExtra(MessageReceiver.EXTRA_PHONE_NUMBER);
            microlog = getMicrologByPhoneNumber(mTelephoneNumber);
            mContactName = microlog.name;
        } else {
            selectedId = intent.getIntExtra(SensorSelectorActivity.EXTRA_SELECTED_ID, 0);
            microlog = getMicrologs().get(selectedId);
            mTelephoneNumber = microlog.sensorPhoneNumber;
            mContactName = microlog.name;
        }

        textViewTelephone.setText(mTelephoneNumber);
        textViewContactName.setText(mContactName);

        List<Temperature> temperatures = getTemperatureList(mTelephoneNumber);
        if (temperatures != null && !temperatures.isEmpty()) {
            lastDataContainer.setVisibility(View.VISIBLE);
            textViewNoInfo.setVisibility(View.GONE);
            Temperature temperature = temperatures.get(temperatures.size() - 1);

            textViewLastKnownTemp.setText(String.valueOf(temperature.tempInFahrenheit) + " \u00B0 F");

            seekBarThermometer.setProgress((int) temperature.tempInFahrenheit);

            textViewStatus.setText(temperature.status);
            switch (temperature.status) {
                case "Normal":
                    textViewStatus.setTextColor(getResources().getColor(R.color.green_700));
                    break;
                case "Atencion":
                    textViewStatus.setTextColor(getResources().getColor(R.color.yellow_700));
                    break;
                case "Advertencia":
                    textViewStatus.setTextColor(getResources().getColor(R.color.orange_500));
                    break;
                case "Alarma":
                    textViewStatus.setTextColor(getResources().getColor(R.color.red_600));
                    break;
            }
            textViewHumidity.setText(String.valueOf(temperature.humidity) + "%");

            Calendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(temperature.timestamp);

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", java.util.Locale.getDefault());
            simpleDateFormat.setCalendar(calendar);

            textViewLastUpdateDate.setText(simpleDateFormat.format(calendar.getTime()));
        }


        bus = new AndroidBus();
        bus.register(this);

        smsManager = SmsManager.getDefault();

        /** toolBar **/
        setUpToolBar();
    }

    /**
     * creates a gradient to show the shades from red to green trying to simulate a thermometer,
     * assigns this gradient to the seekBar and prevents it from capturing on touch events
     */
    private void drawThermometer() {
        LinearGradient test = new LinearGradient(0.f, 0.f, 300.f, 0.0f,

                new int[]{0xFFe53935, 0xFFff9800, 0xFFfbc02d, 0xFF388e3C},
                null, Shader.TileMode.CLAMP);
        ShapeDrawable shape = new ShapeDrawable(new RectShape());
        shape.getPaint().setShader(test);

        seekBarThermometer.setProgressDrawable((Drawable) shape);
        seekBarThermometer.setClickable(false);
        seekBarThermometer.setFocusable(false);
        seekBarThermometer.setOnTouchListener(new View.OnTouchListener() {
                                                  @Override
                                                  public boolean onTouch(View view, MotionEvent motionEvent) {
                                                      return true;
                                                  }
                                              }
        );
    }

    public void getUpdatedSensorInfo(String telephoneNumber) {
        try {
            Log.d("SMS send", "sending message to " + telephoneNumber);
            smsManager.sendTextMessage(telephoneNumber, null, STATUS, null, null);
            Toast.makeText(this, getString(R.string.toast_message_send), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.toast_message_not_send), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


    /**
     * A Microlog is kind of the main object of this app, from this we get temperatures
     *
     * @return a sensor list
     */
    public List<Microlog> getMicrologs() {
        return new Select().from(Microlog.class).execute();
    }

    public Microlog getMicrologByPhoneNumber(String phoneNumber) {
        return new Select().from(Microlog.class).where("sensorPhoneNumber = ?", phoneNumber).executeSingle();
    }

    public List<Temperature> getTemperatureList(String telephoneNumber) {
        return new Select().from(Temperature.class).where("sensorPhoneNumber = ?", telephoneNumber).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_telephones) {
            Intent intent = new Intent(this, TelephoneChangeActivity.class);
            intent.putExtra(EXTRA_TELEPHONE_NUMBER, mTelephoneNumber);
            startActivity(intent);
        } else if (id == R.id.action_alerts) {
            Intent intent = new Intent(this, SetPointsActivity.class);
            intent.putExtra(EXTRA_TELEPHONE_NUMBER, mTelephoneNumber);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * sets up the top bar
     */
    private void setUpToolBar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.app_name));
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        // enabling action bar app icon and behaving it as toggle button
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }
}
