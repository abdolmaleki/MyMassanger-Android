package ir.hfj.library.ui;


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import ir.hfj.library.R;


public class NhToast
{

    public static final int LENGTH_SHORT = 0;
    public static final int LENGTH_LONG = 1;

    public static Toast makeText(Activity activity, int resource, ToastIcon icon, int duration)
    {
        return NhToast.makeText(activity, activity.getString(resource), icon, duration);
    }

    public static Toast makeText(Activity activity, String text, ToastIcon icon, int duration)
    {

        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_nh, (ViewGroup) activity.findViewById(R.id.toast_nh_root));

        layout.setBackgroundResource(icon.getTheme());

        TextView txvText = (TextView) layout.findViewById(R.id.toast_nh_txv_text);
        txvText.setText(text);

        TextView txvIcon = (TextView) layout.findViewById(R.id.toast_nh_txv_icon);

        if (icon == ToastIcon.NONE)
        {
            txvIcon.setVisibility(View.GONE);
            txvText.setTextColor(Color.BLACK);
        }
        else
        {
            txvIcon.setText(icon.getIcon(activity));
        }

        Toast toast = new Toast(activity);
        //toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 210);
        toast.setDuration(duration);
        toast.setView(layout);

        return toast;

    }

    public enum ToastIcon
    {

        ERROR(R.string.icon_circled_close33, R.drawable.shape_toast_red),
        SUCCESS(R.string.icon_circled_approve9, R.drawable.shape_toast_green),
        NONE(0, R.drawable.shape_toast_white),
        INFO(R.string.icon_circled_exclamation22, R.drawable.shape_toast_blue),
        ALERT(R.string.icon_circled_exclamation22, R.drawable.shape_toast_orange);

        private int mIconRes;
        private int mTheme;

        private ToastIcon(int iconRes, int theme)
        {
            mIconRes = iconRes;
            mTheme = theme;
        }

        public String getIcon(Context c)
        {
            return c.getString(mIconRes);
        }

        public int getTheme()
        {
            return mTheme;
        }

    }

}
