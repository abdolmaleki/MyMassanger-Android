package ir.hfj.library.util;

import java.util.Calendar;


public class SamimDate
{

    private final String date;
    private JalaliCalendar jalaliCalendar = new JalaliCalendar();

    public SamimDate(String date)
    {
        this.date = date;
    }

    public String getFirstWeekDate()
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(jalaliCalendar.getGregorianDate(date));
        int d = calendar.get(Calendar.DAY_OF_WEEK);
        d = getDayOfWeek(d);
        calendar.add(Calendar.DAY_OF_MONTH, -d);
        return jalaliCalendar.getJalaliDate(calendar.getTime());
    }

    public String getLastWeekDate()
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(jalaliCalendar.getGregorianDate(date));
        int d = calendar.get(Calendar.DAY_OF_WEEK);
        d = getDayOfWeek(d);
        calendar.add(Calendar.DAY_OF_MONTH, 6 - d);
        return jalaliCalendar.getJalaliDate(calendar.getTime());
    }

    public static String addDay(String date, int count)
    {
        JalaliCalendar jalaliCalendar = new JalaliCalendar();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(jalaliCalendar.getGregorianDate(date));
        calendar.add(Calendar.DAY_OF_MONTH, count);
        return jalaliCalendar.getJalaliDate(calendar.getTime());
    }

    private int getDayOfWeek(int day)
    {
        return (day == 7) ? 0 : day;
    }


    public static boolean isKabise(int year)
    {
        String date = year + "/12/29";
        JalaliCalendar jalaliCalendar = new JalaliCalendar();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(jalaliCalendar.getGregorianDate(date));
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        return (jalaliCalendar.getJalaliDate(calendar.getTime()).endsWith("/30"));
    }

}