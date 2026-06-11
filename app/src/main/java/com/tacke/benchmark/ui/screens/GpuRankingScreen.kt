package com.tacke.benchmark.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tacke.benchmark.ui.components.LoadingDialog
import com.tacke.benchmark.ui.theme.*
import com.tacke.benchmark.ui.viewmodel.GpuRankViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GpuRankingScreen(viewModel: GpuRankViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var searchQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val filteredRanks = remember(uiState.ranks, searchQuery) {
        if (searchQuery.isBlank()) uiState.ranks
        else uiState.ranks.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank() && filteredRanks.isNotEmpty()) {
            val originalIndex = uiState.ranks.indexOfFirst { it.name == filteredRanks.first().name }
            if (originalIndex >= 0) {
                listState.animateScrollToItem(originalIndex + 2)
            }
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.refreshSuccess) {
        if (uiState.refreshSuccess) {
            snackbarHostState.showSnackbar("刷新完成")
            viewModel.clearRefreshSuccess()
        }
    }

    if (uiState.isLoading) {
        LoadingDialog("正在加载排名数据...")
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(modifier = Modifier.fillMaxSize()) {
                GpuRankHeader()
                GpuRankSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it }
                )

                if (uiState.ranks.isNotEmpty()) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)
                    ) {
                        item { Spacer(modifier = Modifier.height(4.dp)) }
                        item {
                            RankTableHeader()
                        }
                        itemsIndexed(uiState.ranks) { index, item ->
                            val isHighlighted = searchQuery.isNotBlank() &&
                                item.name.contains(searchQuery, ignoreCase = true)
                            RankTableRow(
                                item = item,
                                index = index,
                                highlightQuery = if (isHighlighted) searchQuery else null
                            )
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }

                    if (searchQuery.isNotBlank() && filteredRanks.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "未找到匹配 \"$searchQuery\" 的显卡",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            FloatingRefreshButton(
                isLoading = uiState.isRefreshing,
                onClick = { viewModel.refreshRanks() }
            )
        }
    }
}

@Composable
private fun GpuRankSearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        placeholder = { Text("搜索显卡名称...", color = TextSecondary) },
        leadingIcon = { Icon(Icons.Default.Search, "搜索", tint = Primary) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, "清除", tint = TextSecondary)
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { }),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Primary,
            unfocusedBorderColor = CardBorder,
            focusedContainerColor = CardBackground,
            unfocusedContainerColor = CardBackground,
            cursorColor = Primary
        )
    )
}

@Composable
private fun GpuRankHeader() {
    Column(modifier = Modifier.padding(horizontal = 16.dp).padding(top = 8.dp, bottom = 4.dp)) {
        Text(
            "GPU 基准测试排名",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            "3DMark 显卡性能排行榜",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun RankTableHeader() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "排名",
                modifier = Modifier.width(44.dp),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Primary,
                textAlign = TextAlign.Center
            )
            Text(
                "名称",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Primary,
                textAlign = TextAlign.Center
            )
            Text(
                "类型",
                modifier = Modifier.width(72.dp),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Primary,
                textAlign = TextAlign.Center
            )
            Text(
                "分数",
                modifier = Modifier.width(80.dp),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Primary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RankTableRow(
    item: com.tacke.benchmark.data.model.GpuRankItem,
    index: Int,
    highlightQuery: String? = null
) {
    val bgColor = if (index % 2 == 0) Color.White else Color(0xFFFAFBFC)
    val isHighlighted = highlightQuery != null

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) Primary.copy(alpha = 0.05f) else bgColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val rankColor = when (item.rank) {
                1 -> Color(0xFFFFD700)
                2 -> Color(0xFFC0C0C0)
                3 -> Color(0xFFCD7F32)
                else -> TextSecondary
            }
            val rankBg = when (item.rank) {
                1 -> Color(0xFFFFF8E1)
                2 -> Color(0xFFF0F0F0)
                3 -> Color(0xFFF5EDE0)
                else -> Color.Transparent
            }

            Box(
                modifier = Modifier
                    .width(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(rankBg)
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${item.rank}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (item.rank <= 3) FontWeight.Bold else FontWeight.Medium,
                    color = rankColor
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            val nameText = if (highlightQuery != null) {
                buildAnnotatedString {
                    val text = item.name
                    val lowerText = text.lowercase()
                    val lowerQuery = highlightQuery.lowercase()
                    var start = 0
                    while (true) {
                        val found = lowerText.indexOf(lowerQuery, start)
                        if (found < 0) break
                        append(text.substring(start, found))
                        withStyle(SpanStyle(background = Color(0xFFFFEB3B).copy(alpha = 0.5f), fontWeight = FontWeight.Bold)) {
                            append(text.substring(found, found + highlightQuery.length))
                        }
                        start = found + highlightQuery.length
                    }
                    append(text.substring(start))
                }
            } else {
                buildAnnotatedString { append(item.name) }
            }

            Text(
                text = nameText,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.width(6.dp))

            Column(
                modifier = Modifier.width(72.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                item.type.split("/").forEach { part ->
                    val trimmed = part.trim()
                    val chipColor = when (trimmed) {
                        "Desktop" -> Color(0xFF7C3AED)
                        "Mobile" -> Color(0xFF06B6D4)
                        "Mac" -> Color(0xFF10B981)
                        else -> TextSecondary
                    }
                    val chipBg = when (trimmed) {
                        "Desktop" -> Color(0xFFF3E8FF)
                        "Mobile" -> Color(0xFFE0F2FE)
                        "Mac" -> Color(0xFFD1FAE5)
                        else -> Color(0xFFF1F5F9)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(chipBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = trimmed,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = MaterialTheme.typography.labelSmall.fontSize,
                            fontWeight = FontWeight.SemiBold,
                            color = chipColor,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = String.format("%,.0f", item.score),
                modifier = Modifier.width(80.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Accent,
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
private fun FloatingRefreshButton(
    isLoading: Boolean,
    onClick: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isLoading) 360f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "rotation"
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(end = 16.dp, bottom = 24.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        FloatingActionButton(
            onClick = onClick,
            shape = CircleShape,
            containerColor = Primary,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 4.dp,
                pressedElevation = 8.dp
            ),
            modifier = Modifier
                .size(48.dp)
                .shadow(4.dp, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "刷新排名数据",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
