package com.yoshione.fingen.fts;

import com.yoshione.fingen.fts.models.FtsResponse;

import io.reactivex.Single;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Url;

/**
 * Created by slv on 30.01.2018.
 *
 */

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
}
