package com.tacke.myapplication.data.repository

import com.google.gson.Gson
import com.tacke.myapplication.data.model.CpuBenchmarkScores
import com.tacke.myapplication.data.model.CpuScoreItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class CpuBenchmarkRepository {
    private var cache: List<CpuScoreItem> = emptyList()
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()

    suspend fun fetchCpuScores(): Result<List<CpuScoreItem>> = withContext(Dispatchers.IO) {
        try {
            val url = "https://gh-proxy.com/https://raw.githubusercontent.com/tackeyxy/cpu_benchmark_data/main/cinebench_scores.json"
            val request = okhttp3.Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: throw Exception("响应为空")
            val items = gson.fromJson(body, Array<CpuScoreItem>::class.java).toList()
            cache = items
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun searchCpu(query: String): List<CpuScoreItem> {
        if (query.isBlank()) return emptyList()
        return cache.filter { it.cpu.contains(query, ignoreCase = true) }
    }

    fun getScores(cpuName: String): CpuBenchmarkScores? {
        val item = cache.find { it.cpu == cpuName } ?: return null
        return CpuBenchmarkScores(
            singleScore = item.singleScore,
            multiScore = item.multiScore
        )
    }

    fun isDataLoaded(): Boolean = cache.isNotEmpty()
}
