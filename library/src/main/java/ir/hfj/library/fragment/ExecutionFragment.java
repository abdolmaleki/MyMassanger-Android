package ir.hfj.library.fragment;

import android.app.Activity;
import android.support.v4.app.Fragment;

import ir.hfj.library.activity.ActivationActivity;


public abstract class ExecutionFragment extends Fragment
{
	public static final int RESULT_OK = 1;
	public static final int RESULT_CANCEL = 2;
	public static final int RESULT_ERROR = 3;
	public static final int RESULT_BACK = 4;
	public static final int RESULT_EXTRA = 5;

	private boolean isRunning = false;
	protected OnFragmentBackResult mBackResultListener;
	public Activity mActivity;

	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);

		if (!(activity instanceof OnFragmentBackResult))
		{
			throw new RuntimeException("########## Activity must implement OnFragmentBackResult interface");
		}

		mBackResultListener = (OnFragmentBackResult) activity;
		mActivity = activity;

	}

	public final void startCommand()
	{
		if (!isRunning)
		{
			isRunning = true;
			bodyCommand();
		}



	}


	protected void setBtnPanelState(boolean back, boolean close, boolean next)
	{
		if ((mActivity instanceof ActivationActivity))
		{
			((ActivationActivity) mActivity).setBtnPanelState(back, close, next);
		}
	}

	/**
	 * Don't Call Directly
	 */
	protected abstract void bodyCommand();

	/**
	 * Must called, when end of executing command in bodyCommand method
	 * @param result ex: {@link #RESULT_OK}
	 */
	public final void onBackResult(int result)
	{
		isRunning = false;
		mBackResultListener.onBackResult(this, result);
	}

}
