package com.yoshione.fingen;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.yoshione.fingen.adapter.AdapterTreeModel;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.interfaces.IAdapterEventsListener;
import com.yoshione.fingen.utils.RequestCodes;
import com.yoshione.fingen.widgets.ToolbarActivity;

import java.util.HashSet;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ActivityList extends ToolbarActivity implements SearchView.OnQueryTextListener, IAdapterEventsListener {

    @BindView(R.id.container)
    LinearLayout container;
    FragmentTreeModelList fragment;
    private int mViewMode;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        IAbstractModel model = getIntent().getParcelableExtra("model");

        if (getSupportActionBar() != null) {
            switch (model.getModelType()) {
                case IAbstractModel.MODEL_TYPE_CABBAGE: {
                    getSupportActionBar().setTitle(getResources().getString(R.string.ent_currencies));
                    break;
                }
                case IAbstractModel.MODEL_TYPE_CATEGORY: {
                    getSupportActionBar().setTitle(getResources().getString(R.string.ent_categories));
                    break;
                }
                case IAbstractModel.MODEL_TYPE_PAYEE: {
                    getSupportActionBar().setTitle(getResources().getString(R.string.ent_payees));
                    break;
                }
                case IAbstractModel.MODEL_TYPE_PROJECT: {
                    getSupportActionBar().setTitle(getResources().getString(R.string.ent_projects));
                    break;
                }
                case IAbstractModel.MODEL_TYPE_DEPARTMENT: {
                    getSupportActionBar().setTitle(getResources().getString(R.string.ent_departments));
                    break;
                }
                case IAbstractModel.MODEL_TYPE_LOCATION: {
                    getSupportActionBar().setTitle(getResources().getString(R.string.ent_locations));
                    break;
                }
                case IAbstractModel.MODEL_TYPE_ACCOUNT: {
                    getSupportActionBar().setTitle(getResources().getString(R.string.ent_accounts));
                    break;
                }
                case IAbstractModel.MODEL_TYPE_SENDER: {
                    getSupportActionBar().setTitle(getResources().getString(R.string.ent_senders));
                    break;
                }
            }
        }

        if (savedInstanceState == null) {
            int requestCode = getIntent().getIntExtra("requestCode", RequestCodes.REQUEST_CODE_VIEW_MODELS);

            switch (requestCode) {
                case RequestCodes.REQUEST_CODE_VIEW_MODELS:
                    mViewMode = AdapterTreeModel.MODE_VIEW;
                    break;
                case RequestCodes.REQUEST_CODE_SELECT_MODEL:
                    mViewMode = AdapterTreeModel.MODE_SINGLECHOICE;
                    break;
                case RequestCodes.REQUEST_CODE_BULK_SELECT_MODEL:
                    mViewMode = AdapterTreeModel.MODE_MULTICHOICE;
                    break;
                default:
                    mViewMode = AdapterTreeModel.MODE_VIEW;
            }

            HashSet<Long> checkedIDs;
            if (requestCode == RequestCodes.REQUEST_CODE_BULK_SELECT_MODEL) {
                checkedIDs = (HashSet<Long>) getIntent().getSerializableExtra("checked_ids");
            } else {
                checkedIDs = null;
            }
            fragment = FragmentTreeModelList.newInstance(model, checkedIDs, mViewMode);
//            fragment.setAdapterEventsListener(this);
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
        }
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_category_list;
    }

    @Override
    protected String getLayoutTitle() {
        return getString(R.string.ent_categories);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        fragment.setmFilter(newText);
        fragment.updateRecyclerView();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (mViewMode == AdapterTreeModel.MODE_MULTICHOICE) {
            getMenuInflater().inflate(R.menu.menu_multiselect, menu);
            MenuItem unselectAll = menu.findItem(R.id.action_unselect_all).setIcon(getDrawable(R.drawable.ic_checkbox_multiple_unmarked_white));
            unselectAll.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    fragment.adapter.unselectAll();
                    return true;
                }
            });
            MenuItem selectAll = menu.findItem(R.id.action_select_all).setIcon(getDrawable(R.drawable.ic_checkbox_multiple_marked_white));
            selectAll.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    fragment.adapter.selectAll();
                    return true;
                }
            });
            MenuItem selectComplete = menu.findItem(R.id.action_complete_selection).setIcon(getDrawable(R.drawable.ic_check_white));
            selectComplete.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch (getIntent().getExtras().getInt("requestCode")) {
                        case RequestCodes.REQUEST_CODE_VIEW_MODELS:
                            break;
                        case RequestCodes.REQUEST_CODE_SELECT_MODEL:
                            break;
                        case RequestCodes.REQUEST_CODE_BULK_SELECT_MODEL:
                            Intent intent = new Intent();
                            intent.putExtra("model", fragment.adapter.getTree().getModel());
                            intent.putExtra("filterID", getIntent().getLongExtra("filterID", -1));
                            intent.putExtra("checked_ids", fragment.adapter.getTree().getSelectedModelsIDs());
                            setResult(RESULT_OK, intent);
                            finish();
                            break;
                        default:
                            break;
                    }
                    return true;
                }
            });
        } else {
            getMenuInflater().inflate(R.menu.menu_model_list, menu);
        }


        super.onCreateOptionsMenu(menu);
        final MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public void onItemClick(IAbstractModel model) {
        switch (getIntent().getExtras().getInt("requestCode")) {
            case RequestCodes.REQUEST_CODE_VIEW_MODELS:
                break;
            case RequestCodes.REQUEST_CODE_SELECT_MODEL:
                Intent intent = new Intent();
                intent.putExtra("model", model);
                intent.putStringArrayListExtra(FgConst.SELECTED_TRANSACTIONS_IDS, getIntent().getStringArrayListExtra(FgConst.SELECTED_TRANSACTIONS_IDS));
                setResult(RESULT_OK, intent);
                finish();
                break;
            case RequestCodes.REQUEST_CODE_BULK_SELECT_MODEL:
            default:
                break;
        }
    }
}
