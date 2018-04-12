/*
 * Copyright (c) 2015.
 */

package com.yoshione.fingen;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.widget.LinearLayout;

import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.managers.TransactionManager;
import com.yoshione.fingen.model.Template;
import com.yoshione.fingen.utils.RequestCodes;
import com.yoshione.fingen.widgets.ToolbarActivity;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by slv on 07.12.2015.
 *
 */
public class ActivityModelList extends ToolbarActivity implements SearchView.OnQueryTextListener {

    @BindView(R.id.container)
    LinearLayout container;
    private FragmentModelList fragmentModelList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        if (getIntent().getAction() != null && getIntent().getAction().equals(FgConst.ACT_SHOW_TEMPLATES)) {
            getIntent().putExtra("showHomeButton", false);
            getIntent().putExtra("model", new Template());
            getIntent().putExtra("requestCode", RequestCodes.REQUEST_CODE_SELECT_MODEL);
        }
        IAbstractModel model = getIntent().getParcelableExtra("model");

        if (getSupportActionBar() != null) {
            switch (model.getModelType()) {
                case IAbstractModel.MODEL_TYPE_SMSMARKER: {
                    getSupportActionBar().setTitle(getResources().getString(R.string.ent_sms_markers));
                    break;
                }
                case IAbstractModel.MODEL_TYPE_CREDIT: {
                    getSupportActionBar().setTitle(getResources().getString(R.string.ent_credits));
                    break;
                }
                case IAbstractModel.MODEL_TYPE_TEMPLATE: {
                    getSupportActionBar().setTitle(getResources().getString(R.string.ent_templates));
                    break;
                }
                case IAbstractModel.MODEL_TYPE_PRODUCT: {
                    getSupportActionBar().setTitle(getResources().getString(R.string.ent_products));
                    break;
                }
            }
        }

        if (savedInstanceState == null) {
            final int requestCode = getIntent().getExtras().getInt("requestCode");
            fragmentModelList = FragmentModelList.newInstance((IAbstractModel) getIntent().getParcelableExtra("model"), requestCode);
            fragmentModelList.setmModelListEventListener(new FragmentModelList.ModelListEventListener() {
                @Override
                public void OnModelClick(IAbstractModel abstractModel) {

                    if (requestCode == RequestCodes.REQUEST_CODE_SELECT_MODEL) {
                        if (getIntent().getAction() != null && (abstractModel.getClass().equals(Template.class)) && getIntent().getAction().equals(FgConst.ACT_SHOW_TEMPLATES)) {
                            TransactionManager.createTransactionFromTemplate((Template) abstractModel, getApplicationContext());
                        } else {
                            Intent intent = new Intent();
                            intent.putExtra("model", abstractModel);
                            ActivityModelList.this.setResult(RESULT_OK, intent);
                        }
                        ActivityModelList.this.finish();
                    }
                }
            });
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, fragmentModelList)
                    .commit();
        }

//        setContentView(container);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        fragmentModelList.setmFilter(newText);
        fragmentModelList.updateRecyclerView();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_model_list, menu);
        super.onCreateOptionsMenu(menu);
        final android.view.MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_model_list;
    }

    @Override
    protected String getLayoutTitle() {
        return "";
    }
}
