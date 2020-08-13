package com.yoshione.fingen.fts.api;

import com.yoshione.fingen.fts.models.login.LoginResponse;
import com.yoshione.fingen.fts.models.login.PhoneLoginRequest;

import io.reactivex.Single;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface LoginApi {
    @POST("v2/auth/phone/verify")
    Single<Response<LoginResponse>> loginUser(@Body PhoneLoginRequest body);
}
