package com.example.trainyourglove.data.repositories

import android.app.Application
import android.util.Log
import com.example.trainyourglove.data.models.PostRemoveGesture
import com.example.trainyourglove.data.models.PostReset
import com.example.trainyourglove.data.models.PostTranslate
import com.example.trainyourglove.data.models.SyncedGesture
import com.example.trainyourglove.data.net.ApiInterface
import com.readystatesoftware.chuck.ChuckInterceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val TAG = "NetRepository"

class NetRepository(application: Application) {

    private val mApiInterface: ApiInterface

    init {
        val client = OkHttpClient.Builder()
            .addInterceptor(ChuckInterceptor(application))
            .build()

        val retrofit = Retrofit.Builder()
            .client(client)
            .baseUrl("https://ggtweb.herokuapp.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        mApiInterface = retrofit.create(ApiInterface::class.java)
    }

    fun getSyncedGestures(callback: Callback<List<SyncedGesture>>) {
        setupRetrofitCallback(
            mApiInterface.getSyncedGestures(),
            callback
        )
    }

    fun sync(
        gesture: SyncedGesture,
        callback: Callback<String>
    ) {
        val call = mApiInterface.sync(gesture)
        setupRetrofitCallback(call, callback)
    }

    fun translate(
        data: String,
        callback: Callback<String>
    ) {
        val call = mApiInterface.translate(PostTranslate(data))
        setupRetrofitCallback(call, callback)
    }

    fun remove(
        mappedText: String,
        callback: Callback<String>
    ) {
        val call = mApiInterface.removeSyncedGesture(PostRemoveGesture(mappedText))
        setupRetrofitCallback(call, callback)
    }

    fun reset(
        callback: Callback<String>
    ) {
        // Insecure hardcoded passkey
        val call = mApiInterface.reset(PostReset("asdf1234"))
        setupRetrofitCallback(call, callback)
    }

    private fun <T> setupRetrofitCallback(call: Call<T>, callback: Callback<T>) {
        call.enqueue(object : retrofit2.Callback<T> {
            override fun onResponse(
                call: Call<T>,
                response: Response<T>
            ) {
                if (response.code() == 200) {
                    callback.onData(response.body()!!)
                } else {
                    callback.onData(null)
                }
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                Log.e(TAG, "onFailure: ", t)
                callback.onData(null)
            }
        })
    }

    interface Callback<T> {
        fun onData(data: T?)
    }

    companion object {
        @Volatile
        private var INSTANCE: NetRepository? = null

        fun getInstance(application: Application): NetRepository {
            var localRef = INSTANCE

            return localRef ?: synchronized(NetRepository::class) {
                localRef = INSTANCE
                localRef ?: NetRepository(application).also {
                    INSTANCE = it
                }
            }
        }
    }
}