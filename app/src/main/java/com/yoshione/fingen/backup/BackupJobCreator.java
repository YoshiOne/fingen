package com.yoshione.fingen.backup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

/**
 * Created by slv on 27.10.2017.
 *
 */

public class BackupJobCreator implements JobCreator {

    @Override
    @Nullable
    public Job create(@NonNull String tag) {
        switch (tag) {
            case BackupJob.TAG:
                return new BackupJob();
            default:
                return null;
        }
    }
}
