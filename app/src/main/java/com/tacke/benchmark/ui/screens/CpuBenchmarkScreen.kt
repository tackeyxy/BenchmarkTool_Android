package com.tacke.benchmark.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tacke.benchmark.data.model.CpuBenchmarkProject
import com.tacke.benchmark.data.model.CpuBenchmarkScores
import com.tacke.benchmark.data.model.SearchItem
import com.tacke.benchmark.ui.components.LoadingDialog
import com.tacke.benchmark.ui.components.SearchSheet
import com.tacke.benchmark.ui.theme.GradientEnd
import com.tacke.benchmark.ui.theme.GradientStart
import com.tacke.benchmark.ui.theme.PositiveDelta
import com.tacke.benchmark.ui.theme.Primary
import com.tacke.benchmark.ui.theme.Secondary

import com.tacke.benchmark.ui.viewmodel.CpuBenchmarkUiState
import com.tacke.benchmark.ui.viewmodel.CpuBenchmarkViewModel
import com.tacke.benchmark.ui.viewmodel.QueryMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CpuBenchmarkScreen(
    viewModel: CpuBenchmarkViewModel = viewModel(),
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    if (uiState.isLoadingData) {
        LoadingDialog("正在加载数据...")
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                CpuHeaderSection(
                    selectedProject = uiState.selectedProject,
                    isDarkTheme = isDarkTheme,
                    onSelectProject = viewModel::selectProject,
                    onToggleTheme = onToggleTheme
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                ModeSection(
                    selectedMode = uiState.queryMode,
                    onModeChange = viewModel::setQueryMode
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            if (!uiState.isLoadingData && !hasUsableData(uiState)) {
                item {
                    DataErrorBanner(
                        message = uiState.errorMessage ?: "未获取到基准测试数据",
                        onRetry = viewModel::retryLoadData
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            item {
                SectionTitle("选择处理器")
                Spacer(modifier = Modifier.height(10.dp))
                CpuSelectionArea(
                    selectedHardware = uiState.selectedHardware,
                    queryMode = uiState.queryMode,
                    onAddClick = { viewModel.openSearchSheet() },
                    onRemoveClick = { index -> viewModel.removeHardware(index) }
                )
            }

            if (uiState.selectedHardware.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    CpuScoreDashboard(
                        mode = uiState.queryMode,
                        hardware = uiState.selectedHardware,
                        scores = uiState.benchmarkScores,
                        benchmarkYear = uiState.benchmarkYear ?: uiState.selectedProject.year
                    )
                }
            }
        }

        if (uiState.showSearchSheet) {
            SearchSheet(
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                title = "选择处理器",
                placeholder = "输入处理器型号 (e.g., 14900K, Ryzen 9 7950X)",
                emptyText = if (uiState.errorMessage != null) "数据加载失败，请点击下方重试" else "未找到匹配的处理器",
                query = uiState.searchQuery,
                results = uiState.searchResults,
                isSearching = uiState.isSearching,
                isLoadingData = uiState.isLoadingData,
                errorMessage = uiState.errorMessage?.let { "$it\n可关闭弹窗后点击「重试」重新加载" },
                onQueryChange = viewModel::updateSearchQuery,
                onSelect = viewModel::selectHardware,
                onDismiss = viewModel::closeSearchSheet
            )
        }
    }
}

@Composable
private fun CpuHeaderSection(
    selectedProject: CpuBenchmarkProject,
    isDarkTheme: Boolean = false,
    onSelectProject: (CpuBenchmarkProject) -> Unit = {},
    onToggleTheme: () -> Unit = {}
) {
    var showProjectMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Primary.copy(alpha = 0.15f),
                        Secondary.copy(alpha = 0.05f)
                    )
                )
            )
            .clickable { showProjectMenu = true }
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(listOf(GradientStart, GradientEnd))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Memory,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        selectedProject.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = "选择基准测试项目",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    "查询处理器基准测试分数与性能对比",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(onClick = onToggleTheme),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "切换主题",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // 项目选择器居中显示，避免下拉菜单受顶部卡片位置影响。
        if (showProjectMenu) {
            Dialog(onDismissRequest = { showProjectMenu = false }) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "选择基准测试项目",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        CpuBenchmarkProject.entries.forEach { project ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        showProjectMenu = false
                                        onSelectProject(project)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    project.title,
                                    modifier = Modifier.weight(1f),
                                    color = if (project == selectedProject) Primary
                                    else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (project == selectedProject) FontWeight.Bold
                                    else FontWeight.Normal
                                )
                                if (project == selectedProject) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "当前项目",
                                        tint = Primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeSection(
    selectedMode: QueryMode,
    onModeChange: (QueryMode) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ModeCard(
            label = "单项查询",
            icon = Icons.Default.Star,
            isSelected = selectedMode == QueryMode.SINGLE,
            onClick = { onModeChange(QueryMode.SINGLE) },
            modifier = Modifier.weight(1f)
        )
        ModeCard(
            label = "性能对比",
            icon = Icons.AutoMirrored.Filled.CompareArrows,
            isSelected = selectedMode == QueryMode.COMPARE,
            onClick = { onModeChange(QueryMode.COMPARE) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ModeCard(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.surface
                else MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(
            1.dp,
            if (isSelected) Primary.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isSelected) {
                        Modifier.background(
                            Brush.linearGradient(
                                colors = listOf(
                                    GradientStart.copy(alpha = 0.08f),
                                    GradientEnd.copy(alpha = 0.03f)
                                )
                            )
                        )
                    } else Modifier
                )
                .padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = if (isSelected) {
                    Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.linearGradient(listOf(GradientStart, GradientEnd)))
                } else {
                    Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

/** 数据没能加载成功（失败或为空）时才提示，避免与「正在加载」重复。 */
private fun hasUsableData(state: CpuBenchmarkUiState): Boolean =
    state.errorMessage == null && state.isDataAvailable

/**
 * 标题跟随数据源年份变化：数据里是 "Cinebench 2026" 就显示 2026。
 * 年份未知时退回不带年份的名称，避免继续强调已经过期的 2024。
 */
private fun cinebenchTitle(benchmarkYear: String?): String =
    if (benchmarkYear.isNullOrBlank()) "Cinebench" else "Cinebench $benchmarkYear"

@Composable
private fun DataErrorBanner(
    message: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.error)
                    .clickable(onClick = onRetry)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    "重试",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onError
                )
            }
        }
    }
}

