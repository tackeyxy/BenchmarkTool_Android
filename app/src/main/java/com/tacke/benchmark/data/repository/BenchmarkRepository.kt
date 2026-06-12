package com.tacke.benchmark.data.repository

import com.tacke.benchmark.data.api.RetrofitClient
import com.tacke.benchmark.data.model.BenchmarkScores
import com.tacke.benchmark.data.model.ScoreType
import com.tacke.benchmark.data.model.SearchItem

class BenchmarkRepository {
    private val apiService = RetrofitClient.apiService

    suspend fun searchGpu(query: String): Result<List<SearchItem>> = try {
        Result.success(apiService.searchGpu(query))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getBenchmarkScores(
        scoreType: ScoreType,
        hardwareId: Int
    ): Result<BenchmarkScores> = try {
        val maxScoreResponse = apiService.getMaxScore(test = scoreType.code, gpuId = hardwareId)
        val medianScoreResponse = apiService.getMedianScore(test = scoreType.code, gpuId = hardwareId)
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
