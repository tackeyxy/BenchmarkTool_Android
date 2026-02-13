package com.tacke.myapplication.data.api

import com.tacke.myapplication.data.model.MaxScoreResponse
import com.tacke.myapplication.data.model.MedianScoreResponse
import com.tacke.myapplication.data.model.SearchItem
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface BenchmarkApiService {

    @GET("proxycon/ajax/search/gpuname")
    suspend fun searchGpu(@Query("term") term: String): List<SearchItem>

    @GET("proxycon/ajax/search/cpuname")
    suspend fun searchCpu(@Query("term") term: String): List<SearchItem>

    @Headers("Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
    @GET("proxycon/ajax/newsearch")
    suspend fun getMaxScore(
        @Query("test") test: String,
        @Query("gpuId") gpuId: Int,
        @Query("gpuCount") gpuCount: Int = 1,
        @Query("scoreType") scoreType: String = "graphicsScore"
    ): MaxScoreResponse

    @Headers("Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
    @GET("proxycon/ajax/medianscore")
    suspend fun getMedianScore(
        @Query("test") test: String,
        @Query("gpuId") gpuId: Int,
        @Query("gpuCount") gpuCount: Int = 1,
        @Query("country") country: String = "",
        @Query("scoreType") scoreType: String = "graphicsScore"
    ): MedianScoreResponse

    companion object {
        const val BASE_URL = "https://www.3dmark.com/"
    }
}
