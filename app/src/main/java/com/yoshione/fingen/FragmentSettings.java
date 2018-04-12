package com.yoshione.fingen;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.XpPreferenceFragment;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.github.omadahealth.lollipin.lib.managers.AppLock;
import com.yoshione.fingen.dao.DepartmentsDAO;
import com.yoshione.fingen.fts.ActivityFtsLogin;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.utils.PrefUtils;
import com.yoshione.fingen.utils.RequestCodes;
import com.yoshione.fingen.widgets.CustomPinActivity;

import net.xpece.android.support.preference.ListPreference;
import net.xpece.android.support.preference.MultiSelectListPreference;
import net.xpece.android.support.preference.PreferenceDividerDecoration;
import net.xpece.android.support.preference.PreferenceScreenNavigationStrategy;
import net.xpece.android.support.preference.SharedPreferencesCompat;
import net.xpece.android.support.preference.SwitchPreference;

import java.util.HashSet;
import java.util.Set;

import static android.app.Activity.RESULT_OK;
import static com.yoshione.fingen.utils.RequestCodes.REQUEST_CODE_SELECT_MODEL;

/**
 * A placeholder fragment containing a simple view.
 */
public class FragmentSettings extends XpPreferenceFragment implements ICanPressBack {
//private static final String TAG = FragmentSettings.class.getSimpleName();

    private static final int REQUEST_CODE_ENABLE = 11;
    //    private static final int REQUEST_CODE_UNLOCK = 12;
    private static final int REQUEST_CODE_DISABLE = 13;
    // These are used to navigate back and forth between subscreens.
//    private PreferenceScreenNavigationStrategy mPreferenceScreenNavigation;
    private static final Activity[] activities = {null};

    private final Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);
            } else if (preference instanceof MultiSelectListPreference) {
                preference.setSummary(convertValuesToSummary((MultiSelectListPreference) preference));
            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                if (preference.getKey().equals(FgConst.PREF_DEFAULT_DEPARTMENT)) {
                    preference.setSummary(PrefUtils.getDefaultDepartment(getActivity()).getFullName());
                } else {
                    preference.setSummary(stringValue);
                }
            }
            return true;
        }
    };


    public static FragmentSettings newInstance(String rootKey) {
        Bundle args = new Bundle();
        args.putString(FragmentSettings.ARG_PREFERENCE_ROOT, rootKey);
        FragmentSettings fragment = new FragmentSettings();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public String[] getCustomDefaultPackages() {
        return new String[]{BuildConfig.APPLICATION_ID};
    }

    @Override
    public void onCreatePreferences2(final Bundle savedInstanceState, final String rootKey) {
        // Add 'general' preferences.
        addPreferencesFromResource(R.xml.prefs_general);

        activities[0] = getActivity();

        bindPreferenceSummaryToValue(findPreference("start_tab"));
        bindPreferenceSummaryToValue(findPreference("theme"));
        bindPreferenceSummaryToValue(findPreference(FgConst.PREF_ACCOUNT_CLICK_ACTION));
        bindPreferenceSummaryToValue(findPreference("balance_compare_error"));
        bindPreferenceSummaryToValue(findPreference("autocreate_prerequisites"));
        bindPreferenceSummaryToValue(findPreference("payee_selection_style"));
        bindPreferenceSummaryToValue(findPreference("pin_length"));
        bindPreferenceSummaryToValue(findPreference(FgConst.PREF_DEFAULT_DEPARTMENT));
        findPreference("enable_pin_lock").setOnPreferenceChangeListener(sCheckBoxPreferenceChangeListener);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean pinEnabled = prefs.getBoolean("enable_pin_lock", false);
        getPreferenceScreen().findPreference("change_pin").setEnabled(pinEnabled);
        getPreferenceScreen().findPreference("pin_length").setEnabled(!pinEnabled);
        findPreference("change_pin").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (((SwitchPreference) FragmentSettings.this.findPreference("enable_pin_lock")).isChecked()) {
                    Intent intent = new Intent(activities[0], CustomPinActivity.class);
                    intent.putExtra(AppLock.EXTRA_TYPE, AppLock.CHANGE_PIN);
                    activities[0].startActivity(intent);
                    return true;
                } else {
                    return false;
                }
            }
        });
        findPreference(FgConst.PREF_TAB_ORDER).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                FragmentTabOrderDialog dialog = new FragmentTabOrderDialog();
                dialog.show(getActivity().getSupportFragmentManager(),"fragment_tab_order");
                return true;
            }
        });
        findPreference(FgConst.PREF_FTS_CREDENTIALS).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivityForResult(
                        new Intent(getActivity(), ActivityFtsLogin.class),
                        RequestCodes.REQUEST_CODE_ENTER_FTS_LOGIN);
                return true;
            }
        });
        findPreference(FgConst.PREF_DEFAULT_DEPARTMENT).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), ActivityList.class);
                intent.putExtra("showHomeButton", false);
                intent.putExtra("model", PrefUtils.getDefaultDepartment(getActivity()));
                intent.putExtra("requestCode", REQUEST_CODE_SELECT_MODEL);
                startActivityForResult(intent, REQUEST_CODE_SELECT_MODEL);
                return true;
            }
        });
        findPreference(FgConst.PREF_RESET_DEFAULT_DEPARTMENT).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().remove(FgConst.PREF_DEFAULT_DEPARTMENT).apply();
                return true;
            }
        });

        // Setup root preference title.
        getPreferenceScreen().setTitle(getActivity().getTitle());

        // Setup root preference key from arguments.
