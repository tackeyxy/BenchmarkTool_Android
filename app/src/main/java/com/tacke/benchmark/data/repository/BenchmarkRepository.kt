package com.tacke.benchmark.data.repository

import com.tacke.benchmark.data.api.RetrofitClient
import com.tacke.benchmark.data.model.BenchmarkScores
import com.tacke.benchmark.data.model.MaxScoreResponse
import com.tacke.benchmark.data.model.MedianScoreResponse
import com.tacke.benchmark.data.model.ScoreType
import com.tacke.benchmark.data.model.SearchItem

class BenchmarkRepository {
    private val apiService = RetrofitClient.apiService

    suspend fun searchGpu(query: String): Result<List<SearchItem>> = try {
        val result = apiService.searchGpu(query)
        Result.success(result)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun searchCpu(query: String): Result<List<SearchItem>> = try {
        val result = apiService.searchCpu(query)
        Result.success(result)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getMaxScore(test: String, gpuId: Int): Result<MaxScoreResponse> = try {
        val result = apiService.getMaxScore(test = test, gpuId = gpuId)
        Result.success(result)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getMedianScore(test: String, gpuId: Int): Result<MedianScoreResponse> = try {
        val result = apiService.getMedianScore(test = test, gpuId = gpuId)
        Result.success(result)
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