@Composable
private fun CpuSelectionArea(
    selectedHardware: List<SearchItem>,
    queryMode: QueryMode,
    onAddClick: () -> Unit,
    onRemoveClick: (Int) -> Unit
) {
    if (queryMode == QueryMode.SINGLE) {
        CpuSelectionCard(
            hardware = selectedHardware.firstOrNull(),
            onClick = onAddClick,
            onRemove = { onRemoveClick(0) }
        )
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CpuSelectionCard(
                hardware = selectedHardware.getOrNull(0),
                onClick = onAddClick,
                onRemove = { onRemoveClick(0) },
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(GradientStart, GradientEnd))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "VS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            CpuSelectionCard(
                hardware = selectedHardware.getOrNull(1),
                onClick = onAddClick,
                onRemove = { onRemoveClick(1) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CpuSelectionCard(
    hardware: SearchItem?,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasHardware = hardware != null
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (hasHardware) MaterialTheme.colorScheme.surface
                else MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = if (hasHardware) {
            BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            )
        } else BorderStroke(
            1.5.dp,
            Primary.copy(alpha = 0.25f)
        )
    ) {
        if (hasHardware) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(listOf(GradientStart, GradientEnd))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Memory,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    hardware.label,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "移除",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = Primary.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    "点击选择",
                    color = Primary.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun CpuScoreDashboard(
    mode: QueryMode,
    hardware: List<SearchItem>,
    scores: List<CpuBenchmarkScores?>,
    benchmarkYear: String? = null
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp, 16.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Primary)
            )
            Text(
                "${cinebenchTitle(benchmarkYear)} 跑分结果",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (mode == QueryMode.SINGLE) {
            val score = scores.firstOrNull()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BigScoreCard(
                    label = "单核分数",
                    score = score?.singleScore,
                    gradient = listOf(Secondary, Secondary.copy(alpha = 0.5f)),
                    modifier = Modifier.weight(1f)
                )
                BigScoreCard(
                    label = "多核分数",
                    score = score?.multiScore,
                    gradient = listOf(GradientStart, GradientEnd),
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            val score1 = scores.getOrNull(0)
            val score2 = scores.getOrNull(1)

            CompareSection(
                label = "单核分数",
                h1 = hardware.getOrNull(0),
                s1 = score1?.singleScore,
                h2 = hardware.getOrNull(1),
                s2 = score2?.singleScore
            )
            Spacer(modifier = Modifier.height(16.dp))
            CompareSection(
                label = "多核分数",
                h1 = hardware.getOrNull(0),
                s1 = score1?.multiScore,
                h2 = hardware.getOrNull(1),
                s2 = score2?.multiScore
            )
        }
    }
}

@Composable
private fun BigScoreCard(
    label: String,
    score: Int?,
    gradient: List<Color>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        Brush.horizontalGradient(gradient)
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (score != null) String.format("%,d", score) else "-",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 28.sp
                    ),
                    fontWeight = FontWeight.Bold,
                    color = gradient.first()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CompareSection(
    label: String,
    h1: SearchItem?,
    s1: Int?,
    h2: SearchItem?,
    s2: Int?
) {
    val score1 = (s1 ?: 0).toFloat()
    val score2 = (s2 ?: 0).toFloat()
    val maxScore = maxOf(score1, score2).coerceAtLeast(1f)
    val ratio1 = score1 / maxScore
    val ratio2 = score2 / maxScore
    val pct = if (score1 != 0f) ((score2 - score1) / score1) * 100 else 0f

    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(10.dp))
        CompareBar(h1?.label, s1, ratio1, Primary)
        Spacer(modifier = Modifier.height(6.dp))
        CompareBar(h2?.label, s2, ratio2, Secondary, pct)
    }
}

@Composable
private fun CompareBar(
    label: String?,
    score: Int?,
    ratio: Float,
    color: Color,
    percentageDiff: Float = 0f
) {
    val animRatio by animateFloatAsState(
        targetValue = ratio.coerceIn(0f, 1f),
        animationSpec = tween(600),
        label = "barRatio"
    )
    val pctText = if (percentageDiff != 0f) {
        val sign = if (percentageDiff > 0) "+" else ""
        String.format("%s%.1f%%", sign, percentageDiff)
    } else ""
    val pctColor = when {
        percentageDiff < 0 -> com.tacke.benchmark.ui.theme.NegativeDelta
        percentageDiff > 0 -> PositiveDelta
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label ?: "-",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animRatio)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.horizontalGradient(listOf(color, color.copy(alpha = 0.5f)))
                        )
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                if (score != null) String.format("%,d", score) else "-",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp
                ),
                fontWeight = FontWeight.Bold,
                color = color
            )
            if (pctText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (percentageDiff > 0) {
                        Icon(
                            Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = pctColor,
                            modifier = Modifier.size(12.dp)
                        )
                    } else if (percentageDiff < 0) {
                        Icon(
                            Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = pctColor,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    Text(
                        pctText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = pctColor
                    )
                }
            }
        }
    }
}
