package com.example.weatherapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.R;
import com.example.weatherapp.models.DailyItem;
import com.example.weatherapp.utils.WeatherCodeIconMapper;
import com.example.weatherapp.utils.WeatherCodeMapper;

import java.util.ArrayList;
import java.util.List;

/** Step 68: RecyclerView adapter for the vertical 7-day forecast. */
public class DailyAdapter extends RecyclerView.Adapter<DailyAdapter.DayViewHolder> {

    private final List<DailyItem> items = new ArrayList<>();       // Step 68

    /** Step 68: replace the whole list (7 rows at most). */
    public void submitList(List<DailyItem> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();                                    // Step 68
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_daily, parent, false);      // Step 68
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        holder.bind(items.get(position));                          // Step 68
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /** Step 68: holds the three views of one day row. */
    static class DayViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvDayName;
        private final TextView tvDayRange;
        private final ImageView ivDayIcon;

        DayViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayName = itemView.findViewById(R.id.tvDayName);     // Step 68
            tvDayRange = itemView.findViewById(R.id.tvDayRange);   // Step 68
            ivDayIcon = itemView.findViewById(R.id.ivDayIcon);     // Step 68
        }

        void bind(DailyItem item) {

            tvDayName.setText(item.label);                         // Step 68: "Today" / "Wed"

            if (item.isToday) {                                    // Step 68: first row bold
                tvDayName.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                tvDayName.setTypeface(null, android.graphics.Typeface.NORMAL);
            }

            tvDayRange.setText(Math.round(item.high) + "° / "     // Step 68: high / low (step 70)
                    + Math.round(item.low) + "°");

            ivDayIcon.setImageResource(                            // Step 68: day icon
                    WeatherCodeIconMapper.iconFor(item.weatherCode, true));

            ivDayIcon.setContentDescription(                       // step 80: accessibility
                    WeatherCodeMapper.describe(item.weatherCode));
        }
    }
}
