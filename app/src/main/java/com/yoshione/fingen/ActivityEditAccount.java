package com.yoshione.fingen;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.yoshione.fingen.dao.AccountsDAO;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.managers.AccountManager;
import com.yoshione.fingen.model.Account;
import com.yoshione.fingen.model.Cabbage;
import com.yoshione.fingen.model.Transaction;
import com.yoshione.fingen.utils.RequestCodes;
import com.yoshione.fingen.widgets.AmountEditor;
import com.yoshione.fingen.widgets.ToolbarActivity;

import java.math.BigDecimal;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

public class ActivityEditAccount extends ToolbarActivity {

//    private static final String TAG = "ActivityEditAccount";

    //<editor-fold desc="View bindings">
    @BindView(R.id.editTextName)
    EditText editTextName;
    @BindView(R.id.editTextType)
    EditText editTextType;
    @BindView(R.id.editTextCabbage)
    EditText editTextCabbage;
    @BindView(R.id.editTextEmitent)
    EditText editTextEmitent;
    @BindView(R.id.editTextLast4Digits)
    EditText editTextLast4Digits;
    @BindView(R.id.editTextComment)
    EditText editTextComment;
    @BindView(R.id.amountEditorStartBalance)
    AmountEditor amountEditorStartBalance;
    @BindView(R.id.checkboxAccountClosed)
    AppCompatCheckBox checkboxAccountClosed;
    @BindView(R.id.amountEditorCreditLimit)
    AmountEditor amountEditorCreditLimit;
    @BindView(R.id.textInputLayoutEmitent)
    TextInputLayout mTextInputLayoutEmitent;
    @BindView(R.id.textInputLayoutLast4Digits)
    TextInputLayout mTextInputLayoutLast4Digits;
    @BindView(R.id.textInputLayoutName)
    TextInputLayout mTextInputLayoutName;
    @BindView(R.id.textInputLayoutCabbage)
    TextInputLayout mTextInputLayoutCabbage;
    //</editor-fold>

    private static final String SHOWCASE_ID = "Edit account showcase";

