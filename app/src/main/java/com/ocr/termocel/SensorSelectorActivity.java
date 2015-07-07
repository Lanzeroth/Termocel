package com.ocr.termocel;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.activeandroid.query.Select;
import com.ocr.termocel.custom.recyclerView.CustomAdapter;
import com.ocr.termocel.custom.recyclerView.EmptyRecyclerView;
import com.ocr.termocel.events.SensorClickedEvent;
import com.ocr.termocel.model.Microlog;
import com.ocr.termocel.utilities.AndroidBus;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class SensorSelectorActivity extends AppCompatActivity {

    private final String TAG = SensorSelectorActivity.class.getSimpleName();

    public static Bus bus;

    protected CustomAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected List<Microlog> mDataSet;

    public static final String EXTRA_SELECTED_ID = "EXTRA_SELECTED_ID";
    public static final String EXTRA_COMES_FROM_RECEIVER = "EXTRA_COMES_FROM_RECEIVER";
    private final int ACTIVITY_RESULT_CONTACT = 101;

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

    @OnClick(R.id.buttonGetContact)
    public void getContactFromIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, ACTIVITY_RESULT_CONTACT);
    }


    @OnClick(R.id.buttonSave)
    public void onButtonSaveClicked() {
        if (!editTextNewPhoneNumber.getText().toString().equalsIgnoreCase("")) {
            Microlog microlog = new Microlog(
                    editTextNewPhoneNumber.getText().toString(),
                    null,
                    editTextName.getText().toString(),
                    "NORMAL",
                    3
            );
            microlog.save();
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
     *
     * @param event
     */
    @Subscribe
    public void sensorClicked(final SensorClickedEvent event) {
        if (event.getResultCode() == 1) {
            if (event.isDelete()) {
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.dialog_title_delete))
                        .setMessage(getString(R.string.dialog_message_confirm_delete))
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.dialog_yes_delete), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                deleteMicrolog(event.getElementId());
                            }
                        })
                        .setNegativeButton(getString(R.string.dialog_no), null)
                        .show();
            } else {
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra(EXTRA_COMES_FROM_RECEIVER, false);
                intent.putExtra(EXTRA_SELECTED_ID, event.getElementId());
                startActivity(intent);
            }
        }

    }

    private void deleteMicrolog(int elementId) {
        try {
            Microlog microlog = mDataSet.get(elementId);
            microlog.delete();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            refresh();
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

    public List<Microlog> getSensors() {
        return new Select().from(Microlog.class).execute();
    }


    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (reqCode == ACTIVITY_RESULT_CONTACT) {
            try {
                if (resultCode == Activity.RESULT_OK) {
                    Uri contactData = data.getData();
                    Cursor cur = managedQuery(contactData, null, null, null, null);
                    ContentResolver contact_resolver = getContentResolver();

                    if (cur.moveToFirst()) {
                        String id = cur.getString(cur.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                        String name = "";
                        String no = "";

                        Cursor phoneCur = contact_resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);

                        if (phoneCur.moveToFirst()) {
                            name = phoneCur.getString(phoneCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                            no = phoneCur.getString(phoneCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            if (no.length() > 10) {
                                no = no.substring(no.length() - 10);
                            }
                            Microlog microlog = new Microlog(
                                    no,
                                    null,
                                    name,
                                    null,
                                    3
                            );
                            microlog.save();
                            refresh();
                        }


                        phoneCur.close();

                        Log.e("Name and phone number", name + " : " + no);
                    }
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                Log.e(TAG, e.toString());
            }
        }

    }
}
