package com.example.mediguard;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MediGuard.db";
    private static final int DATABASE_VERSION = 6;

    // =====================================================
    // USERS
    // =====================================================

    private static final String TABLE_USERS = "users";
    private static final String USER_ID = "id";
    private static final String USERNAME = "username";
    private static final String USER_CONTACT = "contact";
    private static final String USER_PASSWORD = "password";

    // =====================================================
    // MEDICINES
    // =====================================================

    private static final String TABLE_MEDICINES = "medicines";
    private static final String MEDICINE_ID = "medicine_id";
    private static final String MEDICINE_USER_ID = "user_id";
    private static final String MEDICINE_NAME = "medicine_name";
    private static final String MEDICINE_PURPOSE = "purpose";
    private static final String MEDICINE_TYPE = "type";
    private static final String MEDICINE_QUANTITY = "quantity";
    private static final String MEDICINE_UNIT = "unit";
    private static final String MEDICINE_EXPIRY = "expiry_date";
    private static final String MEDICINE_DOSAGE = "dosage";
    private static final String MEDICINE_PHOTO = "photo_path";

    // =====================================================
    // REMINDERS
    // =====================================================

    private static final String TABLE_REMINDERS =
            "medicine_reminders";

    private static final String REMINDER_ID =
            "reminder_id";

    private static final String REMINDER_USER_ID =
            "user_id";

    private static final String REMINDER_MEDICINE_ID =
            "medicine_id";

    private static final String REMINDER_DOSE =
            "dose";

    private static final String REMINDER_DOSE_UNIT =
            "dose_unit";

    private static final String REMINDER_START_TIME =
            "start_time";

    private static final String REMINDER_START_DATE =
            "start_date";

    private static final String REMINDER_DURATION_DAYS =
            "duration_days";

    private static final String REMINDER_SCHEDULE_TYPE =
            "schedule_type";

    private static final String REMINDER_CUSTOM_SCHEDULE =
            "custom_schedule";

    private static final String REMINDER_REPEAT_HOURS =
            "repeat_hours";

    private static final String REMINDER_DAYS =
            "days";

    private static final String REMINDER_CONTACT =
            "contact_number";

    private static final String REMINDER_ENABLED =
            "enabled";

    public DatabaseHelper(Context context) {
        super(
                context,
                DATABASE_NAME,
                null,
                DATABASE_VERSION
        );
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createUsersTable(db);
        createMedicinesTable(db);
        createReminderTable(db);
    }

    private void createUsersTable(SQLiteDatabase db) {

        String sql =
                "CREATE TABLE IF NOT EXISTS " +
                        TABLE_USERS +
                        " (" +
                        USER_ID +
                        " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        USERNAME +
                        " TEXT UNIQUE NOT NULL, " +
                        USER_CONTACT +
                        " TEXT, " +
                        USER_PASSWORD +
                        " TEXT NOT NULL" +
                        ")";

        db.execSQL(sql);
    }

    private void createMedicinesTable(SQLiteDatabase db) {

        String sql =
                "CREATE TABLE IF NOT EXISTS " +
                        TABLE_MEDICINES +
                        " (" +
                        MEDICINE_ID +
                        " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        MEDICINE_USER_ID +
                        " INTEGER NOT NULL, " +
                        MEDICINE_NAME +
                        " TEXT NOT NULL, " +
                        MEDICINE_PURPOSE +
                        " TEXT, " +
                        MEDICINE_TYPE +
                        " TEXT, " +
                        MEDICINE_QUANTITY +
                        " INTEGER, " +
                        MEDICINE_UNIT +
                        " TEXT, " +
                        MEDICINE_EXPIRY +
                        " TEXT, " +
                        MEDICINE_DOSAGE +
                        " TEXT, " +
                        MEDICINE_PHOTO +
                        " TEXT" +
                        ")";

        db.execSQL(sql);
    }

    private void createReminderTable(SQLiteDatabase db) {

        String sql =
                "CREATE TABLE IF NOT EXISTS " +
                        TABLE_REMINDERS +
                        " (" +
                        REMINDER_ID +
                        " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        REMINDER_USER_ID +
                        " INTEGER NOT NULL, " +
                        REMINDER_MEDICINE_ID +
                        " INTEGER NOT NULL UNIQUE, " +
                        REMINDER_DOSE +
                        " TEXT NOT NULL, " +
                        REMINDER_DOSE_UNIT +
                        " TEXT NOT NULL, " +
                        REMINDER_START_TIME +
                        " TEXT NOT NULL, " +
                        REMINDER_START_DATE +
                        " TEXT NOT NULL DEFAULT '', " +
                        REMINDER_REPEAT_HOURS +
                        " INTEGER NOT NULL, " +
                        REMINDER_DAYS +
                        " TEXT NOT NULL, " +
                        REMINDER_DURATION_DAYS +
                        " INTEGER NOT NULL DEFAULT 0, " +
                        REMINDER_SCHEDULE_TYPE +
                        " TEXT NOT NULL DEFAULT 'same', " +
                        REMINDER_CUSTOM_SCHEDULE +
                        " TEXT NOT NULL DEFAULT '', " +
                        REMINDER_CONTACT +
                        " TEXT NOT NULL, " +
                        REMINDER_ENABLED +
                        " INTEGER NOT NULL DEFAULT 1" +
                        ")";

        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion
    ) {
        ensureMedicineUserId(db);
        createReminderTable(db);
        ensureReminderColumns(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);

        /*
         * This also repairs a database that already reports
         * version 5 but is missing the reminder table.
         */
        ensureMedicineUserId(db);
        createReminderTable(db);
        ensureReminderColumns(db);
    }

    private void ensureReminderColumns(SQLiteDatabase db) {

        if (!tableExists(db, TABLE_REMINDERS)) {
            createReminderTable(db);
            return;
        }

        if (!columnExists(db, TABLE_REMINDERS, REMINDER_START_DATE)) {
            db.execSQL(
                    "ALTER TABLE " +
                            TABLE_REMINDERS +
                            " ADD COLUMN " +
                            REMINDER_START_DATE +
                            " TEXT NOT NULL DEFAULT ''"
            );
        }

        if (!columnExists(db, TABLE_REMINDERS, REMINDER_DURATION_DAYS)) {
            db.execSQL(
                    "ALTER TABLE " +
                            TABLE_REMINDERS +
                            " ADD COLUMN " +
                            REMINDER_DURATION_DAYS +
                            " INTEGER NOT NULL DEFAULT 0"
            );
        }

        if (!columnExists(db, TABLE_REMINDERS, REMINDER_SCHEDULE_TYPE)) {
            db.execSQL(
                    "ALTER TABLE " +
                            TABLE_REMINDERS +
                            " ADD COLUMN " +
                            REMINDER_SCHEDULE_TYPE +
                            " TEXT NOT NULL DEFAULT 'same'"
            );
        }

        if (!columnExists(db, TABLE_REMINDERS, REMINDER_CUSTOM_SCHEDULE)) {
            db.execSQL(
                    "ALTER TABLE " +
                            TABLE_REMINDERS +
                            " ADD COLUMN " +
                            REMINDER_CUSTOM_SCHEDULE +
                            " TEXT NOT NULL DEFAULT ''"
            );
        }
    }

    private void ensureMedicineUserId(SQLiteDatabase db) {

        if (!tableExists(db, TABLE_MEDICINES)) {
            createMedicinesTable(db);
            return;
        }

        if (columnExists(
                db,
                TABLE_MEDICINES,
                MEDICINE_USER_ID
        )) {
            return;
        }

        db.execSQL(
                "ALTER TABLE " +
                        TABLE_MEDICINES +
                        " ADD COLUMN " +
                        MEDICINE_USER_ID +
                        " INTEGER NOT NULL DEFAULT 0"
        );
    }

    private boolean tableExists(
            SQLiteDatabase db,
            String tableName
    ) {

        Cursor cursor =
                db.rawQuery(
                        "SELECT name FROM sqlite_master " +
                                "WHERE type='table' AND name=?",
                        new String[]{tableName}
                );

        boolean exists =
                cursor.moveToFirst();

        cursor.close();

        return exists;
    }

    private boolean columnExists(
            SQLiteDatabase db,
            String tableName,
            String columnName
    ) {

        Cursor cursor =
                db.rawQuery(
                        "PRAGMA table_info(" +
                                tableName +
                                ")",
                        null
                );

        boolean exists = false;

        while (cursor.moveToNext()) {

            String name =
                    cursor.getString(1);

            if (columnName.equals(name)) {
                exists = true;
                break;
            }
        }

        cursor.close();

        return exists;
    }

    // =====================================================
    // USERS
    // =====================================================

    public boolean registerUser(
            String username,
            String contact,
            String password
    ) {

        SQLiteDatabase db =
                getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put(
                USERNAME,
                username
        );

        values.put(
                USER_CONTACT,
                contact
        );

        values.put(
                USER_PASSWORD,
                password
        );

        long result =
                db.insert(
                        TABLE_USERS,
                        null,
                        values
                );

        return result != -1;
    }

    public boolean checkUsername(
            String username
    ) {

        SQLiteDatabase db =
                getReadableDatabase();

        Cursor cursor =
                db.rawQuery(
                        "SELECT " +
                                USER_ID +
                                " FROM " +
                                TABLE_USERS +
                                " WHERE " +
                                USERNAME +
                                "=?",
                        new String[]{username}
                );

        boolean exists =
                cursor.moveToFirst();

        cursor.close();

        return exists;
    }

    public boolean loginUser(
            String username,
            String password
    ) {

        SQLiteDatabase db =
                getReadableDatabase();

        Cursor cursor =
                db.rawQuery(
                        "SELECT " +
                                USER_ID +
                                " FROM " +
                                TABLE_USERS +
                                " WHERE " +
                                USERNAME +
                                "=? AND " +
                                USER_PASSWORD +
                                "=?",
                        new String[]{
                                username,
                                password
                        }
                );

        boolean success =
                cursor.moveToFirst();

        cursor.close();

        return success;
    }

    public int getUserId(
            String username
    ) {

        SQLiteDatabase db =
                getReadableDatabase();

        Cursor cursor =
                db.rawQuery(
                        "SELECT " +
                                USER_ID +
                                " FROM " +
                                TABLE_USERS +
                                " WHERE " +
                                USERNAME +
                                "=?",
                        new String[]{username}
                );

        int userId = -1;

        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0);
        }

        cursor.close();

        return userId;
    }

    public String getUserContact(
            int userId
    ) {

        SQLiteDatabase db =
                getReadableDatabase();

        Cursor cursor =
                db.rawQuery(
                        "SELECT " +
                                USER_CONTACT +
                                " FROM " +
                                TABLE_USERS +
                                " WHERE " +
                                USER_ID +
                                "=?",
                        new String[]{
                                String.valueOf(userId)
                        }
                );

        String contact = "";

        if (cursor.moveToFirst()) {
            contact = cursor.getString(0);
        }

        cursor.close();

        return contact;
    }

    // =====================================================
    // MEDICINES
    // =====================================================

    public boolean insertMedicine(
            int userId,
            String name,
            String purpose,
            String type,
            int quantity,
            String unit,
            String expiry,
            String photoPath
    ) {

        SQLiteDatabase db =
                getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put(
                MEDICINE_USER_ID,
                userId
        );

        values.put(
                MEDICINE_NAME,
                name
        );

        values.put(
                MEDICINE_PURPOSE,
                purpose
        );

        values.put(
                MEDICINE_TYPE,
                type
        );

        values.put(
                MEDICINE_QUANTITY,
                quantity
        );

        values.put(
                MEDICINE_UNIT,
                unit
        );

        values.put(
                MEDICINE_EXPIRY,
                expiry
        );

        values.putNull(
                MEDICINE_DOSAGE
        );

        values.put(
                MEDICINE_PHOTO,
                photoPath
        );

        long result =
                db.insert(
                        TABLE_MEDICINES,
                        null,
                        values
                );

        return result != -1;
    }

    public Cursor getAllMedicines(
            int userId
    ) {

        SQLiteDatabase db =
                getReadableDatabase();

        return db.query(
                TABLE_MEDICINES,
                null,
                MEDICINE_USER_ID +
                        "=?",
                new String[]{
                        String.valueOf(userId)
                },
                null,
                null,
                MEDICINE_ID +
                        " DESC"
        );
    }

    public boolean updateMedicine(
            int userId,
            int medicineId,
            String name,
            String purpose,
            String type,
            int quantity,
            String unit,
            String expiry,
            String photoPath
    ) {

        SQLiteDatabase db =
                getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put(
                MEDICINE_NAME,
                name
        );

        values.put(
                MEDICINE_PURPOSE,
                purpose
        );

        values.put(
                MEDICINE_TYPE,
                type
        );

        values.put(
                MEDICINE_QUANTITY,
                quantity
        );

        values.put(
                MEDICINE_UNIT,
                unit
        );

        values.put(
                MEDICINE_EXPIRY,
                expiry
        );

        values.putNull(
                MEDICINE_DOSAGE
        );

        values.put(
                MEDICINE_PHOTO,
                photoPath
        );

        int result =
                db.update(
                        TABLE_MEDICINES,
                        values,
                        MEDICINE_USER_ID +
                                "=? AND " +
                                MEDICINE_ID +
                                "=?",
                        new String[]{
                                String.valueOf(userId),
                                String.valueOf(medicineId)
                        }
                );

        return result > 0;
    }

    public boolean deleteMedicine(
            int userId,
            int medicineId
    ) {

        SQLiteDatabase db =
                getWritableDatabase();

        db.delete(
                TABLE_REMINDERS,
                REMINDER_USER_ID +
                        "=? AND " +
                        REMINDER_MEDICINE_ID +
                        "=?",
                new String[]{
                        String.valueOf(userId),
                        String.valueOf(medicineId)
                }
        );

        int result =
                db.delete(
                        TABLE_MEDICINES,
                        MEDICINE_USER_ID +
                                "=? AND " +
                                MEDICINE_ID +
                                "=?",
                        new String[]{
                                String.valueOf(userId),
                                String.valueOf(medicineId)
                        }
                );

        return result > 0;
    }

    // =====================================================
    // REMINDERS
    // =====================================================

    public boolean saveReminder(
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

        SQLiteDatabase db =
                getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put(
                REMINDER_USER_ID,
                userId
        );

        values.put(
                REMINDER_MEDICINE_ID,
                medicineId
        );

        values.put(
                REMINDER_DOSE,
                dose
        );

        values.put(
                REMINDER_DOSE_UNIT,
                doseUnit
        );

        values.put(
                REMINDER_START_TIME,
                startTime
        );

        values.put(
                REMINDER_START_DATE,
                startDate
        );

        values.put(
                REMINDER_REPEAT_HOURS,
                repeatHours
        );

        values.put(
                REMINDER_DAYS,
                days
        );

        values.put(
                REMINDER_DURATION_DAYS,
                durationDays
        );

        values.put(
                REMINDER_SCHEDULE_TYPE,
                scheduleType
        );

        values.put(
                REMINDER_CUSTOM_SCHEDULE,
                customSchedule
        );

        values.put(
                REMINDER_CONTACT,
                contactNumber
        );

        values.put(
                REMINDER_ENABLED,
                enabled ? 1 : 0
        );

        long result =
                db.insertWithOnConflict(
                        TABLE_REMINDERS,
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_REPLACE
                );

        return result != -1;
    }

    public Cursor getReminderForMedicine(
            int userId,
            int medicineId
    ) {

        SQLiteDatabase db =
                getReadableDatabase();

        return db.query(
                TABLE_REMINDERS,
                null,
                REMINDER_USER_ID +
                        "=? AND " +
                        REMINDER_MEDICINE_ID +
                        "=?",
                new String[]{
                        String.valueOf(userId),
                        String.valueOf(medicineId)
                },
                null,
                null,
                null
        );
    }

    public boolean hasReminder(
            int userId,
            int medicineId
    ) {

        Cursor cursor =
                getReminderForMedicine(
                        userId,
                        medicineId
                );

        boolean exists =
                cursor.moveToFirst();

        cursor.close();

        return exists;
    }
}
