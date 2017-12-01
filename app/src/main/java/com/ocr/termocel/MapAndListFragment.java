package com.ocr.termocel;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ocr.termocel.custom.recyclerView.CustomAdapter;
import com.ocr.termocel.custom.recyclerView.EmptyRecyclerView;
import com.ocr.termocel.events.EditNameEvent;
import com.ocr.termocel.events.RefreshMicrologsEvent;
import com.ocr.termocel.events.SelectContactFromPhoneEvent;
import com.ocr.termocel.events.SensorClickedEvent;
import com.ocr.termocel.model.Microlog;
import com.ocr.termocel.utilities.AndroidBus;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class MapAndListFragment extends Fragment implements OnMapReadyCallback {

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 200;

    //    public static List<MessagesLocation> mLocations;
    public static Bus mapBus;
    private static View view;
    private final String TAG = MapAndListFragment.class.getSimpleName();
    protected CustomAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;

//    private HashMap<String, GoLockyMarker> eventMarkerMap;

    //    public static GoLockyMarker goLockyMarker;
    protected List<Microlog> mDataSet;
    @BindView(R.id.my_recycler_view)
    EmptyRecyclerView mRecyclerView;
    @BindView(R.id.textViewSelectMicrolog)
    TextView mTextViewSelectMicrolog;
    @BindView(R.id.fab_menu)
    FloatingActionsMenu mFloatingActionsMenu;
    LatLngBounds mBounds;
    private Unbinder unbinder;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LatLng mLatLong;
    private AlertDialog mMarkerDialog = null;
    private AlertDialog mAddDialog = null;
    private ShowcaseView mShowcaseView;
    private int mShowCaseCounter = 0;

    public MapAndListFragment() {
        // Required empty public constructor
    }

    @OnClick(R.id.fab_add)
    public void addNewContactClicked() {
        inflateAddDialogFragment();
    }

    @OnClick(R.id.fab_select)
    public void selectContactFromPhone() {
        MainActivity.bus.post(new SelectContactFromPhoneEvent());
        askForPermissions();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mapBus = new AndroidBus();
        mapBus.register(this);

        // Inflate the layout for this fragment
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.fragment_map, container, false);
            unbinder = ButterKnife.bind(this, view);

        } catch (InflateException e) {
            e.printStackTrace();
        /* map is already there, just return view as it is */
        }

        setUpMapIfNeeded();

        initDataSet();

        if (mDataSet == null) {
            mRecyclerView.setVisibility(View.GONE);
        } else if (mDataSet.isEmpty()) {
            mTextViewSelectMicrolog.setText(R.string.add_a_microlog);
            mLayoutManager = new LinearLayoutManager(getActivity());
            mRecyclerView.setLayoutManager(mLayoutManager);

            mAdapter = new CustomAdapter(mDataSet);
            mRecyclerView.setAdapter(mAdapter);
//            DetailFragment.bus.post(new CurrentSelectedMicrologEvent(null, true)); //telephone is null, thus is empty
        } else {
            // use a linear layout manager
            mLayoutManager = new LinearLayoutManager(getActivity());
            mRecyclerView.setLayoutManager(mLayoutManager);

            mAdapter = new CustomAdapter(mDataSet);
            mRecyclerView.setAdapter(mAdapter);
        }

        fabOverlayThingOnFab();

        setHasOptionsMenu(true);

        return view;
    }

    private void fabOverlayThingOnFab() {
//        final RelativeLayout mMapAndListContainer = (RelativeLayout) view.findViewById(R.id.mapAndListContainer);

        final FloatingActionsMenu mFabMenu = (FloatingActionsMenu) view.findViewById(R.id.fab_menu);

        mFabMenu.getBackground().setAlpha(0);
        mFabMenu.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
            @Override
            public void onMenuExpanded() {
                mFabMenu.getBackground().setAlpha(240);
                mFabMenu.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        mFabMenu.collapse();

                        return true;
                    }
                });
            }

            @Override
            public void onMenuCollapsed() {
                mFabMenu.getBackground().setAlpha(0);
                mFabMenu.setOnTouchListener(null);
            }
        });
    }

    /**
     * refreshes the ui
     */
    public void refreshRecyclerView() {

        hideFloatingActionsMenu();

        initDataSet();

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new CustomAdapter(mDataSet);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void hideFloatingActionsMenu() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mFloatingActionsMenu.collapse();
            }
        }, 500);
    }

    private void initDataSet() {
        mDataSet = getSensors();
    }

    public List<Microlog> getSensors() {
        return new Select().from(Microlog.class).execute();
    }

    @Subscribe
    public void refreshMicrologs(RefreshMicrologsEvent event) {
        refreshRecyclerView();
    }

    @Subscribe
    public void deleteMicrolog(SensorClickedEvent event) {

        if (event != null) {
            try {
                Microlog microlog = mDataSet.get(event.getElementId());
                microlog.delete();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                refreshRecyclerView();
            }
        }
    }

    @Subscribe
    public void handleLocation(LatLng latLng) {
        mLatLong = latLng;
        CameraUpdate cameraUpdateFactory = CameraUpdateFactory.newLatLngZoom(latLng, 17);
        mMap.animateCamera(cameraUpdateFactory);
    }

    @Subscribe
    public void callToInitDataSetFromDetail(EditNameEvent event) {
        initDataSet();
        mAdapter.notifyDataSetChanged();
    }


    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment. (THIS TOOK 2 HOURS)
            ((SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setUpMap();
        if (mMap != null && mDataSet != null && !mDataSet.isEmpty()) {
            populateTheMap();
            if (mBounds != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(mBounds, 100));
            } else if (mLatLong != null && mLatLong.latitude != 0.0 && mLatLong.longitude != 0.0) {
                CameraUpdate cameraUpdateFactory = CameraUpdateFactory.newLatLngZoom(mLatLong, 17);
                mMap.animateCamera(cameraUpdateFactory);
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        if (this.getContext() != null) {
            // get permissions on first run
            if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);

            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
//                goLockyMarker = eventMarkerMap.get(marker.getId());
//                inflateMarkerClickedDialog(goLockyMarker);
                }
            });


            // Initiate loadings
            if (mBounds != null) {
                mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                    @Override
                    public void onMapLoaded() {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(mBounds, 100));
                    }
                });
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();

    }

    /**
     * Create Makers on the map based on backend info
     */
    private void populateTheMap() {
        Log.e("populate", "populate called");
        if (mDataSet != null) {
            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

            String markerAddress = "";
            Double markerReward = 0.0;
            String markerName = "";
            String markerPictureURL = "";
            String snippetName = "";

            mMap.clear();

            int index = 0;
            boolean weHaveBounds = false;
            for (Microlog microlog : mDataSet) {
                if (microlog.getLatitude() != 0 && microlog.getLongitude() != 0) {
                    weHaveBounds = true;
                    if (microlog.getName() != null) {
                        markerName = microlog.getName();
                    }
                    snippetName = microlog.getSensorPhoneNumber();
                    LatLng latLng = new LatLng(microlog.getLatitude(), microlog.getLongitude());

                    boundsBuilder.include(latLng);

                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .snippet(snippetName)
                            .title(markerName));
                }

            }
            if (weHaveBounds) {
                mBounds = boundsBuilder.build();
            }
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void askForPermissions() {
        if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this.getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_CONTACTS},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);

            return;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    private void inflateAddDialogFragment() {

        askForPermissions();

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add, null);

        final EditText editTextNewPhoneNumber = (EditText) view.findViewById(R.id.editTextNewPhoneNumber);

        final EditText editTextName = (EditText) view.findViewById(R.id.editTextName);

        final EditText editTextMicrologID = view.findViewById(R.id.editTextID);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!editTextNewPhoneNumber.getText().toString().equalsIgnoreCase("")) {
                    Microlog microlog = new Microlog(
                            editTextNewPhoneNumber.getText().toString(),
                            editTextMicrologID.getText().toString(),
                            editTextName.getText().toString(),
                            "NORMAL",
                            3
                    );
                    microlog.save();
                    refreshRecyclerView();
                }

            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mAddDialog.dismiss();
            }
        });
        builder.setView(view);

        mAddDialog = builder.show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_map_and_list, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_help) {
            showShowcaseHelp();
        }
        return super.onOptionsItemSelected(item);
    }


    private void showShowcaseHelp() {

        // this is to put the button on the left
        RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lps.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        lps.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        int margin = ((Number) (getResources().getDisplayMetrics().density * 12)).intValue();
        lps.setMargins(margin, margin, margin, margin);

        mShowcaseView = new ShowcaseView.Builder(getActivity())
                .setTarget(new ViewTarget(getActivity().findViewById(R.id.fab_add)))
                .setContentText(getString(R.string.help_main_add))
                .setStyle(R.style.CustomShowcaseTheme4)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        switch (mShowCaseCounter) {
                            case 0:
                                mShowcaseView.setShowcase(new ViewTarget(getActivity().findViewById(R.id.my_recycler_view)), true);
                                mShowcaseView.setContentText(getString(R.string.help_main_list));
                                break;

                            case 1:
                                mShowcaseView.setShowcase(new ViewTarget(getActivity().findViewById(R.id.map)), true);
                                mShowcaseView.setContentText(getString(R.string.help_main_map));
                                break;
                            case 2:
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
