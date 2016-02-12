package com.ocr.termocel;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ocr.termocel.custom.tabs.SlidingTabLayout;
import com.ocr.termocel.custom.tabs.ViewPagerAdapter;
import com.ocr.termocel.events.SensorClickedEvent;
import com.ocr.termocel.utilities.AndroidBus;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TabFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TabFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TabFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;


    public static Bus bus;

    ViewPager pager;
    ViewPagerAdapter adapter;
    SlidingTabLayout tabs;
    CharSequence Titles[];
    int numberOfTabs = 2;

    public TabFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TabFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TabFragment newInstance(String param1, String param2) {
        TabFragment fragment = new TabFragment();
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
        View view = inflater.inflate(R.layout.fragment_tab, container, false);

        bus = new AndroidBus();
        bus.register(this);

        if (isAdded()) {
            Titles = getResources().getStringArray(R.array.tab_titles);
            // Creating The ViewPagerAdapter and Passing Fragment Manager, Titles fot the Tabs and Number Of Tabs.
            adapter = new ViewPagerAdapter(getChildFragmentManager(), Titles, numberOfTabs);

            // Assigning ViewPager View and setting the adapter
            pager = (ViewPager) view.findViewById(R.id.tabsPager);
            pager.setAdapter(adapter);

            // Assigning the Sliding Tab Layout View
            tabs = (SlidingTabLayout) view.findViewById(R.id.tabs);
            tabs.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width

            tabs.setSelectedIndicatorColors(getResources().getColor(R.color.tabsScrollColor));

            tabs.setCustomTabView(R.layout.tab_indicator, android.R.id.text1);

            // Setting the ViewPager For the SlidingTabsLayout
            tabs.setViewPager(pager);
        }
        return view;
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    /**
     * comes from the custom adapter, what we want is the element id to pass into the next activity
     *
     * @param event
     */
    @Subscribe
    public void RecyclerItemClicked(SensorClickedEvent event) {
        if (event.getResultCode() == 1) {
            if (event.isDelete()) {
                new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.dialog_title_delete))
                        .setMessage(getString(R.string.dialog_message_confirm_delete))
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.dialog_yes_delete), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
//                                deleteMicrolog(event.getElementId());
                            }
                        })
                        .setNegativeButton(getString(R.string.dialog_no), null)
                        .show();
            } else {
                pager.setCurrentItem(2);
//                Intent intent = new Intent(this, MainActivity.class);
//                intent.putExtra(Constants.EXTRA_COMES_FROM_RECEIVER, false);
//                intent.putExtra(Constants.EXTRA_SELECTED_ID, event.getElementId());
//                startActivity(intent);
            }
        }
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
