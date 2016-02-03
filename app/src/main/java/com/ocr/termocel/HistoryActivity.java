package com.ocr.termocel;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.ocr.termocel.custom.recyclerView.EmptyRecyclerView;
import com.ocr.termocel.custom.recyclerView.TemperatureAdapter;
import com.ocr.termocel.model.Temperature;
import com.ocr.termocel.utilities.AndroidBus;
import com.ocr.termocel.utilities.ExcelReportWriter;
import com.squareup.otto.Bus;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class HistoryActivity extends AppCompatActivity {

    private final String TAG = HistoryActivity.class.getSimpleName();

    private static final int EMAIL_INTENT_REQ = 99;

    public static Bus bus;

    protected List<Temperature> mDataSet;
    protected TemperatureAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;

    String mTelephoneNumber;


    @Bind(R.id.recycler_view_history)
    EmptyRecyclerView recyclerViewHistory;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        ButterKnife.bind(this);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_history, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_print_log) {
            printLogInXLS();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void printLogInXLS() {
        ExcelReportWriter reportWriter = new ExcelReportWriter();
//        String path = getBaseContext().getFilesDir().getPath();
//        path = path + "/test.xls";
//

        final String fileName = "TempReport.xls";

        //Saving file in external storage
        File sdCard = Environment.getExternalStorageDirectory();
        File directory = new File(sdCard.getAbsolutePath() + "/temp.todo");

        //create directory if not exist
        if (!directory.isDirectory()) {
            directory.mkdirs();
        }

        //file path
        File file = new File(directory, fileName);
        reportWriter.setOutputFile(file);
        reportWriter.setData(mDataSet);
        try {
            shareIntent(reportWriter.write());
            Log.d("good", "write good");
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("bad", "write bad");

        }
    }

    private void shareIntent(File filePath) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/xls");
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.extra_subject) + currentDateTimeString);
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.extra_text));
        File file = filePath;
        if (!file.exists() || !file.canRead()) {
            Toast.makeText(this, "Attachment Error", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Uri uri = Uri.fromFile(file);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.send_email)), EMAIL_INTENT_REQ);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("result: ", String.valueOf(resultCode));
        if (requestCode == EMAIL_INTENT_REQ) {
            Toast.makeText(this, R.string.email_sent, Toast.LENGTH_SHORT).show();
        }
    }

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
