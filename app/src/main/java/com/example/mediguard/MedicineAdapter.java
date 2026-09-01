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

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder> {

    private Context context;
    private ArrayList<Medicine> medicineList;

    public MedicineAdapter(Context context, ArrayList<Medicine> medicineList) {
        this.context = context;
        this.medicineList = medicineList;
    }

    @NonNull
    @Override
    public MedicineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_medicine, parent, false);

        return new MedicineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicineViewHolder holder, int position) {

        Medicine medicine = medicineList.get(position);

        holder.txtMedicineName.setText(medicine.getName());

        holder.txtMedicineType.setText(
                medicine.getType() + " • "
                        + medicine.getQuantity() + " "
                        + medicine.getUnit());

        holder.txtExpiry.setText(
                "Expires: " + medicine.getExpiryDate());

        // Photo thumbnail (falls back to the pill icon if none saved)
        String photoPath = medicine.getPhotoPath();

        if (!TextUtils.isEmpty(photoPath) && new File(photoPath).exists()) {

            Bitmap thumbnail = decodeSampledBitmap(photoPath, 150, 150);

            if (thumbnail != null) {
                holder.imgMedicine.setImageTintList(null);
                holder.imgMedicine.setImageBitmap(thumbnail);
            } else {
                holder.imgMedicine.setImageResource(R.drawable.ic_pill);
            }

        } else {
            holder.imgMedicine.setImageResource(R.drawable.ic_pill);
        }

        // Open Details
        holder.itemView.setOnClickListener(v -> {

            Intent intent = new Intent(context, MedicineDetailsActivity.class);

            intent.putExtra("id", medicine.getId());
            intent.putExtra("name", medicine.getName());
            intent.putExtra("purpose", medicine.getPurpose());
            intent.putExtra("type", medicine.getType());
            intent.putExtra("quantity", medicine.getQuantity());
            intent.putExtra("unit", medicine.getUnit());
            intent.putExtra("expiry", medicine.getExpiryDate());
            intent.putExtra("photo", medicine.getPhotoPath());

            context.startActivity(intent);

        });

    }

    @Override
    public int getItemCount() {
        return medicineList.size();
    }

    // Decodes a bitmap at a reduced size so large camera photos load
    // reliably as small list thumbnails instead of failing or wasting memory.
    private Bitmap decodeSampledBitmap(String path, int reqWidth, int reqHeight) {

        BitmapFactory.Options boundsOptions = new BitmapFactory.Options();
        boundsOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, boundsOptions);

        int sampleSize = 1;
        int halfHeight = boundsOptions.outHeight / 2;
        int halfWidth = boundsOptions.outWidth / 2;

        while ((halfHeight / sampleSize) >= reqHeight
                && (halfWidth / sampleSize) >= reqWidth) {
            sampleSize *= 2;
        }

        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        decodeOptions.inSampleSize = sampleSize;

        return BitmapFactory.decodeFile(path, decodeOptions);
    }

    public static class MedicineViewHolder extends RecyclerView.ViewHolder {

        ImageView imgMedicine;
        TextView txtMedicineName;
        TextView txtMedicineType;
        TextView txtExpiry;

        public MedicineViewHolder(@NonNull View itemView) {
            super(itemView);

            imgMedicine = itemView.findViewById(R.id.imgMedicine);
            txtMedicineName = itemView.findViewById(R.id.txtMedicineName);
            txtMedicineType = itemView.findViewById(R.id.txtMedicineType);
            txtExpiry = itemView.findViewById(R.id.txtExpiry);
        }
    }
}