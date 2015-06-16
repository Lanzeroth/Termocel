package com.ocr.termocel;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.activeandroid.query.Select;
import com.ocr.termocel.model.Sensor;
import com.ocr.termocel.model.Temperature;
import com.ocr.termocel.utilities.AndroidBus;
import com.squareup.otto.Bus;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity {

    public static Bus bus;

    private int selectedId;
    private Sensor sensor;

    private String mTelephoneNumber;

    private final String STATUS = "S";

    SmsManager smsManager;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @OnClick(R.id.button)
    public void buttonClicked() {
        getUpdatedSensorInfo(mTelephoneNumber);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        selectedId = intent.getIntExtra(SensorSelectorActivity.EXTRA_SELECTED_ID, 0);

        sensor = getSensors().get(selectedId);

        mTelephoneNumber = sensor.telephoneNumber;

        List<Temperature> temperatures = getTemperatureList(mTelephoneNumber);
        if (temperatures != null) {
            //
        }

        ButterKnife.inject(this);

        bus = new AndroidBus();
        bus.register(this);

        smsManager = SmsManager.getDefault();

        /** toolBar **/
        setUpToolBar();
    }

    public void getUpdatedSensorInfo(String telephoneNumber) {
        Log.d("SMS send", "sending message to " + telephoneNumber);
        smsManager.sendTextMessage(telephoneNumber, null, STATUS, null, null);
    }


    public List<Sensor> getSensors() {
        return new Select().from(Sensor.class).execute();
    }

    public List<Temperature> getTemperatureList(String telephoneNumber) {
        return new Select().from(Temperature.class).where("number = ?", telephoneNumber).execute();
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
        if (id == R.id.action_settings) {
            return true;
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


    public List<Sensor> getTermocelContact() {
        return new Select().from(Sensor.class).executeSingle();
    }

    private void sendSMS() {
        try {
            String sms = "S";
            String contact1 = "+526251048275";
            String contact2 = "+526143032079";

            smsManager.sendTextMessage(contact2, null, sms, null, null);
        } catch (Exception e) {
            Log.e("thing", "errorr", e);
        }
    }


}
