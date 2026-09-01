package com.example.mediguard;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Turns a saved ReminderConfig into concrete dose occurrence times.
 *
 * Assumption (no end-of-day cutoff exists in the schema): on every
 * active day, doses repeat every "repeat_hours" hours for a full
 * 24-hour cycle starting at "start_time" -- (24 / repeatHours) doses
 * per active day. Late start times can roll a dose past midnight
 * into the next calendar day.
 */
public class ReminderScheduleCalculator {

    private static final String TAG = "ReminderScheduleCalc";

    private static final String[] DAY_NAMES = {
            "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"
    };

    /**
     * Dose times for one specific active date, anchored to that
     * date's start_time. Returns an empty list if this date is
     * outside the reminder's active window or day pattern.
     */
    public static List<Calendar> getDoseTimesForDate(
            ReminderConfig config,
            Calendar date
    ) {

        List<Calendar> times = new ArrayList<>();

        if (!isDateActive(config, date)) {
            return times;
        }

        Calendar anchor = atStartOfDay(date);

        Calendar[] startTime = parseStartTime(config.getStartTime());

        if (startTime == null) {
            return times;
        }

        anchor.set(Calendar.HOUR_OF_DAY, startTime[0].get(Calendar.HOUR_OF_DAY));
        anchor.set(Calendar.MINUTE, startTime[0].get(Calendar.MINUTE));
        anchor.set(Calendar.SECOND, 0);
        anchor.set(Calendar.MILLISECOND, 0);

        int repeatHours = config.getRepeatHours();

        if (repeatHours <= 0) {
            return times;
        }

        int dosesPerDay = Math.max(1, 24 / repeatHours);

        for (int i = 0; i < dosesPerDay; i++) {

            Calendar occurrence = (Calendar) anchor.clone();
            occurrence.add(Calendar.HOUR_OF_DAY, i * repeatHours);

            times.add(occurrence);
        }

        return times;
    }

    /**
     * The earliest dose occurrence strictly after "after", scanning
     * forward day by day through the reminder's active window.
     * Returns null once the window is exhausted (reminder is done).
     */
    public static Calendar findNextOccurrence(
            ReminderConfig config,
            Calendar after
    ) {

        Calendar startDate = parseDate(config.getStartDate());

        if (startDate == null || config.getDurationDays() <= 0) {
            return null;
        }

        Calendar endDate = (Calendar) startDate.clone();
        endDate.add(Calendar.DAY_OF_MONTH, config.getDurationDays() - 1);

        Calendar cursor = atStartOfDay((Calendar) after.clone());

        // Doses can roll past midnight, so also check the day
        // before "after" in case a late-anchored dose lands today.
        cursor.add(Calendar.DAY_OF_MONTH, -1);

        Calendar scanLimit = (Calendar) endDate.clone();
        scanLimit.add(Calendar.DAY_OF_MONTH, 2);

        Calendar best = null;

        while (!cursor.after(scanLimit)) {

            for (Calendar occurrence : getDoseTimesForDate(config, cursor)) {

                if (occurrence.after(after)
                        && (best == null || occurrence.before(best))) {

                    best = occurrence;
                }
            }

            cursor.add(Calendar.DAY_OF_MONTH, 1);
        }

        return best;
    }

    // =====================================================
    // ACTIVE DAY CHECK
    // =====================================================

    private static boolean isDateActive(
            ReminderConfig config,
            Calendar date
    ) {

        Calendar startDate = parseDate(config.getStartDate());

        if (startDate == null || config.getDurationDays() <= 0) {
            return false;
        }

        Calendar today = atStartOfDay((Calendar) date.clone());
        Calendar start = atStartOfDay(startDate);

        long daysSinceStart =
                daysBetween(start, today);

        if (daysSinceStart < 0
                || daysSinceStart >= config.getDurationDays()) {
            return false;
        }

        int dayIndex = mondayIndex(today);

        if ("custom".equals(config.getScheduleType())) {

            int weekNumber = (int) (daysSinceStart / 7) + 1;

            return customWeekHasDay(
                    config.getCustomSchedule(),
                    weekNumber,
                    dayIndex
            );
        }

        String days = config.getDays();

        return days != null
                && Arrays.asList(days.split(","))
                .contains(DAY_NAMES[dayIndex]);
    }

    private static boolean customWeekHasDay(
            String customScheduleJson,
            int weekNumber,
            int dayIndex
    ) {

        if (customScheduleJson == null || customScheduleJson.isEmpty()) {
            return false;
        }

        try {

            JSONArray weeks = new JSONArray(customScheduleJson);

            for (int i = 0; i < weeks.length(); i++) {

                JSONObject week = weeks.getJSONObject(i);

                if (week.optInt("week", -1) != weekNumber) {
                    continue;
                }

                JSONArray days = week.optJSONArray("days");

                if (days == null) {
                    return false;
                }

                for (int d = 0; d < days.length(); d++) {

                    if (days.optInt(d, -1) == dayIndex) {
                        return true;
                    }
                }

                return false;
            }

        } catch (JSONException e) {

            android.util.Log.e(
                    TAG,
                    "Unable to parse custom_schedule",
                    e
            );
        }

        return false;
    }

    // =====================================================
    // PARSING HELPERS
    // =====================================================

    private static Calendar parseDate(String startDate) {

        if (startDate == null || startDate.isEmpty()) {
            return null;
        }

        try {

            Calendar calendar = Calendar.getInstance();

            calendar.setTime(
                    new SimpleDateFormat("MMM d, yyyy", Locale.US)
                            .parse(startDate)
            );

            return atStartOfDay(calendar);

        } catch (ParseException e) {

            android.util.Log.e(
                    TAG,
                    "Unable to parse start_date: " + startDate,
                    e
            );

            return null;
        }
    }

    private static Calendar[] parseStartTime(String startTime) {

        if (startTime == null || startTime.isEmpty()) {
            return null;
        }

        try {

            Calendar calendar = Calendar.getInstance();

            calendar.setTime(
                    new SimpleDateFormat("hh:mm a", Locale.US)
                            .parse(startTime)
            );

            return new Calendar[]{calendar};

        } catch (ParseException e) {

            android.util.Log.e(
                    TAG,
                    "Unable to parse start_time: " + startTime,
                    e
            );

            return null;
        }
    }

    private static Calendar atStartOfDay(Calendar calendar) {

        Calendar copy = (Calendar) calendar.clone();

        copy.set(Calendar.HOUR_OF_DAY, 0);
        copy.set(Calendar.MINUTE, 0);
        copy.set(Calendar.SECOND, 0);
        copy.set(Calendar.MILLISECOND, 0);

        return copy;
    }

    private static long daysBetween(Calendar start, Calendar end) {

        long startMillis = start.getTimeInMillis();
        long endMillis = end.getTimeInMillis();

        return Math.round((endMillis - startMillis) / 86400000.0);
    }

    /**
     * Calendar.DAY_OF_WEEK is SUN=1..SAT=7; this app's day arrays
     * are Mon=0..Sun=6, matching SetReminderActivity's dayNames[].
     */
    private static int mondayIndex(Calendar calendar) {

        int calendarDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        return (calendarDayOfWeek - Calendar.MONDAY + 7) % 7;
    }
}