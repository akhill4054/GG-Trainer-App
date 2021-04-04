package com.example.trainyourglove.data.net

import com.example.trainyourglove.data.models.PostRemoveGesture
import com.example.trainyourglove.data.models.PostReset
import com.example.trainyourglove.data.models.PostTranslate
import com.example.trainyourglove.data.models.SyncedGesture
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiInterface {
    @GET("/ml/api/gestures/")
    fun getSyncedGestures(): Call<List<SyncedGesture>>

    @POST("/ml/api/translate/")
    fun translate(@Body body: PostTranslate): Call<String>

    @POST("/ml/api/sync/")
    fun sync(@Body body: SyncedGesture): Call<String>

    @POST("/ml/api/remove/")
    fun removeSyncedGesture(@Body body: PostRemoveGesture): Call<String>

    @POST("/ml/api/reset/")
    fun reset(@Body body: PostReset): Call<String>
}