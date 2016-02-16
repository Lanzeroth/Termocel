package com.ocr.termocel;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.widget.LinearLayout;

import com.ocr.termocel.custom.recyclerView.CustomAdapter;
import com.ocr.termocel.model.Microlog;
import com.ocr.termocel.utilities.AndroidBus;
import com.squareup.otto.Bus;

import java.util.List;

import butterknife.Bind;


public class SensorSelectorActivity extends AppCompatActivity {

    private final String TAG = SensorSelectorActivity.class.getSimpleName();

    public static Bus bus;

    protected CustomAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected List<Microlog> mDataSet;

    private Handler mHandler;

//    @Bind(R.id.editTextNewPhoneNumber)
//    EditText editTextNewPhoneNumber;
//
//    @Bind(R.id.editTextName)
//    EditText editTextName;


    @Bind(R.id.newContactContainer)
    LinearLayout newContactContainer;
//
//    @OnClick(R.id.buttonNewContact)
//    public void newContactClicked() {
//        if (newContactContainer.getVisibility() == View.VISIBLE) {
//            newContactContainer.setVisibility(View.GONE);
//        } else if (newContactContainer.getVisibility() == View.GONE) {
//            newContactContainer.setVisibility(View.VISIBLE);
//        }
//    }
//
//    @OnClick(R.id.buttonGetContact)
//    public void getContactFromIntent() {
//        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
//        startActivityForResult(intent, Constants.ACTIVITY_RESULT_CONTACT);
//    }
//
//
//    @OnClick(R.id.buttonSave)
//    public void onButtonSaveClicked() {
//        if (!editTextNewPhoneNumber.getText().toString().equalsIgnoreCase("")) {
//            Microlog microlog = new Microlog(
//                    editTextNewPhoneNumber.getText().toString(),
//                    null,
//                    editTextName.getText().toString(),
//                    "NORMAL",
//                    3
//            );
//            microlog.save();
////            refreshRecyclerView();
//        }
//        if (newContactContainer.getVisibility() == View.VISIBLE) {
//            newContactContainer.setVisibility(View.GONE);
//        } else if (newContactContainer.getVisibility() == View.GONE) {
//            newContactContainer.setVisibility(View.VISIBLE);
//        }
//
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_selector);

//        ButterKnife.bind(this);

//        newContactContainer.setVisibility(View.GONE);

        bus = new AndroidBus();
        bus.register(this);

        mHandler = new Handler();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startMainActivity();
            }
        }, 500);



    }

//    /**
//     * comes from the custom adapter, what we want is the element id to pass into the next activity
//     *
//     * @param event
//     */
//    @Subscribe
//    public void sensorClicked(final SensorClickedEvent event) {
//        if (event.getResultCode() == 1) {
//            if (event.isDelete()) {
//                new AlertDialog.Builder(this)
//                        .setTitle(getString(R.string.dialog_title_delete))
//                        .setMessage(getString(R.string.dialog_message_confirm_delete))
//                        .setCancelable(false)
//                        .setPositiveButton(getString(R.string.dialog_yes_delete), new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                deleteMicrolog(event.getElementId());
//                            }
//                        })
//                        .setNegativeButton(getString(R.string.dialog_no), null)
//                        .show();
//            } else {
//                Intent intent = new Intent(this, MainActivity.class);
//                intent.putExtra(Constants.EXTRA_COMES_FROM_RECEIVER, false);
//                intent.putExtra(Constants.EXTRA_SELECTED_ID, event.getElementId());
//                startActivity(intent);
//            }
//        }
//
//    }










    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
