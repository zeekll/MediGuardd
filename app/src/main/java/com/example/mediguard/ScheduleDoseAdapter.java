package com.example.mediguard;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Renders one row per dose occurrence for "Today's Schedule", with a
 * colored dot + pill badge for its status: Taken / Pending / Missed /
 * Upcoming (matches the Alerts & Reminder Flow mockup).
 */
public class ScheduleDoseAdapter
        extends RecyclerView.Adapter<ScheduleDoseAdapter.ViewHolder> {

    /** One row's display data. */
    public static class DoseRow {

        public final String scheduledAt;
        public final String timeLabel;
        public final String amountLabel;
        public final String status;

        public DoseRow(
                String scheduledAt,
                String timeLabel,
                String amountLabel,
                String status
        ) {
            this.scheduledAt = scheduledAt;
            this.timeLabel = timeLabel;
            this.amountLabel = amountLabel;
            this.status = status;
        }
    }

    private final Context context;
    private final List<DoseRow> rows;

    public ScheduleDoseAdapter(Context context, List<DoseRow> rows) {
        this.context = context;
        this.rows = rows;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(context).inflate(
                R.layout.item_schedule_dose,
                parent,
                false
        );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        DoseRow row = rows.get(position);

        holder.txtDoseTime.setText(row.timeLabel);
        holder.txtDoseAmount.setText(row.amountLabel);
        holder.txtDoseStatus.setText(capitalize(row.status));

        int color;
        int badgeBg;

        switch (row.status) {

            case DatabaseHelper.DOSE_STATUS_TAKEN:
                color = ContextCompat.getColor(context, R.color.greenBadge);
                badgeBg = ContextCompat.getColor(context, R.color.greenBadgeBg);
                break;

            case DatabaseHelper.DOSE_STATUS_MISSED:
                color = ContextCompat.getColor(context, R.color.missedBadgeText);
                badgeBg = ContextCompat.getColor(context, R.color.missedBadgeBg);
                break;

            case DatabaseHelper.DOSE_STATUS_PENDING:
                color = ContextCompat.getColor(context, R.color.pendingBadgeText);
                badgeBg = ContextCompat.getColor(context, R.color.pendingBadgeBg);
                break;

            default: // "Upcoming" (no dose_log row yet)
                color = ContextCompat.getColor(context, R.color.upcomingBadgeText);
                badgeBg = ContextCompat.getColor(context, R.color.upcomingBadgeBg);
                break;
        }

        holder.dotStatus.setBackgroundTintList(ColorStateList.valueOf(color));
        holder.txtDoseStatus.setTextColor(color);
        holder.txtDoseStatus.setBackgroundTintList(ColorStateList.valueOf(badgeBg));
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    private String capitalize(String status) {

        if (status == null || status.isEmpty()) {
            return "Upcoming";
        }

        return status.charAt(0)
                + status.substring(1).toLowerCase();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        View dotStatus;
        TextView txtDoseTime;
        TextView txtDoseAmount;
        TextView txtDoseStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            dotStatus = itemView.findViewById(R.id.dotStatus);
            txtDoseTime = itemView.findViewById(R.id.txtDoseTime);
            txtDoseAmount = itemView.findViewById(R.id.txtDoseAmount);
            txtDoseStatus = itemView.findViewById(R.id.txtDoseStatus);
        }
    }
}