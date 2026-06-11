# BenchmarkTool

![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose)
![License](https://img.shields.io/badge/License-MIT-yellow)

**3DMark GPU 性能排行榜 & 基准测试分数查询工具** | 3DMark GPU Performance Ranking & Benchmark Score Query Tool

查询 3DMark 显卡性能排行榜、GPU 基准测试分数、Cinebench 2024 处理器分数，支持单项查询与双卡/双 CPU 性能对比。

---

## 功能特性 | Features

###  GPU 性能排行榜 | GPU Ranking
- 浏览 3DMark 显卡性能排行榜（排名、名称、类型、分数）
- 搜索显卡名称，匹配文字高亮显示，自动滚动至首个匹配项
- 前三名金/银/铜牌样式，奇偶行交替背景色
- 下拉刷新排行榜数据
- Desktop / Mobile / Mac 类型标签色块

###  GPU 基准测试分数查询 | GPU Benchmark Score Query
- 查询任意 GPU 在 17 种 3DMark 测试项目中的分数（Time Spy、Speed Way、Port Royal、Fire Strike 等）
- **最高分**（Max Score）与**中位分**（Median Score）
- 数据来源：3DMark 官方网站 API

###  GPU 性能对比 | GPU Score Comparison
- 将两张显卡并排对比
- 显示最高分和中位分的**百分比差值**（红跌绿涨 + 箭头图标）
- **横向进度条**直观展示性能差距

###  处理器基准测试分数查询 | CPU Benchmark Score Query
- 查询任意处理器在 Cinebench 2024 中的单核与多核分数
- 数据来源：社区维护的 Cinebench 数据集（GitHub）

###  CPU 性能对比 | CPU Score Comparison
- 将两颗处理器并排对比
- 显示单核/多核分数的百分比差值与进度条



## 技术栈 | Tech Stack

| 层级 | 技术 |
|------|------|
| 语言 | Kotlin |
| UI | Jetpack Compose + Material 3 |
| 架构 | MVVM (ViewModel + StateFlow + Repository) |
| 网络 | Retrofit 2 + OkHttp 4 |
| 序列化 | Gson |
| 最低 SDK | Android 7.0 (API 24) |
| 目标 SDK | Android 14 (API 34) |

---

## 项目结构 | Project Structure

```
app/src/main/java/com/tacke/benchmark/
├── MainActivity.kt
├── data/
│   ├── api/
│   │   ├── BenchmarkApiService.kt      # 3DMark API 接口定义
│   │   └── RetrofitClient.kt           # Retrofit 单例
│   ├── model/
│   │   ├── BenchmarkScores.kt          # GPU 分数模型
│   │   ├── CpuBenchmarkScores.kt       # CPU 分数模型
│   │   ├── CpuScoreItem.kt             # Cinebench 数据项
│   │   ├── GpuRankItem.kt              # GPU 排行数据项
│   │   ├── ScoreResponse.kt            # API 响应模型
│   │   ├── ScoreType.kt                # 基准测试类型枚举
│   │   └── SearchItem.kt               # 搜索项模型
│   └── repository/
│       ├── BenchmarkRepository.kt      # GPU 分数仓库 (Retrofit)
│       ├── CpuBenchmarkRepository.kt   # CPU 分数仓库 (OkHttp + GitHub JSON)
│       └── GpuRankRepository.kt        # GPU 排行仓库 (OkHttp + GitHub JSON)
├── ui/
│   ├── components/
│   │   ├── CommonComponents.kt         # 通用组件 (LoadingDialog, SearchBar, ScoreCard 等)
│   │   ├── CpuSearchSheet.kt           # CPU 搜索底部弹窗
│   │   └── GpuSearchSheet.kt           # GPU 搜索底部弹窗
│   ├── screens/
│   │   ├── BenchmarkScreen.kt          # GPU 分数查询 / 对比页面
│   │   ├── CpuBenchmarkScreen.kt       # CPU 分数查询 / 对比页面
│   │   ├── GpuRankingScreen.kt         # GPU 排行榜页面
│   │   ├── HomeScreen.kt               # 首页
│   │   └── MainScreen.kt               # 主框架 (底部导航 + GPU子标签)
│   ├── theme/
│   │   ├── Color.kt                    # 颜色定义
│   │   ├── Theme.kt                    # 主题配置
│   │   └── Type.kt                     # 文字样式
│   └── viewmodel/
│       ├── BenchmarkViewModel.kt       # GPU 分数 ViewModel
│       ├── CpuBenchmarkViewModel.kt    # CPU 分数 ViewModel
│       └── GpuRankViewModel.kt         # GPU 排行 ViewModel
```

---

## 数据来源 | Data Sources

| 数据 | 来源 | 方式 |
|------|------|------|
| GPU 3DMark 排行榜 | [GPU_benchmark_rank](https://github.com/tackeyxy/GPU_benchmark_rank) (GitHub JSON) | OkHttp 直连 |
| GPU 基准测试分数 | 3DMark 官方 API (`www.3dmark.com`) | Retrofit |
| CPU Cinebench 分数 | [cpu_benchmark_data](https://github.com/tackeyxy/cpu_benchmark_data) (GitHub JSON) | OkHttp 直连 |

---

## 构建与运行 | Build & Run

```bash
# 克隆仓库
git clone https://github.com/tackeyxy/BenchmarkTool_Android.git

# 打开项目
cd BenchmarkTool_Android

# 使用 Android Studio 打开项目根目录，或命令行构建：

# Debug APK
./gradlew assembleDebug

# Release APK（需配置签名密钥）
./gradlew assembleRelease
```

---

## 许可 | License

[MIT](LICENSE)
