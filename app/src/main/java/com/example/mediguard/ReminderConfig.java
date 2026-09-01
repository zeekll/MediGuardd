package com.example.mediguard;

public class ReminderConfig {

    private final int reminderId;
    private final int userId;
    private final int medicineId;
    private final String dose;
    private final String doseUnit;
    private final String startTime;
    private final String startDate;
    private final int repeatHours;
    private final String days;
    private final int durationDays;
    private final String scheduleType;
    private final String customSchedule;
    private final String contactNumber;
    private final boolean enabled;

    public ReminderConfig(
            int reminderId,
            int userId,
            int medicineId,
            String dose,
            String doseUnit,
            String startTime,
            String startDate,
            int repeatHours,
            String days,
            int durationDays,
            String scheduleType,
            String customSchedule,
            String contactNumber,
            boolean enabled
    ) {
        this.reminderId = reminderId;
        this.userId = userId;
        this.medicineId = medicineId;
        this.dose = dose;
        this.doseUnit = doseUnit;
        this.startTime = startTime;
        this.startDate = startDate;
        this.repeatHours = repeatHours;
        this.days = days;
        this.durationDays = durationDays;
        this.scheduleType = scheduleType;
        this.customSchedule = customSchedule;
        this.contactNumber = contactNumber;
        this.enabled = enabled;
    }

    public int getReminderId() {
        return reminderId;
    }

    public int getUserId() {
        return userId;
    }

    public int getMedicineId() {
        return medicineId;
    }

    public String getDose() {
        return dose;
    }

    public String getDoseUnit() {
        return doseUnit;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getStartDate() {
        return startDate;
    }

    public int getRepeatHours() {
        return repeatHours;
    }

    public String getDays() {
        return days;
    }

    public int getDurationDays() {
        return durationDays;
    }

    public String getScheduleType() {
        return scheduleType;
    }

    public String getCustomSchedule() {
        return customSchedule;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public boolean isEnabled() {
        return enabled;
    }
}