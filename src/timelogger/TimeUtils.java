package timelogger;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtils
{
    /**
     * Compute delta in minutes.
     *
     * @param start start time in hhmm format (24-hr time)
     * @param stop stop time in hhmm format (24-hr time)
     * @return the difference between the times in minutes or zero if start and
     * stop are not valid times.
     */
    public static int getDelta(String start, String stop)
    {
        long delta = 0;
        try
        {
            SimpleDateFormat fmt = new SimpleDateFormat("HHmm");
            fmt.setLenient(false);
            Date begin = fmt.parse(start);
            Date end = fmt.parse(stop);
            long e = end.getTime();
            long b = begin.getTime();
            if (e > b)
            {
                delta = e - b;
            }
            else
            {
                delta = b - e;
            }
            delta = delta / 1000 / 60; //convert from millisec to min
        } catch (java.text.ParseException ex)
        {
            System.out.println(ex);
            delta = 0;
        }
        // Assumes delta < INT_MAX
        return (int) delta;

    }
}
