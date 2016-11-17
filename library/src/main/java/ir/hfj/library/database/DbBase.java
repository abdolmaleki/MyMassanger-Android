package ir.hfj.library.database;


import android.database.SQLException;
import android.util.Log;

import com.activeandroid.query.Select;

import ir.hfj.library.application.AppConfig;
import ir.hfj.library.database.model.UserSettingModel;

public abstract class DbBase
{

    public final static class UserSetting
    {

        public static UserSettingModel select()
        {
            return new Select()
                    .from(UserSettingModel.class)
                    .executeSingle();
        }

        public static boolean updateUserSetting(UserSettingModel model)
        {
            try
            {

                UserSettingModel existModel = select();

                if (existModel == null)
                {

                    try
                    {
                        model.save();
                        return true;
                    }
                    catch (SQLException ex)
                    {
                        if (AppConfig.DEBUG)
                        {
                            Log.e(AppConfig.LOG_TAG, ex.getMessage());
                        }
                        return false;
                    }

                }
                else
                {
                    existModel.token = model.token;
                    existModel.name = model.name;
                    existModel.family = model.family;
                    existModel.key = model.key;
                    existModel.expired = model.expired;
                    existModel.phoneNumber = model.phoneNumber;
                    existModel.imageUrl = model.imageUrl;
                    existModel.save();
                    return true;
                }

            }
            catch (Exception ex)
            {
                if (AppConfig.DEBUG)
                {
                    Log.e(AppConfig.LOG_TAG, ex.getMessage());
                }
            }

            return false;
        }

        public static boolean updateUserSettingExpire(boolean expired)
        {
            try
            {
                UserSettingModel existModel = select();
                if (existModel != null)
                {
                    if (existModel.expired != expired)
                    {
                        existModel.expired = expired;
                        existModel.save();
                    }
                    return true;
                }
            }
            catch (Exception ex)
            {
                if (AppConfig.DEBUG)
                {
                    Log.e(AppConfig.LOG_TAG, ex.getMessage());
                }
            }
            return false;
        }

    }


}
