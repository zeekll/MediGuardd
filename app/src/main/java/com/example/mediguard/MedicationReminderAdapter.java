package com.example.mediguard;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

public class MedicationReminderAdapter
        extends RecyclerView.Adapter<MedicationReminderAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<Medicine> medicines;

    public MedicationReminderAdapter(
            Context context,
            ArrayList<Medicine> medicines
    ) {
        this.context = context;
        this.medicines = medicines;
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

            Intent intent = new Intent(
                    context,
                    SelectMedicineActivity.class
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
                    "medicine_purpose",
                    medicine.getPurpose()
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
                    "medicine_expiry",
                    medicine.getExpiryDate()
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
        }
    }
}
