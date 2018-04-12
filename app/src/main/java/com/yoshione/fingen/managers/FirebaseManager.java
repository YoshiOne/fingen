package com.yoshione.fingen.managers;

import com.yoshione.fingen.interfaces.IAbstractModel;

/**
 * Created by slv on 26.08.2016.
 *
 */

public final class FirebaseManager {

    public static String getNodeNameByType(int modelType) {
        switch (modelType) {
            case IAbstractModel.MODEL_TYPE_CABBAGE:
                return "currencies";
            case IAbstractModel.MODEL_TYPE_LOCATION:
                return "locations";
            case IAbstractModel.MODEL_TYPE_PAYEE:
                return "payees";
            case IAbstractModel.MODEL_TYPE_PROJECT:
                return "projects";
            case IAbstractModel.MODEL_TYPE_SMSMARKER:
                return "smsmarkers";
            case IAbstractModel.MODEL_TYPE_ACCOUNT:
                return "accounts";
            case IAbstractModel.MODEL_TYPE_CATEGORY:
                return "categories";
            case IAbstractModel.MODEL_TYPE_SMS:
                return "sms";
            case IAbstractModel.MODEL_TYPE_TRANSACTION:
                return "transactions";
            case IAbstractModel.MODEL_TYPE_CREDIT:
                return "credits";
            case IAbstractModel.MODEL_TYPE_TEMPLATE:
                return "templates";
            case IAbstractModel.MODEL_TYPE_DEPARTMENT:
                return "departments";
            case IAbstractModel.MODEL_TYPE_SIMPLEDEBT:
                return "simpledebts";
            case IAbstractModel.MODEL_TYPE_SENDER:
                return "senders";
            case IAbstractModel.MODEL_TYPE_BUDGET:
                return "budget_cat";
            case IAbstractModel.MODEL_TYPE_BUDGET_DEBT:
                return "budget_debt";
        }
        return "";
    }

//    public static boolean isModelSyncAllowed(IAbstractModel model) {
//        return model.getClass().equals(Payee.class) |
//                model.getClass().equals(Category.class) |
//                model.getClass().equals(Account.class) |
//                model.getClass().equals(Project.class) |
//                model.getClass().equals(Department.class) |
//                model.getClass().equals(Location.class) |
//                model.getClass().equals(SimpleDebt.class) |
//                model.getClass().equals(Credit.class) |
//                model.getClass().equals(Transaction.class) |
//                model.getClass().equals(Template.class) |
//                model.getClass().equals(Cabbage.class);
//    }
}
