/*
 *  ******************************************************************************
 *     Copyright (c) 2013 Gabriele Mariotti.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *    *****************************************************************************
 */
package com.yoshione.fingen;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.yoshione.fingen.utils.ModUtils;
import com.yoshione.fingen.widgets.ToolbarActivity;

import it.gmariotti.changelibs.library.view.ChangeLogRecyclerView;

/**
 * Example with Dialog
 *
 * @author Gabriele Mariotti (gabri.mariotti@gmail.com)
 */
public class FragmentChangelog extends DialogFragment {

        public static final int CHANGELOG_DEFAULT = R.layout.fragment_changelog;
        public static final int CHANGELOG_X = R.layout.fragment_changelog_x;
        public static final int CHANGELOG_URL = R.layout.fragment_changelog_from_url;

        private int resourceChangelog;

        public FragmentChangelog() {
            this(CHANGELOG_DEFAULT);
        }

        public FragmentChangelog(int resourceChangelog) {
            this.resourceChangelog = resourceChangelog;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            ChangeLogRecyclerView chgList= (ChangeLogRecyclerView) layoutInflater.inflate(resourceChangelog, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setTitle(resourceChangelog == CHANGELOG_URL ? R.string.ttl_changelog_new : R.string.ttl_changelog)
                    .setView(chgList)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss());
            if (resourceChangelog == CHANGELOG_URL) {
                builder.setNeutralButton(R.string.act_do_not_remind, (dialog, which) -> {
                    ((ToolbarActivity) getActivity()).mPreferences.edit().putInt(FgConst.PREF_VERSION_X_CHECK, ModUtils.LAST_VERSION_CHECKED).apply();
                    dialog.dismiss();
                });
            }
            return builder.create();
        }

        static void show(ToolbarActivity activity, int resourceChangelog) {
            FragmentChangelog fragmentChangelog = new FragmentChangelog(resourceChangelog);
            FragmentManager fm = activity.getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            Fragment prev = fm.findFragmentByTag("changelogdemo_dialog");
            if (prev != null) {
                ft.remove(prev);
            }
            fragmentChangelog.show(ft, "changelogdemo_dialog");
        }
}
