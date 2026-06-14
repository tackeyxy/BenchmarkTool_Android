package com.tacke.benchmark.data.api

import android.util.Log
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val TAG = "RetrofitClient"
    val gson: Gson = Gson()

    private val browserInterceptor = Interceptor { chain ->
        val original = chain.request()
        Log.d(TAG, "Interceptor: ${original.method} ${original.url}")
        val requestBuilder = original.newBuilder()
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36")
            .header("Referer", "https://www.3dmark.com/")
            .header("Accept", "application/json, text/javascript, */*; q=0.01")
            .header("X-Requested-With", "XMLHttpRequest")
        val response = chain.proceed(requestBuilder.build())
        Log.d(TAG, "Interceptor response: ${response.code} for ${original.url}")
        response
    }

    val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(browserInterceptor)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val apiService: BenchmarkApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BenchmarkApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(BenchmarkApiService::class.java)
    }
}
