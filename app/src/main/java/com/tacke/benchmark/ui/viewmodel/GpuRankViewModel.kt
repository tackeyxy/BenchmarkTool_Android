package com.tacke.benchmark.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tacke.benchmark.data.model.GpuRankItem
import com.tacke.benchmark.data.repository.GpuRankRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GpuRankUiState(
    val ranks: List<GpuRankItem> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val refreshSuccess: Boolean = false
)

class GpuRankViewModel : ViewModel() {
    private val repository = GpuRankRepository()

    private val _uiState = MutableStateFlow(GpuRankUiState())
    val uiState: StateFlow<GpuRankUiState> = _uiState.asStateFlow()

    init {
        fetchRanks()
    }

    fun fetchRanks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.fetchGpuRanks()
            result.fold(
                onSuccess = { ranks ->
                    _uiState.update { it.copy(ranks = ranks, isLoading = false) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "数据加载失败: ${error.message}")
                    }
                }
            )
        }
    }

    fun refreshRanks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null, refreshSuccess = false) }
            val result = repository.fetchGpuRanks()
            result.fold(
                onSuccess = { ranks ->
                    _uiState.update { it.copy(ranks = ranks, isRefreshing = false, refreshSuccess = true) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(isRefreshing = false, errorMessage = "刷新失败: ${error.message}", refreshSuccess = false)
                    }
                }
            )
        }
    }

    fun clearRefreshSuccess() {
        _uiState.update { it.copy(refreshSuccess = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
