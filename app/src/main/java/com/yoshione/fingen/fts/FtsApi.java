package com.yoshione.fingen.fts;

import com.google.gson.JsonObject;
import com.yoshione.fingen.fts.models.FtsResponse;
import com.yoshione.fingen.fts.models.ReceiptStatus;
import com.yoshione.fingen.fts.models.RegistrationData;

import io.reactivex.Single;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Url;

/**
 * Created by slv on 30.01.2018.
 *
 */

public interface FtsApi {
    @GET
    Single<Response<FtsResponse>> getData(
            @Url String url,
            @Header("sessionId") String session,
            @Header("ClientVersion") String clientVer,
            @Header("Device-Id") String deviceID,
            @Header("Device-OS") String deviceOS,
            @Header("User-Agent") String userAgent
    );

    @POST
    Single<Response<ReceiptStatus>> getReceiptStatus(
            @Url String url,
            @Body JsonObject body,
            @Header("sessionId") String session,
            @Header("ClientVersion") String clientVer,
            @Header("Device-Id") String deviceID,
            @Header("Device-OS") String deviceOS,
            @Header("User-Agent") String userAgent
    );

    @POST
    Single<Response<RegistrationData>> getSessionAndToken(
            @Url String url,
            @Body JsonObject body,
            @Header("ClientVersion") String clientVer,
            @Header("Device-Id") String deviceID,
            @Header("Device-OS") String deviceOS,
            @Header("User-Agent") String userAgent
    );
}
