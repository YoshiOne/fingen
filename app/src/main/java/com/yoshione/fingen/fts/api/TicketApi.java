package com.yoshione.fingen.fts.api;

import com.yoshione.fingen.fts.models.tickets.Ticket;
import com.yoshione.fingen.fts.models.tickets.TicketFindById;
import com.yoshione.fingen.fts.models.tickets.TicketQrCodeRequest;

import io.reactivex.Single;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TicketApi {

    @POST("v2/ticket")
    Single<Response<Ticket>> addTicketQR(@Body TicketQrCodeRequest ticketQrCodeRequest);

    @GET("v2/tickets/{ticketId}")
    Single<Response<TicketFindById>> getTicket(@Path("ticketId") String ticketId);

    @GET("v2/check/ticket")
    Single<Response<Ticket>> validateTicket(@Query("fsId") String fsId, @Query("operationType") int opType, @Query("documentId") String documentId, @Query("fiscalSign") String fiscalSign, @Query("date") String date, @Query("sum") long sum);
}
