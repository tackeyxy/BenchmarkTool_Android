package com.tacke.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class CpuScoreItem(
    @SerializedName("Ranking")
    val ranking: Int,
    @SerializedName("Platform")
    val platform: String,
    @SerializedName("CPU")
    val cpu: String,
    @SerializedName("2024 SingleScore*")
    val singleScoreRaw: String,
    @SerializedName("2024 MultiScore**")
    val multiScoreRaw: String,
    @SerializedName("Cores")
    val cores: String,
    @SerializedName("Max. TDP")
    val maxTdp: String
) {
    val singleScore: Int? get() = singleScoreRaw.toIntOrNull()
    val multiScore: Int? get() = multiScoreRaw.toIntOrNull()
}
