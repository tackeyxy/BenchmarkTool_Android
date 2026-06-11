package com.tacke.benchmark.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tacke.benchmark.data.model.BenchmarkScores
import com.tacke.benchmark.data.model.ScoreType
import com.tacke.benchmark.data.model.SearchItem
import com.tacke.benchmark.ui.components.*
import com.tacke.benchmark.ui.theme.*
import com.tacke.benchmark.ui.viewmodel.BenchmarkViewModel
import com.tacke.benchmark.ui.viewmodel.QueryMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BenchmarkScreen(viewModel: BenchmarkViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    if (uiState.isLoadingScores) {
        LoadingDialog("正在获取分数...")
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Background
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            GpuHeader()
            ModeSwitcher(uiState.queryMode, viewModel::setQueryMode)

            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                item {
                    Text(
                        "选择显卡",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                item {
                    GpuHardwareSelection(
                        selectedHardware = uiState.selectedHardware,
                        queryMode = uiState.queryMode,
                        onAddClick = { index -> viewModel.setSelectingFor(index) },
                        onRemoveClick = { index -> viewModel.removeHardware(index) }
                    )
                }

                val showTestItems = uiState.queryMode == QueryMode.SINGLE ||
                    (uiState.queryMode == QueryMode.COMPARE && uiState.selectedHardware.size >= 2)
                if (uiState.selectedHardware.isNotEmpty() && showTestItems) {
                    item { Spacer(modifier = Modifier.height(if (uiState.queryMode == QueryMode.SINGLE) 20.dp else 12.dp)) }
                    item {
                        Text(
                            "选择测试项目",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(if (uiState.queryMode == QueryMode.SINGLE) 10.dp else 6.dp))
                        ScoreTypeGrid(uiState.selectedScoreType, viewModel::selectScoreType)
                    }
                }

                if (uiState.benchmarkScores.isNotEmpty() && uiState.benchmarkScores.any { it != null }) {
                    item { Spacer(modifier = Modifier.height(12.dp)) }
                    item {
                        GpuScoreResultSection(
                            uiState.queryMode,
                            uiState.selectedScoreType,
                            uiState.selectedHardware,
                            uiState.benchmarkScores
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }

        if (uiState.showSearchSheet) {
            GpuSearchSheet(
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                query = uiState.searchQuery,
                results = uiState.searchResults,
                isSearching = uiState.isSearching,
                onQueryChange = viewModel::updateSearchQuery,
                onSelect = viewModel::selectHardware,
                onDismiss = viewModel::closeSearchSheet
            )
        }
    }
}

@Composable
private fun GpuHeader() {
    Column(modifier = Modifier.padding(horizontal = 16.dp).padding(top = 4.dp, bottom = 0.dp)) {
        Text(
            "3DMark 显卡测试",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            "查询显卡基准测试分数与性能对比",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}

@Composable
fun ModeSwitcher(selectedMode: QueryMode, onModeChange: (QueryMode) -> Unit) {
    val primaryColor = Primary
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            QueryMode.entries.forEach { mode ->
                val isSelected = selectedMode == mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) primaryColor else Color.Transparent)
                        .clickable { onModeChange(mode) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (mode == QueryMode.SINGLE) "单项查询" else "性能对比",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun GpuHardwareSelection(
    selectedHardware: List<SearchItem>,
    queryMode: QueryMode,
    onAddClick: (Int) -> Unit,
    onRemoveClick: (Int) -> Unit
) {
    if (queryMode == QueryMode.SINGLE) {
        GpuHardwareCard(
            hardware = selectedHardware.firstOrNull(),
            onClick = { onAddClick(0) },
            onRemove = { onRemoveClick(0) }
        )
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GpuHardwareCard(
                hardware = selectedHardware.getOrNull(0),
                onClick = { onAddClick(0) },
                onRemove = { onRemoveClick(0) },
                modifier = Modifier.weight(1f)
            )
            GpuHardwareCard(
                hardware = selectedHardware.getOrNull(1),
                onClick = { onAddClick(1) },
                onRemove = { onRemoveClick(1) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun GpuHardwareCard(
    hardware: SearchItem?,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (hardware != null) CardBackground else SurfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = if (hardware != null) 1.dp else 0.dp),
        border = BorderStroke(
            width = if (hardware != null) 1.dp else 2.dp,
            color = if (hardware != null) CardBorder else Primary.copy(alpha = 0.3f)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            if (hardware != null) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        hardware.label,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = TextPrimary,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = Primary.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "点击选择显卡",
                        color = Primary.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ScoreTypeGrid(selectedScoreType: ScoreType?, onScoreTypeSelect: (ScoreType) -> Unit) {
    val scoreTypes = ScoreType.getGpuBenchmarks()
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        scoreTypes.forEach { scoreType ->
            ScoreTypeChip(
                scoreType = scoreType,
                isSelected = selectedScoreType == scoreType,
                onClick = { onScoreTypeSelect(scoreType) }
            )
        }
    }
}

@Composable
private fun ScoreTypeChip(scoreType: ScoreType, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Primary else CardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 3.dp else 0.dp
        ),
        border = BorderStroke(1.dp, if (isSelected) Primary else CardBorder)
    ) {
        Box(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = scoreType.displayName.replace("3DMark ", ""),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else TextPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun GpuScoreResultSection(
    mode: QueryMode,
    scoreType: ScoreType?,
    hardware: List<SearchItem>,
    scores: List<BenchmarkScores?>
) {
    Column {
        Text(
            "${scoreType?.displayName?.replace("3DMark ", "") ?: ""} 跑分详情",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))

        if (mode == QueryMode.SINGLE) {
            val h = hardware.firstOrNull()
            val s = scores.firstOrNull()
            if (h != null && s != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GpuScoreCard("最高分", s.maxScore, Primary, Modifier.weight(1f))
                    GpuScoreCard("中位分", s.medianScore, Accent, Modifier.weight(1f))
                }
            }
        } else {
            val score1 = scores.getOrNull(0)
            val score2 = scores.getOrNull(1)
            val maxScore1 = score1?.maxScore ?: 0
            val maxScore2 = score2?.maxScore ?: 0
            val medianScore1 = score1?.medianScore ?: 0.0
            val medianScore2 = score2?.medianScore ?: 0.0

            Text(
                "最高分数",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(2.dp))
            GpuScoreComparisonRow(hardware.getOrNull(0), maxScore1, hardware.getOrNull(1), maxScore2)

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                "中位分数",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(2.dp))
            GpuScoreComparisonRow(hardware.getOrNull(0), medianScore1, hardware.getOrNull(1), medianScore2)
        }
    }
}

@Composable
private fun GpuScoreCard(title: String, score: Number, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (score is Double) String.format("%,.0f", score) else String.format("%,d", score),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(title, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
    }
}

@Composable
fun GpuScoreComparisonRow(h1: SearchItem?, s1: Number, h2: SearchItem?, s2: Number) {
    val score1 = s1.toFloat()
    val score2 = s2.toFloat()
    val maxScore = maxOf(score1, score2).coerceAtLeast(1f)
    val ratio1 = score1 / maxScore
    val ratio2 = score2 / maxScore

    val percentageDiff = if (score1 != 0f) {
        ((score2 - score1) / score1) * 100
    } else {
        0f
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        GpuComparisonBar(h1?.label, s1, ratio1, Primary)
        GpuComparisonBar(h2?.label, s2, ratio2, Accent, percentageDiff)
    }
}

@Composable
fun GpuComparisonBar(label: String?, score: Number, ratio: Float, color: Color, percentageDiff: Float = 0f) {
    val percentageText = if (percentageDiff != 0f) {
        val sign = if (percentageDiff > 0) "+" else ""
        String.format(locale = java.util.Locale.US, "%s%.1f%%", sign, percentageDiff)
    } else {
        ""
    }
    val percentageColor = if (percentageDiff < 0) Color.Red else if (percentageDiff > 0) Color(0xFF4CAF50) else TextSecondary

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label ?: "-",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (score is Double) String.format("%,.0f", score) else String.format("%,d", score),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                if (percentageText.isNotEmpty()) {
                    Spacer(modifier = Modifier.size(6.dp))
                    Box(
                        modifier = Modifier
                            .background(percentageColor.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (percentageDiff > 0) {
                                Icon(
                                    Icons.Default.ArrowUpward,
                                    contentDescription = "up",
                                    tint = percentageColor,
                                    modifier = Modifier.size(12.dp)
                                )
                            } else if (percentageDiff < 0) {
                                Icon(
                                    Icons.Default.ArrowDownward,
                                    contentDescription = "down",
                                    tint = percentageColor,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            Spacer(modifier = Modifier.size(2.dp))
                            Text(
                                percentageText,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = percentageColor
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(SurfaceVariant, RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(ratio.coerceIn(0f, 1f))
                    .height(6.dp)
                    .background(
                        brush = Brush.horizontalGradient(listOf(color, color.copy(alpha = 0.7f))),
                        shape = RoundedCornerShape(3.dp)
                    )
            )
        }
    }
}
