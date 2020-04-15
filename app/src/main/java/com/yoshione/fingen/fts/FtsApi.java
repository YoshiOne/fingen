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
    Single<Response<FtsResponse>> getData(
            @Url String url,
            @Header("Authorization") String authorization,
            @Header("User-Agent") String userAgent,
            @Header("Device-Id") String deviceID,
            @Header("Device-OS") String deviceOS,
            @Header("Version") String version,
            @Header("ClientVersion") String clientVersion,
            @Header("Host") String host,
            @Header("Connection") String connection
    );

    @GET
    Single<Response<AuthResponse>> checkAuth(
            @Url String url,
            @Header("Authorization") String authorization,
            @Header("User-Agent") String userAgent,
            @Header("Device-Id") String deviceID,
            @Header("Device-OS") String deviceOS,
            @Header("Version") String version,
            @Header("ClientVersion") String clientVersion,
            @Header("Host") String host,
            @Header("Connection") String connection
    );

    @POST
    Single<Response<Object>> signUp(
            @Url String url,
            @Body SignUpRequest body
    );

    @POST
    Single<Response<Object>> restoreCode(
            @Url String url,
            @Body RestoreRequest body
    );

}
