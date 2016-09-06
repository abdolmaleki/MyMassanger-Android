package ir.hfj.library.application;


import android.app.Activity;
import android.app.Application;
import android.util.Log;

import com.github.mikephil.charting.utils.Utils;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.utils.L;

import ir.hfj.library.R;
import ir.hfj.library.database.DbBase;
import ir.hfj.library.database.model.UserSettingModel;
import ir.hfj.library.receiver.SamimAction;
import ir.hfj.library.util.Helper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public abstract class App extends Application
{


    protected UserSettingModel mUserSetting = null;


    @Override
    public void onCreate()
    {

//        if (Constants.Config.DEVELOPER_MODE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
//        {
//        	StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyDialog().build());
//        	StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyDeath().build());
//        }

        super.onCreate();

        if (SamimAction.ACTIVATION_EXPIRED.isEmpty() ||
                SamimAction.ACTIVATION_SUCCESSFUL.isEmpty() ||
                SamimAction.ACTIVATION_NULL.isEmpty() ||
                AppConfig.NETWORK_HOST_SR.isEmpty())
        {
            throw new RuntimeException("##### SamimAction must be initialization");
        }

        AppConfig.init(Helper.getVersionName(this));

        //Chart configuration------------------------------------------------------------------------------------------------------
        Utils.init(this);

        //AA configuration------------------------------------------------------------------------------------------------------
        ActiveAndroidInitialize();

        //font configuration------------------------------------------------------------------------------------------------------
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                                              //.setDefaultFontPath("fonts/icon.ttf")
                                              .setFontAttrId(R.attr.fontPath).build());

        //image loader configuration----------------------------------------------------------------------------------------------

        // This configuration tuning is custom. You can tune every option, you may tune some of them,
        // or you can create default configuration by
        //  ImageLoaderConfiguration.createDefault(this);
        // method.
        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(getApplicationContext());
        config.threadPriority(Thread.NORM_PRIORITY - 2);
        config.denyCacheImageMultipleSizesInMemory();
        config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
        config.diskCacheSize(50 * 1024 * 1024); // 50 MiB
        config.tasksProcessingOrder(QueueProcessingType.LIFO);
        L.writeLogs(false);
        L.writeDebugLogs(false);
        if (AppConfig.DEBUG)
        {
            config.writeDebugLogs(); // Remove for release app
        }
        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config.build());

        //------------------------------------------------------------------------------------------------------------------------


        loadUserSetting();

        Log.v(AppConfig.LOG_TAG, "App start...");

    }

    protected abstract void ActiveAndroidInitialize();

    public static App getInstance(Activity activity)
    {
        return ((App) activity.getApplication());
    }

    public boolean isUserSetting()
    {
        return mUserSetting != null;
    }

    public String getToken()
    {
        if (mUserSetting == null)
        {
            return null;
        }
        return mUserSetting.token;
    }

    public boolean isExpired()
    {
        if (mUserSetting == null)
        {
            return true;
        }
        return mUserSetting.expired;
    }

    public String getKey()
    {
        if (mUserSetting == null)
        {
            return null;
        }
        return mUserSetting.key;
    }


    public void loadUserSetting()
    {
        mUserSetting = DbBase.UserSetting.select();
    }



}