package com.yoshione.fingen.di.modules;

import com.yoshione.fingen.iab.BillingService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class BillingModule {
    @Provides
    @Singleton
    public BillingService provideBillingService() {
        return new BillingService();
    }
}
