package util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Duration;

public class DatetimeHelper {
    public static String toFormattedDate(Timestamp timestamp) {
        return new SimpleDateFormat("EEEEE, MMMMM d yyyy").format(timestamp);
    }

    public static String toFormattedTime(Timestamp timestamp) {
        return new SimpleDateFormat("HH:mm:ss").format(timestamp);
    }

    public static Duration between(Timestamp startTimestamp, Timestamp endTimestamp) {
        return Duration.between(startTimestamp.toInstant(), endTimestamp.toInstant());
    }

    public static String toMinutesSeconds(Duration duration) {
        StringBuilder testDurationText = new StringBuilder();

        long seconds = duration.getSeconds() % 60;

        String hourText = formatTextInUnit(duration.toHours(), "hour");
        String minutesText = formatTextInUnit(duration.toMinutes(), "minute");
        String secondsText = formatTextInUnit(seconds, "second");

        testDurationText.append(hourText);

        if (minutesText.length() > 0 && testDurationText.length() > 0)
            testDurationText.append(" ");

        testDurationText.append(minutesText);

        if (secondsText.length() > 0 && testDurationText.length() > 0)
            testDurationText.append(" ");

        testDurationText.append(secondsText);

        return testDurationText.toString();
    }

    private static String formatTextInUnit(long unitValue, String unitName) {
        StringBuilder text = new StringBuilder();

        if (unitValue > 0) {
            text
                    .append(unitValue)
                    .append(" ")
                    .append(unitName);

            if (unitValue > 1)
                text.append("s");

        } else
            if (unitName.equals("second"))
                text.append("less than a second");

        return text.toString();
    }
}