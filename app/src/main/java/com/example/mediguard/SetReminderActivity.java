package com.example.mediguard;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public class SetReminderActivity extends AppCompatActivity {

    // =====================================================
    // DATABASE
    // =====================================================

    private DatabaseHelper databaseHelper;

    // =====================================================
    // INPUTS
    // =====================================================

    private TextInputEditText etDose;
    private AutoCompleteTextView actDoseUnit;

    private TextInputEditText etStartDate;
    private TextInputEditText etStartTime;
    private TextInputEditText actRepeatEvery;

    private AutoCompleteTextView actDuration;
    private TextInputEditText etContactNumber;

    // =====================================================
    // CONTROLS
    // =====================================================

    private SwitchMaterial switchReminder;

    private MaterialButton btnSaveReminder;
    private MaterialButton btnSameEveryWeek;
    private MaterialButton btnCustomSchedule;
    private MaterialButton btnAddWeek;

    // =====================================================
    // TEXT INPUT LAYOUTS
    // =====================================================

    private TextInputLayout layoutDoseUnit;
    private TextInputLayout layoutStartDate;
    private TextInputLayout layoutStartTime;
    private TextInputLayout layoutRepeatEvery;
    private TextInputLayout layoutDuration;

    // =====================================================
    // SCHEDULE CONTAINERS
    // =====================================================

    private LinearLayout customScheduleContainer;
    private LinearLayout sameEveryWeekDaysContainer;

    private TextView txtScheduleInstruction;

    private MaterialButton[] sameWeekDayButtons;

    // =====================================================
    // CUSTOM SCHEDULE DATA
    // =====================================================

    private final ArrayList<CustomWeek> customWeeks =
            new ArrayList<>();

    private final String[] dayNames = {
            "Mon",
            "Tue",
            "Wed",
            "Thu",
            "Fri",
            "Sat",
            "Sun"
    };

    // =====================================================
    // USER / MEDICINE
    // =====================================================

    private int userId;
    private int medicineId;

    private String scheduleType = "same";

    // =====================================================
    // MAXIMUM CUSTOM WEEKS
    // =====================================================

    private int maxWeeks = 1;

    // =====================================================
    // COLORS
    // =====================================================

    private static final int BLUE = 0xFF1565F9;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int BORDER = 0xFFDCE5F2;
    private static final int TEXT = 0xFF1B2A4E;

    // =====================================================
    // ON CREATE
    // =====================================================

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(
                savedInstanceState
        );

        setContentView(
                R.layout.activity_set_reminder
        );

        databaseHelper =
                new DatabaseHelper(this);

        userId =
                getIntent().getIntExtra(
                        "user_id",
                        -1
                );

        if (userId == -1) {
            userId =
                    getLoggedInUserId();
        }

        medicineId =
                getIntent().getIntExtra(
                        "medicine_id",
                        -1
                );

        initializeViews();

        setupMedicineHeader();
        setupDoseUnit();
        setupStartDate();
        setupStartTime();
        setupRepeatEvery();
        setupDuration();
        setupScheduleControls();
        setupDefaultContact();

        loadExistingReminder();

        updateMaxWeeksFromDuration();
        adjustCustomWeeksToDuration();
        updateAddWeekVisibility();

        if ("custom".equals(scheduleType)) {
            renderCustomWeeks();
        }

        btnSaveReminder.setOnClickListener(
                v -> saveReminder()
        );
    }

    // =====================================================
    // INITIALIZE VIEWS
    // =====================================================

    private void initializeViews() {

        ImageButton btnBack =
                findViewById(
                        R.id.btnBack
                );

        btnBack.setOnClickListener(
                v -> finish()
        );

        etDose =
                findViewById(
                        R.id.etDose
                );

        actDoseUnit =
                findViewById(
                        R.id.actDoseUnit
                );

        etStartDate =
                findViewById(
                        R.id.etStartDate
                );

        etStartTime =
                findViewById(
                        R.id.etStartTime
                );

        actRepeatEvery =
                findViewById(
                        R.id.actRepeatEvery
                );

        actDuration =
                findViewById(
                        R.id.actDuration
                );

        etContactNumber =
                findViewById(
                        R.id.etContactNumber
                );

        layoutDoseUnit =
                findViewById(
                        R.id.layoutDoseUnit
                );

        layoutStartDate =
                findViewById(
                        R.id.layoutStartDate
                );

        layoutStartTime =
                findViewById(
                        R.id.layoutStartTime
                );

        layoutRepeatEvery =
                findViewById(
                        R.id.layoutRepeatEvery
                );

        layoutDuration =
                findViewById(
                        R.id.layoutDuration
                );

        switchReminder =
                findViewById(
                        R.id.switchReminder
                );

        btnSaveReminder =
                findViewById(
                        R.id.btnSaveReminder
                );

        btnSameEveryWeek =
                findViewById(
                        R.id.btnSameEveryWeek
                );

        btnCustomSchedule =
                findViewById(
                        R.id.btnCustomSchedule
                );

        btnAddWeek =
                findViewById(
                        R.id.btnAddWeek
                );

        customScheduleContainer =
                findViewById(
                        R.id.customScheduleContainer
                );

        sameEveryWeekDaysContainer =
                findViewById(
                        R.id.sameEveryWeekDaysContainer
                );

        txtScheduleInstruction =
                findViewById(
                        R.id.txtScheduleInstruction
                );

        sameWeekDayButtons =
                new MaterialButton[]{
                        findViewById(R.id.btnMon),
                        findViewById(R.id.btnTue),
                        findViewById(R.id.btnWed),
                        findViewById(R.id.btnThu),
                        findViewById(R.id.btnFri),
                        findViewById(R.id.btnSat),
                        findViewById(R.id.btnSun)
                };
    }

    // =====================================================
    // MEDICINE HEADER
    // =====================================================

    private void setupMedicineHeader() {

        ImageView imgMedicine =
                findViewById(
                        R.id.imgMedicine
                );

        TextView txtMedicineName =
                findViewById(
                        R.id.txtMedicineName
                );

        TextView txtMedicineType =
                findViewById(
                        R.id.txtMedicineType
                );

        String name =
                safeExtra(
                        "medicine_name"
                );

        String type =
                safeExtra(
                        "medicine_type"
                );

        String quantity =
                safeExtra(
                        "medicine_quantity"
                );

        String photoPath =
                safeExtra(
                        "medicine_photo"
                );

        txtMedicineName.setText(
                name
        );

        if (
                quantity.isEmpty()
        ) {

            txtMedicineType.setText(
                    type
            );

        } else {

            txtMedicineType.setText(
                    type +
                            " • " +
                            quantity
            );
        }

        loadPhoto(
                imgMedicine,
                photoPath
        );
    }

    private String safeExtra(
            String key
    ) {

        String value =
                getIntent().getStringExtra(
                        key
                );

        return value == null
                ? ""
                : value;
    }

    // =====================================================
    // DOSE UNIT
    // =====================================================

    private void setupDoseUnit() {

        String type =
                safeExtra(
                        "medicine_type"
                ).trim();

        String[] units;

        if (
                type.equalsIgnoreCase(
                        "syrup"
                )
        ) {

            units =
                    new String[]{
                            "mL",
                            "Tablespoon",
                            "Spoonful",
                            "Teaspoon"
                    };

        } else {

            units =
                    new String[]{
                            "pcs"
                    };
        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout
                                .simple_dropdown_item_1line,
                        units
                );

        actDoseUnit.setAdapter(
                adapter
        );

        actDoseUnit.setText(
                "",
                false
        );

        actDoseUnit.setOnClickListener(
                v -> {

                    actDoseUnit.requestFocus();

                    actDoseUnit.showDropDown();
                }
        );

        if (
                layoutDoseUnit != null
        ) {

            layoutDoseUnit.setEndIconOnClickListener(
                    v -> {

                        actDoseUnit.requestFocus();

                        actDoseUnit.showDropDown();
                    }
            );
        }
    }

    // =====================================================
    // START DATE
    // =====================================================

    private void setupStartDate() {

        etStartDate.setText(
                ""
        );

        etStartDate.setOnTouchListener(
                (v, event) -> {

                    if (
                            event.getAction()
                                    == MotionEvent.ACTION_UP
                    ) {

                        etStartDate.requestFocus();

                        etStartDate.post(
                                this::showStartDatePicker
                        );
                    }

                    return true;
                }
        );

        if (
                layoutStartDate != null
        ) {

            layoutStartDate.setEndIconOnClickListener(
                    v -> {

                        etStartDate.requestFocus();

                        showStartDatePicker();
                    }
            );
        }
    }

    private void showStartDatePicker() {

        Calendar now =
                Calendar.getInstance();

        DatePickerDialog dialog =
                new DatePickerDialog(
                        this,

                        (
                                view,
                                year,
                                month,
                                dayOfMonth
                        ) -> {

                            Calendar selected =
                                    Calendar.getInstance();

                            selected.set(
                                    year,
                                    month,
                                    dayOfMonth
                            );

                            etStartDate.setText(
                                    formatDate(
                                            selected
                                    )
                            );

                            etStartDate.requestFocus();
                        },

                        now.get(
                                Calendar.YEAR
                        ),

                        now.get(
                                Calendar.MONTH
                        ),

                        now.get(
                                Calendar.DAY_OF_MONTH
                        )
                );

        dialog.show();
    }

    private String formatDate(
            Calendar calendar
    ) {

        return new SimpleDateFormat(
                "MMM d, yyyy",
                Locale.US
        ).format(
                calendar.getTime()
        );
    }

    // =====================================================
    // START TIME
    // =====================================================

    private void setupStartTime() {

        etStartTime.setText(
                ""
        );

        etStartTime.setOnTouchListener(
                (v, event) -> {

                    if (
                            event.getAction()
                                    == MotionEvent.ACTION_UP
                    ) {

                        etStartTime.requestFocus();

                        etStartTime.post(
                                this::showStartTimePicker
                        );
                    }

                    return true;
                }
        );

        if (
                layoutStartTime != null
        ) {

            layoutStartTime.setEndIconOnClickListener(
                    v -> {

                        etStartTime.requestFocus();

                        showStartTimePicker();
                    }
            );
        }
    }

    private void showStartTimePicker() {

        Calendar now =
                Calendar.getInstance();

        TimePickerDialog dialog =
                new TimePickerDialog(
                        this,

                        (
                                view,
                                hour,
                                minute
                        ) -> {

                            Calendar selected =
                                    Calendar.getInstance();

                            selected.set(
                                    Calendar.HOUR_OF_DAY,
                                    hour
                            );

                            selected.set(
                                    Calendar.MINUTE,
                                    minute
                            );

                            etStartTime.setText(
                                    new SimpleDateFormat(
                                            "hh:mm a",
                                            Locale.US
                                    ).format(
                                            selected.getTime()
                                    )
                            );

                            etStartTime.requestFocus();
                        },

                        now.get(
                                Calendar.HOUR_OF_DAY
                        ),

                        now.get(
                                Calendar.MINUTE
                        ),

                        false
                );

        dialog.show();
    }

    // =====================================================
    // REPEAT EVERY
    // =====================================================

    private void setupRepeatEvery() {

        actRepeatEvery.setText(
                ""
        );

        actRepeatEvery.setOnTouchListener(
                (v, event) -> {

                    if (
                            event.getAction()
                                    == MotionEvent.ACTION_UP
                    ) {

                        actRepeatEvery.requestFocus();

                        actRepeatEvery.post(
                                this::showRepeatEveryPicker
                        );
                    }

                    return true;
                }
        );

        if (
                layoutRepeatEvery != null
        ) {

            layoutRepeatEvery.setEndIconOnClickListener(
                    v -> {

                        actRepeatEvery.requestFocus();

                        showRepeatEveryPicker();
                    }
            );
        }
    }

    private void showRepeatEveryPicker() {

        TimePickerDialog dialog =
                new TimePickerDialog(
                        this,

                        (
                                view,
                                hourOfDay,
                                minute
                        ) -> {

                            int hours =
                                    hourOfDay;

                            if (
                                    hours == 0
                            ) {

                                hours = 24;
                            }

                            actRepeatEvery.setText(
                                    "Every " +
                                            hours +
                                            (
                                                    hours == 1
                                                            ? " hour"
                                                            : " hours"
                                            )
                            );

                            actRepeatEvery.requestFocus();
                        },

                        6,
                        0,
                        true
                );

        dialog.setTitle(
                "Repeat Every"
        );

        dialog.show();
    }

    // =====================================================
    // DURATION
    // =====================================================

    private void setupDuration() {

        String[] durationOptions = {
                "7 days",
                "14 days",
                "30 days",
                "60 days",
                "90 days"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout
                                .simple_dropdown_item_1line,
                        durationOptions
                );

        actDuration.setAdapter(
                adapter
        );

        actDuration.setText(
                "",
                false
        );

        actDuration.setOnClickListener(
                v -> {

                    actDuration.requestFocus();

                    actDuration.showDropDown();
                }
        );

        /*
         * When the user selects a duration,
         * immediately update the maximum
         * number of custom weeks.
         */
        actDuration.setOnItemClickListener(
                (parent, view, position, id) -> {

                    updateMaxWeeksFromDuration();

                    /*
                     * If the new duration is shorter,
                     * remove weeks that are no longer valid.
                     */
                    adjustCustomWeeksToDuration();

                    /*
                     * Re-number and redraw the cards.
                     */
                    if (
                            "custom".equals(
                                    scheduleType
                            )
                    ) {

                        renderCustomWeeks();
                    }

                    updateAddWeekVisibility();
                }
        );

        if (
                layoutDuration != null
        ) {

            layoutDuration.setEndIconOnClickListener(
                    v -> {

                        actDuration.requestFocus();

                        actDuration.showDropDown();
                    }
            );
        }
    }

    // =====================================================
    // UPDATE MAXIMUM WEEKS
    // =====================================================

    private void updateMaxWeeksFromDuration() {

        int durationDays =
                getDurationDays();

        if (
                durationDays <= 0
        ) {

            maxWeeks = 1;

            return;
        }

        /*
         * Round UP to the number of weeks.
         *
         * 7  days  = 1 week
         * 14 days  = 2 weeks
         * 30 days  = 5 weeks
         * 60 days  = 9 weeks
         * 90 days  = 13 weeks
         */
        maxWeeks =
                (durationDays + 6) / 7;
    }

    // =====================================================
    // REMOVE EXCESS CUSTOM WEEKS
    // =====================================================

    private void adjustCustomWeeksToDuration() {

        updateMaxWeeksFromDuration();

        while (
                customWeeks.size() > maxWeeks
        ) {

            customWeeks.remove(
                    customWeeks.size() - 1
            );
        }

        renumberWeeks();
    }

    // =====================================================
    // UPDATE ADD WEEK VISIBILITY
    // =====================================================

    private void updateAddWeekVisibility() {

        if (
                btnAddWeek == null
        ) {

            return;
        }

        updateMaxWeeksFromDuration();

        if (
                !"custom".equals(
                        scheduleType
                )
        ) {

            btnAddWeek.setVisibility(
                    View.GONE
            );

            return;
        }

        if (
                customWeeks.size() >= maxWeeks
        ) {

            btnAddWeek.setVisibility(
                    View.GONE
            );

        } else {

            btnAddWeek.setVisibility(
                    View.VISIBLE
            );
        }
    }

    // =====================================================
    // SCHEDULE CONTROLS
    // =====================================================

    private void setupScheduleControls() {

        for (
                int i = 0;
                i < sameWeekDayButtons.length;
                i++
        ) {

            final int index =
                    i;

            sameWeekDayButtons[i]
                    .setOnClickListener(
                            v -> {

                                boolean selected =
                                        Boolean.TRUE.equals(
                                                v.getTag()
                                        );

                                setButtonSelected(
                                        sameWeekDayButtons[index],
                                        !selected
                                );
                            }
                    );

            setButtonSelected(
                    sameWeekDayButtons[i],
                    false
            );
        }

        setScheduleType(
                "same"
        );

        btnSameEveryWeek.setOnClickListener(
                v -> setScheduleType(
                        "same"
                )
        );

        btnCustomSchedule.setOnClickListener(
                v -> setScheduleType(
                        "custom"
                )
        );

        btnAddWeek.setOnClickListener(
                v -> addCustomWeek()
        );

        setAddWeekButtonStyle();
    }

    private void setScheduleType(
            String type
    ) {

        scheduleType =
                type;

        boolean same =
                "same".equals(
                        type
                );

        sameEveryWeekDaysContainer
                .setVisibility(
                        same
                                ? View.VISIBLE
                                : View.GONE
                );

        customScheduleContainer
                .setVisibility(
                        same
                                ? View.GONE
                                : View.VISIBLE
                );

        setToggleButton(
                btnSameEveryWeek,
                same
        );

        setToggleButton(
                btnCustomSchedule,
                !same
        );

        setAddWeekButtonStyle();

        if (
                txtScheduleInstruction != null
        ) {

            txtScheduleInstruction.setText(
                    same
                            ? "Select the days when you need to take this medicine."
                            : "Create a different schedule for each week."
            );
        }

        if (
                !same
        ) {

            updateMaxWeeksFromDuration();

            /*
             * Existing custom weeks from an edited reminder
             * must never exceed the current duration.
             */
            adjustCustomWeeksToDuration();

            if (
                    customWeeks.isEmpty()
            ) {

                addCustomWeek();

            } else {

                renderCustomWeeks();
            }
        }

        updateAddWeekVisibility();
    }

    private void setToggleButton(
            MaterialButton button,
            boolean selected
    ) {

        button.setBackgroundTintList(
                android.content.res.ColorStateList
                        .valueOf(
                                selected
                                        ? BLUE
                                        : WHITE
                        )
        );

        button.setTextColor(
                selected
                        ? WHITE
                        : TEXT
        );
    }

    private void setAddWeekButtonStyle() {

        if (
                btnAddWeek == null
        ) {

            return;
        }

        btnAddWeek.setBackgroundTintList(
                android.content.res.ColorStateList
                        .valueOf(
                                BLUE
                        )
        );

        btnAddWeek.setTextColor(
                WHITE
        );

        btnAddWeek.setAllCaps(
                false
        );

        btnAddWeek.setSingleLine(
                true
        );
    }

    private void setButtonSelected(
            MaterialButton button,
            boolean selected
    ) {

        button.setTag(
                selected
        );

        button.setBackgroundTintList(
                android.content.res.ColorStateList
                        .valueOf(
                                selected
                                        ? BLUE
                                        : WHITE
                        )
        );

        button.setTextColor(
                selected
                        ? WHITE
                        : BLUE
        );

        button.setSingleLine(
                true
        );

        button.setMaxLines(
                1
        );

        button.setEllipsize(
                null
        );
    }

    // =====================================================
    // CUSTOM SCHEDULE
    // =====================================================

    private void addCustomWeek() {

        updateMaxWeeksFromDuration();

        /*
         * Do not allow another week once the
         * selected duration has reached its limit.
         */
        if (
                customWeeks.size() >= maxWeeks
        ) {

            Toast.makeText(
                    this,
                    "You can only add " +
                            maxWeeks +
                            (
                                    maxWeeks == 1
                                            ? " week"
                                            : " weeks"
                            ) +
                            " for this duration.",
                    Toast.LENGTH_SHORT
            ).show();

            updateAddWeekVisibility();

            return;
        }

        CustomWeek week =
                new CustomWeek(
                        customWeeks.size() + 1
                );

        customWeeks.add(
                week
        );

        renderCustomWeeks();

        updateAddWeekVisibility();
    }

    private void renderCustomWeeks() {

        customScheduleContainer
                .removeAllViews();

        for (
                int i = 0;
                i < customWeeks.size();
                i++
        ) {

            final int weekIndex =
                    i;

            CustomWeek week =
                    customWeeks.get(
                            i
                    );

            MaterialCardView card =
                    new MaterialCardView(
                            this
                    );

            card.setRadius(
                    dp(12)
            );

            card.setCardElevation(
                    dp(2)
            );

            card.setCardBackgroundColor(
                    WHITE
            );

            card.setStrokeColor(
                    BORDER
            );

            card.setStrokeWidth(
                    dp(1)
            );

            LinearLayout container =
                    new LinearLayout(
                            this
                    );

            container.setOrientation(
                    LinearLayout.VERTICAL
            );

            container.setPadding(
                    dp(12),
                    dp(10),
                    dp(12),
                    dp(10)
            );

            LinearLayout titleRow =
                    new LinearLayout(
                            this
                    );

            titleRow.setGravity(
                    android.view.Gravity
                            .CENTER_VERTICAL
            );

            TextView title =
                    new TextView(
                            this
                    );

            title.setText(
                    "Week " +
                            week.number
            );

            title.setTextColor(
                    TEXT
            );

            title.setTextSize(
                    15
            );

            title.setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
            );

            titleRow.addView(
                    title,
                    new LinearLayout.LayoutParams(
                            0,
                            dp(40),
                            1f
                    )
            );

            ImageButton delete =
                    new ImageButton(
                            this
                    );

            delete.setImageResource(
                    android.R.drawable
                            .ic_menu_delete
            );

            delete.setBackgroundColor(
                    android.graphics.Color
                            .TRANSPARENT
            );

            delete.setColorFilter(
                    android.graphics.Color
                            .DKGRAY
            );

            delete.setContentDescription(
                    "Delete Week " +
                            week.number
            );

            delete.setOnClickListener(
                    v -> {

                        if (
                                customWeeks.size()
                                        == 1
                        ) {

                            Toast.makeText(
                                    this,
                                    "At least one custom week is required.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }

                        customWeeks.remove(
                                weekIndex
                        );

                        renumberWeeks();

                        renderCustomWeeks();

                        updateAddWeekVisibility();
                    }
            );

            titleRow.addView(
                    delete,
                    new LinearLayout.LayoutParams(
                            dp(40),
                            dp(40)
                    )
            );

            container.addView(
                    titleRow
            );

            container.addView(
                    createCustomDayRow(
                            week,
                            0,
                            4
                    )
            );

            LinearLayout secondRow =
                    createCenteredCustomDayRow(
                            week
                    );

            LinearLayout.LayoutParams
                    secondRowParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            secondRowParams.topMargin =
                    dp(4);

            container.addView(
                    secondRow,
                    secondRowParams
            );

            card.addView(
                    container
            );

            LinearLayout.LayoutParams
                    cardParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            cardParams.setMargins(
                    0,
                    dp(8),
                    0,
                    0
            );

            customScheduleContainer.addView(
                    card,
                    cardParams
            );
        }

        updateAddWeekVisibility();
    }

    // =====================================================
    // CUSTOM DAY ROW 1
    // MON TUE WED THU
    // =====================================================

    private LinearLayout createCustomDayRow(
            CustomWeek week,
            int start,
            int end
    ) {

        LinearLayout row =
                new LinearLayout(
                        this
                );

        row.setOrientation(
                LinearLayout.HORIZONTAL
        );

        row.setGravity(
                android.view.Gravity
                        .CENTER_HORIZONTAL
        );

        for (
                int i = start;
                i < end;
                i++
        ) {

            final int dayIndex =
                    i;

            MaterialButton button =
                    createDayButton(
                            dayNames[i]
                    );

            setButtonSelected(
                    button,
                    week.days[i]
            );

            button.setOnClickListener(
                    v -> {

                        week.days[dayIndex] =
                                !week.days[dayIndex];

                        setButtonSelected(
                                button,
                                week.days[dayIndex]
                        );
                    }
            );

            LinearLayout.LayoutParams
                    params =
                    new LinearLayout.LayoutParams(
                            0,
                            dp(44),
                            1f
                    );

            if (
                    i > start
            ) {

                params.leftMargin =
                        dp(4);
            }

            row.addView(
                    button,
                    params
            );
        }

        return row;
    }

    // =====================================================
    // CUSTOM DAY ROW 2
    // FRI SAT SUN
    // =====================================================

    private LinearLayout createCenteredCustomDayRow(
            CustomWeek week
    ) {

        LinearLayout row =
                new LinearLayout(
                        this
                );

        row.setOrientation(
                LinearLayout.HORIZONTAL
        );

        row.setGravity(
                android.view.Gravity.CENTER
        );

        addCustomDayButton(
                row,
                week,
                4
        );

        addCustomDayButton(
                row,
                week,
                5
        );

        addCustomDayButton(
                row,
                week,
                6
        );

        return row;
    }

    private void addCustomDayButton(
            LinearLayout row,
            CustomWeek week,
            int dayIndex
    ) {

        MaterialButton button =
                createDayButton(
                        dayNames[dayIndex]
                );

        setButtonSelected(
                button,
                week.days[dayIndex]
        );

        button.setOnClickListener(
                v -> {

                    week.days[dayIndex] =
                            !week.days[dayIndex];

                    setButtonSelected(
                            button,
                            week.days[dayIndex]
                    );
                }
        );

        LinearLayout.LayoutParams
                params =
                new LinearLayout.LayoutParams(
                        dp(76),
                        dp(44)
                );

        if (
                dayIndex > 4
        ) {

            params.leftMargin =
                    dp(4);
        }

        row.addView(
                button,
                params
        );
    }

    // =====================================================
    // CREATE DAY BUTTON
    // =====================================================

    private MaterialButton createDayButton(
            String text
    ) {

        MaterialButton button =
                new MaterialButton(
                        this,
                        null,
                        com.google.android.material.R.attr
                                .materialButtonOutlinedStyle
                );

        button.setText(
                text
        );

        button.setAllCaps(
                false
        );

        button.setTextSize(
                12
        );

        button.setSingleLine(
                true
        );

        button.setMaxLines(
                1
        );

        button.setEllipsize(
                null
        );

        button.setPadding(
                dp(4),
                0,
                dp(4),
                0
        );

        return button;
    }

    private void renumberWeeks() {

        for (
                int i = 0;
                i < customWeeks.size();
                i++
        ) {

            customWeeks.get(
                    i
            ).number =
                    i + 1;
        }
    }

    // =====================================================
    // DEFAULT CONTACT
    // =====================================================

    private void setupDefaultContact() {

        if (
                userId <= 0
        ) {

            etContactNumber.setText(
                    ""
            );

            return;
        }

        try {

            String contact =
                    databaseHelper.getUserContact(
                            userId
                    );

            etContactNumber.setText(
                    contact == null
                            ? ""
                            : contact
            );

        } catch (
                Exception e
        ) {

            android.util.Log.e(
                    "SetReminderActivity",
                    "Unable to load registered contact",
                    e
            );

            etContactNumber.setText(
                    ""
            );
        }
    }

    // =====================================================
    // LOAD EXISTING REMINDER
    // =====================================================

    private void loadExistingReminder() {

        if (
                userId <= 0
                        || medicineId <= 0
        ) {

            return;
        }

        Cursor cursor = null;

        try {

            cursor =
                    databaseHelper.getReminderForMedicine(
                            userId,
                            medicineId
                    );

            /*
             * No existing reminder:
             * leave fields empty.
             */
            if (
                    !cursor.moveToFirst()
            ) {

                return;
            }

            // ---------------------------------------------
            // DOSE
            // ---------------------------------------------

            etDose.setText(
                    getColumnOrDefault(
                            cursor,
                            "dose",
                            ""
                    )
            );

            // ---------------------------------------------
            // DOSE UNIT
            // ---------------------------------------------

            setDoseUnitFromSavedValue(
                    getColumnOrDefault(
                            cursor,
                            "dose_unit",
                            ""
                    )
            );

            // ---------------------------------------------
            // START DATE
            // ---------------------------------------------

            etStartDate.setText(
                    getColumnOrDefault(
                            cursor,
                            "start_date",
                            ""
                    )
            );

            // ---------------------------------------------
            // START TIME
            // ---------------------------------------------

            etStartTime.setText(
                    getColumnOrDefault(
                            cursor,
                            "start_time",
                            ""
                    )
            );

            // ---------------------------------------------
            // REPEAT EVERY
            // ---------------------------------------------

            int repeatHours =
                    getIntColumnOrDefault(
                            cursor,
                            "repeat_hours",
                            0
                    );

            if (
                    repeatHours > 0
            ) {

                actRepeatEvery.setText(
                        "Every " +
                                repeatHours +
                                (
                                        repeatHours == 1
                                                ? " hour"
                                                : " hours"
                                )
                );

            } else {

                actRepeatEvery.setText(
                        ""
                );
            }

            // ---------------------------------------------
            // DURATION
            // ---------------------------------------------

            int durationDays =
                    getIntColumnOrDefault(
                            cursor,
                            "duration_days",
                            0
                    );

            if (
                    durationDays > 0
            ) {

                actDuration.setText(
                        durationDays +
                                " days",
                        false
                );

                /*
                 * Immediately calculate the number
                 * of valid custom weeks.
                 */
                maxWeeks =
                        (durationDays + 6) / 7;

            } else {

                actDuration.setText(
                        "",
                        false
                );

                maxWeeks = 1;
            }

            // ---------------------------------------------
            // DAYS
            // ---------------------------------------------

            String savedDays =
                    getColumnOrDefault(
                            cursor,
                            "days",
                            ""
                    );

            applySameWeekDays(
                    savedDays
            );

            // ---------------------------------------------
            // SCHEDULE TYPE
            // ---------------------------------------------

            scheduleType =
                    getColumnOrDefault(
                            cursor,
                            "schedule_type",
                            "same"
                    );

            if (
                    TextUtils.isEmpty(
                            scheduleType
                    )
            ) {

                scheduleType =
                        "same";
            }

            // ---------------------------------------------
            // CUSTOM SCHEDULE
            // ---------------------------------------------

            String customSchedule =
                    getColumnOrDefault(
                            cursor,
                            "custom_schedule",
                            ""
                    );

            customWeeks.clear();

            loadCustomWeeks(
                    customSchedule
            );

            /*
             * Never allow saved custom weeks to exceed
             * the selected duration.
             */
            updateMaxWeeksFromDuration();

            adjustCustomWeeksToDuration();

            if (
                    "custom".equals(
                            scheduleType
                    )
            ) {

                if (
                        customWeeks.isEmpty()
                ) {

                    addCustomWeek();

                } else {

                    renderCustomWeeks();
                }

            } else {

                setScheduleType(
                        "same"
                );
            }

            // ---------------------------------------------
            // CONTACT
            // ---------------------------------------------

            String savedContact =
                    getColumnOrDefault(
                            cursor,
                            "contact_number",
                            ""
                    );

            if (
                    !savedContact.isEmpty()
            ) {

                etContactNumber.setText(
                        savedContact
                );
            }

            // ---------------------------------------------
            // ENABLE
            // ---------------------------------------------

            switchReminder.setChecked(
                    getIntColumnOrDefault(
                            cursor,
                            "enabled",
                            1
                    ) == 1
            );

        } catch (
                Exception e
        ) {

            android.util.Log.e(
                    "SetReminderActivity",
                    "Unable to load existing reminder",
                    e
            );

        } finally {

            if (
                    cursor != null
            ) {

                cursor.close();
            }
        }
    }

    // =====================================================
    // SAFE COLUMN READ
    // =====================================================

    private String getColumnOrDefault(
            Cursor cursor,
            String column,
            String defaultValue
    ) {

        int index =
                cursor.getColumnIndex(
                        column
                );

        if (
                index < 0
                        || cursor.isNull(
                        index
                )
        ) {

            return defaultValue;
        }

        String value =
                cursor.getString(
                        index
                );

        return value == null
                ? defaultValue
                : value;
    }

    private int getIntColumnOrDefault(
            Cursor cursor,
            String column,
            int defaultValue
    ) {

        int index =
                cursor.getColumnIndex(
                        column
                );

        if (
                index < 0
                        || cursor.isNull(
                        index
                )
        ) {

            return defaultValue;
        }

        return cursor.getInt(
                index
        );
    }

    // =====================================================
    // LOAD SAVED DOSE UNIT
    // =====================================================

    private void setDoseUnitFromSavedValue(
            String value
    ) {

        if (
                TextUtils.isEmpty(
                        value
                )
        ) {

            actDoseUnit.setText(
                    "",
                    false
            );

            return;
        }

        String type =
                safeExtra(
                        "medicine_type"
                );

        if (
                type.equalsIgnoreCase(
                        "syrup"
                )
        ) {

            if (
                    value.equalsIgnoreCase("mL")
                            || value.equalsIgnoreCase(
                            "Tablespoon"
                    )
                            || value.equalsIgnoreCase(
                            "Spoonful"
                    )
                            || value.equalsIgnoreCase(
                            "Teaspoon"
                    )
            ) {

                actDoseUnit.setText(
                        value,
                        false
                );
            }

        } else if (
                value.equalsIgnoreCase(
                        "pcs"
                )
        ) {

            actDoseUnit.setText(
                    "pcs",
                    false
            );
        }
    }

    // =====================================================
    // APPLY SAME WEEK DAYS
    // =====================================================

    private void applySameWeekDays(
            String savedDays
    ) {

        Set<String> selected =
                new LinkedHashSet<>();

        if (
                savedDays != null
                        && !savedDays.trim().isEmpty()
        ) {

            for (
                    String day :
                    savedDays.split(",")
            ) {

                if (
                        !day.trim().isEmpty()
                ) {

                    selected.add(
                            day.trim()
                    );
                }
            }
        }

        for (
                int i = 0;
                i < sameWeekDayButtons.length;
                i++
        ) {

            setButtonSelected(
                    sameWeekDayButtons[i],
                    selected.contains(
                            dayNames[i]
                    )
            );
        }
    }

    // =====================================================
    // LOAD CUSTOM WEEKS
    // =====================================================

    private void loadCustomWeeks(
            String customSchedule
    ) {

        if (
                TextUtils.isEmpty(
                        customSchedule
                )
        ) {

            return;
        }

        try {

            JSONArray weeks =
                    new JSONArray(
                            customSchedule
                    );

            for (
                    int i = 0;
                    i < weeks.length();
                    i++
            ) {

                JSONObject object =
                        weeks.getJSONObject(
                                i
                        );

                CustomWeek week =
                        new CustomWeek(
                                object.optInt(
                                        "week",
                                        i + 1
                                )
                        );

                JSONArray days =
                        object.optJSONArray(
                                "days"
                        );

                if (
                        days != null
                ) {

                    for (
                            int d = 0;
                            d < days.length();
                            d++
                    ) {

                        int dayIndex =
                                days.optInt(
                                        d,
                                        -1
                                );

                        if (
                                dayIndex >= 0
                                        && dayIndex < 7
                        ) {

                            week.days[dayIndex] =
                                    true;
                        }
                    }
                }

                customWeeks.add(
                        week
                );
            }

        } catch (
                Exception e
        ) {

            android.util.Log.e(
                    "SetReminderActivity",
                    "Unable to parse custom schedule",
                    e
            );
        }
    }

    // =====================================================
    // REPEAT HOURS
    // =====================================================

    private int getRepeatHours() {

        String text =
                actRepeatEvery
                        .getText()
                        .toString()
                        .trim();

        String digits =
                text.replaceAll(
                        "[^0-9]",
                        ""
                );

        if (
                digits.isEmpty()
        ) {

            return -1;
        }

        try {

            return Integer.parseInt(
                    digits
            );

        } catch (
                NumberFormatException e
        ) {

            return -1;
        }
    }

    // =====================================================
    // DURATION DAYS
    // =====================================================

    private int getDurationDays() {

        String text =
                actDuration
                        .getText()
                        .toString()
                        .trim();

        String digits =
                text.replaceAll(
                        "[^0-9]",
                        ""
                );

        if (
                digits.isEmpty()
        ) {

            return -1;
        }

        try {

            return Integer.parseInt(
                    digits
            );

        } catch (
                NumberFormatException e
        ) {

            return -1;
        }
    }

    // =====================================================
    // SELECTED SAME WEEK DAYS
    // =====================================================

    private String getSelectedSameWeekDays() {

        ArrayList<String> selected =
                new ArrayList<>();

        for (
                int i = 0;
                i < sameWeekDayButtons.length;
                i++
        ) {

            if (
                    Boolean.TRUE.equals(
                            sameWeekDayButtons[i]
                                    .getTag()
                    )
            ) {

                selected.add(
                        dayNames[i]
                );
            }
        }

        return TextUtils.join(
                ",",
                selected
        );
    }

    // =====================================================
    // CUSTOM SCHEDULE JSON
    // =====================================================

    private String buildCustomScheduleJson() {

        JSONArray weeks =
                new JSONArray();

        try {

            for (
                    CustomWeek week :
                    customWeeks
            ) {

                JSONObject object =
                        new JSONObject();

                JSONArray days =
                        new JSONArray();

                for (
                        int i = 0;
                        i < 7;
                        i++
                ) {

                    if (
                            week.days[i]
                    ) {

                        days.put(
                                i
                        );
                    }
                }

                object.put(
                        "week",
                        week.number
                );

                object.put(
                        "days",
                        days
                );

                weeks.put(
                        object
                );
            }

        } catch (
                JSONException e
        ) {

            android.util.Log.e(
                    "SetReminderActivity",
                    "Unable to build custom schedule JSON",
                    e
            );
        }

        return weeks.toString();
    }

    private String getCustomDaysUnion() {

        Set<String> selected =
                new LinkedHashSet<>();

        for (
                CustomWeek week :
                customWeeks
        ) {

            for (
                    int i = 0;
                    i < 7;
                    i++
            ) {

                if (
                        week.days[i]
                ) {

                    selected.add(
                            dayNames[i]
                    );
                }
            }
        }

        return TextUtils.join(
                ",",
                selected
        );
    }

    private boolean allCustomWeeksHaveDays() {

        if (
                customWeeks.isEmpty()
        ) {

            return false;
        }

        for (
                CustomWeek week :
                customWeeks
        ) {

            if (
                    !week.hasAnyDay()
            ) {

                return false;
            }
        }

        return true;
    }

    // =====================================================
    // SAVE REMINDER
    // =====================================================

    private void saveReminder() {

        if (
                userId <= 0
        ) {

            Toast.makeText(
                    this,
                    "User session not found.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (
                medicineId <= 0
        ) {

            Toast.makeText(
                    this,
                    "Medicine not found.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String dose =
                etDose
                        .getText()
                        .toString()
                        .trim();

        String doseUnit =
                actDoseUnit
                        .getText()
                        .toString()
                        .trim();

        String startDate =
                etStartDate
                        .getText()
                        .toString()
                        .trim();

        String startTime =
                etStartTime
                        .getText()
                        .toString()
                        .trim();

        String contact =
                etContactNumber
                        .getText()
                        .toString()
                        .trim();

        int repeatHours =
                getRepeatHours();

        int durationDays =
                getDurationDays();

        if (
                TextUtils.isEmpty(
                        dose
                )
                        || TextUtils.isEmpty(
                        doseUnit
                )
                        || TextUtils.isEmpty(
                        startDate
                )
                        || TextUtils.isEmpty(
                        startTime
                )
                        || repeatHours <= 0
                        || durationDays <= 0
                        || TextUtils.isEmpty(
                        contact
                )
        ) {

            Toast.makeText(
                    this,
                    "Please complete all reminder fields.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (
                !contact.matches(
                        "09\\d{9}"
                )
        ) {

            Toast.makeText(
                    this,
                    "Enter a valid 11-digit Philippine mobile number.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String days;
        String customSchedule = "";

        if (
                "same".equals(
                        scheduleType
                )
        ) {

            days =
                    getSelectedSameWeekDays();

            if (
                    TextUtils.isEmpty(
                            days
                    )
            ) {

                Toast.makeText(
                        this,
                        "Select at least one day for the reminder.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

        } else {

            /*
             * Make sure the custom schedule is
             * still valid for the selected duration.
             */
            updateMaxWeeksFromDuration();
            adjustCustomWeeksToDuration();

            if (
                    !allCustomWeeksHaveDays()
            ) {

                Toast.makeText(
                        this,
                        "Select at least one day in every custom week.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            days =
                    getCustomDaysUnion();

            customSchedule =
                    buildCustomScheduleJson();
        }

        try {

            boolean saved =
                    databaseHelper.saveReminder(
                            userId,
                            medicineId,
                            dose,
                            doseUnit,
                            startTime,
                            startDate,
                            repeatHours,
                            days,
                            durationDays,
                            scheduleType,
                            customSchedule,
                            contact,
                            switchReminder.isChecked()
                    );

            if (
                    !saved
            ) {

                Toast.makeText(
                        this,
                        "Unable to save reminder.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            Toast.makeText(
                    this,
                    "Reminder saved successfully.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

        } catch (
                Exception e
        ) {

            android.util.Log.e(
                    "SetReminderActivity",
                    "Error saving reminder",
                    e
            );

            Toast.makeText(
                    this,
                    "Unable to save reminder. Please try again.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // =====================================================
    // PHOTO
    // =====================================================

    private void loadPhoto(
            ImageView imageView,
            String photoPath
    ) {

        if (
                TextUtils.isEmpty(
                        photoPath
                )
        ) {

            return;
        }

        File file =
                new File(
                        photoPath
                );

        if (
                !file.exists()
        ) {

            return;
        }

        Bitmap bitmap =
                decodeSampledBitmap(
                        photoPath,
                        200,
                        200
                );

        if (
                bitmap != null
        ) {

            imageView.setImageTintList(
                    null
            );

            imageView.setImageBitmap(
                    bitmap
            );
        }
    }

    private Bitmap decodeSampledBitmap(
            String path,
            int reqWidth,
            int reqHeight
    ) {

        BitmapFactory.Options bounds =
                new BitmapFactory.Options();

        bounds.inJustDecodeBounds =
                true;

        BitmapFactory.decodeFile(
                path,
                bounds
        );

        int sampleSize =
                1;

        while (
                bounds.outWidth / sampleSize
                        > reqWidth * 2
                        &&
                        bounds.outHeight / sampleSize
                                > reqHeight * 2
        ) {

            sampleSize *= 2;
        }

        BitmapFactory.Options options =
                new BitmapFactory.Options();

        options.inSampleSize =
                sampleSize;

        return BitmapFactory.decodeFile(
                path,
                options
        );
    }

    // =====================================================
    // USER ID
    // =====================================================

    private int getLoggedInUserId() {

        SharedPreferences preferences =
                getSharedPreferences(
                        "MediGuardPrefs",
                        MODE_PRIVATE
                );

        return preferences.getInt(
                "user_id",
                -1
        );
    }

    // =====================================================
    // DP
    // =====================================================

    private int dp(
            int value
    ) {

        return Math.round(
                value *
                        getResources()
                                .getDisplayMetrics()
                                .density
        );
    }

    // =====================================================
    // CUSTOM WEEK MODEL
    // =====================================================

    private static class CustomWeek {

        int number;

        boolean[] days =
                new boolean[7];

        CustomWeek(
                int number
        ) {

            this.number =
                    number;
        }

        boolean hasAnyDay() {

            for (
                    boolean day :
                    days
            ) {

                if (day) {
                    return true;
                }
            }

            return false;
        }
    }
}