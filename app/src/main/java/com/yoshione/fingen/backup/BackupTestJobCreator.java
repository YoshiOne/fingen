package com.yoshione.fingen.backup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

/**
 * Created by slv on 30.10.2017.
 */

public class BackupTestJobCreator implements JobCreator {
    @Override
    @Nullable
    public Job create(@NonNull String tag) {
        switch (tag) {
            case BackupTestJob.TAG:
                return new BackupTestJob();
            default:
                return null;
        }
    }
}
