package com.tacke.benchmark.data.repository

import com.google.gson.Gson
import com.tacke.benchmark.data.model.GpuRankItem
import com.tacke.benchmark.data.model.GpuRankResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class GpuRankRepository {
    private var cache: List<GpuRankItem> = emptyList()
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()

    suspend fun fetchGpuRanks(): Result<List<GpuRankItem>> = withContext(Dispatchers.IO) {
        try {
            val url = "https://gh-proxy.com/https://raw.githubusercontent.com/tackeyxy/GPU_benchmark_rank/main/gpu_benchmarks.json"
            val request = okhttp3.Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: throw Exception("响应为空")
            val parsed = gson.fromJson(body, GpuRankResponse::class.java)
            cache = parsed.gpus
            Result.success(parsed.gpus)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCachedRanks(): List<GpuRankItem> = cache

    fun isDataLoaded(): Boolean = cache.isNotEmpty()
}
