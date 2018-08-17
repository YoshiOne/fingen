package com.yoshione.fingen.di.modules;

import com.yoshione.fingen.fts.FtsApi;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;

@Module(includes = {FtsRetrofitModule.class})
public class FtsApiModule {
    @Provides
    @Singleton
    public FtsApi provideSyncApi(Retrofit retrofit) {
        return retrofit.create(FtsApi.class);
    }
}
