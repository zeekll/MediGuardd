package com.example.mediguard;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MediGuard.db";
    private static final int DATABASE_VERSION = 7;

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

    // =====================================================
    // DOSE LOGS (per-occurrence status: PENDING / TAKEN / MISSED)
    // =====================================================

    private static final String TABLE_DOSE_LOGS =
            "dose_logs";

    private static final String DOSE_LOG_ID =
            "log_id";

    private static final String DOSE_LOG_USER_ID =
            "user_id";

    private static final String DOSE_LOG_MEDICINE_ID =
            "medicine_id";

    private static final String DOSE_LOG_SCHEDULED_AT =
            "scheduled_at";

    private static final String DOSE_LOG_STATUS =
            "status";

    private static final String DOSE_LOG_TAKEN_AT =
            "taken_at";

    public static final String DOSE_STATUS_PENDING = "PENDING";
    public static final String DOSE_STATUS_TAKEN = "TAKEN";
    public static final String DOSE_STATUS_MISSED = "MISSED";

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
        createDoseLogTable(db);
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

    private void createDoseLogTable(SQLiteDatabase db) {

        String sql =
                "CREATE TABLE IF NOT EXISTS " +
                        TABLE_DOSE_LOGS +
                        " (" +
                        DOSE_LOG_ID +
                        " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        DOSE_LOG_USER_ID +
                        " INTEGER NOT NULL, " +
                        DOSE_LOG_MEDICINE_ID +
                        " INTEGER NOT NULL, " +
                        DOSE_LOG_SCHEDULED_AT +
                        " TEXT NOT NULL, " +
                        DOSE_LOG_STATUS +
                        " TEXT NOT NULL DEFAULT '" +
                        DOSE_STATUS_PENDING +
                        "', " +
                        DOSE_LOG_TAKEN_AT +
                        " TEXT, " +
                        "UNIQUE(" +
                        DOSE_LOG_MEDICINE_ID +
                        ", " +
                        DOSE_LOG_SCHEDULED_AT +
                        ")" +
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
        createDoseLogTable(db);
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
        createDoseLogTable(db);
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

    /**
     * Looks up a medicine's display name by id (no user_id needed —
     * medicine_id is unique). Returns null if the medicine doesn't
     * exist, e.g. it was deleted after an alarm was already scheduled.
     */
    public String getMedicineName(int medicineId) {

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_MEDICINES,
                new String[]{MEDICINE_NAME},
                MEDICINE_ID + "=?",
                new String[]{String.valueOf(medicineId)},
                null,
                null,
                null
        );

        String name = null;

        if (cursor.moveToFirst()) {
            name = cursor.getString(
                    cursor.getColumnIndexOrThrow(MEDICINE_NAME)
            );
        }

        cursor.close();

        return name;
    }

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

    /**
     * Medicines that already have a saved reminder (used by the
     * Alerts list, so tapping an entry there always opens a real
     * reminder instead of "This medicine has no saved reminder").
     */
    public Cursor getMedicinesWithReminders(
            int userId
    ) {

        SQLiteDatabase db =
                getReadableDatabase();

        String sql =
                "SELECT m.* FROM " +
                        TABLE_MEDICINES + " m " +
                        "INNER JOIN " + TABLE_REMINDERS + " r " +
                        "ON m." + MEDICINE_ID + " = r." + REMINDER_MEDICINE_ID +
                        " WHERE m." + MEDICINE_USER_ID + " = ? " +
                        "ORDER BY m." + MEDICINE_ID + " DESC";

        return db.rawQuery(
                sql,
                new String[]{String.valueOf(userId)}
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

    /**
     * Flips only the ON/OFF switch for a medicine's saved reminder
     * (used by the reminder detail screen's quick toggle). The
     * schedule itself is left untouched -- turning OFF just stops
     * future alerts/SMS; the reminder stays saved so it can be
     * turned back ON later.
     */
    public boolean setReminderEnabled(
            int userId,
            int medicineId,
            boolean enabled
    ) {

        SQLiteDatabase db =
                getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put(
                REMINDER_ENABLED,
                enabled ? 1 : 0
        );

        int result =
                db.update(
                        TABLE_REMINDERS,
                        values,
                        REMINDER_USER_ID +
                                "=? AND " +
                                REMINDER_MEDICINE_ID +
                                "=?",
                        new String[]{
                                String.valueOf(userId),
                                String.valueOf(medicineId)
                        }
                );

        return result > 0;
    }

    /**
     * Deletes only the reminder/schedule for a medicine (the medicine
     * itself and its dose history are left alone). Used by "Delete
     * Reminder" on the reminder detail screen.
     */
    public boolean deleteReminder(
            int userId,
            int medicineId
    ) {

        SQLiteDatabase db =
                getWritableDatabase();

        int result =
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

        return result > 0;
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

    /**
     * Returns the saved reminder for one medicine as a ReminderConfig,
     * or null if this medicine has no reminder set up.
     */
    public ReminderConfig getReminderConfig(
            int userId,
            int medicineId
    ) {

        Cursor cursor =
                getReminderForMedicine(
                        userId,
                        medicineId
                );

        ReminderConfig config = null;

        if (cursor.moveToFirst()) {

            config = new ReminderConfig(
                    cursor.getInt(
                            cursor.getColumnIndexOrThrow(REMINDER_ID)
                    ),
                    cursor.getInt(
                            cursor.getColumnIndexOrThrow(REMINDER_USER_ID)
                    ),
                    cursor.getInt(
                            cursor.getColumnIndexOrThrow(REMINDER_MEDICINE_ID)
                    ),
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(REMINDER_DOSE)
                    ),
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(REMINDER_DOSE_UNIT)
                    ),
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(REMINDER_START_TIME)
                    ),
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(REMINDER_START_DATE)
                    ),
                    cursor.getInt(
                            cursor.getColumnIndexOrThrow(REMINDER_REPEAT_HOURS)
                    ),
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(REMINDER_DAYS)
                    ),
                    cursor.getInt(
                            cursor.getColumnIndexOrThrow(REMINDER_DURATION_DAYS)
                    ),
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(REMINDER_SCHEDULE_TYPE)
                    ),
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(REMINDER_CUSTOM_SCHEDULE)
                    ),
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(REMINDER_CONTACT)
                    ),
                    cursor.getInt(
                            cursor.getColumnIndexOrThrow(REMINDER_ENABLED)
                    ) == 1
            );
        }

        cursor.close();

        return config;
    }

    // =====================================================
    // DOSE LOGS
    // =====================================================

    /**
     * Creates the PENDING row for a scheduled dose when its alarm
     * fires. Uses CONFLICT_IGNORE on (medicine_id, scheduled_at) so
     * a duplicate alarm firing twice cannot create two rows.
     *
     * @return true if a new row was inserted, false if one already
     * existed for this dose.
     */
    public boolean insertDoseLogIfAbsent(
            int userId,
            int medicineId,
            String scheduledAt
    ) {

        SQLiteDatabase db =
                getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put(
                DOSE_LOG_USER_ID,
                userId
        );

        values.put(
                DOSE_LOG_MEDICINE_ID,
                medicineId
        );

        values.put(
                DOSE_LOG_SCHEDULED_AT,
                scheduledAt
        );

        values.put(
                DOSE_LOG_STATUS,
                DOSE_STATUS_PENDING
        );

        long result =
                db.insertWithOnConflict(
                        TABLE_DOSE_LOGS,
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_IGNORE
                );

        return result != -1;
    }

    /**
     * Flips a dose from PENDING to TAKEN. No-ops (returns false) if
     * the dose isn't currently PENDING, so a late tap can't overwrite
     * a dose already marked MISSED.
     */
    public boolean markDoseTaken(
            int userId,
            int medicineId,
            String scheduledAt,
            String takenAt
    ) {

        SQLiteDatabase db =
                getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put(
                DOSE_LOG_STATUS,
                DOSE_STATUS_TAKEN
        );

        values.put(
                DOSE_LOG_TAKEN_AT,
                takenAt
        );

        int result =
                db.update(
                        TABLE_DOSE_LOGS,
                        values,
                        DOSE_LOG_USER_ID +
                                "=? AND " +
                                DOSE_LOG_MEDICINE_ID +
                                "=? AND " +
                                DOSE_LOG_SCHEDULED_AT +
                                "=? AND " +
                                DOSE_LOG_STATUS +
                                "=?",
                        new String[]{
                                String.valueOf(userId),
                                String.valueOf(medicineId),
                                scheduledAt,
                                DOSE_STATUS_PENDING
                        }
                );

        return result > 0;
    }

    /**
     * Flips a dose from PENDING to MISSED. No-ops (returns false) if
     * the dose was already marked TAKEN before the deadline.
     */
    public boolean markDoseMissed(
            int userId,
            int medicineId,
            String scheduledAt
    ) {

        SQLiteDatabase db =
                getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put(
                DOSE_LOG_STATUS,
                DOSE_STATUS_MISSED
        );

        int result =
                db.update(
                        TABLE_DOSE_LOGS,
                        values,
                        DOSE_LOG_USER_ID +
                                "=? AND " +
                                DOSE_LOG_MEDICINE_ID +
                                "=? AND " +
                                DOSE_LOG_SCHEDULED_AT +
                                "=? AND " +
                                DOSE_LOG_STATUS +
                                "=?",
                        new String[]{
                                String.valueOf(userId),
                                String.valueOf(medicineId),
                                scheduledAt,
                                DOSE_STATUS_PENDING
                        }
                );

        return result > 0;
    }

    /**
     * Returns the dose_logs row for one specific scheduled dose, or
     * null if it hasn't been triggered yet (still Upcoming).
     */
    public Cursor getDoseLog(
            int userId,
            int medicineId,
            String scheduledAt
    ) {

        SQLiteDatabase db =
                getReadableDatabase();

        return db.query(
                TABLE_DOSE_LOGS,
                null,
                DOSE_LOG_USER_ID +
                        "=? AND " +
                        DOSE_LOG_MEDICINE_ID +
                        "=? AND " +
                        DOSE_LOG_SCHEDULED_AT +
                        "=?",
                new String[]{
                        String.valueOf(userId),
                        String.valueOf(medicineId),
                        scheduledAt
                },
                null,
                null,
                null
        );
    }

    /**
     * Returns all dose_logs rows for one medicine on one calendar
     * day (datePrefix format "yyyy-MM-dd"), ordered earliest first.
     */
    public Cursor getDoseLogsForDate(
            int userId,
            int medicineId,
            String datePrefix
    ) {

        SQLiteDatabase db =
                getReadableDatabase();

        return db.query(
                TABLE_DOSE_LOGS,
                null,
                DOSE_LOG_USER_ID +
                        "=? AND " +
                        DOSE_LOG_MEDICINE_ID +
                        "=? AND " +
                        DOSE_LOG_SCHEDULED_AT +
                        " LIKE ?",
                new String[]{
                        String.valueOf(userId),
                        String.valueOf(medicineId),
                        datePrefix + "%"
                },
                null,
                null,
                DOSE_LOG_SCHEDULED_AT +
                        " ASC"
        );
    }
}