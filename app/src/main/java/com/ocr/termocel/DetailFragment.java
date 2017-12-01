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
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.gms.maps.model.LatLng;
import com.ocr.termocel.events.EditNameEvent;
import com.ocr.termocel.events.GoToDetailEvent;
import com.ocr.termocel.model.Microlog;
import com.ocr.termocel.model.Temperature;
import com.ocr.termocel.receivers.MessageReceiver;
import com.ocr.termocel.utilities.AndroidBus;
import com.ocr.termocel.utilities.Tools;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class DetailFragment extends Fragment {


    public static Bus bus;
    private final String TAG = MainActivity.class.getSimpleName();
    private final String STATUS = "S";
    SmsManager smsManager;
    List<Temperature> mTemperatures;
    boolean comesFromReceiver = false;
    @BindView(R.id.textViewContactName)
    TextView textViewContactName;
    @BindView(R.id.textViewTelephone)
    TextView textViewTelephone;
    @BindView(R.id.textViewLastKnownTemp)
    TextView textViewLastKnownTemp;
    @BindView(R.id.textViewStatus)
    TextView textViewStatus;
    @BindView(R.id.textViewHumidity)
    TextView textViewHumidity;
    @BindView(R.id.textViewNoInfo)
    TextView textViewNoInfo;
    @BindView(R.id.textViewLastUpdateDate)
    TextView textViewLastUpdateDate;
    @BindView(R.id.lastDataContainer)
    LinearLayout lastDataContainer;
    @BindView(R.id.seekBarThermometer)
    SeekBar seekBarThermometer;
    @BindView(R.id.buttonUpdateState)
    Button mButtonUpdateSelected;
    private Microlog mMicrolog;
    private String mContactName;
    private String mTelephoneNumber;
    private boolean isSomethingSelected = false;
    private ShowcaseView mShowcaseView;
    private int mShowCaseCounter = 0;
    private LatLng mLatLng;

    public DetailFragment() {
        // Required empty public constructor
    }

    @OnClick(R.id.buttonUpdateState)
    public void buttonClicked() {
        new AlertDialog.Builder(getActivity())
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_detail, container, false);

        bus = new AndroidBus();
        bus.register(this);

        ButterKnife.bind(this, view);

        drawThermometer();

        lastDataContainer.setVisibility(View.GONE);

        Intent intent = getActivity().getIntent();
        comesFromReceiver = intent.getBooleanExtra(Constants.EXTRA_COMES_FROM_RECEIVER, false);
        if (comesFromReceiver) {
            mTelephoneNumber = intent.getStringExtra(MessageReceiver.EXTRA_PHONE_NUMBER);
            mMicrolog = getMicrologByPhoneNumber(mTelephoneNumber);
            mContactName = mMicrolog.name;
        }

        isSomethingSelected = MainActivity.getIsSomethingSelected();
        if (!isSomethingSelected) {
            textViewNoInfo.setText(R.string.no_microlog_selected);
            mButtonUpdateSelected.setVisibility(View.INVISIBLE);
        }


        smsManager = SmsManager.getDefault();

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * In here we define what happens when we click a mMicrolog from the recycler view
     *
     * @param event
     */
    @Subscribe
    public void micrologClicked(GoToDetailEvent event) {
        if (event != null) {
            isSomethingSelected = true;
            mMicrolog = event.getMicrolog();
            mTelephoneNumber = mMicrolog.sensorPhoneNumber;
            mContactName = mMicrolog.name;

            textViewTelephone.setText(mTelephoneNumber);
            textViewContactName.setText(mContactName);

            mButtonUpdateSelected.setVisibility(View.VISIBLE);
            getExistingTemperaturesForMain();


        }
    }

    @Subscribe
    public void updateLatLng(LatLng latLng) {
        mLatLng = latLng;
    }

    @OnClick(R.id.fab_edit)
    public void buttonEditClicked() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edit, null);

        final EditText editTextName = (EditText) view.findViewById(R.id.editTextDialogEditName);
        final EditText editTextLat = (EditText) view.findViewById(R.id.editTextDialogEditLat);
        final EditText editTextLon = (EditText) view.findViewById(R.id.editTextDialogEditLon);
        final EditText editTextID = (EditText) view.findViewById(R.id.editTextDialogEditMicrologId);

        ImageButton imageButtonActualCords = (ImageButton) view.findViewById(R.id.imageButtonActualCords);
        imageButtonActualCords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLatLng != null) {
                    editTextLat.setText(String.valueOf(mLatLng.latitude));
                    editTextLon.setText(String.valueOf(mLatLng.longitude));
                }
            }
        });

        if (mMicrolog != null) {
            editTextName.setText(mMicrolog.getName());
            editTextID.setText(mMicrolog.getSensorId());
            editTextLat.setText(String.valueOf(mMicrolog.getLatitude()));
            editTextLon.setText(String.valueOf(mMicrolog.getLongitude()));
        }

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
        builder.setView(view);

        builder.setTitle(R.string.dialog_edit_sensor_title);
        builder.setPositiveButton(R.string.dialog_update_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!editTextName.getText().toString().isEmpty()) {
                    mMicrolog.setName(editTextName.getText().toString());
                    mMicrolog.setSensorId(editTextID.getText().toString());
                    mMicrolog.setLatitude(Double.parseDouble(editTextLat.getText().toString()));
                    mMicrolog.setLongitude(Double.parseDouble(editTextLon.getText().toString()));
                    mMicrolog.save();

                    mTelephoneNumber = mMicrolog.getSensorPhoneNumber();
                    mContactName = mMicrolog.getName();

                    textViewTelephone.setText(mTelephoneNumber);
                    textViewContactName.setText(mContactName);

                    MapAndListFragment.mapBus.post(new EditNameEvent());
                }
            }
        });
        builder.setNegativeButton(R.string.dialog_update_negative, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();

    }


    private void getExistingTemperaturesForMain() {
        mTemperatures = getTemperatureList(mTelephoneNumber);
        if (mTemperatures != null && !mTemperatures.isEmpty()) {
            lastDataContainer.setVisibility(View.VISIBLE);
            textViewNoInfo.setVisibility(View.GONE);
            Temperature temperature = mTemperatures.get(mTemperatures.size() - 1);

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

            if (comesFromReceiver) {

//                Firebase myFirebaseRef = new Firebase(getResources().getString(R.string.firebase_url));
//                Calendar c = Calendar.getInstance();
//                myFirebaseRef.child(mTelephoneNumber).child(String.valueOf(c.getTimeInMillis())).setValue(temperature);

                // clear the flag
                comesFromReceiver = false;

            }
        } else {
            textViewNoInfo.setVisibility(View.VISIBLE);
            lastDataContainer.setVisibility(View.GONE);
        }
    }

    /**
     * creates a gradient to show the shades from red to green trying to simulate a thermometer,
     * assigns this gradient to the seekBar and prevents it from capturing on touch events
     */
    private void drawThermometer() {
        seekBarThermometer.setClickable(false);
        seekBarThermometer.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                Log.d(TAG, "PROGRESS CHANGED " + progress);
                seekBarThermometer.setThumb(writeOnDrawable(R.drawable.thumb, String.valueOf(progress)));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBarThermometer.setFocusable(false);
        seekBarThermometer.setOnTouchListener(new View.OnTouchListener() {
                                                  @Override
                                                  public boolean onTouch(View view, MotionEvent motionEvent) {
                                                      return true;
                                                  }
                                              }
        );
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

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setTextSize(Tools.fromDpToPx(14));


        Canvas canvas = new Canvas(bm);
        canvas.save();

        int x = (bm.getWidth() - canvas.getWidth()) / 2;
        int y = (bm.getHeight() + canvas.getHeight()) / 2;

        Log.d("X and Y", "x=" + x + " y=" + y);
        canvas.rotate(90f);

        canvas.drawText(text, 5, -y / 4, paint);

        canvas.restore();

        return new BitmapDrawable(getResources(), bm);
    }

    public void getUpdatedSensorInfo(String telephoneNumber) {
        try {
//            Log.d("SMS send", "sending message to " + telephoneNumber);
            smsManager.sendTextMessage(telephoneNumber, null, STATUS, null, null);
            Toast.makeText(getActivity(), getString(R.string.toast_message_send), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getActivity(), getString(R.string.toast_message_not_send), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


    public Microlog getMicrologByPhoneNumber(String phoneNumber) {
        return new Select().from(Microlog.class).where("sensorPhoneNumber = ?", phoneNumber).executeSingle();
    }

    public List<Temperature> getTemperatureList(String telephoneNumber) {
        return new Select().from(Temperature.class).where("sensorPhoneNumber = ?", telephoneNumber).orderBy("timestamp ASC").execute();
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (!isSomethingSelected) {
            menu.getItem(1).setEnabled(false);
            menu.getItem(2).setEnabled(false);
            menu.getItem(3).setEnabled(false);
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_detail, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_telephones) {
            Intent intent = new Intent(getActivity(), TelephoneChangeActivity.class);
            intent.putExtra(Constants.EXTRA_TELEPHONE_NUMBER, mTelephoneNumber);
            startActivity(intent);
        } else if (id == R.id.action_alerts) {
            Intent intent = new Intent(getActivity(), SetPointsActivity.class);
            intent.putExtra(Constants.EXTRA_TELEPHONE_NUMBER, mTelephoneNumber);
            startActivity(intent);
        } else if (id == R.id.action_history) {
            Intent intent = new Intent(getActivity(), HistoryActivity.class);
            intent.putExtra(Constants.EXTRA_TELEPHONE_NUMBER, mTelephoneNumber);
            startActivity(intent);
        } else if (id == R.id.action_detail_help) {
            showShowcaseHelp();
        }

        return super.onOptionsItemSelected(item);
    }


    private void showShowcaseHelp() {
        // this is to put the button on the right
        RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lps.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        lps.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        int margin = ((Number) (getResources().getDisplayMetrics().density * 12)).intValue();
        lps.setMargins(margin, margin, margin, margin);

        mShowcaseView = new ShowcaseView.Builder(getActivity())
                .setTarget(new ViewTarget(getActivity().findViewById(R.id.buttonUpdateState)))
                .setContentText(getString(R.string.help_detail_update))
                .setStyle(R.style.CustomShowcaseTheme4)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {


                        switch (mShowCaseCounter) {
                            case 0:
                                mShowcaseView.setShowcase(new ViewTarget(getActivity().findViewById(R.id.lastDataContainer)), true);
                                mShowcaseView.setContentText(getString(R.string.help_detail_container));

                                break;
                            case 1:
                                mShowcaseView.hide();
//                setAlpha(1.0f, textView1, textView2, textView3);
                                mShowCaseCounter = -1;
                                break;
                        }
                        mShowCaseCounter++;
                    }
                })
                .build();
        mShowcaseView.setButtonText(getString(R.string.next));
        mShowcaseView.setHideOnTouchOutside(true);
        mShowcaseView.setButtonPosition(lps);

    }


}
