package com.yoshione.fingen.di.modules;

import com.yoshione.fingen.fts.FtsHelper;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class FtsHelperModule {
    @Provides
    @Singleton
    public FtsHelper provideFtsHelper() {
        return new FtsHelper();
    }
}
