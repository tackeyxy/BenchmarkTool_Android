package com.tacke.benchmark.data.model

/** CPU 页可选的基准测试项目。URL 必须是原始 JSON 地址，而不是 GitHub 的 HTML 页面。 */
enum class CpuBenchmarkProject(
    val year: String,
    val rawUrl: String
) {
    CINEBENCH_2026(
        year = "2026",
        rawUrl = "https://raw.githubusercontent.com/tackeyxy/cpu_benchmark_data/main/cinebench_scores.json"
    ),
    CINEBENCH_2024(
        year = "2024",
        rawUrl = "https://raw.githubusercontent.com/tackeyxy/cpu_benchmark_data/0a5b13a8c155989c6f2263518455f190b09fa277/cinebench_scores.json"
    );

    val title: String get() = "Cinebench $year"
}
