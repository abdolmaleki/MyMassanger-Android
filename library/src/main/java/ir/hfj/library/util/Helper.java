package ir.hfj.library.util;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Surface;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import ir.hfj.library.application.AppConfig;
import ir.hfj.library.exception.MyMessangerException;


public class Helper
{

    private static int todayDayOfWeek;
    public static List<Integer> getFlags(int nn)
    {
        double n = nn;
        List<Integer> flags = new ArrayList<Integer>();
        double n2 = 0;
        while (n != 0)
        {
            n2 = Math.log(n);
            n2 = Math.pow(2, n2);
            n = n - n2;
            flags.add((int) n2);
        }
        return flags;
    }

    public static boolean hasFlags(long flags, long n)
    {
        return (flags & n) == n;

    }

    public static boolean isFlag(int f)
    {
        double n2 = Math.log(f);
        if (Math.log(f) > n2)
        {
            return false;
        }
        return true;
    }

    public static String getIMEI(Context context)
    {
        try
        {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return telephonyManager.getDeviceId();
        }
        catch (Exception e)
        {
        }
        return "";
    }

    public static String md5(String s) throws MyMessangerException
    {
        try
        {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest)
            {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        }
        catch (Exception e)
        {
            throw new MyMessangerException("خطای رمزنگاری اطلاعات، لطفا مجددا سعی نمایید.");
        }

    }

    public static boolean pingUrl(String address) throws IOException
    {

        URL url = new URL(address);

        HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
        urlc.setRequestProperty("User-Agent", "Android Application");
        urlc.setRequestProperty("Connection", "close");
        urlc.setConnectTimeout(1000 * 5); // mTimeout is in seconds
        urlc.setReadTimeout(2000); // mTimeout is in seconds
        urlc.connect();

        return (urlc.getResponseCode() == 200);

    }

    public static int div(int a, int b)
    {
        return (int) (a / b);
    }

    public static String gregorianToJalali(String date)
    {
        String[] ymd = date.split("/");
        if (ymd.length != 3)
        {
            throw new RuntimeException("Data format error, yyyy/mm/dd");
        }

        try
        {
            return gregorianToJalali(Integer.parseInt(ymd[0]), Integer.parseInt(ymd[1]), Integer.parseInt(ymd[2]));
        }
        catch (Exception ex)
        {
            throw new RuntimeException("Data format error, yyyy/mm/dd");
        }

    }

    public static String gregorianToJalali(Date date)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        //return gregorianToJalali(new SimpleDateFormat("yyyy/MM/dd").format(date).toString());

