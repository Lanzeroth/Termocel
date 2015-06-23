package com.ocr.termocel;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.activeandroid.query.Select;
import com.ocr.termocel.custom.recyclerView.CustomAdapter;
import com.ocr.termocel.custom.recyclerView.EmptyRecyclerView;
import com.ocr.termocel.events.SensorClickedEvent;
import com.ocr.termocel.model.Sensor;
import com.ocr.termocel.utilities.AndroidBus;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class SensorSelectorActivity extends AppCompatActivity {

    public static Bus bus;

    protected CustomAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected List<Sensor> mDataSet;

    public static final String EXTRA_SELECTED_ID = "EXTRA_SELECTED_ID";

    @InjectView(R.id.editTextNewPhoneNumber)
    EditText editTextNewPhoneNumber;

    @InjectView(R.id.editTextName)
    EditText editTextName;

    @InjectView(R.id.my_recycler_view)
    EmptyRecyclerView mRecyclerView;

    @InjectView(R.id.newContactContainer)
    LinearLayout newContactContainer;

    @OnClick(R.id.buttonNewContact)
    public void newContactClicked() {
        if (newContactContainer.getVisibility() == View.VISIBLE) {
            newContactContainer.setVisibility(View.GONE);
        } else if (newContactContainer.getVisibility() == View.GONE) {
            newContactContainer.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.buttonSave)
    public void onButtonSaveClicked() {
        if (!editTextNewPhoneNumber.getText().toString().equalsIgnoreCase("")) {
            Sensor sensor = new Sensor(
                    editTextNewPhoneNumber.getText().toString(),
                    editTextName.getText().toString(),
                    "NORMAL"
            );
            sensor.save();
            refresh();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_selector);

        ButterKnife.inject(this);

        newContactContainer.setVisibility(View.GONE);

        bus = new AndroidBus();
        bus.register(this);


        initDataSet();

        if (mDataSet != null) {
            // use a linear layout manager
            mLayoutManager = new LinearLayoutManager(this);
            mRecyclerView.setLayoutManager(mLayoutManager);

            mAdapter = new CustomAdapter(mDataSet);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mRecyclerView.setVisibility(View.GONE);
        }

    }

    /**
     * comes from the custom adapter, what we want is the element id to pass into the next activity
     * @param event
     */
    @Subscribe
    public void sensorClicked(SensorClickedEvent event) {
        if (event.getResultCode() == 1) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(EXTRA_SELECTED_ID, event.getElementId());
            startActivity(intent);
        }

    }

    /**
     * refreshes the ui
     */
    public void refresh() {
        initDataSet();

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new CustomAdapter(mDataSet);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void initDataSet() {
        mDataSet = getSensors();
    }

    public List<Sensor> getSensors() {
        return new Select().from(Sensor.class).execute();
    }

}
