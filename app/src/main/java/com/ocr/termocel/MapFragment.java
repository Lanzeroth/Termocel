package com.ocr.termocel;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.activeandroid.query.Select;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.ocr.termocel.custom.recyclerView.CustomAdapter;
import com.ocr.termocel.custom.recyclerView.EmptyRecyclerView;
import com.ocr.termocel.model.Microlog;
import com.ocr.termocel.utilities.AndroidBus;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment {

    private final String TAG = MapFragment.class.getSimpleName();

//    public static List<MessagesLocation> mLocations;

    private static View view;

    public static Bus mapBus;


    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

//    private HashMap<String, GoLockyMarker> eventMarkerMap;

//    public static GoLockyMarker goLockyMarker;

    private LatLng mLatLong;

    private AlertDialog mMarkerDialog = null;
    private AlertDialog mAddDialog = null;


    public MapFragment() {
        // Required empty public constructor
    }

    @Bind(R.id.my_recycler_view)
    EmptyRecyclerView mRecyclerView;


//    @OnClick(R.id.fabAdd)
//    public void addFabClicked() {
//        inflateAddDialogFragment();
//    }


    protected CustomAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected List<Microlog> mDataSet;


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
            ButterKnife.bind(this, view);

        } catch (InflateException e) {
            e.printStackTrace();
        /* map is already there, just return view as it is */
        }

        setUpMapIfNeeded();

        initDataSet();

        if (mDataSet != null) {
            // use a linear layout manager
            mLayoutManager = new LinearLayoutManager(getActivity());
            mRecyclerView.setLayoutManager(mLayoutManager);

            mAdapter = new CustomAdapter(mDataSet);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mRecyclerView.setVisibility(View.GONE);
        }


        fabOverlayThing();

        return view;
    }

    private void fabOverlayThing() {
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
        initDataSet();

        mLayoutManager = new LinearLayoutManager(getActivity());
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

    @Subscribe
    public void handleLocation(LatLng latLng) {
        mLatLong = latLng;
        CameraUpdate cameraUpdateFactory = CameraUpdateFactory.newLatLngZoom(latLng, 17);
        mMap.animateCamera(cameraUpdateFactory);
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
            mMap = ((SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
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
        mMap.setMyLocationEnabled(true);

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
//                goLockyMarker = eventMarkerMap.get(marker.getId());
//                inflateMarkerClickedDialog(goLockyMarker);
            }
        });

        // Initiate loadings

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
//                MainActivity.bus.post(new StartRegisteringUserEvent(StartRegisteringUserEvent.Type.STARTED, 1));
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
//        if (mMap != null && mLocations != null && !mLocations.isEmpty()) {
//            populateTheMap(mLocations);
//            if (mLatLong != null && mLatLong.latitude != 0.0 && mLatLong.longitude != 0.0) {
//                CameraUpdate cameraUpdateFactory = CameraUpdateFactory.newLatLngZoom(mLatLong, 17);
//                mMap.animateCamera(cameraUpdateFactory);
//            }
//        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }


    private void inflateAddDialogFragment() {
//        LayoutInflater inflater = getActivity().getLayoutInflater();
//        View view = inflater.inflate(R.layout.fragment_screen_slide_page, null);
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        builder.setView(view);
//
//        mAddDialog = builder.show();
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
}
