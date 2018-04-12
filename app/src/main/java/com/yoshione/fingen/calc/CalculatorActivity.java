package com.yoshione.fingen.calc;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.yoshione.fingen.FgConst;
import com.yoshione.fingen.R;
import com.yoshione.fingen.widgets.ToolbarActivity;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CalculatorActivity extends ToolbarActivity {
    public static final int NUMBER_EDIT_TEXT_MAX_LENGTH = 30;
    public static final int REQUEST_RESULT_SUCCESSFUL = 2;
    public static final String TITLE_ACTIVITY = "title_activity";
    public static final String VALUE = "value_calculator";
    public static final String RESULT = "result_calculator";

    public static final String ZERO = "0";
    public static final String ZERO_ZERO = "00";
    public static final String POINT = ".";
    public static final String CLICK_ARITHMETIC_OPERATOR = "clickArithmeticOperator";
    public static final String CLEAR_INPUT = "clearInput";
    public static final String FIRST_VALUE = "firstValue";
    public static final String SECONDS_VALUE = "secondsValue";
    public static final String OPERATOR_EXECUTE = "operatorExecute";
    public static final String PREV_OPERATOR_EXECUTE = "prevOperatorExecute";
    @BindView(R.id.developing_operation_inputText)
    TextView mDevelopingOperationInputText;
    @BindView(R.id.number_inputText)
    NumericEditText mNumberInputText;
    @BindView(R.id.clear_button)
    Button mClearButton;
    @BindView(R.id.seven_button)
    Button mSevenButton;
    @BindView(R.id.four_button)
    Button mFourButton;
    @BindView(R.id.one_button)
    Button mOneButton;
    @BindView(R.id.zero_button)
    Button mZeroButton;
    @BindView(R.id.divider_button)
    Button mDividerButton;
    @BindView(R.id.eight_button)
    Button mEightButton;
    @BindView(R.id.five_button)
    Button mFiveButton;
    @BindView(R.id.two_button)
    Button mTwoButton;
    @BindView(R.id.two_zero_button)
    Button mTwoZeroButton;
    @BindView(R.id.multiplication_button)
    Button mMultiplicationButton;
    @BindView(R.id.nine_button)
    Button mNineButton;
    @BindView(R.id.six_button)
    Button mSixButton;
    @BindView(R.id.three_button)
    Button mThreeButton;
    @BindView(R.id.point_button)
    Button mPointButton;
    @BindView(R.id.delete_button)
    Button mDeleteButton;
    @BindView(R.id.subtraction_button)
    Button mSubtractionButton;
    @BindView(R.id.sum_button)
    Button mSumButton;
    @BindView(R.id.equal_button)
    Button mEqualButton;
    @BindView(R.id.submit_button)
    Button mSubmitButton;

    private DecimalFormat decimalFormat;

    private boolean mFirstTap = true;

    //operations values
    private boolean clickArithmeticOperator;
    private boolean clearInput;
    private final OnClickListener mOnNumberBtnClickListener = new OnClickListener() {
        public void onClick(View view) {
            if (view instanceof Button) {
                String value = ((Button) view).getText().toString();
                concatNumeric(value);
                clickArithmeticOperator = false;
            }
        }
    };
    private Double firstValue;
    private Double secondsValue;
    private String operatorExecute = Operators.NONE;
    private String prevOperatorExecute = Operators.NONE;
    private final OnClickListener mOnOperatorBtnClickListener = new OnClickListener() {
        public void onClick(View view) {
            if (view instanceof Button) {
                String value = ((Button) view).getText().toString();

                switch (value) {
                    case Operators.SUM:
                    case Operators.SUBTRACTION:
                    case Operators.MULTIPLICATION:
                    case Operators.DIVIDER: {
                        if (TextUtils.isEmpty(mNumberInputText.getText())
                                || mNumberInputText.getText().toString().equals("."))
                            return;

                        mEqualButton.setVisibility(View.VISIBLE);
                        mSubmitButton.setVisibility(View.GONE);
                        operatorExecute = value;

                        if (!clickArithmeticOperator) {
                            clickArithmeticOperator = true;
                            prepareOperation(false);
                        } else {
                            replaceOperator(value);
                        }
                        break;
                    }
                    case Operators.CLEAR: {
                        clear();
                        break;
                    }
                    case Operators.DELETE: {
                        removeLastNumber();
                        break;
                    }
                    case Operators.EQUAL:
                    case Operators.SUBMIT: {
                        if (mNumberInputText.getText().toString().equals(".")) {
                            String temp = mDevelopingOperationInputText.getText().toString();
                            clear();
                            mNumberInputText.setText(temp);
                            return;
                        }

                        if (operatorExecute.equals(Operators.SUBMIT) || firstValue == null) {
                            returnResultOperation();
                        } else {
                            prepareOperation(true);
                            clearInput = false;
                            clickArithmeticOperator = false;
                            operatorExecute = Operators.SUBMIT;
                            prevOperatorExecute = Operators.NONE;
                            firstValue = null;
                            secondsValue = null;
                        }
                        break;
                    }
                }
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.action_go_home).setVisible(false);
        return true;
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_calculator;
    }

    @Override
    protected String getLayoutTitle() {
        return getString(R.string.ent_calculator);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        initComponents();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (outState != null) {
            outState.putBoolean(CLICK_ARITHMETIC_OPERATOR, clickArithmeticOperator);
            outState.putBoolean(CLEAR_INPUT, clearInput);

            if (firstValue != null)
                outState.putDouble(FIRST_VALUE, firstValue);

            if (secondsValue != null)
                outState.putDouble(SECONDS_VALUE, secondsValue);
            outState.putString(OPERATOR_EXECUTE, operatorExecute);
            outState.putString(PREV_OPERATOR_EXECUTE, prevOperatorExecute);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null) {
            clickArithmeticOperator = savedInstanceState.getBoolean(CLICK_ARITHMETIC_OPERATOR);
            clearInput = savedInstanceState.getBoolean(CLEAR_INPUT);

            if (savedInstanceState.containsKey(FIRST_VALUE))
                firstValue = savedInstanceState.getDouble(FIRST_VALUE);

            if (savedInstanceState.containsKey(SECONDS_VALUE))
                secondsValue = savedInstanceState.getDouble(SECONDS_VALUE);

            operatorExecute = savedInstanceState.getString(OPERATOR_EXECUTE);
            prevOperatorExecute = savedInstanceState.getString(PREV_OPERATOR_EXECUTE);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    private void initComponents() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(ContextCompat.getDrawable(this, R.drawable.ic_close_white));
        }
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        decimalFormatSymbols.setGroupingSeparator(',');
        decimalFormatSymbols.setDecimalSeparator('.');
        decimalFormat = new DecimalFormat("#,###,##0.00", decimalFormatSymbols);

        String title = getIntent().getStringExtra(TITLE_ACTIVITY);
        if (!TextUtils.isEmpty(title)) {
            setTitle(getIntent().getStringExtra(TITLE_ACTIVITY));
        } else {
            setTitle(getString(R.string.app_name));
        }

        String value = TextUtils.isEmpty(getIntent().getStringExtra(VALUE)) ? "" : getIntent().getStringExtra(VALUE);

        mNumberInputText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(NUMBER_EDIT_TEXT_MAX_LENGTH)});
        mNumberInputText.setText(value);

        List<Button> arithmeticOperators = new ArrayList<>();
        arithmeticOperators.add(mDividerButton);
        arithmeticOperators.add(mMultiplicationButton);
        arithmeticOperators.add(mSubtractionButton);
        arithmeticOperators.add(mSumButton);

        List<Button> secondaryOperators = new ArrayList<>();
        secondaryOperators.add(mClearButton);
        secondaryOperators.add(mDeleteButton);
        secondaryOperators.add(mEqualButton);
        secondaryOperators.add(mSubmitButton);

        List<Button> numericOperators = new ArrayList<>();
        numericOperators.add(mPointButton);
        numericOperators.add(mZeroButton);
        numericOperators.add(mTwoZeroButton);
        numericOperators.add(mOneButton);
        numericOperators.add(mTwoButton);
        numericOperators.add(mThreeButton);
        numericOperators.add(mFourButton);
        numericOperators.add(mFiveButton);
        numericOperators.add(mSixButton);
        numericOperators.add(mSevenButton);
        numericOperators.add(mEightButton);
        numericOperators.add(mNineButton);

        setOnClickListenerBtn(arithmeticOperators, mOnOperatorBtnClickListener);
        setOnClickListenerBtn(secondaryOperators, mOnOperatorBtnClickListener);
        setOnClickListenerBtn(numericOperators, mOnNumberBtnClickListener);
    }

    private void prepareOperation(boolean isEqualExecute) {
        clearInput = true;

        if (isEqualExecute) {
            mEqualButton.setVisibility(View.GONE);
            mSubmitButton.setVisibility(View.VISIBLE);
            mDevelopingOperationInputText.setText("");
        } else {
            concatDevelopingOperation(operatorExecute, mNumberInputText.getText().toString(), false);
        }

        if (firstValue == null) {
            firstValue = Double.parseDouble(mNumberInputText.getText().toString().replaceAll("[, ]", ""));
        } else if (secondsValue == null) {
            if (!mNumberInputText.getText().toString().isEmpty()) {
                secondsValue = Double.parseDouble(mNumberInputText.getText().toString().replaceAll("[, ]", ""));
            } else {
                secondsValue = firstValue;
            }
            executeOperation(prevOperatorExecute);
        }

        prevOperatorExecute = operatorExecute;
    }

    private void executeOperation(String operator) {
        if (firstValue == null || secondsValue == null) {
            return;
        }

        double resultOperation = 0.0;

        switch (operator) {
            case Operators.SUM: {
                resultOperation = firstValue + secondsValue;
                break;
            }
            case Operators.SUBTRACTION: {
                resultOperation = firstValue - secondsValue;
                break;
            }
            case Operators.MULTIPLICATION: {
                resultOperation = firstValue * secondsValue;
                break;
            }
            case Operators.DIVIDER: {
                if (secondsValue > 0) {
                    resultOperation = firstValue / secondsValue;
                }
                break;
            }
        }

        mNumberInputText.setText(formatValue(resultOperation));
        firstValue = resultOperation;
        secondsValue = null;
    }

    private void concatNumeric(String value) {
        if (value == null || mNumberInputText.getText() == null) {
            return;
        }

        String oldValue;
        if (mFirstTap) {
            oldValue = "";
            mFirstTap = false;
        } else {
            oldValue = mNumberInputText.getText().toString();
        }
        String newValue = clearInput || (oldValue.equals(ZERO) && !value.equals(POINT)) ? value : oldValue + value;
        newValue = oldValue.equals(ZERO) && value.equals(ZERO_ZERO) ? oldValue : newValue;

        mNumberInputText.setText(newValue);
        clearInput = false;
    }

    private void concatDevelopingOperation(String operator, String value, boolean clear) {
        boolean noValidCharacter = operator.equals(Operators.CLEAR) || operator.equals(Operators.DELETE) || operator.equals(Operators.EQUAL);

        if (!noValidCharacter) {
            String oldValue = clear ? "" : mDevelopingOperationInputText.getText().toString();
            mDevelopingOperationInputText.setText(String.format("%s %s %s", oldValue, value, operator));


        }
    }

    private void removeLastNumber() {
        String value = mNumberInputText.getText().toString();
        if (value.length() != 0)
            mNumberInputText.setText(value.substring(0, value.length() - 1));
    }

    private void clear() {
        mEqualButton.setVisibility(View.GONE);
        mSubmitButton.setVisibility(View.VISIBLE);

        firstValue = null;
        secondsValue = null;
        operatorExecute = Operators.NONE;
        prevOperatorExecute = Operators.NONE;

        mDevelopingOperationInputText.setText("");
        mNumberInputText.setText("");
    }

    private void setOnClickListenerBtn(List<Button> buttons, OnClickListener onClickListener) {
        for (Button button : buttons) {
            button.setOnClickListener(onClickListener);
        }
    }

    private void returnResultOperation() {
        String result = mNumberInputText.getText().toString().replaceAll(" ", "");

        Intent broadcastIntent = new Intent(FgConst.ACT_CALC_DONE);


        Intent resultIntent = new Intent();

        if (result.equals(".")) {
            result = "";
        }

        broadcastIntent.putExtra(RESULT, result);
        sendBroadcast(broadcastIntent);
        resultIntent.putExtra(RESULT, result);
        setResult(REQUEST_RESULT_SUCCESSFUL, resultIntent);
        finish();
    }

    private void replaceOperator(String operator) {
        String operationValue = mDevelopingOperationInputText.getText().toString();

        if (TextUtils.isEmpty(operationValue)) {
            return;
        }

        String oldOperator = operationValue.substring(operationValue.length() - 1, operationValue.length());

        if (oldOperator.equals(operator)) {
            return;
        }

        String operationNewValue = operationValue.substring(0, operationValue.length() - 2);
        concatDevelopingOperation(operator, operationNewValue, true);
    }

    private String formatValue(double value) {
        String valueStr = decimalFormat.format(value);

        String integerValue = valueStr.substring(0, valueStr.indexOf(POINT));
        String decimalValue = valueStr.substring(valueStr.indexOf(POINT) + 1, valueStr.length());

        if (decimalValue.equals(ZERO_ZERO) || decimalValue.equals(ZERO)) {
            return integerValue;
        }

        return valueStr;
    }
}
