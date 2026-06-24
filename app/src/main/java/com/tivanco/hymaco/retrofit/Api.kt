package com.tivanco.hymaco.retrofit

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface Api {
    @POST("crane_logs/insert_logs.php")
    suspend fun sendLogsToServer(@Body request: SyncLogRequest): Response<SyncLogResponse>
}