package com.yoshione.fingen.classes;

import java.util.List;

/**
 * Created by Leonid on 04.09.2016.
 * a
 */
public class TableInfo {
    private final String mTableName;
    private final String mTableCreateSQL;
    private final String mTableFields;

    public TableInfo(String tableName, String tableCreateSQL, String tableFields) {
        mTableName = tableName;
        mTableCreateSQL = tableCreateSQL;
        mTableFields = tableFields;
    }

    public String getTableName() {
        return mTableName;
    }

    public String getTableCreateSQL() {
        return mTableCreateSQL;
    }

    public String getTableFields() {
        return mTableFields;
    }


//    public String getFieldsAsString(boolean addTableNames, List<String> exclude) {
//        String result = "";
//        for (String field : mTableFields) {
//            if (exclude.indexOf(field) < 0) {
//                if (result.isEmpty()) {
//                    result = (addTableNames ? mTableName + "." : "") + field;
//                } else {
//                    result = result + ", " + (addTableNames ? mTableName + "." : "") + field;
//                }
//            }
//        }
//        return result;
//    }
}
