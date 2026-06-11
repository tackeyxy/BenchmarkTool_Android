package com.tacke.benchmark.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tacke.benchmark.ui.theme.Primary
import com.tacke.benchmark.ui.theme.SurfaceVariant
import com.tacke.benchmark.ui.theme.TextSecondary
import com.tacke.benchmark.ui.viewmodel.CpuBenchmarkViewModel

enum class MainTab(val label: String, val icon: ImageVector) {
    GPU("显卡", Icons.Default.VideogameAsset),
    CPU("处理器", Icons.Default.Memory)
}

enum class GpuSubTab(val label: String) {
    RANK("基准测试排名"),
    BENCHMARK("显卡测试")
}

@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf(MainTab.GPU) }
    var gpuSubTab by remember { mutableStateOf(GpuSubTab.RANK) }
    val cpuViewModel = viewModel<CpuBenchmarkViewModel>()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                MainTab.entries.forEach { tab ->
                    val selected = selectedTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            selectedTab = tab
                            gpuSubTab = GpuSubTab.RANK
                        },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label
                            )
                        },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Primary,
                            selectedTextColor = Primary,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = Primary.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            Crossfade(
                targetState = selectedTab,
                animationSpec = tween(durationMillis = 300)
            ) { tab ->
                when (tab) {
                    MainTab.GPU -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            GpuSubTabBar(
                                selectedTab = gpuSubTab,
                                onTabChange = { gpuSubTab = it }
                            )
                            Crossfade(
                                targetState = gpuSubTab,
                                animationSpec = tween(durationMillis = 200)
                            ) { subTab ->
                                when (subTab) {
                                    GpuSubTab.RANK -> GpuRankingScreen()
                                    GpuSubTab.BENCHMARK -> BenchmarkScreen()
                                }
                            }
                        }
                    }
                    MainTab.CPU -> CpuBenchmarkScreen(viewModel = cpuViewModel)
                }
            }
        }
    }
}

@Composable
private fun GpuSubTabBar(selectedTab: GpuSubTab, onTabChange: (GpuSubTab) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            GpuSubTab.entries.forEach { tab ->
                val isSelected = selectedTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Primary else Color.Transparent)
                        .clickable { onTabChange(tab) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else TextSecondary
                    )
                }
            }
        }
    }
}
