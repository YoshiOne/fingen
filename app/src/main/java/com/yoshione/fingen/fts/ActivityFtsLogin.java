package com.yoshione.fingen.fts;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
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
import com.yoshione.fingen.fts.models.AuthResponse;
import com.yoshione.fingen.widgets.ToolbarActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import ru.tinkoff.decoro.MaskImpl;
import ru.tinkoff.decoro.slots.PredefinedSlots;
import ru.tinkoff.decoro.watchers.FormatWatcher;
import ru.tinkoff.decoro.watchers.MaskFormatWatcher;

public class ActivityFtsLogin extends ToolbarActivity {

    //<editor-fold desc="Static declarations" defaultstate="collapsed">
    private static final int MODE_BLANK = 0;
    private static final int MODE_CREATE = 1;
    private static final int MODE_RECOVERY = 2;
    private static final int MODE_AUTH = 3;
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
    @BindView(R.id.buttonCreateAccount)
    Button mButtonCreateAccount;
    @BindView(R.id.buttonRecoveryCode)
    Button mButtonRecoveryCode;
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

        setMode(mFtsHelper.isFtsCredentialsAvailable(this) ? MODE_AUTH : MODE_BLANK);
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_fts_login;
    }

    @Override
    protected String getLayoutTitle() {
        return getString(R.string.ttl_connection_params);
    }

    @OnClick({R.id.buttonCreateAccount, R.id.buttonRecoveryCode, R.id.buttonBack, R.id.buttonSave})
    public void onViewClicked(View view) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        switch (view.getId()) {
            case R.id.buttonBack:
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

                AuthListener authListener = new AuthListener(spinAnim);

                switch (mode_current) {
                    case MODE_BLANK:
                        authListener.callback = auth -> {
                            Toast.makeText(this, R.string.ttl_auth_success, Toast.LENGTH_LONG).show();
                            preferences.edit()
                                    .putString(FgConst.PREF_FTS_LOGIN, formatWatcher.getMask().toUnformattedString())
                                    .putString(FgConst.PREF_FTS_PASS, mEditTextCode.getText().toString())
                                    .putString(FgConst.PREF_FTS_NAME, auth.getName())
                                    .putString(FgConst.PREF_FTS_EMAIL, auth.getEmail())
                                    .apply();
                            setResult(RESULT_OK);
                            finish();
                        };
                        unsubscribeOnDestroy(mFtsHelper.checkAuth(formatWatcher.getMask().toUnformattedString(), mEditTextCode.getText().toString(), authListener));
                        break;
                    case MODE_CREATE:
                    case MODE_RECOVERY:
                        authListener.callback = auth -> {
                            preferences.edit()
                                    .putString(FgConst.PREF_FTS_LOGIN, formatWatcher.getMask().toUnformattedString())
                                    .apply();
                            setMode(MODE_BLANK);
                        };
                        if (mode_current == MODE_CREATE)
                            unsubscribeOnDestroy(mFtsHelper.signUpAuth(formatWatcher.getMask().toUnformattedString(), mEditTextName.getText().toString(), mEditTextEmail.getText().toString(), authListener));
                        else
                            unsubscribeOnDestroy(mFtsHelper.recoveryCode(formatWatcher.getMask().toUnformattedString(), authListener));
                        break;
                    case MODE_AUTH:
                        preferences.edit()
                                .remove(FgConst.PREF_FTS_LOGIN)
                                .remove(FgConst.PREF_FTS_PASS)
                                .remove(FgConst.PREF_FTS_NAME)
                                .remove(FgConst.PREF_FTS_EMAIL)
                                .apply();
                        setMode(MODE_BLANK);
                        break;
                }
                break;
            case R.id.buttonCreateAccount:
                setMode(MODE_CREATE);
                break;
            case R.id.buttonRecoveryCode:
                setMode(MODE_RECOVERY);
                break;
        }
    }

    public void setMode(int mode) {
        boolean isModeBlank = mode == MODE_BLANK;
        boolean isModeAuth = mode == MODE_AUTH;
        boolean isModeCreate = mode == MODE_CREATE;
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
            case MODE_CREATE:
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(R.string.ttl_create_account);
                mButtonSave.setText(R.string.ttl_create_account);
                break;
            case MODE_RECOVERY:
                mEditTextPhoneNo.setText(preferences.getString(FgConst.PREF_FTS_LOGIN, ""));
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(R.string.ttl_recovery_code);
                mButtonSave.setText(R.string.ttl_recovery_code);
                break;
        }
        mTextInputLayoutPhoneNo.setVisibility(View.VISIBLE);
        mTextInputLayoutCode.setVisibility(isModeBlank || isModeAuth ? View.VISIBLE : View.GONE);
        mTextInputLayoutName.setVisibility(isModeCreate || isModeAuth ? View.VISIBLE : View.GONE);
        mTextInputLayoutEmail.setVisibility(isModeCreate || isModeAuth ? View.VISIBLE : View.GONE);
        mLayoutChecking.setVisibility(View.GONE);
        mButtonCreateAccount.setVisibility(isModeBlank ? View.VISIBLE : View.GONE);
        mButtonRecoveryCode.setVisibility(isModeBlank ? View.VISIBLE : View.GONE);
        mButtonSave.setBackgroundResource(isModeAuth ? R.drawable.selector_red_button : R.drawable.selector_blue_button);

        mEditTextPhoneNo.setEnabled(!isModeAuth);
        mEditTextCode.setEnabled(!isModeAuth);
        mEditTextName.setEnabled(!isModeAuth);
        mEditTextEmail.setEnabled(!isModeAuth);

        mode_current = mode;
    }

    class AuthListener implements IAuthListener {

        final RotateAnimation spinAnim;
        OnCallback callback;

        AuthListener(RotateAnimation spinAnim) {
            this.spinAnim = spinAnim;
            this.callback = null;
        }

        @Override
        public void onAccepted(Object response) {
            AuthResponse auth = (AuthResponse) response;
            if (callback != null)
                callback.onCallback(auth);
        }

        @Override
        public void onFailure(String errMsg) {
            mImageViewCheckingData.clearAnimation();
            mImageViewCheckingData.setVisibility(View.GONE);
            mTextViewCheckingData.setText(errMsg);
        }
    }

}
