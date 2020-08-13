package com.yoshione.fingen.di.modules;

import com.yoshione.fingen.fts.api.LoginApi;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;

@Module(includes = {FtsRetrofitModule.class})
public class LoginApiModule {
    @Provides
    @Singleton
    public LoginApi provideLoginApi(Retrofit retrofit) {
        return retrofit.create(LoginApi.class);
    }
}
