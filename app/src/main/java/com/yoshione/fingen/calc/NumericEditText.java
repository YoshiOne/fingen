package com.yoshione.fingen.calc;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import org.apache.commons.lang3.StringUtils;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class NumericEditText extends AppCompatEditText {
    private final char GROUPING_SEPARATOR = ' ';
    private final char DECIMAL_SEPARATOR = '.';
    private final String LEADING_ZERO_FILTER_REGEX = "^0+(?!$)";

    private String mDefaultText = null;
    private String mPreviousText = "";
    private String mNumberFilterRegex = "[^\\d\\-" + DECIMAL_SEPARATOR + "]";

    private char mDecimalSeparator = DECIMAL_SEPARATOR;
    private boolean hasCustomDecimalSeparator = false;
    private List<NumericValueWatcher> mNumericListeners = new ArrayList<>();
    private final TextWatcher mTextWatcher = new TextWatcher() {
        private boolean validateLock = false;

        @Override
        public void afterTextChanged(Editable s) {
            if (validateLock) {
                return;
            }

            // valid decimal number should not have more than 2 decimal separators
            if (StringUtils.countMatches(s.toString(), String.valueOf(mDecimalSeparator)) > 1) {
                validateLock = true;
                setText(mPreviousText); // cancel change and revert to previous input
                setSelection(mPreviousText.length());
                validateLock = false;
                return;
            }

            if (s.length() == 0) {
                handleNumericValueCleared();
                return;
            }

            setTextInternal(format(s.toString()));
            setSelection(getText().length());
            handleNumericValueChanged();
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // do nothing
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // do nothing
        }
    };

    public NumericEditText(Context context, AttributeSet attrs) {
        super(context, attrs);

        addTextChangedListener(mTextWatcher);
        setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // disable moving cursor
                setSelection(getText().length());
            }
        });
    }

    private void handleNumericValueCleared() {
        mPreviousText = "";
        for (NumericValueWatcher listener : mNumericListeners) {
            listener.onCleared();
        }
    }

    private void handleNumericValueChanged() {
        mPreviousText = getText().toString();
        for (NumericValueWatcher listener : mNumericListeners) {
            listener.onChanged(getNumericValue());
        }
    }

    /**
     * Add listener for numeric value changed events
     *
     * @param watcher listener to add
     */
    public void addNumericValueChangedListener(NumericValueWatcher watcher) {
        mNumericListeners.add(watcher);
    }

    /**
     * Remove all listeners to numeric value changed events
     */
    public void removeAllNumericValueChangedListeners() {
        while (!mNumericListeners.isEmpty()) {
            mNumericListeners.remove(0);
        }
    }

    /**
     * Set default numeric value and how it should be displayed, this value will be used if
     * {@link #clear} is called
     *
     * @param defaultNumericValue  numeric value
     * @param defaultNumericFormat display format for numeric value
     */
    public void setDefaultNumericValue(double defaultNumericValue, final String defaultNumericFormat) {
        mDefaultText = String.format(defaultNumericFormat, defaultNumericValue);
        if (hasCustomDecimalSeparator) {
            // swap locale decimal separator with custom one for display
            mDefaultText = StringUtils.replace(mDefaultText,
                    String.valueOf(DECIMAL_SEPARATOR), String.valueOf(mDecimalSeparator));
        }

        setTextInternal(mDefaultText);
    }

    /**
     * Use specified character for decimal separator. This will disable formatting.
     * This must be called before {@link #setDefaultNumericValue} if any
     *
     * @param customDecimalSeparator decimal separator to be used
     */
    public void setCustomDecimalSeparator(char customDecimalSeparator) {
        mDecimalSeparator = customDecimalSeparator;
        hasCustomDecimalSeparator = true;
        mNumberFilterRegex = "[^\\d\\" + mDecimalSeparator + "]";
    }

    /**
     * Clear text field and replace it with default value set in {@link #setDefaultNumericValue} if
     * any
     */
    public void clear() {
        setTextInternal(mDefaultText != null ? mDefaultText : "");
        if (mDefaultText != null) {
            handleNumericValueChanged();
        }
    }

    /**
     * Return numeric value represented by the text field
     *
     * @return numeric value
     */
    public double getNumericValue() {
        String original = getText().toString().replaceAll(mNumberFilterRegex, "");
        if (hasCustomDecimalSeparator) {
            // swap custom decimal separator with locale one to allow parsing
            original = StringUtils.replace(original,
                    String.valueOf(mDecimalSeparator), String.valueOf(DECIMAL_SEPARATOR));
        }

        try {
            return NumberFormat.getInstance().parse(original).doubleValue();
        } catch (ParseException e) {
            return Double.NaN;
        }
    }

    /**
     * Add grouping separators to string
     *
     * @param original original string, may already contains incorrect grouping separators
     * @return string with correct grouping separators
     */
    private String format(final String original) {
        final String[] parts = original.split("\\" + mDecimalSeparator, -1);
        String number = parts[0] // since we split with limit -1 there will always be at least 1 part
                .replaceAll(mNumberFilterRegex, "")
                .replaceFirst(LEADING_ZERO_FILTER_REGEX, "");

        // only add grouping separators for non custom decimal separator
        if (!hasCustomDecimalSeparator) {
            // add grouping separators, need to reverse back and forth since Java regex does not support
            // right to left matching
            number = StringUtils.reverse(
                    StringUtils.reverse(number).replaceAll("(.{3})", "$1" + GROUPING_SEPARATOR));
            // remove leading grouping separator if any
            number = StringUtils.removeStart(number, String.valueOf(GROUPING_SEPARATOR));
        }

        // add fraction part if any
        if (parts.length > 1) {
            if (parts[1].length() > 2) {
                number += mDecimalSeparator + parts[1].substring(parts[1].length() - 2, parts[1].length());
            } else {
                number += mDecimalSeparator + parts[1];
            }
        }

        return number;
    }

    /**
     * Change display text without triggering numeric value changed
     *
     * @param text new text to apply
     */
    private void setTextInternal(String text) {
        removeTextChangedListener(mTextWatcher);
        setText(text);
        addTextChangedListener(mTextWatcher);
    }

    /**
     * Interface to notify listeners when numeric value has been changed or cleared
     */
    interface NumericValueWatcher {
        /**
         * Fired when numeric value has been changed
         *
         * @param newValue new numeric value
         */
        void onChanged(double newValue);

        /**
         * Fired when numeric value has been cleared (text field is empty)
         */
        void onCleared();
    }
}