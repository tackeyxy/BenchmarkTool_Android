package com.tacke.myapplication.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tacke.myapplication.data.model.SearchItem
import com.tacke.myapplication.ui.components.CpuSearchSheet
import com.tacke.myapplication.ui.components.LoadingDialog
import com.tacke.myapplication.ui.theme.*
import com.tacke.myapplication.ui.viewmodel.CpuBenchmarkViewModel
import com.tacke.myapplication.ui.viewmodel.QueryMode

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
        Column(modifier = Modifier.padding(paddingValues).statusBarsPadding()) {
            ModeSwitcher(uiState.queryMode, viewModel::setQueryMode)

            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                item { Spacer(modifier = Modifier.height(16.dp)) }

                item {
                    CpuHardwareSelection(uiState.selectedHardware, uiState.queryMode) {
                        viewModel.openSearchSheet()
                    }
                }

                if (uiState.selectedHardware.isNotEmpty()) {
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                    item {
                        CpuScoreResults(
                            uiState.queryMode,
                            uiState.selectedHardware,
                            uiState.benchmarkScores
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
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
private fun CpuHardwareSelection(
    selectedHardware: List<SearchItem>,
    queryMode: QueryMode,
    onAddClick: () -> Unit
) {
    if (queryMode == QueryMode.SINGLE) {
        CpuHardwareCard(selectedHardware.firstOrNull(), onAddClick)
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            CpuHardwareCard(selectedHardware.getOrNull(0), onAddClick, Modifier.weight(1f))
            CpuHardwareCard(selectedHardware.getOrNull(1), onAddClick, Modifier.weight(1f))
        }
    }
}

@Composable
private fun CpuHardwareCard(
    hardware: SearchItem?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
            if (hardware != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text(
                        hardware.label,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Icon(Icons.Default.Done, contentDescription = "Selected", tint = GradientEnd, modifier = Modifier.size(20.dp))
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text("点击选择处理器", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Icon(Icons.Default.Add, contentDescription = "Add CPU", tint = TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun CpuScoreResults(
    mode: QueryMode,
    hardware: List<SearchItem>,
    scores: List<com.tacke.myapplication.data.model.CpuBenchmarkScores?>
) {
    Column {
        Text("Cinebench 2024", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        if (mode == QueryMode.SINGLE) {
            val h = hardware.firstOrNull()
            val s = scores.firstOrNull()
            if (h != null && s != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    CpuScoreCard("单核分数", s.singleScore, Modifier.weight(1f))
                    CpuScoreCard("多核分数", s.multiScore, Modifier.weight(1f))
                }
            }
        } else {
            val score1 = scores.getOrNull(0)
            val score2 = scores.getOrNull(1)

            Text("单核分数", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = TextSecondary)
            Spacer(modifier = Modifier.height(8.dp))
            CpuScoreComparisonRow(hardware.getOrNull(0), score1?.singleScore, hardware.getOrNull(1), score2?.singleScore)

            Spacer(modifier = Modifier.height(16.dp))

            Text("多核分数", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = TextSecondary)
            Spacer(modifier = Modifier.height(8.dp))
            CpuScoreComparisonRow(hardware.getOrNull(0), score1?.multiScore, hardware.getOrNull(1), score2?.multiScore)
        }
    }
}

@Composable
private fun CpuScoreCard(title: String, score: Int?, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (score != null) String.format("%,d", score) else "-",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Primary
            )
        }
    }
}

@Composable
private fun CpuScoreComparisonRow(h1: SearchItem?, s1: Int?, h2: SearchItem?, s2: Int?) {
    val score1 = (s1 ?: 0).toFloat()
    val score2 = (s2 ?: 0).toFloat()
    val total = score1 + score2
    val ratio1 = if (total > 0) score1 / total else 0.5f
    val ratio2 = if (total > 0) score2 / total else 0.5f

    val percentageDiff = if (score1 != 0f) {
        ((score2 - score1) / score1) * 100
    } else 0f

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CpuComparisonBar(h1?.label, s1, ratio1, Primary)
        CpuComparisonBar(h2?.label, s2, ratio2, Accent, percentageDiff)
    }
}

@Composable
private fun CpuComparisonBar(label: String?, score: Int?, ratio: Float, color: Color, percentageDiff: Float = 0f) {
    val percentageText = if (percentageDiff != 0f) {
        val sign = if (percentageDiff > 0) "+" else ""
        String.format(locale = java.util.Locale.US, "%s%.1f%%", sign, percentageDiff)
    } else ""
    val percentageColor = if (percentageDiff < 0) Color.Red else if (percentageDiff > 0) Color(0xFF4CAF50) else TextSecondary

    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(label ?: "-", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(if (score != null) String.format("%,d", score) else "-", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = color)
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
