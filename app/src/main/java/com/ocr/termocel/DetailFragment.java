package com.ocr.termocel;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.ocr.termocel.events.CurrentSelectedMicrologEvent;
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

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DetailFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetailFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private final String TAG = MainActivity.class.getSimpleName();

    public static Bus bus;

    private int selectedId;
    private Microlog microlog;

    private String mContactName;
    private String mTelephoneNumber;

    private final String STATUS = "S";

    SmsManager smsManager;

    private boolean isEmpty = true;

    @Bind(R.id.textViewContactName)
    TextView textViewContactName;

    @Bind(R.id.textViewTelephone)
    TextView textViewTelephone;

    @Bind(R.id.textViewLastKnownTemp)
    TextView textViewLastKnownTemp;

    @Bind(R.id.textViewStatus)
    TextView textViewStatus;

    @Bind(R.id.textViewHumidity)
    TextView textViewHumidity;

    @Bind(R.id.textViewNoInfo)
    TextView textViewNoInfo;

    @Bind(R.id.textViewLastUpdateDate)
    TextView textViewLastUpdateDate;

    @Bind(R.id.lastDataContainer)
    LinearLayout lastDataContainer;

    @Bind(R.id.seekBarThermometer)
    SeekBar seekBarThermometer;

    @Bind(R.id.progressBar)
    ProgressBar progressBar;

    @OnClick(R.id.button)
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

    public DetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DetailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DetailFragment newInstance(String param1, String param2) {
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
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
        boolean comesFromReceiver = intent.getBooleanExtra(Constants.EXTRA_COMES_FROM_RECEIVER, false);
        if (comesFromReceiver) {
            mTelephoneNumber = intent.getStringExtra(MessageReceiver.EXTRA_PHONE_NUMBER);
            microlog = getMicrologByPhoneNumber(mTelephoneNumber);
            mContactName = microlog.name;
        } else {
//            selectedId = intent.getIntExtra(Constants.EXTRA_SELECTED_ID, 0);
//            microlog = getMicrologs().get(selectedId);
//            mTelephoneNumber = microlog.sensorPhoneNumber;
//            mContactName = microlog.name;
        }
//        new SearchForSMSHistory().execute(mTelephoneNumber);
//
//        textViewTelephone.setText(mTelephoneNumber);
//        textViewContactName.setText(mContactName);



        smsManager = SmsManager.getDefault();

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
//        getExistingTemperaturesForMain();

    }

    @Subscribe
    public void updateMicrologStatus(CurrentSelectedMicrologEvent event) {
        if (event.isEmpty()) {
            isEmpty = true;
        } else {
            mTelephoneNumber = event.getPhoneNumber();
            getExistingTemperaturesForMain();
        }
    }

    private void getExistingTemperaturesForMain() {
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
        return new Select().from(Temperature.class).where("sensorPhoneNumber = ?", telephoneNumber).orderBy("timestamp ASC").execute();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
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
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private class SearchForSMSHistory extends AsyncTask<String, Void, Void> {

        boolean micrologIdSaved = false;

        @Override
        protected void onPreExecute() {
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... strings) {
            String telephoneToSearch = strings[0];


            Uri uri = Uri.parse("content://sms");
            Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
            eraseTemperaturesFromLocalDB();

            if (cursor.moveToFirst()) {
                for (int i = 0; i < cursor.getCount(); i++) {
                    String number = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                    if (number.length() > 10) {
                        number = number.substring(number.length() - 10);
                    }
                    if (number.equalsIgnoreCase(telephoneToSearch)) {
                        String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                        String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                        String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                        if (body.contains("MICROLOG") && type.equalsIgnoreCase("1")) {
                            formatSMSMessage(telephoneToSearch, body, date);
                        }
                    }
                    cursor.moveToNext();
                }
            }
            cursor.close();
            return null;
        }

        private void formatSMSMessage(String telephoneToSearch, String sms, String date) {
            String[] spitedMessage = sms.split(" ");

            try {
                String micrologId = spitedMessage[1].substring(1, 3);
                String stateTemp = spitedMessage[2].substring(0, 3);
                String stateString = "";
                if (stateTemp.equalsIgnoreCase("NOR")) {
                    stateString = "Normal";
                } else if (stateTemp.equalsIgnoreCase("ATN")) {
                    stateString = "Atencion";
                } else if (stateTemp.equalsIgnoreCase("ADV")) {
                    stateString = "Advertencia";
                } else if (stateTemp.equalsIgnoreCase("ALA")) {
                    stateString = "Alarma";
                }
                String temperatureString = spitedMessage[5].substring(0, 2);
                String relativeHumidityString = spitedMessage[8];
//                Log.d(TAG, micrologId + " " + stateString + " " + temperatureString + " " + relativeHumidityString);

                if (!micrologIdSaved) { // we just want to do this the first time
                    saveMicrologID(telephoneToSearch, micrologId, stateString);
                }
                saveTemperature(micrologId, stateString, temperatureString, relativeHumidityString, telephoneToSearch, Long.parseLong(date));
            } catch (NumberFormatException e) {
                e.printStackTrace();
//                Log.e(TAG, "SMS must not be well formatted");
            }

        }

        private void saveTemperature(String micrologId, String stateString, String temperatureString, String relativeHumidityString, String temporalAddress, long date) {
            ActiveAndroid.beginTransaction();
            try {
                Temperature temperature = new Temperature(
                        temporalAddress,
                        micrologId,
                        stateString,
                        Double.parseDouble(temperatureString),
                        Double.parseDouble(relativeHumidityString),
                        date
                );
                temperature.save();
                ActiveAndroid.setTransactionSuccessful();
            } catch (NumberFormatException e) {
                e.printStackTrace();
            } finally {
                ActiveAndroid.endTransaction();
            }
        }

        /**
         * Database erase
         * Erases the db so we don't have to check if the reading already exists and don't put duplicates
         */
        private void eraseTemperaturesFromLocalDB() {
            List<Temperature> tempList = new Select().from(Temperature.class).execute();
            if (tempList != null && tempList.size() > 0) {
                ActiveAndroid.beginTransaction();
                try {
                    new Delete().from(Temperature.class).execute();
                    ActiveAndroid.setTransactionSuccessful();
                } catch (Exception e) {
//                    Logger.e(e, "error deleting existing db");
                } finally {
                    ActiveAndroid.endTransaction();
                }
            }

        }

        private void saveMicrologID(String temporalAddress, String micrologId, String stateString) {
            Microlog microlog = new Select().
                    from(Microlog.class).
                    where("sensorPhoneNumber = ?", temporalAddress).
                    executeSingle();

            if (microlog != null) {
                microlog.sensorId = micrologId;
                microlog.lastState = stateString;
                microlog.save();
                micrologIdSaved = true;
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            super.onPostExecute(aVoid);
            getExistingTemperaturesForMain();

        }
    }
}
