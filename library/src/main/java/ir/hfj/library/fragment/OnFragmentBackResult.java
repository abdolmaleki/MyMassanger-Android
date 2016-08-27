package ir.hfj.library.fragment;


import android.support.v4.app.Fragment;

import ir.hfj.library.holder.LoginDataHolder;


public interface OnFragmentBackResult
{
	public abstract void onBackResult(Fragment fragment, int result);

	public abstract boolean onSaveSetting(boolean isNewActivation);
	public abstract void onComplete();


	public abstract LoginDataHolder getLoginData();

}
