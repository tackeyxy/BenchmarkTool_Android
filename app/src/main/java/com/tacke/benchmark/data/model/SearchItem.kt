package com.tacke.benchmark.data.model

data class SearchItem(
    val id: String,
    val label: String
) {
    val idInt: Int get() = id.toIntOrNull() ?: 0
}
