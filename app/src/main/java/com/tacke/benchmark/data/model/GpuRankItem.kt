package com.tacke.benchmark.data.model

import com.google.gson.annotations.SerializedName

data class GpuRankResponse(
    @SerializedName("gpus")
    val gpus: List<GpuRankItem>
)

data class GpuRankItem(
    @SerializedName("rank")
    val rank: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("score")
    val score: Double
)
