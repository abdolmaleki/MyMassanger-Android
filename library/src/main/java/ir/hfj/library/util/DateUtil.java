package ir.hfj.library.util;


import java.util.Calendar;
import java.util.Date;

public class DateUtil
{

    public static Date addMinute(Date date, int minute)
    {
        Calendar calendar = Calendar.getInstance();
        if (date != null)
        {
            calendar.setTime(date);
        }
        calendar.add(Calendar.MINUTE, minute);
        return calendar.getTime();
    }

    public static Date addMinute(int minute)
    {
        return addMinute(null, minute);
    }

    public static Date now()
    {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTime();
    }
    public static String dayOfWeekToString(int day)
    {
        switch (day)
        {
            case 1:
                return "شنبه";
            case 2:
                return "یکشنبه";
            case 3:
                return "دوشنبه";
            case 4:
                return "سه شنبه";
            case 5:
                return "چهارشنبه";
            case 6:
                return "پنجشنبه";
            case 7:
                return "جمعه";
        }
        return null;
    }
}
