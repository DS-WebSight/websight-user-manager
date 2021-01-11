package pl.ds.websight.usermanager.util;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

public final class DateFormatter {

    private static final long MINUTE_MILLIS = 60000;
    private static final long HOUR_MILLIS = MINUTE_MILLIS * 60;
    private static final long WEEK_MILLIS = HOUR_MILLIS * 24 * 7;
    private static final int DAYS_IN_WEEK = 7;

    private DateFormatter() {
        // no instance
    }

    public static String formatDate(Date date) {
        if (date != null) {
            return new SimpleDateFormat("yyyy-MM-dd").format(date);
        }
        return null;
    }

    public static String formatDateTime(Date date) {
        if (date != null) {
            return new SimpleDateFormat("yyyy-MM-dd hh:mm a").format(date);
        }
        return null;
    }

    public static String toRelative(Date date) {
        if (date != null) {
            Date now = new Date();
            long duration = now.getTime() - date.getTime();
            if (duration <= MINUTE_MILLIS) {
                return "a moment ago";
            } else if (duration <= HOUR_MILLIS) {
                return toRelativeMinutes(duration);
            } else if (isSameDay(date, now)) {
                return toRelativeHours(duration);
            } else if (duration <= WEEK_MILLIS) {
                return toRelativeDays(date, now);
            }
        }
        return null;
    }

    private static String toRelativeMinutes(long duration) {
        long minutes = duration / MINUTE_MILLIS;
        return minutes + (minutes > 1 ? " minutes ago" : " minute ago");
    }

    private static String toRelativeHours(long duration) {
        long hours = duration / HOUR_MILLIS;
        return hours + (hours > 1 ? " hours ago" : " hour ago");
    }

    private static String toRelativeDays(Date date, Date nowDate) {
        LocalDateTime now = nowDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        for (int i = 1; i < DAYS_IN_WEEK; i++) {
            LocalDateTime day = now.minusDays(i);
            Date dayDate = Date.from(day.atZone(ZoneId.systemDefault()).toInstant());
            if (isSameDay(date, dayDate)) {
                if (i == 1) {
                    return "yesterday";
                } else {
                    return i + " days ago";
                }
            }
        }
        return "1 week ago";
    }

    private static boolean isSameDay(final Date date1, final Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
}
