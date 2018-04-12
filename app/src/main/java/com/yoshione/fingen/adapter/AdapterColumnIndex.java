package com.yoshione.fingen.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import com.yoshione.fingen.R;
import com.yoshione.fingen.model.EntityToFieldLink;
import com.yoshione.fingen.utils.ImportParams;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by slv on 18.05.2016.
 */
public class AdapterColumnIndex extends RecyclerView.Adapter {


    private final List<EntityToFieldLink> mEntityToFieldLinkList;
    private final List<String> mColumns;
    private final Context mContext;

    public AdapterColumnIndex(List<EntityToFieldLink> entityToFieldLinkList, List<String> columns, Context context) {
        mEntityToFieldLinkList = entityToFieldLinkList;
        mColumns = columns;
        mContext = context;

        for (EntityToFieldLink entityToFieldLink : entityToFieldLinkList) {
            entityToFieldLink.setField(tryDetectField(entityToFieldLink));
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_item_column_index, parent, false);

        vh = new ColumnIndexViewHolder(v);

        return vh;
    }

    private String tryDetectField(EntityToFieldLink entityToFieldLink) {
        String field = "-";
        String entities[] = getEntityEnName(entityToFieldLink);
        for (String entity : entities) {
            for (String column : mColumns) {
                if (column.toLowerCase().trim().contains(entity)) {
                    field = column;
                    break;
                }
            }
        }
        return field;
    }

    private String[] getEntityEnName(EntityToFieldLink entityToFieldLink) {
        String entity[] = new String[]{};
        switch (entityToFieldLink.getType()) {
            case EntityToFieldLink.ENTITY_TYPE_DATE:
                entity = new String[]{"date", "дата"};
                break;
            case EntityToFieldLink.ENTITY_TYPE_TIME:
                entity = new String[]{"time", "время"};
                break;
            case EntityToFieldLink.ENTITY_TYPE_ACCOUNT:
                entity = new String[]{"account", "счет"};
                break;
            case EntityToFieldLink.ENTITY_TYPE_CURRENCY:
                entity = new String[]{"currency", "валюта"};
                break;
            case EntityToFieldLink.ENTITY_TYPE_PAYEE:
                entity = new String[]{"payee", "контрагент", "получатель"};
                break;
            case EntityToFieldLink.ENTITY_TYPE_CATEGORY:
                entity = new String[]{"category", "категория", "статья"};
                break;
            case EntityToFieldLink.ENTITY_TYPE_AMOUNT:
                entity = new String[]{"amount", "сумма"};
                break;
            case EntityToFieldLink.ENTITY_TYPE_LOCATION:
                entity = new String[]{"location", "местоположение", "место"};
                break;
            case EntityToFieldLink.ENTITY_TYPE_PROJECT:
                entity = new String[]{"project", "проект"};
                break;
            case EntityToFieldLink.ENTITY_TYPE_DEPARTMENT:
                entity = new String[]{"department", "подразделение"};
                break;
            case EntityToFieldLink.ENTITY_TYPE_COMMENT:
                entity = new String[]{"comment", "note", "комментарий", "примечание"};
                break;
        }
        return entity;
    }

