package com.tacke.benchmark.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.Speed
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
import androidx.compose.runtime.remember
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tacke.benchmark.data.model.BenchmarkScores
import com.tacke.benchmark.data.model.ScoreType
import com.tacke.benchmark.data.model.SearchItem
import com.tacke.benchmark.ui.components.GradientDivider
import com.tacke.benchmark.ui.components.LoadingDialog
import com.tacke.benchmark.ui.components.SearchSheet
import com.tacke.benchmark.ui.theme.GradientEnd
import com.tacke.benchmark.ui.theme.GradientStart
import com.tacke.benchmark.ui.theme.PositiveDelta
import com.tacke.benchmark.ui.theme.Primary
import com.tacke.benchmark.ui.theme.Secondary

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
                HeaderSection()
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                ModeSection(
                    selectedMode = uiState.queryMode,
                    onModeChange = viewModel::setQueryMode
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                SectionTitle("选择显卡")
                Spacer(modifier = Modifier.height(10.dp))
                GpuSelectionArea(
                    selectedHardware = uiState.selectedHardware,
                    queryMode = uiState.queryMode,
                    onAddClick = { index -> viewModel.setSelectingFor(index) },
                    onRemoveClick = { index -> viewModel.removeHardware(index) }
                )
            }

            val showTestItems = uiState.queryMode == QueryMode.SINGLE ||
                (uiState.queryMode == QueryMode.COMPARE && uiState.selectedHardware.size >= 2)
            if (uiState.selectedHardware.isNotEmpty() && showTestItems) {
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    SectionTitle("选择测试项目")
                    Spacer(modifier = Modifier.height(10.dp))
                    ScoreTypeStrip(
                        selected = uiState.selectedScoreType,
                        onSelect = viewModel::selectScoreType
                    )
                }
            }

            if (uiState.benchmarkScores.isNotEmpty() && uiState.benchmarkScores.any { it != null }) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    ScoreDashboard(
                        mode = uiState.queryMode,
                        scoreType = uiState.selectedScoreType,
                        hardware = uiState.selectedHardware,
                        scores = uiState.benchmarkScores
                    )
                }
            }
        }

        if (uiState.showSearchSheet) {
            SearchSheet(
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                title = "选择显卡",
                placeholder = "输入显卡型号 (e.g., 4060)",
                emptyText = "未找到匹配的显卡",
                query = uiState.searchQuery,
                results = uiState.searchResults,
                isSearching = uiState.isSearching,
                hasSearched = uiState.hasSearched,
                errorMessage = uiState.errorMessage,
                onQueryChange = viewModel::updateSearchQuery,
                onSelect = viewModel::selectHardware,
                onDismiss = viewModel::closeSearchSheet
            )
        }
    }
}

@Composable
private fun HeaderSection() {
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
            .padding(20.dp)
    ) {
        Row(
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
                    Icons.Default.Speed,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "3DMark GPU 测试",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "查询显卡基准测试分数与性能对比",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
            mode = QueryMode.SINGLE,
            label = "单项查询",
            icon = Icons.Default.Star,
            isSelected = selectedMode == QueryMode.SINGLE,
            onClick = { onModeChange(QueryMode.SINGLE) },
            modifier = Modifier.weight(1f)
        )
        ModeCard(
            mode = QueryMode.COMPARE,
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
    mode: QueryMode,
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
private fun GpuSelectionArea(
    selectedHardware: List<SearchItem>,
    queryMode: QueryMode,
    onAddClick: (Int) -> Unit,
    onRemoveClick: (Int) -> Unit
) {
    if (queryMode == QueryMode.SINGLE) {
        GpuSelectionCard(
            hardware = selectedHardware.firstOrNull(),
            onClick = { onAddClick(0) },
            onRemove = { onRemoveClick(0) }
        )
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GpuSelectionCard(
                hardware = selectedHardware.getOrNull(0),
                onClick = { onAddClick(0) },
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
            GpuSelectionCard(
                hardware = selectedHardware.getOrNull(1),
                onClick = { onAddClick(1) },
                onRemove = { onRemoveClick(1) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun GpuSelectionCard(
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
                        Icons.Default.Speed,
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
private fun ScoreTypeStrip(
    selected: ScoreType?,
    onSelect: (ScoreType) -> Unit
) {
    val items = ScoreType.getGpuBenchmarks()
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { scoreType ->
            val isSelected = selected == scoreType
            val label = scoreType.displayName.replace("3DMark ", "")
            Box(
                modifier = if (isSelected) {
                    Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.linearGradient(listOf(GradientStart, GradientEnd)))
                        .clickable { onSelect(scoreType) }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                } else {
                    Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onSelect(scoreType) }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun ScoreDashboard(
    mode: QueryMode,
    scoreType: ScoreType?,
    hardware: List<SearchItem>,
    scores: List<BenchmarkScores?>
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
                "${scoreType?.displayName?.replace("3DMark ", "") ?: ""} 跑分结果",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (mode == QueryMode.SINGLE) {
            val s = scores.firstOrNull()
            if (s != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BigScoreCard(
                        label = "最高分",
                        score = s.maxScore,
                        gradient = listOf(GradientStart, GradientEnd),
                        modifier = Modifier.weight(1f)
                    )
                    BigScoreCard(
                        label = "中位分",
                        score = s.medianScore,
                        gradient = listOf(Secondary, Secondary.copy(alpha = 0.5f)),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        } else {
            val score1 = scores.getOrNull(0)
            val score2 = scores.getOrNull(1)

            CompareSection(
                label = "最高分数",
                h1 = hardware.getOrNull(0),
                s1 = score1?.maxScore,
                h2 = hardware.getOrNull(1),
                s2 = score2?.maxScore
            )
            Spacer(modifier = Modifier.height(16.dp))
            CompareSection(
                label = "中位分数",
                h1 = hardware.getOrNull(0),
                s1 = score1?.medianScore,
                h2 = hardware.getOrNull(1),
                s2 = score2?.medianScore
            )
        }
    }
}

@Composable
private fun BigScoreCard(
    label: String,
    score: Number,
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
                    text = if (score is Double) String.format("%,.0f", score)
                        else String.format("%,d", score),
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
    s1: Number?,
    h2: SearchItem?,
    s2: Number?
) {
    val score1 = (s1?.toFloat() ?: 0f)
    val score2 = (s2?.toFloat() ?: 0f)
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
    score: Number?,
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
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                if (score != null) {
                    if (score is Double) String.format("%,.0f", score)
                    else String.format("%,d", score.toInt())
                } else "-",
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
