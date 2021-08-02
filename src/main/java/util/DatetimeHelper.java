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

        if (duration.toMinutes() == 1)
            testDurationText.append("1 minute ");
        else if (duration.toMinutes() > 1)
            testDurationText
                    .append(duration.toMinutes())
                    .append(" minutes ");

        if (duration.getSeconds() % 60 == 1)
            testDurationText.append("1 second");
        else if (duration.getSeconds() % 60 > 1)
            testDurationText
                    .append(duration.getSeconds() % 60)
                    .append(" seconds");

        return testDurationText.toString();
    }
}
