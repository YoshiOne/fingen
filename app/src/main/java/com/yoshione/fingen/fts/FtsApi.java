package com.yoshione.fingen.fts;

import com.yoshione.fingen.fts.models.AuthResponse;
import com.yoshione.fingen.fts.models.FtsResponse;
import com.yoshione.fingen.fts.models.RestoreRequest;
import com.yoshione.fingen.fts.models.SignUpRequest;

import io.reactivex.Single;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface FtsApi {
    @GET
    Single<Response<Object>> checkExists(
            @Url String url
    );

    @GET
    Single<Response<FtsResponse>> getData(
            @Url String url,
            @Header("Authorization") String authorization,
            @Header("Device-Id") String deviceID,
            @Header("Device-OS") String deviceOS
    );

    @GET("/v1/mobile/users/login")
    Single<Response<AuthResponse>> checkAuth(
            @Header("Authorization") String authorization,
            @Header("Device-Id") String deviceID,
            @Header("Device-OS") String deviceOS
    );

    @POST("/v1/mobile/users/signup")
    Single<Response<Object>> signUp(
            @Body SignUpRequest body
    );

    @POST("/v1/mobile/users/restore")
    Single<Response<Object>> restoreCode(
            @Body RestoreRequest body
    );

}
