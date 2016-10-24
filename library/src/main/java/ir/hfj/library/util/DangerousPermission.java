package ir.hfj.library.util;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import ir.hfj.library.ui.NhDialog;


@TargetApi(Build.VERSION_CODES.M)
public final class DangerousPermission
{

    public static int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    public static String[] DangerousPermission = new String[]
            {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE
            };

    public static void requestPermissionsIfNeeded(Activity activity)
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
        {
            return;
        }


        final List<String> permissionsList = new ArrayList<>();

        for (String permission : DangerousPermission)
        {
            addPermissionIfDented(activity, permissionsList, permission);
        }

        if (permissionsList.size() > 0)
        {
            activity.requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
        }
    }

    private static boolean addPermissionIfDented(Activity activity, List<String> permissionsList, String permission)
    {
        if (activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
        {
            permissionsList.add(permission);
            return true;
        }
        return false;
    }


    public static boolean checkRequestPermissionsResult(final Activity activity, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
        {
            return true;
        }

        if (requestCode != REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS)
        {
            return false;
        }

        boolean isAccepted = true;
        String errorMessage = "لطفا دسترسی های زیر را فعال کنید:";

        for (int i = 0; i < permissions.length; i++)
        {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED)
            {
                isAccepted = false;
                //if (!activity.shouldShowRequestPermissionRationale(permissions[i]))
                {
                    errorMessage += "\n- " + PermissionInfo.getFaName(activity, permissions[i]);
                }
            }
        }

        if (!isAccepted)
        {
            NhDialog dialog = new NhDialog(activity, NhDialog.DialogIcon.ALERT)
                    .setMainTitle("عدم دسترسی")
                    .setSubTitle(errorMessage);
            dialog.setCancelable(false);
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener()
            {
                @Override
                public void onDismiss(DialogInterface dialog)
                {
                    activity.finish();
                }
            });
            dialog.show();

        }

        return true;


    }
}
