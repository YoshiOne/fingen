package com.yoshione.fingen.di.modules;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ContextModule {
	private Context mContext;

	public ContextModule(Context context) {
		mContext = context;
	}

	@Provides
	@Singleton
	public Context provideContext() {
		return mContext;
	}
}
