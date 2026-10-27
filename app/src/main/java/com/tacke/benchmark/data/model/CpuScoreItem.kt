package com.tacke.benchmark.data.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

/**
 * CPU 榜单数据条目。
 *
 * 注意：远程 JSON（cinebench_scores.json）的列名会随年份变化（例如 "2024 SingleScore*" -> "2026 SingleScore*"），
 * 如果直接写死 @SerializedName，反序列化后字符串字段会是 null，读取时就会抛出
 * NullPointerException（"Attempt to invoke virtual method 'java.lang.Class java.lang.Object.getClass()'
 * on a null object reference"——这是 Kotlin 生成的不可变字段 setter 里 Intrinsics.checkNotNull 抛出的）。
 *
 * 所以这里用自定义 JsonDeserializer：所有字段都有默认值，分数列按关键字模糊匹配年份前缀。
 */
@JsonAdapter(CpuScoreItemDeserializer::class)
data class CpuScoreItem(
    @SerializedName("Ranking")
    val ranking: Int = 0,
    @SerializedName("Platform")
    val platform: String = "",
    @SerializedName("CPU")
    val cpu: String = "",
    @SerializedName("Cores")
    val cores: String = "",
    @SerializedName("Max. TDP")
    val maxTdp: String = "",
    val singleScoreRaw: String = "",
    val multiScoreRaw: String = "",
    /** 从列名 "2026 SingleScore*" 中提取的 benchmark 年份，例如 "2026"。取不到时为 null。 */
    val benchmarkYear: String? = null
) {
    val singleScore: Int? get() = singleScoreRaw.toScoreOrNull()
    val multiScore: Int? get() = multiScoreRaw.toScoreOrNull()
}

internal fun String.toScoreOrNull(): Int? =
    trim().filter { it.isDigit() || it == '-' }.toIntOrNull()

/** 从 "2026 SingleScore*" 这类列名里取出年份 "2026"。 */
internal fun String.extractBenchmarkYear(): String? =
    Regex("""(20\d{2})""").find(this)?.groupValues?.get(1)

internal class CpuScoreItemDeserializer : JsonDeserializer<CpuScoreItem> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): CpuScoreItem {
        val obj = json.takeIf { it.isJsonObject }?.asJsonObject ?: JsonObject()
        val singleColumn = obj.findColumnKey("SingleScore")
        val multiColumn = obj.findColumnKey("MultiScore")
        return CpuScoreItem(
            ranking = obj.string("Ranking").toScoreOrNull() ?: 0,
            platform = obj.string("Platform"),
            cpu = obj.string("CPU"),
            cores = obj.string("Cores"),
            maxTdp = obj.string("Max. TDP"),
            singleScoreRaw = obj.string(singleColumn),
            multiScoreRaw = obj.string(multiColumn),
            benchmarkYear = singleColumn.extractBenchmarkYear()
                ?: multiColumn.extractBenchmarkYear()
        )
    }

    private fun JsonObject.string(name: String): String =
        get(name)?.takeIf { !it.isJsonNull }?.asString ?: ""

    /** 匹配形如 "2026 SingleScore*" 的列名，返回列名本身（未命中返回空串）。 */
    private fun JsonObject.findColumnKey(keyword: String): String =
        keySet().firstOrNull { it.contains(keyword, ignoreCase = true) } ?: ""
}
