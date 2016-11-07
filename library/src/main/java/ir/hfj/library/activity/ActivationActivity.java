package ir.hfj.library.activity;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Locale;

import ir.hfj.library.R;
import ir.hfj.library.application.App;
import ir.hfj.library.connection.restapi.ActivationRestApi;
import ir.hfj.library.connection.restapi.jto.ActivationJto;
import ir.hfj.library.connection.restapi.jto.PostBackJto;
import ir.hfj.library.database.DbBase;
import ir.hfj.library.exception.MyMessangerException;
import ir.hfj.library.fragment.ExecutionFragment;
import ir.hfj.library.fragment.OnFragmentBackResult;
import ir.hfj.library.holder.LoginDataHolder;
import ir.hfj.library.receiver.SamimAction;
import ir.hfj.library.ui.NhDialog;
import ir.hfj.library.util.Encryption;
import ir.hfj.library.util.Helper;
import ir.hfj.library.util.Validation;
import me.relex.circleindicator.CircleIndicator;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class ActivationActivity extends FragmentActivity implements View.OnClickListener, OnFragmentBackResult
{

    private final static String KEY_LoginData = "loginData";
    public final static String KEY_CAUSE = "cause";

    public final static int PARAM_CAUSE_EXPIRE = 1;
    public final static int PARAM_CAUSE_ACTIVE = 2;

    private Button uiBtnBack;
    private Button uiBtnClose;
    private Button uiBtnNext;

    private LoginDataHolder mLoginData;

    //=======================================
    public String ACTIVATE_CODE = "";
    //=======================================
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        int openCause = PARAM_CAUSE_ACTIVE;

        Bundle bundle = getIntent().getExtras();
        if (bundle != null)
        {
            openCause = bundle.getInt(KEY_CAUSE);
        }

        if (savedInstanceState == null)
        {
            Fragment fragment;
            if (openCause == PARAM_CAUSE_EXPIRE)
            {
                fragment = ActivationExpireFragment.newInstance();
            }
            else
            {
                fragment = WelComeFragment.newInstance();
            }

            mLoginData = new LoginDataHolder();

            FragmentManager fm = getSupportFragmentManager();

            fm.beginTransaction().add(R.id.fragment_place, fragment).commit();
        }
        else
        {
            mLoginData = savedInstanceState.getParcelable(KEY_LoginData);
        }

        initView();

    }

    @Override
    protected void attachBaseContext(Context newBase)
    {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void initView()
    {

        uiBtnBack = (Button) findViewById(R.id.activity_login_btn_back);
        uiBtnBack.setOnClickListener(this);

        uiBtnClose = (Button) findViewById(R.id.activity_login_btn_close);
        uiBtnClose.setOnClickListener(this);

        uiBtnNext = (Button) findViewById(R.id.activity_login_btn_next);
        uiBtnNext.setOnClickListener(this);

    }

    public void setBtnPanelState(boolean back, boolean close, boolean next)
    {

        uiBtnBack.setVisibility((back) ? View.VISIBLE : View.INVISIBLE);
        uiBtnBack.setOnClickListener((back) ? this : null);

        uiBtnClose.setVisibility((close) ? View.VISIBLE : View.INVISIBLE);
        uiBtnBack.setOnClickListener((close) ? this : null);

        uiBtnNext.setVisibility((next) ? View.VISIBLE : View.INVISIBLE);
        uiBtnBack.setOnClickListener((next) ? this : null);
    }

    @Override
    public void onClick(View v)
    {
        int id = v.getId();
        if (id == R.id.activity_login_btn_next)
        {
            ExecutionFragment fragment = (ExecutionFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_place);
            fragment.startCommand();
        }
        else if (id == R.id.activity_login_btn_back)
        {
            getSupportFragmentManager().popBackStack();
        }
        else if (id == R.id.activity_login_btn_close)
        {
            finish();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        outState.putSerializable(KEY_LoginData, mLoginData);
    }

    @Override
    public void onBackResult(Fragment frg, int result)
    {

        if (result == ExecutionFragment.RESULT_OK)
        {
            if (frg instanceof WelComeFragment || frg instanceof ActivationExpireFragment)
            {
                changeFragment(ActivationPhoneFragment.newInstance());
            }
            else if (frg instanceof ActivationPhoneFragment)
            {
                changeFragment(ActivationCodeFragment.newInstance());

            }
            else if (frg instanceof ActivationCodeFragment)
            {
                changeFragment(ActivationVerifyFragment.newInstance());
            }
            else if (frg instanceof ActivationVerifyFragment)
            {
                changeFragment(ActivationFinishFragment.newInstance());
            }
            //else
            //{
            //}

        }
        else if (result == ExecutionFragment.RESULT_BACK)
        {
            getSupportFragmentManager().popBackStack();
        }
        else if (result == ExecutionFragment.RESULT_EXTRA)
        {
            if (frg instanceof ActivationVerifyFragment)
            {
                //changeFragment(ActivationRegisterFragment.newInstance());
            }
            else if (frg instanceof ActivationExpireFragment)
            {
                this.finish();
            }

        }
        //else
        //{

        //}

    }

    private void changeFragment(Fragment fragment)
    {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2)
        {
			ft.setCustomAnimations(R.anim.anim_slide_in, R.anim.anim_slide_out, R.anim.anim_slide_popin, R.anim.anim_slide_popout);
		}
		else
		{
			ft.setCustomAnimations(R.anim.anim_slide_in, R.anim.anim_slide_out);
		}*/

        ft.replace(R.id.fragment_place, fragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    public LoginDataHolder getLoginData()
    {
        return mLoginData;
    }

    @Override
    public boolean onSaveSetting(boolean isNewActivation)
    {
        boolean result;

        result = DbBase.UserSetting.updateUserSetting(mLoginData.getUserSettingModel());
        App.getInstance(this).loadUserSetting();
        return result;
    }

    @Override
    public void onComplete()
    {
        //send broadcast
        Intent intent = new Intent();
        intent.setAction(SamimAction.ACTIVATION_SUCCESSFUL);
        sendBroadcast(intent);
    }

    //=====================================================================================================
    //=====================================================================================================
    //=====================================================================================================
    //=====================================================================================================
    //=====================================================================================================
    //=====================================================================================================
    //Fragments
    //=====================================================================================================
    //=====================================================================================================
    //=====================================================================================================
    //=====================================================================================================
    //=====================================================================================================
    //=====================================================================================================
    public static class WelComeFragment extends ExecutionFragment
    {

        private MyPagerAdapter mAdapter;
        private ViewPager mPager;

        public static WelComeFragment newInstance()
        {
            WelComeFragment fragment = new WelComeFragment();
            //Bundle args = new Bundle();
            //args.putInt(KEY_URL, link);
            //downloaderFragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onAttach(Activity activity)
        {
            super.onAttach(activity);

            if (!(activity instanceof FragmentActivity))
            {
                throw new RuntimeException("########## Activity must implement FragmentActivity interface");
            }

        }

        @Override
        public void onResume()
        {
            super.onResume();
            setBtnPanelState(false, true, true);
        }

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);

            //getArguments().getInt(KEY_URL, 0);
            FragmentManager fm = getChildFragmentManager();
            mAdapter = new MyPagerAdapter(fm, new int[]{
                    R.layout.fragment_welcome_one,
                    R.layout.fragment_welcome_two,
                    R.layout.fragment_welcome_three,});

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
            View rootView = inflater.inflate(R.layout.fragment_login_welcome, container, false);

            mPager = (ViewPager) rootView.findViewById(R.id.fragment_login_welcome_viewpager);
            mPager.setAdapter(mAdapter);
            //mPager.setCurrentItem(0);

            CircleIndicator mIndicator = (CircleIndicator) rootView.findViewById(R.id.indicator);
            mIndicator.setViewPager(mPager);
            //mIndicator.setCurrentItem(mAdapter.getCount() - 1);
            mPager.setCurrentItem(0);

            return rootView;
        }

        @Override
        public void bodyCommand()
        {
            if(mAdapter != null && mPager.getCurrentItem() < mAdapter.getCount() - 1)
            {
                mPager.setCurrentItem(mPager.getCurrentItem() + 1);
                onBackResult(ExecutionFragment.RESULT_EXTRA);
            }
            else
            {
                onBackResult(ExecutionFragment.RESULT_OK);
            }

        }

        private static class MyPagerAdapter extends FragmentPagerAdapter
        {

            private int[] mResIds;

            public MyPagerAdapter(FragmentManager fm, int[] resIds)
            {
                super(fm);
                mResIds = resIds;
            }

            @Override
            public int getCount()
            {
                return mResIds.length;
            }

            @Override
            public Fragment getItem(int position)
            {

                return WelcomeItemPagerFragment.newInstance(mResIds[position]);

            }
        }

        public static class WelcomeItemPagerFragment extends Fragment implements View.OnClickListener
        {

            private static final String KEY_RES = "res";
            private int mResId = 0;

            public static WelcomeItemPagerFragment newInstance(int resId)
            {
                WelcomeItemPagerFragment fragment = new WelcomeItemPagerFragment();
                Bundle args = new Bundle();
                args.putInt(KEY_RES, resId);
                fragment.setArguments(args);
                return fragment;
            }

            @Override
            public void onCreate(Bundle savedInstanceState)
            {
                super.onCreate(savedInstanceState);
                mResId = getArguments().getInt(KEY_RES, 0);
            }

            @Override
            public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
            {

                View rootView = inflater.inflate(mResId, container, false);

                //rootView.findViewById(R.id.buttonblue).setOnClickListener(this);
                //rootView.findViewById(R.id.buttongreen).setOnClickListener(this);
                //rootView.findViewById(R.id.buttonorange).setOnClickListener(this);
                //rootView.findViewById(R.id.buttonred).setOnClickListener(this);
                //rootView.findViewById(R.id.buttonwhite).setOnClickListener(this);

                return rootView;
            }

            @Override
            public void onClick(View v)
            {
                //int theme = R.style.NH_Dialog_Theme_White;
                /*NhToast.ToastIcon toastIcon = NhToast.ToastIcon.NONE;


				int id = v.getId();

				if (id == R.id.buttonblue)
				{
					toastIcon = NhToast.ToastIcon.NONE;
				}
				else if (id == R.id.buttongreen)
				{
					toastIcon = NhToast.ToastIcon.SUCCESS;

				}
				else if (id == R.id.buttonorange)
				{
					toastIcon = NhToast.ToastIcon.ALERT;

				}
				else if (id == R.id.buttonred)
				{
					toastIcon = NhToast.ToastIcon.ERROR;

				}
				else if (id == R.id.buttonwhite)
				{

				}

				NhToast.makeText(getActivity(), "سلام خوبی من منم و تو!", toastIcon, Toast.LENGTH_SHORT).show();

				NhDialog dialog = new NhDialog(getActivity(), NhDialog.DialogIcon.LOADING);
				dialog.setMainTitle("خطای سیستمی!").setSubTitle("عملیات با موفقیت انجام شد.");
				dialog.show();*/


            }
        }

    }

    //=====================================================================================================

    public static class ActivationExpireFragment extends ExecutionFragment
    {

        public static ActivationExpireFragment newInstance()
        {
            ActivationExpireFragment fragment = new ActivationExpireFragment();
            //Bundle args = new Bundle();
            //args.putInt(KEY_URL, link);
            //downloaderFragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onAttach(Activity activity)
        {
            super.onAttach(activity);

            if (!(activity instanceof FragmentActivity))
            {
                throw new RuntimeException("########## Activity must implement FragmentActivity interface");
            }

        }

        @Override
        public void onResume()
        {
            super.onResume();
            setBtnPanelState(false, true, true);
        }

        @Override
        public void bodyCommand()
        {
            onBackResult(ExecutionFragment.RESULT_OK);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
            return inflater.inflate(R.layout.fragment_login_expire, container, false);
        }

    }

    //========================================================================================================

    public static class ActivationPhoneFragment extends ExecutionFragment
    {

        private EditText uiEdtPhone;

        public static ActivationPhoneFragment newInstance()
        {
            ActivationPhoneFragment fragment = new ActivationPhoneFragment();
            //Bundle args = new Bundle();
            //args.putInt(KEY_URL, link);
            //downloaderFragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onResume()
        {
            super.onResume();
            setBtnPanelState(true, true, true);
        }

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);

            //getArguments().getInt(KEY_URL, 0);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
            View rootView = inflater.inflate(R.layout.fragment_login_phone, container, false);

            final Animation animNumberScale;


            uiEdtPhone = (EditText) rootView.findViewById(R.id.fragment_login_phone_edt_phone);

            animNumberScale = AnimationUtils.loadAnimation(getActivity(), R.anim.number_scale);

            uiEdtPhone.addTextChangedListener(new TextWatcher()
            {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
                {

                }
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
                {
                    uiEdtPhone.startAnimation(animNumberScale);
                }
                @Override
                public void afterTextChanged(Editable editable)
                {
                }
            });

            return rootView;
        }

        @Override
        public void bodyCommand()
        {
            String phoneNumber = uiEdtPhone.getText().toString().trim();

            if (Validation.phoneNumber(phoneNumber))
            {
                new ActivationPhoneAsyncTask(this, phoneNumber).execute();
            }
            else
            {
                NhDialog dialog = new NhDialog(mActivity, NhDialog.DialogIcon.ALERT);
                dialog.setMainTitle(R.string.messanger_login_message_title_validation).setSubTitle(R.string.messanger_login_message_notvalid_phonenumber);
                dialog.show();
                onBackResult(ExecutionFragment.RESULT_ERROR);
            }

        }

        static class ActivationPhoneAsyncTask extends AsyncTask<Void, Void, ActivationJto.Phone.PostBack>
        {

            private String mPhoneNumber;
            private ExecutionFragment mFragment;
            private NhDialog mDialog;
            private ActivationRestApi mRestApi;

            public ActivationPhoneAsyncTask(ExecutionFragment fragment, String phoneNumber)
            {
                this.mPhoneNumber = phoneNumber;
                this.mFragment = fragment;
                mRestApi = new ActivationRestApi(this.mFragment.mActivity);
            }

            @Override
            protected void onPreExecute()
            {

                mDialog = new NhDialog(mFragment.mActivity, NhDialog.DialogIcon.LOADING);
                mDialog.setCancelable(true);
                mDialog.setMainTitle(R.string.messanger_login_message_title_server_sending).setSubTitle(R.string.messanger_message_pleasewait);
                mDialog.setOnCancelListener(new DialogInterface.OnCancelListener()
                {

                    @Override
                    public void onCancel(DialogInterface dialog)
                    {
                        mRestApi.cancel();
                        cancel(true);
                    }
                });
                mDialog.show();

            }

            @Override
            protected void onCancelled()
            {
                mFragment.onBackResult(ExecutionFragment.RESULT_CANCEL);
            }

            @Override
            protected ActivationJto.Phone.PostBack doInBackground(Void... s)
            {

                ActivationJto.Phone.Post dto = new ActivationJto.Phone.Post(this.mPhoneNumber);
                ActivationJto.Phone.PostBack postBackJTO;

                try
                {
                    postBackJTO = mRestApi.activationPhone(dto);
                }
                catch (MyMessangerException e)
                {
                    postBackJTO = new ActivationJto.Phone.PostBack();
                    postBackJTO.stateCode = PostBackJto.RESULT_BAD_REQUEST;
                    postBackJTO.detailMessage = e.getMessage();
                    postBackJTO.subjectMessage = mFragment.getString(R.string.messanger_login_message_title_error_network);
                }

                return postBackJTO;
            }

            @Override
            protected void onPostExecute(ActivationJto.Phone.PostBack dto)
            {

                mDialog.dismiss();
                mDialog = null;

                if (dto.stateCode != PostBackJto.RESULT_OK)
                {
                    mFragment.onBackResult(ExecutionFragment.RESULT_ERROR);

                    mDialog = new NhDialog(mFragment.mActivity, NhDialog.DialogIcon.ERROR);
                    mDialog.setMainTitle(dto.subjectMessage).setSubTitle(dto.detailMessage);
                    mDialog.show();
                }
                else
                {
                    ((OnFragmentBackResult) mFragment.mActivity).getLoginData().phoneNumber = mPhoneNumber;
                    ((OnFragmentBackResult) mFragment.mActivity).getLoginData().activeToken = dto.activeToken;

                    mFragment.onBackResult(ExecutionFragment.RESULT_OK);

                    ///mDialog = new NhDialog(mFragment.mActivity, NhDialog.DialogIcon.SUCCESS);
                    ///mDialog.setMainTitle(dto.subjectMessage).setSubTitle(dto.detailMessage);
                    ///mDialog.show();

                    //+++++++++++++++++++++++++++++++++++++++++++++++++

                    if (dto.detailMessage.startsWith("__"))
                    {
                        ((ActivationActivity) mFragment.getActivity()).ACTIVATE_CODE = dto.detailMessage.substring(2);
                    }
                    else
                    {
                        mDialog = new NhDialog(mFragment.mActivity, NhDialog.DialogIcon.SUCCESS);
                        mDialog.setMainTitle(dto.subjectMessage).setSubTitle(dto.detailMessage);
                        mDialog.show();
                    }

                    //+++++++++++++++++++++++++++++++++++++++++++++++++

                }

            }
        }

    }

    //========================================================================================================

    public static class ActivationCodeFragment extends ExecutionFragment
    {

        private NhDialog mDialog;
        private Animation animNumberScale;
        private EditText uiEdtCode;


        //++++++++++++++++++++++++++
        private boolean firstShow = true;
        //++++++++++++++++++++++++++

        public static ActivationCodeFragment newInstance()
        {
            ActivationCodeFragment fragment = new ActivationCodeFragment();
            //Bundle args = new Bundle();
            //args.putInt(KEY_URL, link);
            //downloaderFragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onResume()
        {
            super.onResume();

            //getActivity().registerReceiver(smsReceiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));

            setBtnPanelState(true, true, true);

            //++++++++++++++++++++++++++
            if (firstShow)
            {
                firstShow = false;
                uiEdtCode.setText(((ActivationActivity) getActivity()).ACTIVATE_CODE);
                ((View.OnClickListener) getActivity()).onClick(getActivity().findViewById(R.id.activity_login_btn_next));
            }
            else
            {
                firstShow = true;
                ((View.OnClickListener) getActivity()).onClick(getActivity().findViewById(R.id.activity_login_btn_back));
            }
            //++++++++++++++++++++++++++
        }

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            //setRetainInstance(true);
            //getArguments().getInt(KEY_URL, 0);
        }

        @Override
        public void onPause()
        {
            super.onPause();
            //getActivity().unregisterReceiver(smsReceiver);
        }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
            View rootView = inflater.inflate(R.layout.fragment_login_code, container, false);

            uiEdtCode = (EditText) rootView.findViewById(R.id.fragment_login_activation_edt_code);

            animNumberScale = AnimationUtils.loadAnimation(getActivity(), R.anim.number_scale);

            uiEdtCode.addTextChangedListener(new TextWatcher()
            {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
                {

                }
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
                {
                    uiEdtCode.startAnimation(animNumberScale);
                }
                @Override
                public void afterTextChanged(Editable editable)
                {

                }
            });

            return rootView;
        }

        @Override
        public void bodyCommand()
        {
            String activeCode = uiEdtCode.getText().toString().trim().toUpperCase(Locale.getDefault());
            if (Validation.simple(activeCode))
            {
                try
                {
                    activeCode = Helper.md5(activeCode);
                }
                catch (MyMessangerException e)
                {
                    mDialog = new NhDialog(mActivity, NhDialog.DialogIcon.ERROR);
                    mDialog.setMainTitle(R.string.messanger_login_message_title_error).setSubTitle(e.getMessage());
                    mDialog.show();
                    onBackResult(ExecutionFragment.RESULT_ERROR);
                    return;
                }
                ((OnFragmentBackResult) mActivity).getLoginData().activeCode = activeCode;

                onBackResult(ExecutionFragment.RESULT_OK);
            }
            else
            {
                onBackResult(ExecutionFragment.RESULT_ERROR);

                mDialog = new NhDialog(mActivity, NhDialog.DialogIcon.ERROR);
                mDialog.setMainTitle(R.string.messanger_login_message_title_validation).setSubTitle(R.string.messanger_login_message_notvalid_activecode);
                mDialog.show();
            }

        }
    }

    //========================================================================================================

    public static class ActivationVerifyFragment extends ExecutionFragment implements View.OnClickListener
    {

        private EditText uiEdtUsername;
        private EditText uiEdtPassword;

        public static ActivationVerifyFragment newInstance()
        {
            ActivationVerifyFragment fragment = new ActivationVerifyFragment();
            //Bundle args = new Bundle();
            //args.putInt(KEY_URL, link);
            //downloaderFragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onResume()
        {
            super.onResume();
            setBtnPanelState(true, true, true);
        }

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);

            //getArguments().getInt(KEY_URL, 0);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
            View rootView = inflater.inflate(R.layout.fragment_login_verify, container, false);

            uiEdtUsername = (EditText) rootView.findViewById(R.id.fragment_login_verify_edt_username);

            uiEdtPassword = (EditText) rootView.findViewById(R.id.fragment_login_verify_edt_password);

            //TextView txvRegister = (TextView) rootView.findViewById(R.id.fragment_login_verify_btn_register);

            Button btnRegisterIcon = (Button) rootView.findViewById(R.id.fragment_login_verify_btn_registericon);
            btnRegisterIcon.setOnClickListener(this);

            return rootView;
        }

        @Override
        public void bodyCommand()
        {
            String username = uiEdtUsername.getText().toString().trim();
            String password = uiEdtPassword.getText().toString().trim();

            boolean isValidUsername = Validation.simple(username);
            boolean isValidPassword = Validation.simple(password);

            if (!isValidUsername || !isValidPassword)
            {
                NhDialog dialog = new NhDialog(mActivity, NhDialog.DialogIcon.ALERT);
                dialog.setMainTitle(R.string.messanger_login_message_title_validation);
                if (!isValidUsername)
                {
                    dialog.setSubTitle(R.string.messanger_login_message_notvalid_username);
                }
                else
                {
                    dialog.setSubTitle(R.string.messanger_login_message_notvalid_password);
                }
                dialog.show();

                onBackResult(ExecutionFragment.RESULT_ERROR);
                return;
            }
            else
            {
                new ActivationVerifyAsyncTask(this, username, password).execute();
            }

        }

        @Override
        public void onClick(View v)
        {
            int i = v.getId();
            if (i == R.id.fragment_login_verify_btn_registericon)
            {
                onBackResult(ExecutionFragment.RESULT_EXTRA);
            }
        }

        static class ActivationVerifyAsyncTask extends AsyncTask<Void, Void, ActivationJto.Verify.PostBack>
        {

            private String mUsername;
            private String mPassword;
            private ExecutionFragment mFragment;
            private NhDialog mDialog;
            private ActivationRestApi mRestApi;

            public ActivationVerifyAsyncTask(ExecutionFragment fragment, String username, String password)
            {
                this.mUsername = username;
                this.mPassword = password;
                this.mFragment = fragment;
                this.mRestApi = new ActivationRestApi(this.mFragment.mActivity);
            }

            @Override
            protected void onPreExecute()
            {

                mDialog = new NhDialog(mFragment.mActivity, NhDialog.DialogIcon.LOADING);
                mDialog.setCancelable(true);
                mDialog.setMainTitle(R.string.messanger_login_message_title_server_authenticating).setSubTitle(R.string.messanger_message_pleasewait);
                mDialog.setOnCancelListener(new DialogInterface.OnCancelListener()
                {

                    @Override
                    public void onCancel(DialogInterface dialog)
                    {
                        mRestApi.cancel();
                        cancel(true);
                    }
                });
                mDialog.show();
            }

            @Override
            protected void onCancelled()
            {
                mFragment.onBackResult(ExecutionFragment.RESULT_CANCEL);
            }

            @Override
            protected ActivationJto.Verify.PostBack doInBackground(Void... s)
            {

                String secretMessage = Helper.generateRandomString(32);
                String key = Helper.generateRandomString(32);//AES

                ((OnFragmentBackResult) mFragment.mActivity).getLoginData().secretMessage = secretMessage;
                ((OnFragmentBackResult) mFragment.mActivity).getLoginData().key = key;

                String activeToken = ((OnFragmentBackResult) mFragment.mActivity).getLoginData().activeToken;
                String imei = Helper.getIMEI(mFragment.mActivity);

                ActivationJto.Verify.Post dto = new ActivationJto.Verify.Post(this.mUsername, this.mPassword, Encryption.encryptAes256Base64(secretMessage, key), imei, key);
                ActivationJto.Verify.PostBack postBackJTO;

                try
                {
                    postBackJTO = mRestApi.activationVerify(activeToken, dto, ((OnFragmentBackResult) mFragment.mActivity).getLoginData().activeCode);
                }
                catch (MyMessangerException e)
                {
                    postBackJTO = new ActivationJto.Verify.PostBack();
                    postBackJTO.stateCode = PostBackJto.RESULT_BAD_REQUEST;
                    postBackJTO.detailMessage = e.getMessage();
                    postBackJTO.subjectMessage = mFragment.getString(R.string.messanger_login_message_title_error_network);
                }

                return postBackJTO;
            }

            @Override
            protected void onPostExecute(ActivationJto.Verify.PostBack dto)
            {

                mDialog.dismiss();
                mDialog = null;

                if (dto.stateCode != PostBackJto.RESULT_OK)
                {
                    mFragment.onBackResult(ExecutionFragment.RESULT_ERROR);

                    mDialog = new NhDialog(mFragment.mActivity, NhDialog.DialogIcon.ERROR);
                    mDialog.setMainTitle(dto.subjectMessage).setSubTitle(dto.detailMessage);
                    mDialog.show();
                }
                else
                {
                    try
                    {
                        String key = ((OnFragmentBackResult) mFragment.mActivity).getLoginData().key;
                        String baseSecretMessage = ((OnFragmentBackResult) mFragment.mActivity).getLoginData().secretMessage;
                        if (Encryption.decryptAes256Base64(dto.secretMessage, key).equals(baseSecretMessage + baseSecretMessage))
                        {
                            LoginDataHolder info = ((OnFragmentBackResult) mFragment.mActivity).getLoginData();
                            info.name = dto.name;
                            info.family = dto.family;
                            info.token = dto.token;

                            boolean success = ((OnFragmentBackResult) mFragment.mActivity).onSaveSetting(true);
                            if (success)
                            {
                                ((OnFragmentBackResult) mFragment.mActivity).onComplete();
                                mFragment.onBackResult(ExecutionFragment.RESULT_OK);
                            }
                            else
                            {
                                mDialog = new NhDialog(mFragment.mActivity, NhDialog.DialogIcon.ERROR);
                                mDialog.setMainTitle(R.string.messanger_login_message_title_error).setSubTitle(R.string.messanger_message_error_savedb);
                                mDialog.show();
                                mFragment.onBackResult(ExecutionFragment.RESULT_ERROR);
                            }
                            return;
                        }
                    }
                    catch (Exception e)
                    {

                        mFragment.onBackResult(ExecutionFragment.RESULT_ERROR);
                    }

                    mDialog = new NhDialog(mFragment.mActivity, NhDialog.DialogIcon.ERROR);
                    mDialog.setMainTitle(R.string.messanger_login_message_title_error).setSubTitle(R.string.messanger_login_message_error_activation_faild);
                    mDialog.show();

                }

            }

        }

    }

    //========================================================================================================

    public static class ActivationFinishFragment extends ExecutionFragment
    {

        public static ActivationFinishFragment newInstance()
        {
            ActivationFinishFragment fragment = new ActivationFinishFragment();
            //Bundle args = new Bundle();
            //args.putInt(KEY_URL, link);
            //downloaderFragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onResume()
        {
            super.onResume();
            setBtnPanelState(false, true, false);
        }

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);

            //getArguments().getInt(KEY_URL, 0);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
            View rootView = inflater.inflate(R.layout.fragment_login_finish, container, false);

            LoginDataHolder info = ((OnFragmentBackResult) mActivity).getLoginData();

            //TextView txvIcon = (TextView) rootView.findViewById(R.id.fragment_login_finish_txv_icon);

            TextView txvHead = (TextView) rootView.findViewById(R.id.fragment_login_finish_txv_head);
            //txvHead.setText((info.gender ? "آقای" : "خانم") + " " + info.name + " " + info.family);
            txvHead.setText("کاربر گرامی " + info.name + " " + info.family);

            TextView txvText = (TextView) rootView.findViewById(R.id.fragment_login_finish_txv_text);
            txvText.setText(R.string.messanger_login_message_error_activation_success);

            return rootView;
        }

        @Override
        public void bodyCommand()
        {
            //onBackResult(ExecutionFragment.RESULT_OK);
        }

    }

    //=======================================Sms Receiver===========================================

    /*public static BroadcastReceiver smsReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final Bundle bundle = intent.getExtras();
            IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");


            try
            {

                if (bundle != null)
                {

                    final Object[] pdusObj = (Object[]) bundle.get("pdus");

                    for (int i = 0; i < pdusObj.length; i++)
                    {

                        SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                        String phoneNumber = currentMessage.getDisplayOriginatingAddress();

                        String senderNum = phoneNumber;
                        String message = currentMessage.getDisplayMessageBody();

                        Log.i("SmsReceiver", "senderNum: " + senderNum + "; message: " + message);


                        // Show alert
						///int duration = Toast.LENGTH_LONG;
						///Toast toast = Toast.makeText(context, "senderNum: "+ senderNum + ", message: " + message, duration);
						///toast.show();

                        if (senderNum.equals("+9830005966000514"))
                        {
                            uiEdtCode.setText(message);
                        }

                    }
                }

            }
            catch (Exception e)
            {
                Log.e("SmsReceiver", "Exception smsReceiver" + e);

            }
        }
    };*/

}
