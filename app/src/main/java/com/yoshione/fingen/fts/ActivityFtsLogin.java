package com.yoshione.fingen.fts;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;

import com.google.android.material.textfield.TextInputLayout;
import com.yoshione.fingen.FGApplication;
import com.yoshione.fingen.FgConst;
import com.yoshione.fingen.R;
import com.yoshione.fingen.fts.models.login.LoginResponse;
import com.yoshione.fingen.widgets.ToolbarActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.disposables.Disposable;
import ru.tinkoff.decoro.MaskImpl;
import ru.tinkoff.decoro.slots.PredefinedSlots;
import ru.tinkoff.decoro.watchers.FormatWatcher;
import ru.tinkoff.decoro.watchers.MaskFormatWatcher;

public class ActivityFtsLogin extends ToolbarActivity {

    //<editor-fold desc="Static declarations" defaultstate="collapsed">
    private static final int MODE_BLANK = 0;
    private static final int MODE_AUTH = 1;
    //</editor-fold>

    //<editor-fold desc="Bind views" defaultstate="collapsed">
    @BindView(R.id.textViewSubtitle)
    TextView mTextViewSubtitle;
    @BindView(R.id.editTextPhoneNo)
    EditText mEditTextPhoneNo;
    @BindView(R.id.textInputLayoutPhoneNo)
    TextInputLayout mTextInputLayoutPhoneNo;
    @BindView(R.id.editTextCode)
    EditText mEditTextCode;
    @BindView(R.id.textInputLayoutCode)
    TextInputLayout mTextInputLayoutCode;
    @BindView(R.id.editTextName)
    EditText mEditTextName;
    @BindView(R.id.textInputLayoutName)
    TextInputLayout mTextInputLayoutName;
    @BindView(R.id.editTextEmail)
    EditText mEditTextEmail;
    @BindView(R.id.textInputLayoutEmail)
    TextInputLayout mTextInputLayoutEmail;
    @BindView(R.id.checkBoxFTSEnabled)
    CheckBox mCheckBoxEnabled;
    @BindView(R.id.textViewInfo)
    TextView mTextViewInfo;
    @BindView(R.id.layoutChecking)
    ConstraintLayout mLayoutChecking;
    @BindView(R.id.imageViewCheckingData)
    ImageView mImageViewCheckingData;
    @BindView(R.id.textViewCheckingData)
    TextView mTextViewCheckingData;
    @BindView(R.id.buttonBack)
    Button mButtonBack;
    @BindView(R.id.buttonSave)
    Button mButtonSave;
    //</editor-fold>

    @Inject
    FtsHelper mFtsHelper;

    FormatWatcher formatWatcher;
    private int mode_current;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FGApplication.getAppComponent().inject(this);

        formatWatcher = new MaskFormatWatcher(
                MaskImpl.createTerminated(PredefinedSlots.RUS_PHONE_NUMBER) // маска для серии и номера
        );
        formatWatcher.installOn(mEditTextPhoneNo);

