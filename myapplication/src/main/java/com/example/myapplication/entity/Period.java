package com.example.myapplication.entity;

public class Period
{

    public static String getPeriodTitle(int recordType, int periodValue)
    {
        switch (recordType)
        {

            // DailyDelay
            case 1:
                return "مراسم صبحگاه";


            // PeriodicAbsence & PeriodicDelay
            case 2:
            case 4:
                switch (periodValue)
                {
                    case 1:
                        return "زنگ اول";
                    case 2:
                        return "زنگ دوم";
                    case 3:
                        return "زنگ سوم";
                    case 4:
                        return "زنگ چهارم";
                    case 5:
                        return "زنگ پنجم";
                    case 6:
                        return "زنگ ششم";
                }

            default:
                return "";
        }
    }

}
