package com.yoshione.fingen;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.yoshione.fingen.adapter.AdapterTreeModel;
import com.yoshione.fingen.adapter.helper.OnStartDragListener;
import com.yoshione.fingen.adapter.helper.SimpleItemTouchHelperCallback;
import com.yoshione.fingen.dao.AbstractDAO;
import com.yoshione.fingen.dao.BaseDAO;
import com.yoshione.fingen.dao.CategoriesDAO;
import com.yoshione.fingen.dao.ProjectsDAO;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.interfaces.IAdapterEventsListener;
import com.yoshione.fingen.interfaces.IUpdateTreeListsEvents;
import com.yoshione.fingen.managers.AbstractModelManager;
import com.yoshione.fingen.managers.CabbageManager;
import com.yoshione.fingen.managers.FilterManager;
import com.yoshione.fingen.managers.SenderManager;
import com.yoshione.fingen.managers.TreeManager;
import com.yoshione.fingen.model.BaseModel;
import com.yoshione.fingen.model.Cabbage;
import com.yoshione.fingen.model.Category;
import com.yoshione.fingen.model.Events;
import com.yoshione.fingen.model.Location;
import com.yoshione.fingen.model.Project;
import com.yoshione.fingen.model.Sender;
import com.yoshione.fingen.utils.BaseNode;
import com.yoshione.fingen.widgets.ContextMenuRecyclerView;
import com.yoshione.fingen.widgets.FgLinearLayoutManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentTreeModelList extends Fragment implements OnStartDragListener, IUpdateTreeListsEvents {

    @BindView(R.id.recycler_view)
    ContextMenuRecyclerView mRecyclerView;
    @BindView(R.id.fabMenu)
    FloatingActionMenu fabMenu;

    AdapterTreeModel adapter;
    @BindView(R.id.fabAddModel)
    FloatingActionButton fabAddModel;
    @BindView(R.id.fabAddNestedModel)
    FloatingActionButton fabAddNestedModel;
    Unbinder unbinder;
    private ItemTouchHelper mItemTouchHelper;
    private String mFilter = "";
    private IAbstractModel mInputModel;
    private IAdapterEventsListener mAdapterEventsListener;
    private boolean mAddUndefined;

    public static FragmentTreeModelList newInstance(IAbstractModel model, HashSet<Long> checkedIDs, int viewMode) {
        FragmentTreeModelList frag = new FragmentTreeModelList();
        Bundle args = new Bundle();
        args.putParcelable("model", model);
        args.putSerializable("checked_ids", checkedIDs);
        args.putInt("viewMode", viewMode);
        frag.setArguments(args);

        return frag;
    }

    void setmFilter(String mFilter) {
        this.mFilter = mFilter;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mAdapterEventsListener = (ActivityList) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnArticleSelectedListener");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tree_model_list, container, false);
        unbinder = ButterKnife.bind(this, view);

        mInputModel = Objects.requireNonNull(getArguments()).getParcelable("model");

        FabMenuItemClickListener fabMenuItemClickListener = new FabMenuItemClickListener();
        fabAddModel.setImageDrawable(Objects.requireNonNull(getActivity()).getDrawable(R.drawable.ic_category_white));
        fabAddModel.setOnClickListener(fabMenuItemClickListener);
        switch (mInputModel.getModelType()) {
            case IAbstractModel.MODEL_TYPE_CABBAGE:
            case IAbstractModel.MODEL_TYPE_SENDER:
                fabAddNestedModel.setVisibility(View.GONE);
                break;
            default:
                fabAddNestedModel.setImageDrawable(getActivity().getDrawable(R.drawable.ic_categories_white));
                fabAddNestedModel.setOnClickListener(fabMenuItemClickListener);
        }
        fabMenu.setClosedOnTouchOutside(true);

        adapter = new AdapterTreeModel(getActivity(), this, this);
        adapter.setmAdapterEventsListener(mAdapterEventsListener);
        adapter.setHasStableIds(true);

        adapter.setViewMode(getArguments().getInt("viewMode"));
        mAddUndefined = adapter.getViewMode() == AdapterTreeModel.MODE_MULTICHOICE;

        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setHasFixedSize(false);

        FgLinearLayoutManager mLayoutManager = new FgLinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
//        mLayoutManager.setAutoMeasureEnabled(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        registerForContextMenu(mRecyclerView);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (Math.abs(dy) > 4) {
                    if (dy > 0) {
                        fabMenu.hideMenu(true);
                    } else {
                        fabMenu.showMenu(true);
                    }
                }
            }
        });

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);

        updateRecyclerView();

        switch (adapter.getViewMode()) {
            case AdapterTreeModel.MODE_SINGLECHOICE:
                if (mInputModel.getID() >= 0) {
                    int pos;
                    try {
                        pos = adapter.getTree().getFlatPos(adapter.getTree().getNodeById(mInputModel.getID()));
                        mRecyclerView.scrollToPosition(Math.max(0, pos - 1));
                        adapter.getItemAtPos(pos).getModel().setSelected(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case AdapterTreeModel.MODE_MULTICHOICE:
                HashSet<Long> checkedIDs = (HashSet<Long>) getArguments().getSerializable("checked_ids");
                if (checkedIDs == null) break;
                for (Long id : checkedIDs) {
                    try {
                        adapter.getTree().getNodeById(id).getModel().setSelected(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                adapter.notifyDataSetChanged();
                break;
        }

        return view;
    }

    @Override
    public void update() {
        updateRecyclerView();
    }

    @SuppressWarnings("unchecked")
    private void loadData() throws Exception {
        AbstractDAO dao = BaseDAO.getDAO(mInputModel.getModelType(), getActivity());
        if (dao == null) return;
        BaseNode tree = TreeManager.convertListToTree((List<IAbstractModel>) dao.getAllModels(), mInputModel.getModelType());
        if (mAddUndefined) {
            BaseNode undefined = new BaseNode(BaseModel.createModelByType(mInputModel.getModelType()), tree);
            undefined.getModel().setName("-");
            tree.getChildren().add(0, undefined);
        }
        if (!mFilter.isEmpty()) {
            tree.applyFilter(mFilter);
        }
        adapter.setTree(tree);
        adapter.notifyDataSetChanged();
    }

    void updateRecyclerView() {
        try {
            loadData();
        } catch (Exception e) {
            //get items error
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        if (adapter.getViewMode() == AdapterTreeModel.MODE_VIEW) {
            mItemTouchHelper.startDrag(viewHolder);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater menuInflater = Objects.requireNonNull(getActivity()).getMenuInflater();
        switch (mInputModel.getModelType()) {
            case IAbstractModel.MODEL_TYPE_CATEGORY:
            case IAbstractModel.MODEL_TYPE_PROJECT:
                menuInflater.inflate(R.menu.context_menu_categories, menu);
                break;
            case IAbstractModel.MODEL_TYPE_CABBAGE:
                menuInflater.inflate(R.menu.context_menu_cabbeges, menu);
                break;
            case IAbstractModel.MODEL_TYPE_SENDER:
                menuInflater.inflate(R.menu.context_menu_models, menu);
                menu.findItem(R.id.action_show_transactions).setVisible(false);
                break;
            default:
                menuInflater.inflate(R.menu.context_menu_tree_model, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        ContextMenuRecyclerView.RecyclerContextMenuInfo info = (ContextMenuRecyclerView.RecyclerContextMenuInfo) item.getMenuInfo();
        final AbstractDAO dao = BaseDAO.getDAO(mInputModel.getModelType(), getActivity());
        if (dao == null) return false;
        switch (item.getItemId()) {
            case R.id.action_add_child: {
                IAbstractModel parent = dao.getModelById(info.id);
                IAbstractModel child = BaseModel.createModelByType(mInputModel.getModelType());
                child.setParentID(parent.getID());
                switch (mInputModel.getModelType()) {
                    case IAbstractModel.MODEL_TYPE_CATEGORY:
                    case IAbstractModel.MODEL_TYPE_PAYEE:
                    case IAbstractModel.MODEL_TYPE_PROJECT:
                    case IAbstractModel.MODEL_TYPE_DEPARTMENT:
                        AbstractModelManager.editNestedModelNameViaDialog(child, getActivity(), null);
                        break;
                    case IAbstractModel.MODEL_TYPE_LOCATION:
                        Intent intent = new Intent(getActivity(), ActivityEditLocation.class);
                        intent.putExtra("location", child);
                        startActivity(intent);
                        break;
                }
                break;
            }
            case R.id.action_edit: {
                IAbstractModel model = dao.getModelById(info.id);
                switch (mInputModel.getModelType()) {
                    case IAbstractModel.MODEL_TYPE_CATEGORY:
                    case IAbstractModel.MODEL_TYPE_PAYEE:
                    case IAbstractModel.MODEL_TYPE_PROJECT:
                    case IAbstractModel.MODEL_TYPE_DEPARTMENT:
                        AbstractModelManager.editNestedModelNameViaDialog(model, getActivity(), null);
                        break;
                    case IAbstractModel.MODEL_TYPE_LOCATION:
                        Intent intent = new Intent(getActivity(), ActivityEditLocation.class);
                        intent.putExtra("location", model);
                        startActivity(intent);
                        break;
                    case IAbstractModel.MODEL_TYPE_CABBAGE:
                        CabbageManager.showEditdialog((Cabbage) model, Objects.requireNonNull(getActivity()).getSupportFragmentManager(), getActivity());
                        break;
                    case IAbstractModel.MODEL_TYPE_SENDER:
                        SenderManager.showEditDialog((Sender) model, Objects.requireNonNull(getActivity()).getSupportFragmentManager(), getActivity());
                        break;
                }
                break;
            }
            case R.id.action_convert_to_child: {
                IAbstractModel model = dao.getModelById(info.id);
                OnSelectParentModelListener listener = new OnSelectParentModelListener(model);
                AbstractModelManager.ShowSelectNestedModelDialog(
                        Objects.requireNonNull(getActivity()).getResources().getString(R.string.ttl_select_parent_item),
                        getActivity(),
                        adapter.getTree(),
                        adapter.getItemAtPos(info.position),
                        listener);
                break;
            }
            case R.id.action_select_color: {
                final IAbstractModel model = dao.getModelById(info.id);
                ColorPickerDialogBuilder
                        .with(Objects.requireNonNull(getActivity()))
                        .setTitle(getActivity().getResources().getString(R.string.ttl_select_color))
                        .initialColor(model.getColor())
                        .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
                        .lightnessSliderOnly()
                        .density(12)
                        .setPositiveButton("ok", new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                IAbstractModel model1 = dao.getModelById(model.getID());
                                if (model.getClass().equals(Category.class)) {
                                    ((Category)model1).setColor(selectedColor);
                                    try {
                                        CategoriesDAO categoriesDAO1 = CategoriesDAO.getInstance(FragmentTreeModelList.this.getActivity());
                                        categoriesDAO1.createCategory((Category) model1, getActivity());
                                    } catch (Exception e) {
                                        Toast.makeText(getActivity(), R.string.msg_error_on_write_to_db, Toast.LENGTH_SHORT).show();
                                    }
                                } else if (model.getClass().equals(Project.class)) {
                                    ((Project) model1).setColor(selectedColor);
                                    try {
                                        ProjectsDAO projectsDAO = ProjectsDAO.getInstance(FragmentTreeModelList.this.getActivity());
                                        projectsDAO.createProject((Project) model1, getActivity());
                                    } catch (Exception e) {
                                        Toast.makeText(getActivity(), R.string.msg_error_on_write_to_db, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .build()
                        .show();
                break;
            }
            case R.id.action_sort: {
                AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
                builder.setTitle(R.string.ttl_confirm_action);
                builder.setMessage(R.string.ttl_sort_items_confirmation);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog1, int which1) {
                        adapter.getTree().sort();
                        adapter.notifyDataSetChanged();
                        CategoriesDAO.getInstance(FragmentTreeModelList.this.getActivity()).updateOrder(adapter.getTree().getOrderList());
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
                break;
            }
            case R.id.action_delete: {
                AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
                builder.setTitle(R.string.ttl_confirm_action);
                builder.setMessage(R.string.ttl_delete_item_confirm);

                List<IAbstractModel> models = new ArrayList<>();
                for (BaseNode node : adapter.getItemAtPos(info.position).getFlatChildrenList()) {
                    models.add(node.getModel());
                }

                models.add(adapter.getItemAtPos(info.position).getModel());

                OnDeleteItemDialogOkClickListener clickListener = new OnDeleteItemDialogOkClickListener(models);
                // Set up the buttons
                builder.setPositiveButton("OK", clickListener);
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
                break;
            }
            case R.id.action_show_transactions:
                IAbstractModel model = Objects.requireNonNull(BaseDAO.getDAO(mInputModel.getModelType(), getActivity())).getModelById(info.id);
                Intent intent = new Intent(getActivity(), ActivityTransactions.class);
                intent.putParcelableArrayListExtra("filter_list", FilterManager.createFilterList(mInputModel.getModelType(), info.id));
                intent.putExtra("caption", model.getFullName());
                intent.putExtra(FgConst.HIDE_FAB, true);
                intent.putExtra(FgConst.LOCK_SLIDINGUP_PANEL, true);
                startActivity(intent);
                break;
        }
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(Events.EventOnModelChanged event) {
        switch (event.getModelType()) {
            case IAbstractModel.MODEL_TYPE_CATEGORY:
            case IAbstractModel.MODEL_TYPE_PAYEE:
            case IAbstractModel.MODEL_TYPE_PROJECT:
            case IAbstractModel.MODEL_TYPE_DEPARTMENT:
            case IAbstractModel.MODEL_TYPE_LOCATION:
            case IAbstractModel.MODEL_TYPE_CABBAGE:
            case IAbstractModel.MODEL_TYPE_SENDER:
                updateRecyclerView();
        }
    }

    private class FabMenuItemClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.fabAddModel:
                    switch (mInputModel.getModelType()) {
                        case IAbstractModel.MODEL_TYPE_CATEGORY:
                        case IAbstractModel.MODEL_TYPE_PAYEE:
                        case IAbstractModel.MODEL_TYPE_PROJECT:
                        case IAbstractModel.MODEL_TYPE_DEPARTMENT:
                            AbstractModelManager.editNestedModelNameViaDialog(BaseModel.createModelByType(mInputModel.getModelType()),
                                    getActivity(), null);
                            break;
                        case IAbstractModel.MODEL_TYPE_CABBAGE:
                            CabbageManager.showEditdialog(new Cabbage(), Objects.requireNonNull(getActivity()).getSupportFragmentManager(), getActivity());
                            break;
                        case IAbstractModel.MODEL_TYPE_SENDER:
                            SenderManager.showEditDialog(new Sender(), Objects.requireNonNull(getActivity()).getSupportFragmentManager(), getActivity());
                            break;
                        case IAbstractModel.MODEL_TYPE_LOCATION:
                            Intent intent = new Intent(getActivity(), ActivityEditLocation.class);
                            intent.putExtra("location", new Location());
                            startActivity(intent);
                            break;
                    }
                    break;
                case R.id.fabAddNestedModel:
                    switch (mInputModel.getModelType()) {
                        case IAbstractModel.MODEL_TYPE_CATEGORY:
                        case IAbstractModel.MODEL_TYPE_PAYEE:
                        case IAbstractModel.MODEL_TYPE_PROJECT:
                        case IAbstractModel.MODEL_TYPE_DEPARTMENT:
                            AbstractModelManager.ShowSelectNestedModelDialog(Objects.requireNonNull(getActivity()).getResources().getString(R.string.ttl_select_parent_item),
                                    getActivity(), adapter.getTree(), null, new AbstractModelManager.EditDialogEventsListener() {
                                        @Override
                                        public void OnOkClick(IAbstractModel parent) {
                                            IAbstractModel model = BaseModel.createModelByType(mInputModel.getModelType());
                                            model.setParentID(parent.getID());
                                            AbstractModelManager.editNestedModelNameViaDialog(model,
                                                    getActivity(), null);
                                        }
                                    });
                            break;
                        case IAbstractModel.MODEL_TYPE_LOCATION:
                            AbstractModelManager.ShowSelectNestedModelDialog(Objects.requireNonNull(getActivity()).getResources().getString(R.string.ttl_select_parent_item),
                                    getActivity(), adapter.getTree(), null, new AbstractModelManager.EditDialogEventsListener() {
                                        @Override
                                        public void OnOkClick(IAbstractModel parent) {
                                            Location location = new Location();
                                            location.setParentID(parent.getID());
                                            Intent intent = new Intent(getActivity(), ActivityEditLocation.class);
                                            intent.putExtra("location", location);
                                            startActivity(intent);
                                        }
                                    });
                    }
                    break;
            }
            fabMenu.close(false);
        }
    }

    private class OnDeleteItemDialogOkClickListener implements DialogInterface.OnClickListener {
        final List<IAbstractModel> mModels;

        OnDeleteItemDialogOkClickListener(List<IAbstractModel> models) {
            mModels = models;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            AbstractDAO dao = BaseDAO.getDAO(mInputModel.getModelType(), getActivity());
            if (dao != null) {
                dao.bulkDeleteModel(mModels, true);
            }
        }
    }

    private class OnSelectParentModelListener implements AbstractModelManager.EditDialogEventsListener {
        private final IAbstractModel mModel;

        OnSelectParentModelListener(IAbstractModel model) {
            mModel = model;
        }

        @Override
        public void OnOkClick(IAbstractModel selectedModel) {
            mModel.setParentID(selectedModel.getID());

            try {
                if (mModel.getClass().equals(Category.class)) {
                    CategoriesDAO.getInstance(getActivity()).createCategory((Category) mModel, getActivity());
                } else if (mModel.getClass().equals(Project.class)) {
                    ProjectsDAO.getInstance(getActivity()).createProject((Project) mModel, getActivity());
                } else {
                    AbstractDAO dao = BaseDAO.getDAO(mModel.getClass(), getActivity());
                    if (dao != null) {
                        dao.createModel(mModel);
                    }
                }

            } catch (Exception e) {
                Toast.makeText(getActivity(), R.string.msg_error_on_write_to_db, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