    private String getEntityName(EntityToFieldLink entityToFieldLink) {
        String entity = "";
        switch (entityToFieldLink.getType()) {
            case EntityToFieldLink.ENTITY_TYPE_DATE:
                entity = mContext.getString(R.string.ent_date);
                break;
            case EntityToFieldLink.ENTITY_TYPE_TIME:
                entity = mContext.getString(R.string.ent_time);
                break;
            case EntityToFieldLink.ENTITY_TYPE_ACCOUNT:
                entity = mContext.getString(R.string.ent_account);
                break;
            case EntityToFieldLink.ENTITY_TYPE_CURRENCY:
                entity = mContext.getString(R.string.ent_currency);
                break;
            case EntityToFieldLink.ENTITY_TYPE_PAYEE:
                entity = mContext.getString(R.string.ent_payee_or_payer);
                break;
            case EntityToFieldLink.ENTITY_TYPE_CATEGORY:
                entity = mContext.getString(R.string.ent_category);
                break;
            case EntityToFieldLink.ENTITY_TYPE_AMOUNT:
                entity = mContext.getString(R.string.ent_amount);
                break;
            case EntityToFieldLink.ENTITY_TYPE_LOCATION:
                entity = mContext.getString(R.string.ent_location);
                break;
            case EntityToFieldLink.ENTITY_TYPE_PROJECT:
                entity = mContext.getString(R.string.ent_project);
                break;
            case EntityToFieldLink.ENTITY_TYPE_DEPARTMENT:
                entity = mContext.getString(R.string.ent_department);
                break;
            case EntityToFieldLink.ENTITY_TYPE_COMMENT:
                entity = mContext.getString(R.string.ent_comment);
                break;
        }
        return entity;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final ColumnIndexViewHolder vh = (ColumnIndexViewHolder) holder;
        final EntityToFieldLink entityToFieldLink = mEntityToFieldLinkList.get(holder.getAdapterPosition());

        final String entity = getEntityName(entityToFieldLink);
        vh.mSpinnerIndex.setHint(entity);
        vh.mTextInputLayoutSpinnerIndex.setHint(entity);

        vh.mSpinnerIndex.setText(entityToFieldLink.getField());
        vh.mSpinnerIndex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builderSingle = new AlertDialog.Builder(mContext);
                final ArrayAdapter<String> arrayAdapterTypes = new ArrayAdapter<>(mContext, android.R.layout.select_dialog_singlechoice, mColumns);

                builderSingle.setSingleChoiceItems(arrayAdapterTypes, mColumns.indexOf(entityToFieldLink.getField()),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, final int which) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        dialog.cancel();
                                        mEntityToFieldLinkList.get(holder.getAdapterPosition()).setField(mColumns.get(which));
                                        vh.mSpinnerIndex.setText(mColumns.get(which));
                                    }
                                }, 200);

                            }
                        });
                builderSingle.setTitle(entity);

                builderSingle.setNegativeButton(mContext.getResources().getString(android.R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                builderSingle.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mEntityToFieldLinkList.size();
    }

    public ImportParams getImportParams() {
        int date = -1;//
        int time = -1;//
        int account = -1;//
        int amount = -1;
        int currency = -1;//
        int category = -1;//
        int payee = -1;//
        int location = -1;
        int project = -1;
        int department = -1;
        int comment = -1;//

        for (EntityToFieldLink link : mEntityToFieldLinkList) {
            int ind = mColumns.indexOf(link.getField()) - 1;
            switch (link.getType()) {
                case EntityToFieldLink.ENTITY_TYPE_DATE:
                    date = ind;
                    break;
                case EntityToFieldLink.ENTITY_TYPE_TIME:
                    time = ind;
                    break;
                case EntityToFieldLink.ENTITY_TYPE_ACCOUNT:
                    account = ind;
                    break;
                case EntityToFieldLink.ENTITY_TYPE_CURRENCY:
                    currency = ind;
                    break;
                case EntityToFieldLink.ENTITY_TYPE_PAYEE:
                    payee = ind;
                    break;
                case EntityToFieldLink.ENTITY_TYPE_CATEGORY:
                    category = ind;
                    break;
                case EntityToFieldLink.ENTITY_TYPE_AMOUNT:
                    amount = ind;
                    break;
                case EntityToFieldLink.ENTITY_TYPE_LOCATION:
                    location = ind;
                    break;
                case EntityToFieldLink.ENTITY_TYPE_PROJECT:
                    project = ind;
                    break;
                case EntityToFieldLink.ENTITY_TYPE_DEPARTMENT:
                    department = ind;
                    break;
                case EntityToFieldLink.ENTITY_TYPE_COMMENT:
                    comment = ind;
                    break;
            }
        }

        return new ImportParams(date, time, account, amount, currency, category, payee, location, project, department, comment, -1, true);
    }

    static class ColumnIndexViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.spinnerIndex)
        EditText mSpinnerIndex;
        @BindView(R.id.textInputLayoutSpinnerIndex)
        TextInputLayout mTextInputLayoutSpinnerIndex;

        ColumnIndexViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
