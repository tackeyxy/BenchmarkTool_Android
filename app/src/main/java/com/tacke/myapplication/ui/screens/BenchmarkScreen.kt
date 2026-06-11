package com.tacke.myapplication.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tacke.myapplication.data.model.BenchmarkScores
import com.tacke.myapplication.data.model.ScoreType
import com.tacke.myapplication.data.model.SearchItem
import com.tacke.myapplication.ui.components.*
import com.tacke.myapplication.ui.theme.*
import com.tacke.myapplication.ui.viewmodel.BenchmarkViewModel
import com.tacke.myapplication.ui.viewmodel.QueryMode

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
    
    if (uiState.isSearching || uiState.isLoadingScores) {
        LoadingDialog(if (uiState.isSearching) "正在搜索..." else "正在获取分数...")
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Background
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).statusBarsPadding()) {
            ModeSwitcher(uiState.queryMode, viewModel::setQueryMode)

            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
                item { Spacer(modifier = Modifier.height(16.dp)) }

                item { 
                    HardwareSelection(uiState, viewModel)
                }

                if (uiState.isSelectingHardware) {
                    item { Spacer(modifier = Modifier.height(12.dp)) }
                    item { SearchBar(uiState.searchQuery, viewModel::updateSearchQuery, viewModel::search, "输入显卡型号 (e.g., 4060)") }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
                
                if (uiState.searchResults.isNotEmpty()) {
                    items(uiState.searchResults) { item ->
                        SearchResultItem(item, onClick = { viewModel.selectHardware(item) })
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                
                if (uiState.selectedHardware.isNotEmpty()) {
                     item { Spacer(modifier = Modifier.height(12.dp)) }
                    item { 
                        Text("选择测试项目", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        ScoreTypeGrid(uiState.selectedScoreType, viewModel::selectScoreType)
                     }
                }

                if (uiState.benchmarkScores.isNotEmpty() && uiState.benchmarkScores.any { it != null }) {
                    item { Spacer(modifier = Modifier.height(12.dp)) }
                    item { 
                        ScoreResultSection(
                            uiState.queryMode,
                            uiState.selectedScoreType,
                            uiState.selectedHardware,
                            uiState.benchmarkScores
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
fun ModeSwitcher(selectedMode: QueryMode, onModeChange: (QueryMode) -> Unit) {
    TabRow(
        selectedTabIndex = selectedMode.ordinal,
        containerColor = Background,
        contentColor = Primary,
        divider = { HorizontalDivider(color = CardBorder) }
    ) {
        Tab(
            selected = selectedMode == QueryMode.SINGLE,
            onClick = { onModeChange(QueryMode.SINGLE) },
            text = { Text("单项查询") },
            selectedContentColor = Primary,
            unselectedContentColor = TextSecondary
        )
        Tab(
            selected = selectedMode == QueryMode.COMPARE,
            onClick = { onModeChange(QueryMode.COMPARE) },
            text = { Text("性能对比") },
            selectedContentColor = Primary,
            unselectedContentColor = TextSecondary
        )
    }
}

@Composable
fun HardwareSelection(uiState: com.tacke.myapplication.ui.viewmodel.BenchmarkUiState, viewModel: BenchmarkViewModel) {
    if (uiState.queryMode == QueryMode.SINGLE) {
        HardwareCard(uiState.selectedHardware.firstOrNull(), 0, viewModel)
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            HardwareCard(uiState.selectedHardware.getOrNull(0), 0, viewModel, Modifier.weight(1f))
            HardwareCard(uiState.selectedHardware.getOrNull(1), 1, viewModel, Modifier.weight(1f))
        }
    }
}

@Composable
fun HardwareCard(hardware: SearchItem?, index: Int, viewModel: BenchmarkViewModel, modifier: Modifier = Modifier) {
    val isSelected = viewModel.uiState.collectAsState().value.selectingFor == index && 
                     viewModel.uiState.collectAsState().value.isSelectingHardware
    
    Card(
        modifier = modifier.clickable { viewModel.setSelectingFor(index) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = if (isSelected) BorderStroke(2.dp, Primary) else BorderStroke(1.dp, CardBorder)
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
            if (hardware != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text(hardware.label, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Icon(Icons.Default.Done, contentDescription = "Selected", tint = GradientEnd)
                }
                IconButton(onClick = { viewModel.removeHardware(index) }, modifier = Modifier.align(Alignment.TopEnd).size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = TextSecondary, modifier = Modifier.size(16.dp))
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text("点击选择", color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Icon(Icons.Default.Add, contentDescription = "Add GPU", tint = TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun ScoreTypeGrid(selectedScoreType: ScoreType?, onScoreTypeSelect: (ScoreType) -> Unit) {
    val scoreTypes = ScoreType.getGpuBenchmarks()
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        scoreTypes.chunked(2).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                rowItems.forEach { scoreType ->
                    ScoreTypeChip(scoreType, selectedScoreType == scoreType, { onScoreTypeSelect(scoreType) }, Modifier.weight(1f))
                }
                 if (rowItems.size < 2) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ScoreTypeChip(scoreType: ScoreType, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) Primary else CardBackground)
            .border(1.dp, if(isSelected) Primary else CardBorder, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = scoreType.displayName.replace("3DMark ", ""),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else TextPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ScoreResultSection(
    mode: QueryMode,
    scoreType: ScoreType?,
    hardware: List<SearchItem>,
    scores: List<BenchmarkScores?>
) {
    Column {
        Text("跑分详情 (${scoreType?.displayName?.replace("3DMark ", "")})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        if (mode == QueryMode.SINGLE) {
            val h = hardware.firstOrNull()
            val s = scores.firstOrNull()
            if (h != null && s != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ScoreCard("最高分", s.maxScore, true, Modifier.weight(1f))
                    ScoreCard("中位分", s.medianScore, false, Modifier.weight(1f))
                }
            }
        } else {
             val score1 = scores.getOrNull(0)
             val score2 = scores.getOrNull(1)
             val maxScore1 = score1?.maxScore ?: 0
             val maxScore2 = score2?.maxScore ?: 0
            
             val medianScore1 = score1?.medianScore ?: 0.0
             val medianScore2 = score2?.medianScore ?: 0.0

            Text("最高分数", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = TextSecondary)
            Spacer(modifier = Modifier.height(8.dp))
            ScoreComparisonRow(hardware.getOrNull(0), maxScore1, hardware.getOrNull(1), maxScore2)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("中位分数", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = TextSecondary)
            Spacer(modifier = Modifier.height(8.dp))
            ScoreComparisonRow(hardware.getOrNull(0), medianScore1, hardware.getOrNull(1), medianScore2)
        }
    }
}

@Composable
fun ScoreComparisonRow(h1: SearchItem?, s1: Number, h2: SearchItem?, s2: Number) {
    val score1 = s1.toFloat()
    val score2 = s2.toFloat()
    val total = score1 + score2
    val ratio1 = if (total > 0) score1 / total else 0.5f
    val ratio2 = if (total > 0) score2 / total else 0.5f

    // Calculate percentage difference relative to the first score
    val percentageDiff = if (score1 != 0f) {
        ((score2 - score1) / score1) * 100
    } else {
        0f
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ComparisonBar(h1?.label, s1, ratio1, Primary)
        ComparisonBar(h2?.label, s2, ratio2, Accent, percentageDiff)
    }
}

@Composable
fun ComparisonBar(label: String?, score: Number, ratio: Float, color: Color, percentageDiff: Float = 0f) {
    val percentageText = if (percentageDiff != 0f) {
        val sign = if (percentageDiff > 0) "+" else ""
        String.format(locale = java.util.Locale.US, "%s%.1f%%", sign, percentageDiff)
    } else {
        ""
    }
    val percentageColor = if (percentageDiff < 0) Color.Red else if (percentageDiff > 0) Color(0xFF4CAF50) else TextSecondary

    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(label ?: "-", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), maxLines=1, overflow = TextOverflow.Ellipsis)
            Text(if (score is Double) String.format("%,.0f", score) else String.format("%,d", score), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = color)
            if (percentageText.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (percentageDiff > 0) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = "up", tint = percentageColor, modifier = Modifier.size(16.dp))
                    } else if (percentageDiff < 0) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = "down", tint = percentageColor, modifier = Modifier.size(16.dp))
                    }
                    Text(percentageText, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = percentageColor)
                }
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Box(modifier = Modifier.fillMaxWidth().height(6.dp).background(CardBackground, RoundedCornerShape(3.dp))) {
            Box(modifier = Modifier.fillMaxWidth(ratio).height(6.dp).background(color, RoundedCornerShape(3.dp)))
        }
    }
}
