package com.yoshione.fingen.di;

import android.content.Context;

import com.yoshione.fingen.ActivityEditTransaction;
import com.yoshione.fingen.ActivityMain;
import com.yoshione.fingen.ActivityPro;
import com.yoshione.fingen.FragmentAccounts;
import com.yoshione.fingen.FragmentSimpleDebts;
import com.yoshione.fingen.FragmentSummary;
import com.yoshione.fingen.FragmentTransactions;
import com.yoshione.fingen.adapter.viewholders.TransactionViewHolderParams;
import com.yoshione.fingen.di.modules.BillingModule;
import com.yoshione.fingen.di.modules.ContextModule;
import com.yoshione.fingen.di.modules.DaoModule;
import com.yoshione.fingen.di.modules.FtsApiModule;
import com.yoshione.fingen.di.modules.FtsHelperModule;
import com.yoshione.fingen.di.modules.FtsRetrofitModule;
import com.yoshione.fingen.di.modules.PreferencesModule;
import com.yoshione.fingen.fts.FtsHelper;
import com.yoshione.fingen.iab.BillingService;
import com.yoshione.fingen.widgets.ToolbarActivity;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {ContextModule.class, PreferencesModule.class, BillingModule.class, DaoModule.class,
		FtsRetrofitModule.class, FtsApiModule.class, FtsHelperModule.class})
public interface AppComponent {
	Context getContext();

	void inject(BillingService billingService);
	void inject(ActivityMain activityMain);
	void inject(FragmentAccounts fragmentAccounts);
	void inject(FragmentSummary fragmentSummary);
	void inject(FragmentTransactions fragmentTransactions);
	void inject(FragmentSimpleDebts fragmentSimpleDebts);
	void inject(ActivityPro activityPro);
	void inject(ToolbarActivity toolbarActivity);
	void inject(ActivityEditTransaction activityEditTransaction);
	void inject(FtsHelper ftsHelper);
	void inject(TransactionViewHolderParams transactionViewHolderParams);
}
