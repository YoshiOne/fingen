package com.yoshione.fingen.calc;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;

public class CalculatorBuilder {
    private String activityTitle;
    private String value;

    public CalculatorBuilder() {
    }

    /**
     * Initialise the calculator view with a predefined value
     *
     * @param value the initial value
     * @return this
     */
    public CalculatorBuilder withValue(String value) {
        this.value = value;
        return this;
    }

    /**
     * Set the title for the window that will be displayed
     *
     * @param activityTitle set the title of the activity
     * @return this
     */
    public CalculatorBuilder withTitle(String activityTitle) {
        this.activityTitle = activityTitle;
        return this;
    }

    /**
     * Start the activity using the parent activity
     *
     * @param activity the current activity
     */
    public void start(Activity activity) {
        Intent i = new Intent(activity, CalculatorActivity.class);
        if (!TextUtils.isEmpty(activityTitle)) {
            i.putExtra(CalculatorActivity.TITLE_ACTIVITY, activityTitle);
        }

        if (!TextUtils.isEmpty(value)) {
            i.putExtra(CalculatorActivity.VALUE, value);
        }

        activity.startActivityForResult(i, CalculatorActivity.REQUEST_RESULT_SUCCESSFUL);
    }
}