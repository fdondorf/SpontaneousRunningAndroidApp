
package org.spontaneous.utility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateTimeHelper {
    /**
     * Returns date formatter that match ISO date-time format.
     *
     * Do not be tempted to optimize it (like: into static field). We require
     * new instance each time, because SimpleDateFormat is famous for it's
     * multi-threading related issues.
     *
     * @return ISO date formatter instance
     */
    public static SimpleDateFormat getIsoDateFormat()
    {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.GERMAN);
    }

    public static Date parseISO8601(String dateString)
    {
        try {
            return getIsoDateFormat().parse(dateString.replaceAll("Z$", "+0000"));
        } catch (ParseException e) {
            return null;
        }
    }
}
