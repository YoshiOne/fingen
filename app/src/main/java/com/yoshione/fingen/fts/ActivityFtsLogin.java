package com.yoshione.fingen.fts;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.constraint.Guideline;
import android.support.design.widget.TextInputLayout;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.yoshione.fingen.FGApplication;
import com.yoshione.fingen.FgConst;
import com.yoshione.fingen.R;
import com.yoshione.fingen.widgets.ToolbarActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import ru.tinkoff.decoro.MaskImpl;
import ru.tinkoff.decoro.slots.PredefinedSlots;
import ru.tinkoff.decoro.watchers.FormatWatcher;
import ru.tinkoff.decoro.watchers.MaskFormatWatcher;

public class ActivityFtsLogin extends ToolbarActivity {

    @Inject
    FtsHelper mFtsHelper;

    @BindView(R.id.textViewSubtitle)
    TextView mTextViewSubtitle;
    @BindView(R.id.textViewFtsLoginHtml)
    TextView mTextViewFtsLoginHtml;
    @BindView(R.id.editTextPhoneNo)
    EditText mEditTextPhoneNo;
    @BindView(R.id.textInputLayoutPhoneNo)
    TextInputLayout mTextInputLayoutPhoneNo;
    @BindView(R.id.editTextCode)
    EditText mEditTextCode;
    @BindView(R.id.textInputLayoutCode)
    TextInputLayout mTextInputLayoutCode;
    @BindView(R.id.textViewFtsLoginError)
    TextView mTextViewFtsLoginError;
    @BindView(R.id.buttonNotNow)
    Button mButtonNotNow;
    @BindView(R.id.buttonSave)
    Button mButtonSave;
    @BindView(R.id.guideline6)
    Guideline mGuideline6;
    @BindView(R.id.textView4)
    TextView mTextView4;
    @BindView(R.id.checkBox)
    CheckBox mCheckBox;
    FormatWatcher formatWatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FGApplication.getAppComponent().inject(this);

        @SuppressWarnings("deprecation")
        Spanned result;
        String html = getString(R.string.msg_fts_login_html);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            result = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html);
        }
        mTextViewFtsLoginHtml.setText(result);

        formatWatcher = new MaskFormatWatcher(
                MaskImpl.createTerminated(PredefinedSlots.RUS_PHONE_NUMBER) // маска для серии и номера
        );
        formatWatcher.installOn(mEditTextPhoneNo);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEditTextPhoneNo.setText(preferences.getString(FgConst.PREF_FTS_LOGIN, ""));
        mEditTextCode.setText(preferences.getString(FgConst.PREF_FTS_PASS, ""));
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_fts_login;
    }

    @Override
    protected String getLayoutTitle() {
        return getString(R.string.ttl_connection_params);
    }

    @OnClick({R.id.buttonNotNow, R.id.buttonSave, R.id.checkBox})
    public void onViewClicked(View view) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        switch (view.getId()) {
            case R.id.buttonNotNow:
                finish();
                break;
            case R.id.buttonSave:
                String phone = formatWatcher.getMask().toUnformattedString();
                String code = mEditTextCode.getText().toString();

                String prevPhone = preferences.getString(FgConst.PREF_FTS_LOGIN, "");
                String prevCode = preferences.getString(FgConst.PREF_FTS_PASS, "");

                if (!prevPhone.equals(phone) || !prevCode.equals(code)) {

                    mFtsHelper.registration(phone, code)
                            .subscribe(
                                    r ->
                                    {
                                        preferences.edit()
                                                .putString(FgConst.PREF_FTS_LOGIN, phone)
                                                .putString(FgConst.PREF_FTS_PASS, code)
                                                .apply();

                                        setResult(RESULT_OK);
                                        finish();
                                    },
                                    error -> mTextViewFtsLoginError.setText(error.getMessage()));
                } else {
                    setResult(RESULT_OK);
                    finish();
                }

                break;
            case R.id.checkBox:
                preferences.edit().putBoolean(FgConst.PREF_FTS_DO_NOT_SHOW_AGAIN, mCheckBox.isChecked()).apply();
                break;
        }
    }
}
