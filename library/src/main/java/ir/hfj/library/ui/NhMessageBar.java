package ir.hfj.library.ui;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ir.hfj.library.R;


public class NhMessageBar
{

	private boolean isShow = false;
	private ViewGroup uiRootView;
	private TextView uiTxvLabel;
	private Activity mActivity;

	public NhMessageBar(Activity activity)
	{
		mActivity = activity;

		View view = activity.findViewById(android.R.id.content);


		LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		uiRootView = (ViewGroup) inflater.inflate(R.layout.ht_messagebar, null);
		uiTxvLabel = (TextView) uiRootView.findViewById(R.id.ht_view_error_label);

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
		params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);

		uiRootView.setVisibility(View.INVISIBLE);

		((ViewGroup) view).addView(uiRootView, params);


	}

	public void setOnClickListener(View.OnClickListener listener)
	{
		uiRootView.setOnClickListener(listener);
	}

	public void show(int res)
	{
		show(mActivity.getString(res));
	}

	public void show(String message)
	{
		setOnClickListener(null);
		uiTxvLabel.setText(message);

		if (!isShow)
		{
			isShow = true;

			ScaleAnimation anim = new ScaleAnimation(1, 1, 0, 1);
			anim.setInterpolator(new AccelerateInterpolator());
			anim.setDuration(300);
			anim.setFillEnabled(true);
			anim.setFillAfter(true);
			uiRootView.startAnimation(anim);

		}

	}

	public void hide()
	{
		setOnClickListener(null);
		uiTxvLabel.setText("");

		if (isShow)
		{
			isShow = false;
			ScaleAnimation anim = new ScaleAnimation(1, 1, 1, 0);
			anim.setInterpolator(new AccelerateInterpolator());
			anim.setDuration(300);
			anim.setFillEnabled(true);
			anim.setFillAfter(true);
			uiRootView.startAnimation(anim);
		}

	}

}
