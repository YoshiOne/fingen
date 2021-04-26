package com.yoshione.fingen.di.modules;

import com.yoshione.fingen.fts.api.TicketApi;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;

@Module(includes = {FtsRetrofitModule.class})
public class TicketApiModule {
    @Provides
    @Singleton
    public TicketApi provideTicketApi(Retrofit retrofit) {
        return retrofit.create(TicketApi.class);
    }
}