    Account account;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_edit_account;
    }

    @Override
    protected String getLayoutTitle() {
        if (account == null) {
            return "";
        }
        if (account.getID() < 0) {
            return getResources().getString(R.string.ent_new_account);
        } else {
            return getResources().getString(R.string.ent_edit_account);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        account = getIntent().getParcelableExtra("account");
    }

    @Override
    public void onResume() {
        super.onResume();
        initUI();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("account", account);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        account = savedInstanceState.getParcelable("account");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == RequestCodes.REQUEST_CODE_SELECT_MODEL) && (resultCode == RESULT_OK) && data != null) {
            IAbstractModel model = data.getParcelableExtra("model");
            if (model.getModelType() == IAbstractModel.MODEL_TYPE_CABBAGE) {
                Cabbage cabbage = (Cabbage) model;
                account.setCabbageId(cabbage.getID());
                editTextCabbage.setText(cabbage.toString());
                amountEditorStartBalance.setScale(cabbage.getDecimalCount());
                amountEditorCreditLimit.setScale(cabbage.getDecimalCount());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.action_go_home).setVisible(false);
        menu.findItem(R.id.action_show_help).setVisible(true);
        return true;
    }

    private void initUI() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(ContextCompat.getDrawable(this, R.drawable.ic_close_white));
        }


        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getLayoutTitle());
        }

        editTextName.setText(account.getName());
        editTextName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                account.setName(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        editTextEmitent.setText(account.getEmitent());
        editTextEmitent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                account.setEmitent(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        editTextLast4Digits.setText(String.format(Locale.ENGLISH, "%04d", account.getLast4Digits()));
        editTextLast4Digits.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int cn;
                try {
                    cn = Integer.parseInt(s.toString());
                } catch (NumberFormatException e) {
                    cn = 0;
                }
                account.setLast4Digits(cn);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        editTextComment.setText(account.getComment());
        editTextComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                account.setComment(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        amountEditorStartBalance.setActivity(this);
        amountEditorStartBalance.setAmount(account.getStartBalance());
        amountEditorStartBalance.setType((account.getStartBalance().compareTo(BigDecimal.ZERO) >= 0) ? Transaction.TRANSACTION_TYPE_INCOME : Transaction.TRANSACTION_TYPE_EXPENSE);
        amountEditorStartBalance.setHint(getResources().getString(R.string.ent_start_balance));
        amountEditorStartBalance.mOnAmountChangeListener = new AmountEditor.OnAmountChangeListener() {
            @Override
            public void OnAmountChange(BigDecimal newAmount, int newType) {
                account.setStartBalance(newAmount.multiply(new BigDecimal((newType > 0) ? 1 : -1)));
                Log.d(TAG, "New start balance = " + account.getStartBalance().toString());
            }
        };

        amountEditorCreditLimit.setActivity(this);
        amountEditorCreditLimit.setAmount(account.getCreditLimit());
        amountEditorCreditLimit.setType(Transaction.TRANSACTION_TYPE_EXPENSE);
        amountEditorCreditLimit.setLockType(true);
        amountEditorCreditLimit.setHint(getResources().getString(R.string.ent_credit_limit));
        amountEditorCreditLimit.mOnAmountChangeListener = new AmountEditor.OnAmountChangeListener() {
            @Override
            public void OnAmountChange(BigDecimal newAmount, int newType) {
                account.setCreditLimit(newAmount.multiply(new BigDecimal((newType > 0) ? 1 : -1)));
            }
        };

        checkboxAccountClosed.setChecked(account.getIsClosed());
        checkboxAccountClosed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                account.setIsClosed(isChecked);
            }
        });

        final String accTypes[] = getResources().getStringArray(R.array.account_types);
        editTextType.setText(accTypes[account.getAccountType().ordinal()]);
        editTextType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builderSingle = new AlertDialog.Builder(ActivityEditAccount.this);
                final ArrayAdapter<String> arrayAdapterTypes = new ArrayAdapter<>(ActivityEditAccount.this, android.R.layout.select_dialog_singlechoice);
                arrayAdapterTypes.addAll(accTypes);

                builderSingle.setSingleChoiceItems(arrayAdapterTypes, account.getAccountType().ordinal(),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, final int which) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        dialog.cancel();
                                        account.setAccountType(Account.AccountType.values()[which]);
                                        editTextType.setText(accTypes[account.getAccountType().ordinal()]);
                                        setFieldsVisibility();
                                    }
                                }, 200);

                            }
                        });
                builderSingle.setTitle(getString(R.string.ent_type));

                builderSingle.setNegativeButton(
                        getResources().getString(android.R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                builderSingle.show();
            }
        });

        Cabbage cabbage = AccountManager.getCabbage(account, this);
        editTextCabbage.setText(cabbage.toString());
        amountEditorStartBalance.setScale(cabbage.getDecimalCount());
        amountEditorCreditLimit.setScale(cabbage.getDecimalCount());

        if (account.getCabbageId() < 0) {
            editTextCabbage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ActivityEditAccount.this.getApplicationContext(), ActivityList.class);
                    intent.putExtra("showHomeButton", false);
                    intent.putExtra("model", new Cabbage());
                    intent.putExtra("requestCode", RequestCodes.REQUEST_CODE_SELECT_MODEL);
                    ActivityEditAccount.this.startActivityForResult(intent, RequestCodes.REQUEST_CODE_SELECT_MODEL);
                }
            });
        }

        setFieldsVisibility();
    }

    private void setFieldsVisibility() {
        boolean bank;
        boolean card = false;
        switch (account.getAccountType()) {
            case atAccount:
                bank = true;
                break;
            case atCreditCard:
            case atDebtCard:
                bank = true;
                card = true;
                break;
            default:
                bank = false;
                card = false;
                break;
        }
        if (bank) {
            mTextInputLayoutEmitent.setVisibility(View.VISIBLE);
        } else {
            mTextInputLayoutEmitent.setVisibility(View.GONE);
        }
        if (card) {
            mTextInputLayoutLast4Digits.setVisibility(View.VISIBLE);
        } else {
            mTextInputLayoutLast4Digits.setVisibility(View.GONE);
        }
//        layMain.invalidate();
    }

    @OnClick(R.id.buttonSaveAccount)
    public void onSaveClick() {
        if (account.isValid()) {
            AccountsDAO accountsDAO = AccountsDAO.getInstance(getApplicationContext());
            try {
                accountsDAO.createModel(account);
            } catch (Exception e) {
                Toast.makeText(this, R.string.msg_error_on_write_to_db, Toast.LENGTH_SHORT).show();
                return;
            }
            finish();
        } else {
            if (account.getName().isEmpty()) {
                mTextInputLayoutName.setErrorEnabled(true);
                mTextInputLayoutName.setError(getString(R.string.err_specify_account_name));
            }
            if (account.getCabbageId() < 0) {
                mTextInputLayoutCabbage.setErrorEnabled(true);
                mTextInputLayoutCabbage.setError(getString(R.string.err_specify_account_currency));
            }
            Toast.makeText(ActivityEditAccount.this, getResources().getString(R.string.msg_invalid_data_in_fields), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean showHelp() {
        super.showHelp();

        MaterialShowcaseView.resetSingleUse(this, SHOWCASE_ID);

        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500); // half second between each showcase view
        config.setMaskColor(ContextCompat.getColor(this, R.color.ColorPrimary));

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, SHOWCASE_ID);

        sequence.setConfig(config);

        String gotIt = getResources().getString(R.string.act_next);

        /*
        hlp_account_editor_intro
        hlp_account_editor_name
        hlp_account_editor_type
        hlp_account_editor_currency
        hlp_account_editor_emitent (если виден)
        hlp_account_editor_last4digits (если виден)
        hlp_account_editor_start_balance
        hlp_account_editor_closed
        */

        sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
                .setTarget(new View(this)).setDismissText(gotIt)
                .setMaskColour(ContextCompat.getColor(this, R.color.ColorPrimaryTransparent))
                .setContentText(getString(R.string.hlp_account_editor_intro))
                .build());

        sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
                .setTarget(editTextName).setDismissText(gotIt).withRectangleShape()
                .setMaskColour(ContextCompat.getColor(this, R.color.ColorPrimaryTransparent))
                .setContentText(R.string.hlp_account_editor_name)
                .build());

        sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
                .setTarget(editTextType).setDismissText(gotIt).withRectangleShape()
                .setMaskColour(ContextCompat.getColor(this, R.color.ColorPrimaryTransparent))
                .setContentText(R.string.hlp_account_editor_type)
                .build());

        sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
                .setTarget(editTextCabbage).setDismissText(gotIt).withRectangleShape()
                .setMaskColour(ContextCompat.getColor(this, R.color.ColorPrimaryTransparent))
                .setContentText(R.string.hlp_account_editor_currency)
                .build());

        if (editTextEmitent.getVisibility() == View.VISIBLE) {
            sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
                    .setTarget(editTextEmitent).setDismissText(gotIt).withRectangleShape()
                    .setMaskColour(ContextCompat.getColor(this, R.color.ColorPrimaryTransparent))
                    .setContentText(R.string.hlp_account_editor_emitent)
                    .build());
        }

        if (editTextLast4Digits.getVisibility() == View.VISIBLE) {
            sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
                    .setTarget(editTextLast4Digits).setDismissText(gotIt).withRectangleShape()
                    .setMaskColour(ContextCompat.getColor(this, R.color.ColorPrimaryTransparent))
                    .setContentText(R.string.hlp_account_editor_last4digits)
                    .build());
        }

        sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
                .setTarget(amountEditorStartBalance).setDismissText(gotIt).withRectangleShape()
                .setMaskColour(ContextCompat.getColor(this, R.color.ColorPrimaryTransparent))
                .setContentText(R.string.hlp_account_editor_start_balance)
                .build());

        sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
                .setTarget(checkboxAccountClosed).setDismissText(gotIt).withRectangleShape()
                .setMaskColour(ContextCompat.getColor(this, R.color.ColorPrimaryTransparent))
                .setContentText(R.string.hlp_account_editor_closed)
                .build());

        sequence.start();

        return true;
    }
}
