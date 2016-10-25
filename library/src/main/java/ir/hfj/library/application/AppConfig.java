package ir.hfj.library.application;

import android.graphics.Bitmap;

import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

import ir.hfj.library.R;

public final class AppConfig
{

    private static String APP_VERSION;
    private static String APP_NAME = "mymessanger";

    public static void init(String version)
    {
        APP_VERSION = version;
    }

    public static String getAppVersion()
    {
        return APP_VERSION;
    }

    public static String getAppName()
    {
        return APP_NAME;
    }


    //=====================================================================================================

    public static String LOG_TAG;
    public static int ROLE = 0;
    public static boolean DEBUG = true;

    public static int NETWORK_SR_TIME_OUT = 25000;
    public static final int CHAT_REPORT_TYPING_INTERVAL = 5000;


    public static String NETWORK_HOST_WS;
    public static String NETWORK_HOST_SR;
    public static String NETWORK_HOST_WEB;

    public static String NETWORK_MESSANGER_HUB;
    public static int NETWORK_HOST_TRY_CONNECT_DURATION;
    public static String NETWORK_HOST_PING;
    public static int NETWORK_HOST_TRY_CONNECT_EMERGENCY_DURATION;//1000 * 60 * 30;

    public static int AUTO_REFRESH_INTERVAL;//minute

    //======================================================================================================

    public static final float SPIN_SPEED = 230f / 360f;

    public static class HttpHeaders
    {

        //Http headers
        public static final String Authorization = "Authorization";
        public static final String Version = "Version";
        public static final String Encryption = "Encryption";
    }

    public static class RestApiAction
    {

        //rest methods
        public static final String ActivationPhone = "UserActivation/Phone";
        public static final String ActivationRegister = "UserActivation/Register";
        public static final String ActivationVerify = "UserActivation/Verify";
        public static final String ActivationVerifyKey = "UserActivation/VerifyKey";
        public static final String AccountLogin = "Account/Login";
        public static final String UtilityUpdateApp = "Utility/UpdateApp";
        public static final String MediaUpload = "Media/Upload";
        public static final String MediaDownload = "Media/Download";
    }

    private static Gson Gson = null;

    public static Gson getGsonSetting()
    {
        return Gson;
    }

    public static void setGsonSetting(Gson gson)
    {
        Gson = gson;
    }

    public static DisplayImageOptions createDisplayImageOptions()
    {
        return new DisplayImageOptions.Builder()

                //.showImageOnLoading(R.drawable.img_loading)
                .showImageForEmptyUri(R.drawable.img_alert)
                .showImageOnFail(R.drawable.img_fail)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .cacheInMemory(false)
                .cacheOnDisk(true)
                .considerExifParams(false)
                        //.displayer(new RoundedBitmapDisplayer(20))
                .build();
    }

    public static DisplayImageOptions createChatContentDisplayImageOptions()
    {

        return new DisplayImageOptions.Builder()

                .showImageOnLoading(R.drawable.img_loading)
                .showImageForEmptyUri(0)
                .showImageOnFail(0)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(false)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();

    }

    public static DisplayImageOptions createNotificationDisplayImageOptions()
    {
        return new DisplayImageOptions.Builder()

                .showImageOnLoading(R.drawable.img_loading)
                .showImageForEmptyUri(R.drawable.img_alert)
                .showImageOnFail(R.drawable.img_fail)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .cacheInMemory(false)
                .cacheOnDisk(true)
                .considerExifParams(false)
                .bitmapConfig(Bitmap.Config.RGB_565)
                        //.displayer(new RoundedBitmapDisplayer(20))
                .build();
    }


}
