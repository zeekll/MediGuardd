package com.example.mediguard;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MedicationReminderAdapter
        extends RecyclerView.Adapter<MedicationReminderAdapter.ViewHolder> {

    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("MMM d, yyyy", Locale.US);

    private final Context context;
    private final ArrayList<Medicine> medicines;
    private final DatabaseHelper databaseHelper;

    public MedicationReminderAdapter(
            Context context,
            ArrayList<Medicine> medicines
    ) {
        this.context = context;
        this.medicines = medicines;
        this.databaseHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(context).inflate(
                R.layout.item_reminder_medicine,
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
        Medicine medicine = medicines.get(position);

        holder.txtMedicineName.setText(
                medicine.getName()
        );

        holder.txtMedicineType.setText(
                medicine.getType()
                        + " • "
                        + medicine.getQuantity()
                        + " "
                        + medicine.getUnit()
        );

        ReminderConfig config = databaseHelper.getReminderConfig(
                medicine.getUserId(),
                medicine.getId()
        );

        if (config != null) {

            holder.txtDoseSchedule.setText(
                    config.getDose()
                            + " " + config.getDoseUnit()
                            + "  •  Every " + config.getRepeatHours() + " hours"
            );

            holder.txtDateRange.setText(
                    formatDateRange(config.getStartDate(), config.getDurationDays())
            );

            boolean enabled = config.isEnabled();

            holder.txtReminderStatus.setText(
                    enabled ? "Reminder ON" : "Reminder OFF"
            );

            int statusColor = ContextCompat.getColor(
                    context,
                    enabled ? R.color.greenBadge : R.color.missedBadgeText
            );

            holder.txtReminderStatus.setTextColor(statusColor);
            holder.dotReminderStatus.setBackgroundTintList(
                    ColorStateList.valueOf(statusColor)
            );

        } else {

            holder.txtDoseSchedule.setText("");
            holder.txtDateRange.setText("");
            holder.txtReminderStatus.setText("");
        }

        String photoPath = medicine.getPhotoPath();

        if (!TextUtils.isEmpty(photoPath)
                && new File(photoPath).exists()) {

            Bitmap bitmap = decodeSampledBitmap(
                    photoPath,
                    120,
                    120
            );

            if (bitmap != null) {
                holder.imgMedicine.setImageTintList(null);
                holder.imgMedicine.setImageBitmap(bitmap);
            } else {
                setDefaultIcon(holder.imgMedicine);
            }

        } else {
            setDefaultIcon(holder.imgMedicine);
        }

        holder.itemView.setOnClickListener(v -> {

            // A saved reminder already exists for medicines shown in
            // this Alerts list, so tapping one opens its schedule
            // (view/mark-as-taken/missed/on-off) instead of the
            // "set up a new reminder" flow.
            Intent intent = new Intent(
                    context,
                    ReminderDetailsActivity.class
            );

            intent.putExtra(
                    "medicine_id",
                    medicine.getId()
            );

            intent.putExtra(
                    "user_id",
                    medicine.getUserId()
            );

            intent.putExtra(
                    "medicine_name",
                    medicine.getName()
            );

            intent.putExtra(
                    "medicine_type",
                    medicine.getType()
            );

            intent.putExtra(
                    "medicine_quantity",
                    medicine.getQuantity()
                            + " "
                            + medicine.getUnit()
            );

            intent.putExtra(
                    "medicine_photo",
                    medicine.getPhotoPath()
            );

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return medicines.size();
    }

    /** "Aug 21, 2026" + 14 days -> "Aug 21 – Sep 3, 2026" */
    private String formatDateRange(String startDate, int durationDays) {

        try {

            Calendar start = Calendar.getInstance();
            start.setTime(DATE_FORMAT.parse(startDate));

            Calendar end = (Calendar) start.clone();
            end.add(Calendar.DAY_OF_YEAR, Math.max(durationDays - 1, 0));

            return DATE_FORMAT.format(start.getTime())
                    + " – "
                    + DATE_FORMAT.format(end.getTime());

        } catch (ParseException e) {
            return startDate;
        }
    }

    private void setDefaultIcon(ImageView imageView) {
        imageView.setImageResource(
                R.drawable.ic_pill
        );
        imageView.setImageTintList(
                android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.rgb(
                                21,
                                101,
                                249
                        )
                )
        );
    }

    private Bitmap decodeSampledBitmap(
            String path,
            int reqWidth,
            int reqHeight
    ) {
        BitmapFactory.Options bounds =
                new BitmapFactory.Options();

        bounds.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(
                path,
                bounds
        );

        int sampleSize = 1;

        while (
                bounds.outWidth / sampleSize > reqWidth * 2
                        &&
                        bounds.outHeight / sampleSize > reqHeight * 2
        ) {
            sampleSize *= 2;
        }

        BitmapFactory.Options options =
                new BitmapFactory.Options();

        options.inSampleSize = sampleSize;

        return BitmapFactory.decodeFile(
                path,
                options
        );
    }

    static class ViewHolder
            extends RecyclerView.ViewHolder {

        ImageView imgMedicine;
        TextView txtMedicineName;
        TextView txtMedicineType;
        TextView txtDoseSchedule;
        TextView txtDateRange;
        View dotReminderStatus;
        TextView txtReminderStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgMedicine = itemView.findViewById(
                    R.id.imgMedicine
            );

            txtMedicineName = itemView.findViewById(
                    R.id.txtMedicineName
            );

            txtMedicineType = itemView.findViewById(
                    R.id.txtMedicineType
            );

            txtDoseSchedule = itemView.findViewById(
                    R.id.txtDoseSchedule
            );

            txtDateRange = itemView.findViewById(
                    R.id.txtDateRange
            );

            dotReminderStatus = itemView.findViewById(
                    R.id.dotReminderStatus
            );

            txtReminderStatus = itemView.findViewById(
                    R.id.txtReminderStatus
            );
        }
    }
}