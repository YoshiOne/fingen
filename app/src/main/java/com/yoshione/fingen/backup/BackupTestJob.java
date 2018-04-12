package com.yoshione.fingen.backup;

import android.support.annotation.NonNull;
import android.util.Log;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

/**
 * Created by slv on 30.10.2017.
 * /
 */

public class BackupTestJob extends Job {

    public static final String TAG = "job_backup_test_tag";

    public static int scheduleJob() {
        int id = new JobRequest.Builder(BackupTestJob.TAG)
                .setPeriodic(900_000)
                .build()
                .schedule();
        Log.d(TAG, "Backup test job ID = " + id);
        return id;
    }

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        Log.d(TAG, "Start test backup job");

        return Result.SUCCESS;
    }
}
