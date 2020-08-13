package com.yoshione.fingen.di.modules;

import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yoshione.fingen.BuildConfig;
import com.yoshione.fingen.FgConst;
import com.yoshione.fingen.fts.FtsHelper;
import com.yoshione.fingen.fts.models.login.ReAuthLoginRequest;
import com.yoshione.fingen.fts.models.login.ReAuthLoginResponse;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

@Module(includes = {PreferencesModule.class})
public class FtsRetrofitModule {

	private static final String BASE_URL = "https://irkkt-mobile.nalog.ru:8888/";

	@Provides
	@Singleton
	public Retrofit provideRetrofit(Retrofit.Builder builder) {
		return builder.baseUrl(BASE_URL).build();
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
				.create();
	}

	@Provides
	@Singleton
	OkHttpClient provideOkHttpClient(HttpLoggingInterceptor httpLoggingInterceptor, SharedPreferences preferences) {
		return new OkHttpClient.Builder()
				.addInterceptor(httpLoggingInterceptor)
				.addInterceptor(chain -> {
					Request.Builder builder = chain.request().newBuilder()
							.addHeader("ClientVersion", "2.9.0")
							.addHeader("Device-Id", "740184f07e8e1af6b29c48b2f2d15fa1")
							.addHeader("Device-OS", "Android");
					if (FtsHelper.isFtsCredentialsAvailable(preferences))
							builder.addHeader("sessionId",  preferences.getString(FgConst.PREF_FTS_SESSION_ID, ""));
					return chain.proceed(builder.build());
				})
				.addInterceptor(chain -> {
					Request request = chain.request();
					Request.Builder builder = request.newBuilder();
					Response response = chain.proceed(builder.build());
					if (response.code() == 401) {
						ReAuthLoginRequest reAuthLoginRequest = new ReAuthLoginRequest(preferences.getString(FgConst.PREF_FTS_CLIENT_SECRET, ""), preferences.getString(FgConst.PREF_FTS_REFRESH_TOKEN, ""));
						RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), reAuthLoginRequest.toJson());
						Request requestAuth = builder.url(BASE_URL + "v2/mobile/users/refresh").method("POST", requestBody).removeHeader("sessionId").build();
						Response responseAuth = chain.proceed(requestAuth);
						if (responseAuth.code() == 200) {
							ResponseBody responseBody = responseAuth.body();
							if (responseBody != null) {
								String responseJson = responseBody.string();
								ReAuthLoginResponse reAuthLoginResponse = new Gson().fromJson(responseJson, ReAuthLoginResponse.class);
								preferences.edit()
										.putString(FgConst.PREF_FTS_REFRESH_TOKEN, reAuthLoginResponse.getRefresh_token())
										.putString(FgConst.PREF_FTS_SESSION_ID, reAuthLoginResponse.getSessionId())
										.apply();
								return chain.proceed(request.newBuilder().header("sessionId", reAuthLoginResponse.getSessionId()).method(request.method(), request.body()).build());
							}
						} else if (responseAuth.code() == 498) {
							// refresh_code incorrect
							FtsHelper.clearFtsCredentials(preferences);
						}
					}
					return response;
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
