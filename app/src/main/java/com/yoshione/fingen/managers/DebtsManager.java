package com.yoshione.fingen.managers;

import android.content.Context;

import com.yoshione.fingen.dao.AccountsDAO;
import com.yoshione.fingen.dao.CategoriesDAO;
import com.yoshione.fingen.dao.PayeesDAO;
import com.yoshione.fingen.model.Account;
import com.yoshione.fingen.model.Category;
import com.yoshione.fingen.model.Credit;
import com.yoshione.fingen.model.Payee;

import java.math.BigDecimal;

/**
 * Created by slv on 03.03.2016.
 *
 */
public class DebtsManager {

    public static Account getAccount(Credit credit, Context context){
        AccountsDAO accountsDAO = AccountsDAO.getInstance(context);
        return accountsDAO.getAccountByID(credit.getAccountID());
    }

    public static Category getCategory(Credit credit, Context context){
        CategoriesDAO categoriesDAO = CategoriesDAO.getInstance(context);
        return categoriesDAO.getCategoryByID(credit.getCategoryID());
    }

    public static Payee getPayee(Credit credit, Context context){
        PayeesDAO payeesDAO = PayeesDAO.getInstance(context);
        return payeesDAO.getPayeeByID(credit.getPayeeID());
    }

    public static BigDecimal getSum(Credit credit, Context context){
        AccountsDAO accountsDAO = AccountsDAO.getInstance(context);
        return accountsDAO.getAccountByID(credit.getAccountID()).getCurrentBalance();
    }
}
