package com.yoshione.fingen.di.modules;

import android.content.Context;

import com.yoshione.fingen.dao.AccountsDAO;
import com.yoshione.fingen.dao.BudgetCreditsDAO;
import com.yoshione.fingen.dao.BudgetDAO;
import com.yoshione.fingen.dao.CabbagesDAO;
import com.yoshione.fingen.dao.CategoriesDAO;
import com.yoshione.fingen.dao.CreditsDAO;
import com.yoshione.fingen.dao.DepartmentsDAO;
import com.yoshione.fingen.dao.LocationsDAO;
import com.yoshione.fingen.dao.PayeesDAO;
import com.yoshione.fingen.dao.ProductsDAO;
import com.yoshione.fingen.dao.ProjectsDAO;
import com.yoshione.fingen.dao.SendersDAO;
import com.yoshione.fingen.dao.SimpleDebtsDAO;
import com.yoshione.fingen.dao.SmsDAO;
import com.yoshione.fingen.dao.SmsMarkersDAO;
import com.yoshione.fingen.dao.TemplatesDAO;
import com.yoshione.fingen.dao.TransactionsDAO;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(includes = {ContextModule.class})
public class DaoModule {

    @Provides
    @Singleton
    public CabbagesDAO provideCabbagesDAO(Context context) {
        return CabbagesDAO.getInstance(context);
    }

    @Provides
    @Singleton
    public LocationsDAO provideLocationsDAO(Context context) {
        return LocationsDAO.getInstance(context);
    }

    @Provides
    @Singleton
    public PayeesDAO providePayeesDAO(Context context) {
        return PayeesDAO.getInstance(context);
    }

    @Provides
    @Singleton
    public ProjectsDAO provideProjectsDAO(Context context) {
        return ProjectsDAO.getInstance(context);
    }

    @Provides
    @Singleton
    public SmsMarkersDAO provideSmsMarkersDAO(Context context) {
        return SmsMarkersDAO.getInstance(context);
    }

    @Provides
    @Singleton
    public AccountsDAO provideAccountsDAO(Context context) {
        return AccountsDAO.getInstance(context);
    }

    @Provides
    @Singleton
    public CategoriesDAO provideCategoriesDAO(Context context) {
        return CategoriesDAO.getInstance(context);
    }

    @Provides
    @Singleton
    public SmsDAO provideSmsDAO(Context context) {
        return SmsDAO.getInstance(context);
    }

    @Provides
    @Singleton
    public TransactionsDAO provideTransactionsDAO(Context context) {
        return TransactionsDAO.getInstance(context);
    }

    @Provides
    @Singleton
    public CreditsDAO provideCreditsDAO(Context context) {
        return CreditsDAO.getInstance(context);
    }

    @Provides
    @Singleton
    public TemplatesDAO provideTemplatesDAO(Context context) {
        return TemplatesDAO.getInstance(context);
    }

    @Provides
    @Singleton
    public DepartmentsDAO provideDepartmentsDAO(Context context) {
        return DepartmentsDAO.getInstance(context);
    }

    @Provides
    @Singleton
    public SimpleDebtsDAO provideSimpleDebtsDAO(Context context) {
        return SimpleDebtsDAO.getInstance(context);
    }

    @Provides
    @Singleton
    public SendersDAO provideSendersDAO(Context context) {
        return SendersDAO.getInstance(context);
    }

    @Provides
    @Singleton
    public BudgetDAO provideBudgetDAO(Context context) {
        return BudgetDAO.getInstance(context);
    }

    @Provides
    @Singleton
    public BudgetCreditsDAO provideBudgetCreditsDAO(Context context) {
        return BudgetCreditsDAO.getInstance(context);
    }

    @Provides
    @Singleton
    public ProductsDAO provideProductsDAO(Context context) {
        return ProductsDAO.getInstance(context);
    }
}
