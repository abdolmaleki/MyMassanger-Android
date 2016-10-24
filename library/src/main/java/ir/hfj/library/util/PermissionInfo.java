package ir.hfj.library.util;


import android.Manifest;
import android.content.Context;

import java.util.HashMap;

import ir.hfj.library.R;


public class PermissionInfo
{

    private static HashMap<String, Integer> mDictionary = new HashMap<>();

    static
    {
        mDictionary.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, R.string.WRITE_EXTERNAL_STORAGE);
        mDictionary.put(Manifest.permission.READ_PHONE_STATE, R.string.READ_PHONE_STATE);
    }

    public static String getFaName(Context context, String permission)
    {
        return context.getString(mDictionary.get(permission));
    }

}
