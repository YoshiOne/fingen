package com.yoshione.fingen.di.modules;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yoshione.fingen.BuildConfig;
import com.yoshione.fingen.fts.ReceiptDeserializer;
import com.yoshione.fingen.fts.models.FtsResponse;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class FtsRetrofitModule {

	@Provides
	@Singleton
	public Retrofit provideRetrofit(Retrofit.Builder builder) {
		return builder.baseUrl("https://proverkacheka.nalog.ru:9999").build();
	}

	@Provides
	@Singleton
	public Retrofit.Builder provideRetrofitBuilder(Converter.Factory converterFactory, OkHttpClient okHttpClient) {
		return new Retrofit.Builder()
				.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
				.addConverterFactory(converterFactory)
				.client(okHttpClient);
	}

	@Provides
	@Singleton
	public Converter.Factory provideConverterFactory(Gson gson) {
		return GsonConverterFactory.create(gson);
	}

	@Provides
	@Singleton
	Gson provideGson() {
		return new GsonBuilder()
				.registerTypeAdapter(FtsResponse.class, new ReceiptDeserializer())
				.create();
	}

	@Provides
	@Singleton
	OkHttpClient provideOkHttpClient(HttpLoggingInterceptor httpLoggingInterceptor) {
		return new OkHttpClient.Builder()
				.addInterceptor(httpLoggingInterceptor)
				.addInterceptor(chain -> {
					Request request = chain.request().newBuilder().addHeader("Prefer", "return=representation").build();
					return chain.proceed(request);
				})
				.build();
	}

	@Provides
	@Singleton
	HttpLoggingInterceptor provideHttpLoggingInterceptor() {
		HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
		interceptor.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);
		return interceptor;
	}
}
