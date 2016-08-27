package ir.hfj.library.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;


public class NhNotifyIcon extends ImageButton
{

    private final Context mContext;
    private boolean mIsShow = false;
    private String mMessage = "";
    public boolean mNeedActivation = false;
    private boolean mNeedUpdateApp;

    public NhNotifyIcon(Context context)
    {
        this(context, null);
    }

    public NhNotifyIcon(Context context, AttributeSet attrs)
    {
        this(context, attrs, android.R.attr.imageButtonStyle);
    }

    public NhNotifyIcon(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
    }

    private void initView()
    {
        setVisibility(View.INVISIBLE);
    }

    public String getMessage()
    {
        return mMessage;
    }

    public void setNeedActivation(boolean b)
    {
        mNeedActivation = b;
    }

    public void setNeedUpdateApp(boolean b)
    {
        mNeedUpdateApp = b;
    }

    public boolean isNeedActivation()
    {
        return mNeedActivation;
    }

    public boolean isNeedUpdateApp()
    {
        return mNeedUpdateApp;
    }

    public void show(int res)
    {
        show(mContext.getString(res));
    }

    public void show(String message)
    {
        mMessage = message;
        setContentDescription(message);
        mNeedActivation = false;
        mNeedUpdateApp = false;

        if (!mIsShow)
        {
            mIsShow = true;
            setVisibility(View.VISIBLE);
        }
    }

    public void hide()
    {
        mMessage = "";
        setContentDescription("");
        mNeedActivation = false;
        mNeedUpdateApp = false;

        if (mIsShow)
        {
            mIsShow = false;
            setVisibility(View.INVISIBLE);
        }
    }

}
