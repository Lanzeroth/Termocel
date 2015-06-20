package com.ocr.termocel;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.ocr.termocel.model.Sensor;
import com.ocr.termocel.model.Temperature;
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
    private Sensor sensor;

    private String mTelephoneNumber;

    private final String STATUS = "S";

    SmsManager smsManager;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

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

    @OnClick(R.id.button)
    public void buttonClicked() {
        getUpdatedSensorInfo(mTelephoneNumber);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.inject(this);

        lastDataContainer.setVisibility(View.GONE);

        Intent intent = getIntent();
        selectedId = intent.getIntExtra(SensorSelectorActivity.EXTRA_SELECTED_ID, 0);

        sensor = getSensors().get(selectedId);

        mTelephoneNumber = sensor.telephoneNumber;

        textViewTelephone.setText(mTelephoneNumber);

        List<Temperature> temperatures = getTemperatureList(mTelephoneNumber);
        if (temperatures != null && !temperatures.isEmpty()) {
            lastDataContainer.setVisibility(View.VISIBLE);
            textViewNoInfo.setVisibility(View.GONE);
            Temperature temperature = temperatures.get(temperatures.size() - 1);

            textViewLastKnownTemp.setText(String.valueOf(temperature.tempInFahrenheit) + " \u00B0 F");
            textViewStatus.setText(temperature.status);
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
     * A sensor is kind of the main object of this app, from this we get temperatures
     *
     * @return a sensor list
     */
    public List<Sensor> getSensors() {
        return new Select().from(Sensor.class).execute();
    }

    public List<Temperature> getTemperatureList(String telephoneNumber) {
        return new Select().from(Temperature.class).where("phoneNumber = ?", telephoneNumber).execute();
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
