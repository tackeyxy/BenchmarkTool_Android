# BenchmarkTool

![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose)
![Version](https://img.shields.io/badge/Version-1.0.2-blue)

**3DMark GPU 性能排行榜 & 基准测试分数查询工具** | 3DMark GPU Performance Ranking & Benchmark Score Query Tool

查询 3DMark 显卡性能排行榜、GPU 基准测试分数、Cinebench 处理器分数（2026 / 2024），支持单项查询与双卡/双 CPU 性能对比。

---

## 下载 | Download

前往 [Releases](https://github.com/tackeyxy/BenchmarkTool_Android/releases/latest) 下载最新 APK。

| 版本 | 说明 |
|------|------|
| [v1.0.2](https://github.com/tackeyxy/BenchmarkTool_Android/releases/tag/v1.0.2) | 支持 Cinebench 2024/2026 切换，修复 CPU 页崩溃（当前） |

安装要求：Android 7.0 (API 24) 及以上。

---

## 功能特性 | Features

### GPU 性能排行榜 | GPU Ranking
- 浏览 3DMark 显卡性能排行榜（排名、名称、类型、分数）
- 搜索显卡名称，匹配文字高亮显示，自动滚动至首个匹配项
- 前三名金/银/铜牌样式，奇偶行交替背景色
- 悬浮按钮刷新排行榜数据
- Desktop / Mobile / Mac 类型标签色块

### GPU 基准测试分数查询 | GPU Benchmark Score Query
- 查询任意 GPU 在 17 种 3DMark 测试项目中的分数（Time Spy、Speed Way、Port Royal、Fire Strike 等）
- **最高分**（Max Score）与**中位分**（Median Score）
- 数据来源：3DMark 官方网站 API

### GPU 性能对比 | GPU Score Comparison
- 将两张显卡并排对比
- 显示最高分和中位分的**百分比差值**（红跌绿涨 + 箭头图标）
- **横向进度条**直观展示性能差距

### 处理器基准测试分数查询 | CPU Benchmark Score Query
- 查询任意处理器在 Cinebench 中的单核与多核分数
- 支持 **Cinebench 2026**（默认）与 **Cinebench 2024** 两个数据集，点击页面顶部的项目卡片即可在弹窗中切换
- 标题年份跟随所选项目与数据源自动更新
- 数据来源：社区维护的 Cinebench 数据集（GitHub）
- 数据加载失败时显示错误提示与「重试」按钮

### CPU 性能对比 | CPU Score Comparison
- 将两颗处理器并排对比
- 显示单核/多核分数的百分比差值与进度条

### 界面 | UI
- 支持浅色 / 深色主题切换（GPU 排行榜页与处理器页右上角）
- 底部导航切换「显卡」与「处理器」，显卡页内含「排行榜 / 跑分」子标签

---

## 技术栈 | Tech Stack

| 层级 | 技术 |
|------|------|
| 语言 | Kotlin |
| UI | Jetpack Compose + Material 3 |
| 架构 | MVVM (ViewModel + StateFlow + Repository) |
| 网络 | Retrofit 2 + OkHttp 4 |
| 序列化 | Gson |
| 图片加载 | Coil |
| 最低 SDK | Android 7.0 (API 24) |
| 目标 SDK | Android 17 (API 37) |

---

## 项目结构 | Project Structure

```
app/src/main/java/com/tacke/benchmark/
├── MainActivity.kt
├── data/
│   ├── api/
│   │   ├── BenchmarkApiService.kt      # 3DMark API 接口定义
│   │   └── RetrofitClient.kt           # Retrofit / OkHttp 单例
│   ├── model/
│   │   ├── BenchmarkScores.kt          # GPU 分数模型
│   │   ├── CpuBenchmarkProject.kt      # Cinebench 2026 / 2024 项目枚举
│   │   ├── CpuBenchmarkScores.kt       # CPU 分数模型
│   │   ├── CpuScoreItem.kt             # Cinebench 数据项（自定义反序列化）
│   │   ├── GpuRankItem.kt              # GPU 排行数据项
│   │   ├── ScoreResponse.kt            # API 响应模型
│   │   ├── ScoreType.kt                # 基准测试类型枚举
│   │   └── SearchItem.kt               # 搜索项模型
│   └── repository/
│       ├── BenchmarkRepository.kt      # GPU 分数仓库 (Retrofit)
│       ├── CpuBenchmarkRepository.kt   # CPU 分数仓库 (OkHttp + GitHub JSON，按项目缓存)
│       └── GpuRankRepository.kt        # GPU 排行仓库 (OkHttp + GitHub JSON)
├── ui/
│   ├── components/
│   │   ├── CommonComponents.kt         # 通用组件 (LoadingDialog, ScoreCard 等)
│   │   └── SearchSheet.kt              # GPU / CPU 共用的搜索底部弹窗
│   ├── screens/
│   │   ├── BenchmarkScreen.kt          # GPU 分数查询 / 对比页面
│   │   ├── CpuBenchmarkScreen.kt       # CPU 分数查询 / 对比页面
│   │   ├── GpuRankingScreen.kt         # GPU 排行榜页面
│   │   └── MainScreen.kt               # 主框架 (底部导航 + GPU 子标签)
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
| CPU Cinebench 2026 分数 | [cpu_benchmark_data](https://github.com/tackeyxy/cpu_benchmark_data) `main` 分支 | OkHttp 直连 |
| CPU Cinebench 2024 分数 | [cpu_benchmark_data](https://github.com/tackeyxy/cpu_benchmark_data/tree/0a5b13a8c155989c6f2263518455f190b09fa277) 固定快照提交 | OkHttp 直连 |

> Cinebench 数据集的分数列名带有年份前缀（如 `"2026 SingleScore*"`），应用会按关键字匹配列名，因此数据源升年也不需要改代码或发版。CPU 数据优先走 `gh-proxy.com` 代理，失败时回退直连 GitHub。

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

# Release APK（需在 app/build.gradle.kts 中配置签名密钥）
./gradlew assembleRelease
```

产物位置：`app/build/outputs/apk/release/app-release.apk`

Release 构建开启了 R8 混淆与资源压缩，混淆映射位于 `app/build/outputs/mapping/release/mapping.txt`，排查线上崩溃时请配合该文件使用。

---

## 更新日志 | Changelog

### v1.0.2
- 新增 Cinebench 2024 / 2026 数据集切换，默认 Cinebench 2026
- 处理器页顶部项目卡片可点击，在屏幕中央弹窗选择测试项目
- 修复选择处理器后因数据字段为空导致的崩溃
- Cinebench 数据列名按年份兼容解析，不再写死年份
- 数据加载失败时显示错误横幅与重试按钮
- 搜索弹窗移除重复的顶部横杠

### v1.0.1
- 全新浅色 / 深色主题
- 优化双显卡对比页面布局
- 修复 3DMark 数据接口访问
