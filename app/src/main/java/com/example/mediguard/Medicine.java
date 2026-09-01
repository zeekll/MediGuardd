package com.example.mediguard;

public class Medicine {

    private int id;
    private int userId;
    private String name;
    private String purpose;
    private String type;
    private int quantity;
    private String unit;
    private String expiryDate;
    private String photoPath;

    public Medicine(
            int id,
            int userId,
            String name,
            String purpose,
            String type,
            int quantity,
            String unit,
            String expiryDate,
            String photoPath
    ) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.purpose = purpose;
        this.type = type;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
        this.photoPath = photoPath;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getPurpose() {
        return purpose;
    }

    public String getType() {
        return type;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public String getPhotoPath() {
        return photoPath;
    }
}
