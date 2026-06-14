package com.tacke.benchmark.data.repository

import android.util.Log
import com.tacke.benchmark.data.api.RetrofitClient
import com.tacke.benchmark.data.model.BenchmarkScores
import com.tacke.benchmark.data.model.MaxScoreResponse
import com.tacke.benchmark.data.model.MedianScoreResponse
import com.tacke.benchmark.data.model.ScoreType
import com.tacke.benchmark.data.model.SearchItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BenchmarkRepository {

    suspend fun searchGpu(query: String): Result<List<SearchItem>> = withContext(Dispatchers.IO) {
        try {
            Log.d("BenchmarkRepo", "searchGpu called with query: $query")
            val url = "https://www.3dmark.com/proxycon/ajax/search/gpuname?term=${java.net.URLEncoder.encode(query, "UTF-8")}"
            val request = okhttp3.Request.Builder().url(url).build()
            val response = RetrofitClient.okHttpClient.newCall(request).execute()
            val body = response.body?.string() ?: throw Exception("响应为空")
            Log.d("BenchmarkRepo", "searchGpu response: $body")
            val items = RetrofitClient.gson.fromJson(body, Array<SearchItem>::class.java).toList()
            Log.d("BenchmarkRepo", "searchGpu result size: ${items.size}")
            Result.success(items)
        } catch (e: Exception) {
            Log.e("BenchmarkRepo", "searchGpu failed", e)
            Result.failure(e)
        }
    }

    suspend fun getBenchmarkScores(
        scoreType: ScoreType,
        hardwareId: Int
    ): Result<BenchmarkScores> = withContext(Dispatchers.IO) {
        try {
            val maxUrl = "https://www.3dmark.com/proxycon/ajax/newsearch?test=${scoreType.code}&gpuId=$hardwareId&gpuCount=1&scoreType=graphicsScore"
            val maxRequest = okhttp3.Request.Builder()
                .url(maxUrl)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                .build()
            val maxResponse = RetrofitClient.okHttpClient.newCall(maxRequest).execute()
            val maxBody = maxResponse.body?.string() ?: throw Exception("响应为空")
            val maxScoreResponse = RetrofitClient.gson.fromJson(maxBody, MaxScoreResponse::class.java)

            val medianUrl = "https://www.3dmark.com/proxycon/ajax/medianscore?test=${scoreType.code}&gpuId=$hardwareId&gpuCount=1&country=&scoreType=graphicsScore"
            val medianRequest = okhttp3.Request.Builder()
                .url(medianUrl)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                .build()
            val medianResponse = RetrofitClient.okHttpClient.newCall(medianRequest).execute()
            val medianBody = medianResponse.body?.string() ?: throw Exception("响应为空")
            val medianScoreResponse = RetrofitClient.gson.fromJson(medianBody, MedianScoreResponse::class.java)

            Result.success(
                BenchmarkScores(
                    maxScore = maxScoreResponse.maxValue,
                    medianScore = medianScoreResponse.median
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
