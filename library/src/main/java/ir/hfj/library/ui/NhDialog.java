package ir.hfj.library.ui;


import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.pnikosis.materialishprogress.ProgressWheel;

import ir.hfj.library.R;


public class NhDialog extends Dialog
{

	private Context mContext;
	private TextView uiTxvText;
	private TextView uiTxvSubText;
	private TextView uiTxvIcon;
	private Button uiBtnCancel;
	private Button uiBtnYes;
	private Button uiBtnNo;
	private ProgressWheel uiProgressbar;

	public NhDialog(Context context)
	{
		this(context, DialogIcon.INFO);
	}

	public NhDialog(Context context, DialogIcon icon)
	{
		super(context, icon.getTheme());

		mContext = context;

		requestWindowFeature(android.app.DialogFragment.STYLE_NO_TITLE);
		requestWindowFeature(android.app.DialogFragment.STYLE_NO_FRAME);

		setContentView(R.layout.dialog_nh);

		Window window = getWindow();
		window.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		window.getAttributes().windowAnimations = R.style.Samim_Dialog_Animation;

		initView(icon);

		setCanceledOnTouchOutside(false);

	}

	private void initView(DialogIcon icon)
	{

		uiTxvText = (TextView) findViewById(R.id.dialog_nh_txv_text);

		uiTxvSubText = (TextView) findViewById(R.id.dialog_nh_txv_subtext);

		uiProgressbar = (ProgressWheel) findViewById(R.id.dialog_nh_progressbar);

		uiTxvIcon = (TextView) findViewById(R.id.dialog_nh_txv_icon);



		if (icon == DialogIcon.LOADING)
		{
			//progress
			/*RotateAnimation anim = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
			anim.setFillEnabled(true);
			anim.setFillAfter(true);
			anim.setDuration(500);
			anim.setRepeatCount(Animation.INFINITE);
			anim.setInterpolator(new LinearInterpolator());
			uiTxvIcon.startAnimation(anim);*/
			//------------
			uiProgressbar.setVisibility(View.VISIBLE);
		}
		else
		{
			uiTxvIcon.setVisibility((icon == DialogIcon.NONE) ? View.GONE : View.VISIBLE);
			uiTxvIcon.setText(icon.getIcon(mContext));

			ViewGroup btnPanel = (ViewGroup) findViewById(R.id.dialog_nh_btn_panel);
			btnPanel.setVisibility(View.VISIBLE);

			uiBtnCancel = (Button) findViewById(R.id.dialog_nh_btn_cancel);

			uiBtnYes = (Button) findViewById(R.id.dialog_nh_btn_yes);

			uiBtnNo = (Button) findViewById(R.id.dialog_nh_btn_no);

			addYesButtonVisible(null);
		}

	}



	private View.OnClickListener onClickListenerDismiss = new View.OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			dismiss();
		}
	};

	public void addCancelButtonVisible(View.OnClickListener listener)
	{
		if (listener == null)
		{
			uiBtnCancel.setOnClickListener(onClickListenerDismiss);
		}
		else
		{
			uiBtnCancel.setOnClickListener(listener);
		}
		uiBtnCancel.setVisibility(View.VISIBLE);
	}

	public void addYesButtonVisible(View.OnClickListener listener)
	{
		if (listener == null)
		{
			uiBtnYes.setOnClickListener(onClickListenerDismiss);
		}
		else
		{
			uiBtnYes.setOnClickListener(listener);
		}
		uiBtnYes.setVisibility(View.VISIBLE);
	}

	public void addNoButtonVisible(View.OnClickListener listener)
	{
		if (listener == null)
		{
			uiBtnNo.setOnClickListener(onClickListenerDismiss);
		}
		else
		{
			uiBtnNo.setOnClickListener(listener);
		}
		uiBtnNo.setVisibility(View.VISIBLE);
	}

	@Override
	public void setTitle(CharSequence title)
	{
		setMainTitle(title);
	}

	@Override
	public void setTitle(int titleId)
	{
		setMainTitle(titleId);
	}

	public NhDialog setMainTitle(CharSequence title)
	{
		if(title == null)
		{
			title = "";
		}
		uiTxvText.setText(title);
		return this;
	}

	public NhDialog setMainTitle(int titleId)
	{
		uiTxvText.setText(titleId);

		return this;
	}

	public NhDialog setSubTitle(CharSequence title)
	{
		if(title == null)
		{
			title = "";
		}

		uiTxvSubText.setText(title);
		uiTxvSubText.setVisibility((title.length() == 0) ? View.GONE : View.VISIBLE);
		return this;
	}

	public NhDialog setSubTitle(int titleId)
	{
		setSubTitle(mContext.getString(titleId));
		return this;
	}

	public void setOnClickButtonCancel(View.OnClickListener lis)
	{
		uiBtnCancel.setOnClickListener(lis);
	}

	public void setOnClickButtonYes(View.OnClickListener lis)
	{
		uiBtnYes.setOnClickListener(lis);
	}

	public void setOnClickButtonNo(View.OnClickListener lis)
	{
		uiBtnNo.setOnClickListener(lis);
	}

	public static enum DialogIcon
	{

		ERROR(R.string.icon_circled_close33, R.style.Samim_Dialog_Red),
		SUCCESS(R.string.icon_circled_approve9, R.style.Samim_Dialog_Green),
		NONE(0, R.style.Samim_Dialog_White),
		LOADING(R.string.icon_circled_circular194, R.style.Samim_Dialog_Blue),
		ALERT(R.string.icon_circled_exclamation22, R.style.Samim_Dialog_Orange),
		INFO(R.string.icon_circled_exclamation22, R.style.Samim_Dialog_Blue);

		private int mIconRes;
		private int mTheme;

		private DialogIcon(int iconRes, int theme)
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
