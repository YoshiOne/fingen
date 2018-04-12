package com.yoshione.fingen.dao;

import android.content.Context;
import android.database.Cursor;
import android.util.Pair;


import com.yoshione.fingen.DBHelper;
import com.yoshione.fingen.interfaces.IDaoInheritor;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.model.Account;
import com.yoshione.fingen.model.Category;
import com.yoshione.fingen.model.Department;
import com.yoshione.fingen.model.Location;
import com.yoshione.fingen.model.Payee;
import com.yoshione.fingen.model.Project;
import com.yoshione.fingen.model.Template;
import com.yoshione.fingen.model.Transaction;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

/**
 * Created by slv on 14.08.2015.
 *
 */
public class TemplatesDAO extends BaseDAO implements AbstractDAO, IDaoInheritor {
    public static final String TAG = "TemplatesDAO";
    private static TemplatesDAO sInstance;

    private TemplatesDAO(Context context) {
        super(context, DBHelper.T_LOG_TEMPLATES, IAbstractModel.MODEL_TYPE_TEMPLATE , DBHelper.T_LOG_TEMPLATES_ALL_COLUMNS);
        super.setDaoInheritor(this);
    }

    public synchronized static TemplatesDAO getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new TemplatesDAO(context);
        }
        return sInstance;
    }

    @Override
    public IAbstractModel cursorToModel(Cursor cursor) {
        return cursorToTemplate(cursor);
    }

    private Template cursorToTemplate(Cursor cursor) {
        Template template = new Template();

        template.setID(cursor.getLong(mColumnIndexes.get(DBHelper.C_ID)));
        if (!cursor.isNull(mColumnIndexes.get(DBHelper.C_LOG_TEMPLATES_SRCACCOUNT))) {
            template.setAccountID(cursor.getLong(mColumnIndexes.get(DBHelper.C_LOG_TEMPLATES_SRCACCOUNT)));
        } else {
            template.setAccountID(-1);
        };
        if (!cursor.isNull(mColumnIndexes.get(DBHelper.C_LOG_TEMPLATES_PAYEE))) {
            template.setPayeeID(cursor.getLong(mColumnIndexes.get(DBHelper.C_LOG_TEMPLATES_PAYEE)));
        } else {
            template.setPayeeID(-1);
        };
        if (!cursor.isNull(mColumnIndexes.get(DBHelper.C_LOG_TEMPLATES_CATEGORY))) {
            template.setCategoryID(cursor.getLong(mColumnIndexes.get(DBHelper.C_LOG_TEMPLATES_CATEGORY)));
        } else {
            template.setCategoryID(-1);
        };
        if (!cursor.isNull(mColumnIndexes.get(DBHelper.C_LOG_TEMPLATES_AMOUNT))) {
            template.setAmount(new BigDecimal(cursor.getDouble(mColumnIndexes.get(DBHelper.C_LOG_TEMPLATES_AMOUNT))));
        } else {
            template.setAmount(BigDecimal.ZERO);
        };
        if (!cursor.isNull(mColumnIndexes.get(DBHelper.C_LOG_TEMPLATES_PROJECT))) {
            template.setProjectID(cursor.getLong(mColumnIndexes.get(DBHelper.C_LOG_TEMPLATES_PROJECT)));
        } else {
            template.setProjectID(-1);
        };
        if (!cursor.isNull(mColumnIndexes.get(DBHelper.C_LOG_TEMPLATES_DEPARTMENT))) {
            template.setDepartmentID(cursor.getLong(mColumnIndexes.get(DBHelper.C_LOG_TEMPLATES_DEPARTMENT)));
        } else {
            template.setDepartmentID(-1);
        };
        if (!cursor.isNull(mColumnIndexes.get(DBHelper.C_LOG_TEMPLATES_LOCATION))) {
            template.setLocationID(cursor.getLong(mColumnIndexes.get(DBHelper.C_LOG_TEMPLATES_LOCATION)));
        } else {
            template.setLocationID(-1);
        };
        template.setName(cursor.getString(mColumnIndexes.get(DBHelper.C_LOG_TEMPLATES_NAME)));

        if (!cursor.isNull(mColumnIndexes.get(DBHelper.C_LOG_TEMPLATES_COMMENT))) {
            template.setComment(cursor.getString(mColumnIndexes.get(DBHelper.C_LOG_TEMPLATES_COMMENT)));
        } else {
            template.setComment("");
        }

        template.setDestAccountID(cursor.getLong(mColumnIndexes.get(DBHelper.C_LOG_TEMPLATES_DESTACCOUNT)));

        if (!cursor.isNull(mColumnIndexes.get(DBHelper.C_LOG_TEMPLATES_TYPE))) {
            int type = cursor.getInt(mColumnIndexes.get(DBHelper.C_LOG_TEMPLATES_TYPE));
            if (type < -1 || type > 1) {
                template.setTrType(Transaction.TRANSACTION_TYPE_EXPENSE);
            } else {
                template.setTrType(type);
            }
        } else {
            template.setTrType(Transaction.TRANSACTION_TYPE_EXPENSE);
        }

        template = (Template) DBHelper.getSyncDataFromCursor(template, cursor, mColumnIndexes);

        return template;
    }

    @SuppressWarnings("unchecked")
    public List<Template> getAllTemplates() throws Exception {
        return (List<Template>) getItems(getTableName(), null, null,
                null, DBHelper.C_LOG_TEMPLATES_NAME, null);
    }

    @Override
    public List<?> getAllModels() throws Exception {
        return getAllTemplates();
    }
}