//        getPreferenceScreen().setKey(rootKey);

        PreferenceScreenNavigationStrategy.ReplaceFragment.onCreatePreferences(this, rootKey);

        prefs.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

//        mPreferenceScreenNavigation.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Change activity title to preference title. Used with ReplaceFragment strategy.
        getActivity().setTitle(getPreferenceScreen().getTitle());
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        if (preference instanceof MultiSelectListPreference) {

            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, convertValuesToSummary((MultiSelectListPreference) preference));
        } else {
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), ""));
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final RecyclerView listView = getListView();

        // We're using alternative divider.
        listView.addItemDecoration(new PreferenceDividerDecoration(getContext()).drawBottom(true).drawBetweenCategories(false));
        setDivider(null);

        // We don't want this. The children are still focusable.
        listView.setFocusable(false);
    }

    // Here follows ReplaceRoot strategy stuff. ====================================================

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }


    private static final Preference.OnPreferenceChangeListener sCheckBoxPreferenceChangeListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            if (preference.getKey().equals("enable_pin_lock")) {
                if (!((SwitchPreference) preference).isChecked()) {
                    Intent intent = new Intent(activities[0], CustomPinActivity.class);
                    intent.putExtra(AppLock.EXTRA_TYPE, AppLock.ENABLE_PINLOCK);
                    activities[0].startActivityForResult(intent, REQUEST_CODE_ENABLE);
                } else {
                    Intent intent = new Intent(activities[0], CustomPinActivity.class);
                    intent.putExtra(AppLock.EXTRA_TYPE, AppLock.DISABLE_PINLOCK);
                    activities[0].startActivityForResult(intent, REQUEST_CODE_DISABLE);
                }
            }
            return true;
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CODE_ENABLE:
                Toast.makeText(getActivity(), getString(R.string.toast_pin_enabled), Toast.LENGTH_SHORT).show();
                break;
            case REQUEST_CODE_DISABLE:
                Toast.makeText(getActivity(), getString(R.string.toast_pin_disabled), Toast.LENGTH_SHORT).show();
                break;
            case REQUEST_CODE_SELECT_MODEL:
                if (resultCode == RESULT_OK && data != null) {
                    IAbstractModel model = data.getParcelableExtra("model");
                    PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString(FgConst.PREF_DEFAULT_DEPARTMENT, String.valueOf(model.getID())).apply();
                }
                break;
        }
    }

    private String convertValuesToSummary(MultiSelectListPreference preference) {
        String summary = "";
        Set<String> values = SharedPreferencesCompat.getStringSet(
                PreferenceManager.getDefaultSharedPreferences(preference.getContext()),
                preference.getKey(),
                new HashSet<String>());
        if (preference.getKey().equals("autocreate_prerequisites")) {
            Resources res = activities[0].getResources();
            for (String s : values) {
                if (!summary.isEmpty()) summary = summary + ", ";
                if (s.equals("account")) summary = summary + res.getString(R.string.ent_account);
                if (s.equals("amount")) summary = summary + res.getString(R.string.ent_amount);
                if (s.equals("type")) summary = summary + res.getString(R.string.ent_type);
                if (s.equals("payee"))
                    summary = summary + res.getString(R.string.ent_payee_or_payer);
                if (s.equals("category")) summary = summary + res.getString(R.string.ent_category);
            }
        } else {
            summary = values.toString();
            summary = summary.trim().substring(1, summary.length() - 1); // strip []
        }
        return summary;
    }

    private final SharedPreferences.OnSharedPreferenceChangeListener listener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                    if (key.equals("autocreate_prerequisites")) {
                        bindPreferenceSummaryToValue(findPreference("autocreate_prerequisites"));
                    }
                    if (key.equals(FgConst.PREF_DEFAULT_DEPARTMENT)) {
                        bindPreferenceSummaryToValue(findPreference(FgConst.PREF_DEFAULT_DEPARTMENT));
                    }
                    if (key.equals("enable_pin_lock")) {
                        boolean pinEnabled = prefs.getBoolean(key, true);
                        getPreferenceScreen().findPreference("change_pin").setEnabled(pinEnabled);
                        getPreferenceScreen().findPreference("pin_length").setEnabled(!pinEnabled);
                    }
                }
            };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefs.unregisterOnSharedPreferenceChangeListener(listener);
    }

}

