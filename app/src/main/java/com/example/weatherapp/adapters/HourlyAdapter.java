package com.example.weatherapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.R;
import com.example.weatherapp.models.HourlyItem;
import com.example.weatherapp.utils.WeatherCodeIconMapper;
import com.example.weatherapp.utils.WeatherCodeMapper;

import java.util.ArrayList;
import java.util.List;

/** Step 63: RecyclerView adapter for the horizontal hourly strip. */
public class HourlyAdapter extends RecyclerView.Adapter<HourlyAdapter.HourViewHolder> {

    private final List<HourlyItem> items = new ArrayList<>();      // Step 63
    private boolean nightMode = false;                             // Step 79: dark palette flag

    /** Step 79: switch between the light and dark cell palette. */
    public void setNightMode(boolean night) {
        if (nightMode == night) return;
        nightMode = night;
        notifyDataSetChanged();
    }

    /** Step 63: replace the whole list (24 cells, few enough to rebind fully). */
    public void submitList(List<HourlyItem> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();                                    // Step 63
    }

    @NonNull
    @Override
    public HourViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hourly, parent, false);     // Step 63
        return new HourViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HourViewHolder holder, int position) {
        holder.bind(items.get(position), nightMode);               // Step 63
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /** Step 63: holds the three views of one hour cell. */
    static class HourViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvTime;
        private final TextView tvTemp;
        private final ImageView ivIcon;

        HourViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvHourTime);       // Step 63
            tvTemp = itemView.findViewById(R.id.tvHourTemp);       // Step 63
            ivIcon = itemView.findViewById(R.id.ivHourIcon);       // Step 63
        }

        void bind(HourlyItem item, boolean nightMode) {

            tvTime.setText(item.label);                            // Step 63: "NOW" / "3 PM"
            tvTemp.setText(Math.round(item.temperature) + "°");    // Step 63

            ivIcon.setImageResource(                                // Step 63: icon per hour
                    WeatherCodeIconMapper.iconFor(item.weatherCode, item.isDay));

            ivIcon.setContentDescription(                           // step 80: accessibility
                    WeatherCodeMapper.describe(item.weatherCode));

            if (item.isNow) {                                      // Step 63: highlight "NOW"
                itemView.setBackgroundResource(R.drawable.bg_hour_selected);
                tvTime.setTextColor(0xFFFFFFFF);
                tvTemp.setTextColor(0xFFFFFFFF);
            } else if (nightMode) {                                // Step 79: night palette
                itemView.setBackgroundResource(R.drawable.bg_hour_item_night);
                tvTime.setTextColor(0xFF8493A8);
                tvTemp.setTextColor(0xFFE6EDF7);
            } else {
                itemView.setBackgroundResource(R.drawable.bg_hour_item);
                tvTime.setTextColor(0xFF718096);
                tvTemp.setTextColor(0xFF172B4D);
            }
        }
    }
}
