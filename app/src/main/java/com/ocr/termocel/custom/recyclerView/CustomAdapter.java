/*
* Copyright (C) 2014 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.ocr.termocel.custom.recyclerView;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ocr.termocel.R;
import com.ocr.termocel.SensorSelectorActivity;
import com.ocr.termocel.events.SensorClickedEvent;
import com.ocr.termocel.model.Microlog;

import java.util.List;

/**
 * Provide views to RecyclerView with data from mDataSet.
 */
public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
    private static final String TAG = "CustomAdapter";

    private List<Microlog> mDataSet;

    // BEGIN_INCLUDE(recyclerViewSampleViewHolder)

    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewName;
        private final TextView textViewNumber;
        private final ImageButton imageButtonClear;

        public ViewHolder(View v) {
            super(v);
            // Define click listener for the ViewHolder's View.
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SensorSelectorActivity.bus.post(new SensorClickedEvent(SensorClickedEvent.Type.STARTED, 1, getPosition(), false));
//                    Logger.d("Element " + getPosition() + " clicked.");
                }
            });
            textViewName = (TextView) v.findViewById(R.id.textViewName);
            textViewNumber = (TextView) v.findViewById(R.id.textViewNumber);
            imageButtonClear = (ImageButton) v.findViewById(R.id.imageButtonClear);
            imageButtonClear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Log.d(TAG, "clear clicked" + getPosition());
                    SensorSelectorActivity.bus.post(new SensorClickedEvent(SensorClickedEvent.Type.STARTED, 1, getPosition(), true));
                }
            });
        }

        public TextView getTextViewName() {
            return textViewName;
        }

        public TextView getTextViewNumber() {
            return textViewNumber;
        }
    }
    // END_INCLUDE(recyclerViewSampleViewHolder)

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet a list with markers
     */
    public CustomAdapter(List<Microlog> dataSet) {
        mDataSet = dataSet;
    }

    // BEGIN_INCLUDE(recyclerViewOnCreateViewHolder)
    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.text_row_item, viewGroup, false);

        return new ViewHolder(v);
    }
    // END_INCLUDE(recyclerViewOnCreateViewHolder)

    // BEGIN_INCLUDE(recyclerViewOnBindViewHolder)
    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
//        Logger.d("Element " + position + " set.");

        // Get element from your dataset at this position and replace the contents of the view
        // with that element
        viewHolder.getTextViewName().setText(mDataSet.get(position).name);
        viewHolder.getTextViewNumber().setText(mDataSet.get(position).sensorPhoneNumber);
    }
    // END_INCLUDE(recyclerViewOnBindViewHolder)

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataSet.size();
    }
}