        return gregorianToJalali(cal.get(Calendar.YEAR),
                                 cal.get(Calendar.MONTH) + 1,
                                 cal.get(Calendar.DAY_OF_MONTH));
    }

    public static String gregorianToJalali(int g_y, int g_m, int g_d)
    {
        int[] g_days_in_month = new int[]
                {
                        31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31
                };
        int[] j_days_in_month = new int[]
                {
                        31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29
                };

        int gy = g_y - 1600;
        int gm = g_m - 1;
        int gd = g_d - 1;

        int g_day_no = 365 * gy + div(gy + 3, 4) - div(gy + 99, 100) + div(gy + 399, 400);

        for (int i = 0; i < gm; ++i)
        {
            g_day_no += g_days_in_month[i];
        }
        if (gm > 1 && ((gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0)))
            /* leap and after Feb */
        {
            g_day_no++;
        }
        g_day_no += gd;

        int j_day_no = g_day_no - 79;

        int j_np = div(j_day_no, 12053); /* 12053 = 365*33 + 32/4 */
        j_day_no = j_day_no % 12053;

        int jy = 979 + 33 * j_np + 4 * div(j_day_no, 1461); /* 1461 = 365*4 + 4/4 */

        j_day_no %= 1461;

        if (j_day_no >= 366)
        {
            jy += div(j_day_no - 1, 365);
            j_day_no = (j_day_no - 1) % 365;
        }
        int i = 0;
        for (; i < 11 && j_day_no >= j_days_in_month[i]; ++i)
        {
            j_day_no -= j_days_in_month[i];
        }
        int jm = i + 1;
        int jd = j_day_no + 1;

        String year_formatted = String.valueOf(jy);
        String month_formatted = (jm > 9) ? String.valueOf(jm) : "0" + String.valueOf(jm);
        String day_formatted = (jd > 9) ? String.valueOf(jd) : "0" + String.valueOf(jd);

        return year_formatted + "/" + month_formatted + "/" + day_formatted;
        //return jy + "/" + jm + "/" + jd;
    }

    public static String getCurrentDate()
    {
        Calendar calendar = Calendar.getInstance();
        return Helper.gregorianToJalali(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
    }

    public static String getCurrentTime()
    {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE);
    }


    public static Date getDateTimeNow(long interval)
    {
        /*Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(calendar.getTimeInMillis() - interval);
        return Helper.gregorianToJalali(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                + " " + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE);*/

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(calendar.getTimeInMillis() - interval);
        return calendar.getTime();
    }

    public static String getVersionName(Context context)
    {
        PackageInfo pInfo;
        try
        {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName;
        }
        catch (Exception e)
        {

        }
        return "0.0.0";
    }

    public static boolean netCheck(Context context)
    {
        try
        {
            ConnectivityManager nInfo = (ConnectivityManager) context.getSystemService(
                    Context.CONNECTIVITY_SERVICE);
            nInfo.getActiveNetworkInfo().isConnectedOrConnecting();
            Log.d(AppConfig.LOG_TAG, "NetCheck > Net avail:" + nInfo.getActiveNetworkInfo().isConnectedOrConnecting());
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(
                    Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnectedOrConnecting())
            {
                Log.d(AppConfig.LOG_TAG, "NetCheck > Network available:true");
                return true;
            }
            else
            {
                Log.d(AppConfig.LOG_TAG, "NetCheck > Network available:false");
                return false;
            }
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public static String generateRandomString(int count)
    {
        Random rnd = new Random();
        int acA = (int) 'A';
        int acZ = (int) 'Z';

        String code = "";

        while (code.length() < count)
        {
            if (rnd.nextBoolean())
            {
                char ch = ((char) (rnd.nextInt(acZ - acA + 1) + acA));
                code += ch;
            }
            else
            {
                code += (rnd.nextInt(8) + 2) + "";
            }

        }

        return code;
    }

    public static String correctJson(String json)
    {
        return json.replaceAll("\\\\", "");//.substring(1);
    }

    public static int getTodayDayOfWeek()
    {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);

        return (day == Calendar.SATURDAY) ? 0 : day;

    }
    public static int getDpi(Context context, int value)
    {
        return ((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, context.getResources().getDisplayMetrics()));
    }

    public static boolean isNeedAutoRefreshTime(Date date)
    {
        return (date == null || DateUtil.addMinute(-AppConfig.AUTO_REFRESH_INTERVAL).after(date));
    }

    public static float convertScore(String score)
    {
        return Float.parseFloat(score.replace("/", "."));
    }

    private static int _UniqueNumber = 1;
    public static int generateUniqueNumber()
    {
        return _UniqueNumber++;
    }

    public static String getDate(Date date)
    {
        return gregorianToJalali(date);
    }

    public static String getTime(Date date)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE);
    }

    public static boolean isLandscapeOrientation(Activity ctx)
    {

        int rotation = ctx.getWindowManager().getDefaultDisplay().getRotation();
        DisplayMetrics dm = new DisplayMetrics();
        ctx.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;

        // if the device's natural orientation is portrait:
        if ((rotation == Surface.ROTATION_0
                || rotation == Surface.ROTATION_180) && height > width ||
                (rotation == Surface.ROTATION_90
                        || rotation == Surface.ROTATION_270) && width > height)
        {
            switch (rotation)
            {
                case Surface.ROTATION_90:
                    return true;

                case Surface.ROTATION_270:
                    return true;
                default:
                    return false;
            }
        }
        // if the device's natural orientation is landscape or if the device
        // is square:
        else
        {
            switch (rotation)
            {
                case Surface.ROTATION_0:
                    return true;
                case Surface.ROTATION_180:
                    return true;

                default:
                    return false;
            }
        }
    }


    public static String getFileSize(String path)
    {
        if (path == null || path.isEmpty())
        {
            return "";
        }

        long fileLength;

        try
        {
            File file = new File(path);
            fileLength = file.length();
        }
        catch (Exception ex)
        {
            return "";
        }

        if ((fileLength / 1024) < 1)
        {
            return fileLength + " Byte";
        }

        long fileSizeInKB = fileLength / 1024;

        if ((fileSizeInKB / 1024) < 1)
        {
            return fileSizeInKB + " KB";
        }

        long fileSizeInMB = fileSizeInKB / 1024;

        if ((fileSizeInMB / 1024) < 1)
        {
            return fileSizeInMB + " MB";
        }

        long fileSizeInGB = fileSizeInMB / 1024;

        if ((fileSizeInGB / 1024) < 1)
        {
            return fileSizeInGB + " GB";
        }

        return "Big Size ! ";

    }

    /*public static String getFileType(File file)
    {
        MimeTypeMap map = MimeTypeMap.getSingleton();
        String ext = MimeTypeMap.getFileExtensionFromUrl(file.getName());
        String type = map.getMimeTypeFromExtension(ext);

        if (type == null)
        {
            //type = "*//*";
        }

        return type;
    }*/

    public static boolean openFileIntent(Context ctx, String path, String ext)
    {
        try
        {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri data = Uri.parse(path);

            MimeTypeMap map = MimeTypeMap.getSingleton();
            String type = map.getMimeTypeFromExtension(ext);
            if (type == null)
            {
                type = "*/*";
            }

            intent.setDataAndType(data, type);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(intent);
            return true;
        }
        catch (Exception ex)
        {
            return false;
        }

    }

    public static String getExtension(String path)
    {
        return path.substring(path.lastIndexOf(".") + 1);
    }

    public static String[] splitDate(String date)
    {
        return date.split("/");
    }

}
