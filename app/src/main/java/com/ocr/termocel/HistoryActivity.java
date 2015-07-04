package com.ocr.termocel;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.activeandroid.query.Select;
import com.ocr.termocel.custom.recyclerView.EmptyRecyclerView;
import com.ocr.termocel.custom.recyclerView.TemperatureAdapter;
import com.ocr.termocel.model.Temperature;
import com.ocr.termocel.utilities.AndroidBus;
import com.squareup.otto.Bus;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class HistoryActivity extends AppCompatActivity {

    private final String TAG = HistoryActivity.class.getSimpleName();

    public static Bus bus;

    protected List<Temperature> mDataSet;
    protected TemperatureAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;

    String mTelephoneNumber;


    @InjectView(R.id.recycler_view_history)
    EmptyRecyclerView recyclerViewHistory;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        ButterKnife.inject(this);

        Intent intent = getIntent();
        mTelephoneNumber = intent.getStringExtra(MainActivity.EXTRA_TELEPHONE_NUMBER);


        bus = new AndroidBus();
        bus.register(this);

        initDataSet();

        if (mDataSet != null) {
            mLayoutManager = new LinearLayoutManager(this);
            recyclerViewHistory.setLayoutManager(mLayoutManager);

            mAdapter = new TemperatureAdapter(mDataSet, this);

            recyclerViewHistory.setAdapter(mAdapter);
        } else {
            recyclerViewHistory.setVisibility(View.GONE);
        }
        /** toolBar **/
        setUpToolBar();
    }

    private void initDataSet() {
        mDataSet = getTemperatures();
    }

    private List<Temperature> getTemperatures() {
        return new Select().from(Temperature.class).where("sensorPhoneNumber = ?", mTelephoneNumber).orderBy("timestamp DESC").execute();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_history, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    /**
     * sets up the top bar
     */
    private void setUpToolBar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.history_activity_title));
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        // enabling action bar app icon and behaving it as toggle button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }
}