        setMode(FtsHelper.isFtsCredentialsAvailable(mPreferences) ? MODE_AUTH : MODE_BLANK);
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_fts_login;
    }

    @Override
    protected String getLayoutTitle() {
        return getString(R.string.ttl_connection_params);
    }

    @OnClick({R.id.checkBoxFTSEnabled, R.id.buttonBack, R.id.buttonSave})
    public void onViewClicked(View view) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        switch (view.getId()) {
            case R.id.checkBoxFTSEnabled:
                preferences.edit().putBoolean(FgConst.PREF_FTS_ENABLED, mCheckBoxEnabled.isChecked()).apply();
                setMode(mode_current);
                break;
            case R.id.buttonBack:
                if (!preferences.getBoolean(FgConst.PREF_FTS_ENABLED, true)) {
                    setResult(RESULT_OK);
                    finish();
                    break;
                }
                switch (mode_current) {
                    case MODE_AUTH:
                        setResult(RESULT_OK);
                    case MODE_BLANK:
                        finish();
                        break;
                    default:
                        setMode(MODE_BLANK);
                }
                break;
            case R.id.buttonSave:
                if (!preferences.getBoolean(FgConst.PREF_FTS_ENABLED, true)) {
                    setResult(RESULT_OK);
                    finish();
                    break;
                }

                final RotateAnimation spinAnim = new RotateAnimation(360, 0f,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);

                spinAnim.setInterpolator(new LinearInterpolator());
                spinAnim.setDuration(2000);
                spinAnim.setRepeatCount(Animation.INFINITE);
                mLayoutChecking.setVisibility(View.VISIBLE);
                mImageViewCheckingData.setVisibility(View.VISIBLE);
                mImageViewCheckingData.startAnimation(spinAnim);
                mTextViewCheckingData.setText(getString(R.string.ttl_checking_data));

                FtsCallback authCallback = new FtsCallback(spinAnim);

                switch (mode_current) {
                    case MODE_BLANK:
                        authCallback.callback = auth -> {
                            Toast.makeText(this, R.string.ttl_auth_success, Toast.LENGTH_LONG).show();
                            preferences.edit()
                                    .putString(FgConst.PREF_FTS_LOGIN, formatWatcher.getMask().toUnformattedString())
                                    .putString(FgConst.PREF_FTS_PASS, mEditTextCode.getText().toString())
                                    .putString(FgConst.PREF_FTS_NAME, auth.getName())
                                    .putString(FgConst.PREF_FTS_EMAIL, auth.getEmail())
                                    .putString(FgConst.PREF_FTS_REFRESH_TOKEN, auth.getRefresh_token())
                                    .putString(FgConst.PREF_FTS_SESSION_ID, auth.getSessionId())
                                    .apply();
                            setResult(RESULT_OK);
                            finish();
                        };
                        Disposable auth = mFtsHelper.checkAuth(formatWatcher.getMask().toUnformattedString(), mEditTextCode.getText().toString(), authCallback);
                        if (auth != null)
                            unsubscribeOnDestroy(auth);
                        break;
                    case MODE_AUTH:
                        FtsHelper.clearFtsCredentials(preferences);
                        setMode(MODE_BLANK);
                        break;
                }
                break;
        }
    }

    public void setMode(int mode) {
        boolean isModeBlank = mode == MODE_BLANK;
        boolean isModeAuth = mode == MODE_AUTH;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        switch (mode) {
            case MODE_AUTH:
                mEditTextName.setText(preferences.getString(FgConst.PREF_FTS_NAME, ""));
                mEditTextEmail.setText(preferences.getString(FgConst.PREF_FTS_EMAIL, ""));
                mButtonSave.setText(R.string.ttl_exit_account);
            case MODE_BLANK:
                mEditTextPhoneNo.setText(preferences.getString(FgConst.PREF_FTS_LOGIN, ""));
                mEditTextCode.setText(preferences.getString(FgConst.PREF_FTS_PASS, ""));
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(getLayoutTitle());
                if (mode == MODE_BLANK)
                    mButtonSave.setText(R.string.act_check_and_save);
                break;
        }
        boolean isEnabled = preferences.getBoolean(FgConst.PREF_FTS_ENABLED, true);
        mCheckBoxEnabled.setChecked(isEnabled);
        mCheckBoxEnabled.setVisibility(isModeBlank || isModeAuth ? View.VISIBLE : View.GONE);
        mTextInputLayoutPhoneNo.setVisibility(isEnabled ? View.VISIBLE : View.GONE);
        mTextInputLayoutCode.setVisibility(isEnabled && (isModeBlank || isModeAuth) ? View.VISIBLE : View.GONE);
        mTextInputLayoutName.setVisibility(isEnabled && isModeAuth ? View.VISIBLE : View.GONE);
        mTextInputLayoutEmail.setVisibility(isEnabled && isModeAuth ? View.VISIBLE : View.GONE);
        mTextViewInfo.setVisibility(isEnabled && isModeBlank ? View.VISIBLE : View.GONE);
        mLayoutChecking.setVisibility(View.GONE);
        mButtonSave.setBackgroundResource(isModeAuth ? R.drawable.selector_red_button : R.drawable.selector_blue_button);
        if (!isEnabled) {
            mButtonSave.setText(R.string.act_save);
            mButtonSave.setBackgroundResource(R.drawable.selector_blue_button);
        }

        mEditTextPhoneNo.setEnabled(!isModeAuth);
        mEditTextCode.setEnabled(!isModeAuth);
        mEditTextName.setEnabled(!isModeAuth);
        mEditTextEmail.setEnabled(!isModeAuth);

        mode_current = mode;
    }

    interface OnAuthCallback {
        void onCallback(LoginResponse auth);
    }

    class FtsCallback implements IFtsCallback {

        final RotateAnimation spinAnim;
        OnAuthCallback callback;

        FtsCallback(RotateAnimation spinAnim) {
            this.spinAnim = spinAnim;
            this.callback = null;
        }

        @Override
        public void onAccepted(Object response) {
            LoginResponse auth = (LoginResponse) response;
            if (callback != null)
                callback.onCallback(auth);
        }

        @Override
        public void onFailure(String errMsg, int responseCode) {
            mImageViewCheckingData.clearAnimation();
            mImageViewCheckingData.setVisibility(View.GONE);
            mTextViewCheckingData.setText(errMsg);
        }
    }

}
