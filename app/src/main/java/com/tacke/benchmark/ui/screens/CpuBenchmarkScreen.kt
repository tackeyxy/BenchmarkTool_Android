package com.tacke.benchmark.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
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
import com.tacke.benchmark.data.model.SearchItem
import com.tacke.benchmark.ui.components.CpuSearchSheet
import com.tacke.benchmark.ui.components.LoadingDialog
import com.tacke.benchmark.ui.theme.*
import com.tacke.benchmark.ui.viewmodel.CpuBenchmarkViewModel
import com.tacke.benchmark.ui.viewmodel.QueryMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CpuBenchmarkScreen(viewModel: CpuBenchmarkViewModel = viewModel()) {
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
        containerColor = Background
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            CpuHeader()
            ModeSwitcher(uiState.queryMode, viewModel::setQueryMode)

            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                item { Spacer(modifier = Modifier.height(12.dp)) }

                item {
                    Text(
                        "选择处理器",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    CpuHardwareSelection(
                        selectedHardware = uiState.selectedHardware,
                        queryMode = uiState.queryMode,
                        onAddClick = { viewModel.openSearchSheet() },
                        onRemoveClick = { index -> viewModel.removeHardware(index) }
                    )
                }

                if (uiState.selectedHardware.isNotEmpty()) {
                    item { Spacer(modifier = Modifier.height(20.dp)) }
                    item {
                        CpuScoreResults(
                            uiState.queryMode,
                            uiState.selectedHardware,
                            uiState.benchmarkScores
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }

        if (uiState.showSearchSheet) {
            CpuSearchSheet(
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                query = uiState.searchQuery,
                results = uiState.searchResults,
                isSearching = uiState.isSearching,
                isLoadingData = uiState.isLoadingData,
                onQueryChange = viewModel::updateSearchQuery,
                onSelect = viewModel::selectHardware,
                onDismiss = viewModel::closeSearchSheet
            )
        }
    }
}

@Composable
private fun CpuHeader() {
    Column(modifier = Modifier.padding(horizontal = 16.dp).padding(top = 8.dp, bottom = 4.dp)) {
        Text(
            "Cinebench 2024",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            "查询处理器基准测试分数与性能对比",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun CpuHardwareSelection(
    selectedHardware: List<SearchItem>,
    queryMode: QueryMode,
    onAddClick: () -> Unit,
    onRemoveClick: (Int) -> Unit
) {
    if (queryMode == QueryMode.SINGLE) {
        CpuHardwareCard(
            hardware = selectedHardware.firstOrNull(),
            onClick = onAddClick,
            onRemove = { onRemoveClick(0) }
        )
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CpuHardwareCard(
                hardware = selectedHardware.getOrNull(0),
                onClick = onAddClick,
                onRemove = { onRemoveClick(0) },
                modifier = Modifier.weight(1f)
            )
            CpuHardwareCard(
                hardware = selectedHardware.getOrNull(1),
                onClick = onAddClick,
                onRemove = { onRemoveClick(1) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CpuHardwareCard(
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
        elevation = CardDefaults.cardElevation(defaultElevation = if (hardware != null) 2.dp else 0.dp),
        border = BorderStroke(
            width = if (hardware != null) 1.dp else 2.dp,
            color = if (hardware != null) CardBorder else Primary.copy(alpha = 0.3f)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            if (hardware != null) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
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
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
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
                        "点击选择处理器",
                        color = Primary.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun CpuScoreResults(
    mode: QueryMode,
    hardware: List<SearchItem>,
    scores: List<com.tacke.benchmark.data.model.CpuBenchmarkScores?>
) {
    Column {
        Text(
            "Cinebench 2024 跑分详情",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (mode == QueryMode.SINGLE) {
            val h = hardware.firstOrNull()
            val s = scores.firstOrNull()
            if (h != null && s != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CpuScoreCard("单核分数", s.singleScore, Primary, Modifier.weight(1f))
                    CpuScoreCard("多核分数", s.multiScore, Accent, Modifier.weight(1f))
                }
            }
        } else {
            val score1 = scores.getOrNull(0)
            val score2 = scores.getOrNull(1)

            Text(
                "单核分数",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            CpuScoreComparisonRow(
                hardware.getOrNull(0), score1?.singleScore,
                hardware.getOrNull(1), score2?.singleScore
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "多核分数",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            CpuScoreComparisonRow(
                hardware.getOrNull(0), score1?.multiScore,
                hardware.getOrNull(1), score2?.multiScore
            )
        }
    }
}

@Composable
private fun CpuScoreCard(title: String, score: Int?, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (score != null) String.format("%,d", score) else "-",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}

@Composable
private fun CpuScoreComparisonRow(h1: SearchItem?, s1: Int?, h2: SearchItem?, s2: Int?) {
    val score1 = (s1 ?: 0).toFloat()
    val score2 = (s2 ?: 0).toFloat()
    val maxScore = maxOf(score1, score2).coerceAtLeast(1f)
    val ratio1 = score1 / maxScore
    val ratio2 = score2 / maxScore

    val percentageDiff = if (score1 != 0f) {
        ((score2 - score1) / score1) * 100
    } else 0f

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CpuComparisonBar(h1?.label, s1, ratio1, Primary)
        CpuComparisonBar(h2?.label, s2, ratio2, Accent, percentageDiff)
    }
}

@Composable
private fun CpuComparisonBar(
    label: String?,
    score: Int?,
    ratio: Float,
    color: Color,
    percentageDiff: Float = 0f
) {
    val percentageText = if (percentageDiff != 0f) {
        val sign = if (percentageDiff > 0) "+" else ""
        String.format(locale = java.util.Locale.US, "%s%.1f%%", sign, percentageDiff)
    } else ""
    val percentageColor =
        if (percentageDiff < 0) Color.Red else if (percentageDiff > 0) Color(0xFF4CAF50) else TextSecondary

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label ?: "-",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (score != null) String.format("%,d", score) else "-",
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
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .background(SurfaceVariant, RoundedCornerShape(5.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(ratio.coerceIn(0f, 1f))
                    .height(10.dp)
                    .background(
                        brush = Brush.horizontalGradient(listOf(color, color.copy(alpha = 0.7f))),
                        shape = RoundedCornerShape(5.dp)
                    )
            )
        }
    }
}
