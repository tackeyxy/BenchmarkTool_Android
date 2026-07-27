package com.tacke.benchmark.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tacke.benchmark.data.model.GpuRankItem
import com.tacke.benchmark.ui.components.LoadingDialog
import com.tacke.benchmark.ui.theme.GradientEnd
import com.tacke.benchmark.ui.theme.GradientStart
import com.tacke.benchmark.ui.theme.Primary
import com.tacke.benchmark.ui.theme.Secondary

import com.tacke.benchmark.ui.viewmodel.GpuRankViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GpuRankingScreen(viewModel: GpuRankViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    val listState = rememberLazyListState()

    val categories = listOf("All", "Win", "Mac")

    val filteredRanks = remember(uiState.ranks, searchQuery, selectedCategory) {
        uiState.ranks.filter { item ->
            val matchSearch = searchQuery.isBlank() ||
                item.name.contains(searchQuery, ignoreCase = true)
            val matchCategory = when {
                selectedCategory == "All" -> true
                selectedCategory == "Win" -> item.type.contains("Desktop", ignoreCase = true) ||
                    item.type.contains("Mobile", ignoreCase = true)
                else -> item.type.contains(selectedCategory, ignoreCase = true)
            }
            matchSearch && matchCategory
        }
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank() && filteredRanks.isNotEmpty()) {
            listState.animateScrollToItem(0)
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
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(modifier = Modifier.fillMaxSize()) {
                GpuRankHeader(searchQuery) { searchQuery = it }
                CategoryFilter(
                    categories = categories,
                    selected = selectedCategory,
                    onSelect = { selectedCategory = it }
                )

                if (uiState.ranks.isNotEmpty()) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        if (filteredRanks.isEmpty() && searchQuery.isNotBlank()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 48.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "未找到匹配 \"$searchQuery\" 的显卡",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        itemsIndexed(filteredRanks) { index, item ->
                            val isHighlighted = searchQuery.isNotBlank() &&
                                item.name.contains(searchQuery, ignoreCase = true)
                            if (item.rank <= 3) {
                                HeroRankCard(
                                    item = item,
                                    highlightQuery = if (isHighlighted) searchQuery else null
                                )
                            } else {
                                CompactRankCard(
                                    item = item,
                                    maxScore = filteredRanks.firstOrNull()?.score ?: 1.0,
                                    highlightQuery = if (isHighlighted) searchQuery else null
                                )
                            }
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
private fun GpuRankHeader(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 4.dp, bottom = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(listOf(GradientStart, GradientEnd))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Leaderboard,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column {
                Text(
                    "GPU 性能排行榜",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "3DMark 显卡性能排名",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text("搜索显卡名称...", color = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "搜索",
                    tint = Primary,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "清除",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { }),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                cursorColor = Primary,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

@Composable
private fun CategoryFilter(
    categories: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            val isSelected = category == selected
            Box(
                modifier = if (isSelected) {
                    Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.linearGradient(listOf(GradientStart, GradientEnd)))
                        .clickable { onSelect(category) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                } else {
                    Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onSelect(category) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun HeroRankCard(
    item: GpuRankItem,
    highlightQuery: String?
) {
    val medalColors = when (item.rank) {
        1 -> listOf(Color(0xFFFFD700), Color(0xFFFFA500))
        2 -> listOf(Color(0xFFE0E0E0), Color(0xFF9E9E9E))
        3 -> listOf(Color(0xFFE8C9A0), Color(0xFFCD7F32))
        else -> listOf(Color.Gray, Color.Gray)
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            medalColors.first().copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(medalColors)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "#${item.rank}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (item.rank == 1) Color(0xFF5C3A00) else Color(0xFF333333)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    val displayName = buildHighlightedText(item.name, highlightQuery)
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    TypeBadges(item.type)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val maxVal = item.score
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(1f)
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(
                                Brush.horizontalGradient(listOf(medalColors.first(), medalColors.first().copy(alpha = 0.4f)))
                            )
                    )
                }
                Text(
                    text = String.format("%,.0f", item.score),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    fontWeight = FontWeight.Bold,
                    color = medalColors.first()
                )
            }
        }
    }
}

@Composable
private fun CompactRankCard(
    item: GpuRankItem,
    maxScore: Double,
    highlightQuery: String?
) {
    val ratio = (item.score / maxScore).coerceIn(0.0, 1.0).toFloat()
    val animRatio by animateFloatAsState(
        targetValue = ratio,
        animationSpec = tween(800),
        label = "rankBar"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.width(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${item.rank}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                val displayName = buildHighlightedText(item.name, highlightQuery)
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                TypeBadges(item.type)
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.width(60.dp)
            ) {
                Text(
                    text = String.format("%,.0f", item.score),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    ),
                    fontWeight = FontWeight.Bold,
                    color = Primary,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animRatio)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                Brush.horizontalGradient(listOf(GradientStart, GradientEnd))
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun TypeBadges(type: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        type.split("/").forEach { part ->
            val trimmed = part.trim()
            val (chipColor, chipBg) = when (trimmed) {
                "Desktop" -> Primary to Primary.copy(alpha = 0.1f)
                "Mobile" -> Secondary to Secondary.copy(alpha = 0.1f)
                "Mac" -> Color(0xFF10B981) to Color(0xFF10B981).copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.onSurfaceVariant to MaterialTheme.colorScheme.surfaceVariant
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(chipBg)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = trimmed,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = chipColor,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun buildHighlightedText(
    text: String,
    highlightQuery: String?
) = buildAnnotatedString {
    if (highlightQuery != null) {
        val lowerText = text.lowercase()
        val lowerQuery = highlightQuery.lowercase()
        var start = 0
        while (true) {
            val found = lowerText.indexOf(lowerQuery, start)
            if (found < 0) break
            append(text.substring(start, found))
            withStyle(
                SpanStyle(
                    background = Primary.copy(alpha = 0.2f),
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            ) {
                append(text.substring(found, found + highlightQuery.length))
            }
            start = found + highlightQuery.length
        }
        append(text.substring(start))
    } else {
        append(text)
    }
}

@Composable
private fun FloatingRefreshButton(
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(end = 16.dp, bottom = 16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        FloatingActionButton(
            onClick = onClick,
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.Transparent,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp
            ),
            modifier = Modifier
                .size(52.dp)
                .shadow(8.dp, RoundedCornerShape(16.dp))
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(listOf(GradientStart, GradientEnd))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "刷新排名数据",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
