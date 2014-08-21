package nl.bneijt.unitrans.blockstore;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class Formatters {
    public static final DateTimeFormatter DATE_TIME_FORMATTER = ISODateTimeFormat.dateTime();

    public String formatDateTime(DateTime dateTime) {
        return DATE_TIME_FORMATTER.print(dateTime);
    }
}
