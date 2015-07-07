package com.ocr.termocel.custom.recyclerView;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ocr.termocel.R;
import com.ocr.termocel.model.Temperature;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Provide views to RecyclerView with data from mDataSet.
 */
public class TemperatureAdapter extends RecyclerView.Adapter<TemperatureAdapter.ViewHolder> {
    private static final String TAG = "CustomAdapter";

    private List<Temperature> mDataSet;

    Context context;

    // BEGIN_INCLUDE(recyclerViewSampleViewHolder)

    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewTempStatus;
        private final TextView textViewTempF;
        private final TextView textViewTempHumidity;
        private final TextView textViewTempTime;

        public ViewHolder(View v) {
            super(v);
            // Define click listener for the ViewHolder's View.
//            v.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    SensorSelectorActivity.bus.post(new SensorClickedEvent(SensorClickedEvent.Type.STARTED, 1, getPosition()));
////                    Logger.d("Element " + getPosition() + " clicked.");
//                }
//            });
            textViewTempStatus = (TextView) v.findViewById(R.id.textViewTempStatus);
            textViewTempF = (TextView) v.findViewById(R.id.textViewTempF);
            textViewTempHumidity = (TextView) v.findViewById(R.id.textViewTempHumidity);
            textViewTempTime = (TextView) v.findViewById(R.id.textViewTempTime);
        }

        public TextView getTextViewTempStatus() {
            return textViewTempStatus;
        }

        public TextView getTextViewTempF() {
            return textViewTempF;
        }

        public TextView getTextViewTempHumidity() {
            return textViewTempHumidity;
        }

        public TextView getTextViewTempTime() {
            return textViewTempTime;
        }
    }
    // END_INCLUDE(recyclerViewSampleViewHolder)

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet a list with markers
     */
    public TemperatureAdapter(List<Temperature> dataSet, Context newContext) {
        mDataSet = dataSet;
        context = newContext;
    }

    // BEGIN_INCLUDE(recyclerViewOnCreateViewHolder)
    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.temperature_row_item, viewGroup, false);

        return new ViewHolder(v);
    }
    // END_INCLUDE(recyclerViewOnCreateViewHolder)

    // BEGIN_INCLUDE(recyclerViewOnBindViewHolder)
    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the contents of the view
        // with that element
        String statusString = mDataSet.get(position).status;
        switch (statusString) {
            case "Normal":
                viewHolder.getTextViewTempStatus().setTextColor(context.getResources().getColor(R.color.green_700));
                break;
            case "Atencion":
                viewHolder.getTextViewTempStatus().setTextColor(context.getResources().getColor(R.color.yellow_700));
                break;
            case "Advertencia":
                viewHolder.getTextViewTempStatus().setTextColor(context.getResources().getColor(R.color.orange_500));
                break;
            case "Alarma":
                viewHolder.getTextViewTempStatus().setTextColor(context.getResources().getColor(R.color.red_600));
                break;
        }
        viewHolder.getTextViewTempStatus().setText(mDataSet.get(position).status);
        viewHolder.getTextViewTempF().setText(String.valueOf(mDataSet.get(position).tempInFahrenheit));
        viewHolder.getTextViewTempHumidity().setText(String.valueOf(mDataSet.get(position).humidity));

        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(mDataSet.get(position).timestamp);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM HH:mm", java.util.Locale.getDefault());
        simpleDateFormat.setCalendar(calendar);

        viewHolder.getTextViewTempTime().setText(simpleDateFormat.format(calendar.getTime()));
    }
    // END_INCLUDE(recyclerViewOnBindViewHolder)

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataSet.size();
    }
}
