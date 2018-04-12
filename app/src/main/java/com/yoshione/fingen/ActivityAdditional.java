package com.yoshione.fingen;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.yoshione.fingen.adapter.AdapterMenuItems;
import com.yoshione.fingen.model.MenuItem;
import com.yoshione.fingen.widgets.ToolbarActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ActivityAdditional extends ToolbarActivity {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_additional;
    }

    @Override
    protected String getLayoutTitle() {
        return getString(R.string.ent_additional);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        List<MenuItem> menuItemList = new ArrayList<>();

        final Intent[] intent = new Intent[1];

        menuItemList.add(new MenuItem(ContextCompat.getDrawable(this, R.drawable.ic_import),
                getString(R.string.ent_import_csv_fingen),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        intent[0] = new Intent(ActivityAdditional.this, ActivityImportCSV.class);
                        intent[0].putExtra("type", "fingen");
                        ActivityAdditional.this.startActivity(intent[0]);
                    }
                }, 0));

        menuItemList.add(new MenuItem(ContextCompat.getDrawable(this, R.drawable.ic_import),
                getString(R.string.ent_import_csv_financisto),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        intent[0] = new Intent(ActivityAdditional.this, ActivityImportCSV.class);
                        intent[0].putExtra("type", "financisto");
                        ActivityAdditional.this.startActivity(intent[0]);
                    }
                }, 1));

        menuItemList.add(new MenuItem(ContextCompat.getDrawable(this, R.drawable.ic_import),
                getString(R.string.ent_import_csv_advanced),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        intent[0] = new Intent(ActivityAdditional.this, ActivityImportCSVAdvanced.class);
                        ActivityAdditional.this.startActivity(intent[0]);
                    }
                }, 2));

        menuItemList.add(new MenuItem(ContextCompat.getDrawable(this, R.drawable.ic_export),
                getString(R.string.ent_export_data),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        intent[0] = new Intent(ActivityAdditional.this, ActivityExportCSV.class);
                        ActivityAdditional.this.startActivity(intent[0]);
                    }
                }, 3));

        menuItemList.add(new MenuItem(ContextCompat.getDrawable(this, R.drawable.ic_backup_and_restore),
                getString(R.string.ent_backup_data),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        intent[0] = new Intent(ActivityAdditional.this, ActivityBackup.class);
                        ActivityAdditional.this.startActivity(intent[0]);
                    }
                }, 4));

//        menuItemList.add(new MenuItem(iconGenerator.getBackupRestoreIcon(this),
//                "Export sheet",
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//
//                    }
//                }, 2));

//        if (BuildConfig.FLAVOR.equals("sync")) {
//            menuItemList.add(new MenuItem(iconGenerator.getSyncIcon(this),
//                    getString(R.string.ent_sync),
//                    new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            intent[0] = new Intent(ActivityAdditional.this, ActivitySyncSettings.class);
//                            ActivityAdditional.this.startActivity(intent[0]);
//                        }
//                    }, 2));
//        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        AdapterMenuItems adapter = new AdapterMenuItems(menuItemList);
        adapter.setHasStableIds(true);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
    }
}
