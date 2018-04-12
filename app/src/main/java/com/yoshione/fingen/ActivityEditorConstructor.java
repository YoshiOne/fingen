package com.yoshione.fingen;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.widget.Button;

import com.yoshione.fingen.adapter.AdapterEditorConstructor;
import com.yoshione.fingen.adapter.helper.OnStartDragListener;
import com.yoshione.fingen.adapter.helper.SimpleItemTouchHelperCallback;
import com.yoshione.fingen.model.EditorConstructorItem;
import com.yoshione.fingen.widgets.ToolbarActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class ActivityEditorConstructor extends ToolbarActivity {

    AdapterEditorConstructor mAdapter;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.buttonSave)
    Button mButtonSave;
    private ItemTouchHelper mItemTouchHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        List<EditorConstructorItem> list = new ArrayList<>();
        list.add(new EditorConstructorItem(getString(R.string.ent_date_time), AdapterEditorConstructor.ID_DATE_TIME, 1, true, false, true, true));
        list.add(new EditorConstructorItem(getString(R.string.ent_account), AdapterEditorConstructor.ID_ACCOUNT, 2, true, true, true, true));
        list.add(new EditorConstructorItem(getString(R.string.ent_payee_dest_account), AdapterEditorConstructor.ID_PAYEE_DESTACCOUNT, 3, true, true, true, true));
        list.add(new EditorConstructorItem(getString(R.string.ent_category), AdapterEditorConstructor.ID_CATEGORY, 4, true, false, true, false));
        list.add(new EditorConstructorItem(getString(R.string.ent_amount), AdapterEditorConstructor.ID_AMOUNT, 5, true, true, true, true));
        list.add(new EditorConstructorItem(getString(R.string.ent_sms_body), AdapterEditorConstructor.ID_SMS, 6, true, true, true, true));
        list.add(new EditorConstructorItem(getString(R.string.ent_project), AdapterEditorConstructor.ID_PROJECT, 7, true, false, true, false));
        list.add(new EditorConstructorItem(getString(R.string.ent_debt), AdapterEditorConstructor.ID_SIMPLEDEBT, 8, true, false, true, false));
        list.add(new EditorConstructorItem(getString(R.string.ent_department), AdapterEditorConstructor.ID_DEPARTMENT, 9, true, false, true, false));
        list.add(new EditorConstructorItem(getString(R.string.ent_location), AdapterEditorConstructor.ID_LOCATION, 10, true, false, true, false));
        list.add(new EditorConstructorItem(getString(R.string.ent_comment), AdapterEditorConstructor.ID_COMMENT, 10, true, false, true, false));

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        mAdapter = new AdapterEditorConstructor(list, new OnStartDragListener() {
            @Override
            public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
                mItemTouchHelper.startDrag(viewHolder);
            }
        }, this);
        mAdapter.setHasStableIds(true);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_editor_constructor;
    }

    @Override
    protected String getLayoutTitle() {
        return getString(R.string.ent_editor_constructor);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.action_go_home).setVisible(false);
        return true;
    }

    @OnClick(R.id.buttonSave)
    public void onViewClicked() {
    }
}
