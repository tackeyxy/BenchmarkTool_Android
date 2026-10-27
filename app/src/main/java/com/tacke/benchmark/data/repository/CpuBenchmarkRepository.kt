package com.tacke.benchmark.data.repository

import com.tacke.benchmark.data.api.RetrofitClient
import com.tacke.benchmark.data.model.CpuBenchmarkProject
import com.tacke.benchmark.data.model.CpuBenchmarkScores
import com.tacke.benchmark.data.model.CpuScoreItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CpuBenchmarkRepository {
    // 每个测试项目独立缓存，切换项目不会污染另一份榜单。
    private val caches = mutableMapOf<CpuBenchmarkProject, List<CpuScoreItem>>()

    suspend fun fetchCpuScores(
        project: CpuBenchmarkProject
    ): Result<List<CpuScoreItem>> = withContext(Dispatchers.IO) {
        try {
            // gh-proxy 是国内首选；失败时回退到 GitHub 原始文件地址。
            val urls = listOf(
                "https://gh-proxy.com/${project.rawUrl}",
                project.rawUrl
            )
            var lastError: Exception? = null
            for (url in urls) {
                try {
                    val request = okhttp3.Request.Builder().url(url).build()
                    RetrofitClient.okHttpClient.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) throw Exception("HTTP ${response.code}")
                        val body = response.body?.string()?.takeIf { it.isNotBlank() }
                            ?: throw Exception("响应为空")
                        val items = RetrofitClient.gson
                            .fromJson(body, Array<CpuScoreItem>::class.java)
                            ?.filter { it.cpu.isNotBlank() }
                            ?: emptyList()
                        if (items.isEmpty()) throw Exception("数据格式异常")
                        caches[project] = items
                    }
                    return@withContext Result.success(caches.getValue(project))
                } catch (e: Exception) {
                    lastError = e
                }
            }
            Result.failure(lastError ?: Exception("数据加载失败"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun searchCpu(project: CpuBenchmarkProject, query: String): List<CpuScoreItem> {
        if (query.isBlank()) return emptyList()
        return caches[project].orEmpty().filter { it.cpu.contains(query, ignoreCase = true) }
    }

    fun getScores(project: CpuBenchmarkProject, cpuName: String): CpuBenchmarkScores? {
        val item = caches[project].orEmpty().find { it.cpu == cpuName } ?: return null
        return CpuBenchmarkScores(
            singleScore = item.singleScore,
            multiScore = item.multiScore
        )
    }

    fun isDataLoaded(project: CpuBenchmarkProject): Boolean =
        caches[project].orEmpty().isNotEmpty()

    /** 标题以用户选择的项目为准；远程列名不可用时也有确定的年份可显示。 */
    fun benchmarkYear(project: CpuBenchmarkProject): String =
        caches[project].orEmpty().firstNotNullOfOrNull { it.benchmarkYear } ?: project.year
}
