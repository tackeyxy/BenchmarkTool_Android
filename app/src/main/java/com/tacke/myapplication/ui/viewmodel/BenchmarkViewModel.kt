package com.tacke.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tacke.myapplication.data.model.BenchmarkScores
import com.tacke.myapplication.data.model.ScoreType
import com.tacke.myapplication.data.model.SearchItem
import com.tacke.myapplication.data.repository.BenchmarkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class QueryMode { SINGLE, COMPARE }

data class BenchmarkUiState(
    val queryMode: QueryMode = QueryMode.SINGLE,
    val searchQuery: String = "",
    val searchResults: List<SearchItem> = emptyList(),
    val isSearching: Boolean = false,
    val selectedHardware: List<SearchItem> = emptyList(),
    val selectedScoreType: ScoreType? = null,
    val benchmarkScores: List<BenchmarkScores?> = emptyList(),
    val isLoadingScores: Boolean = false,
    val errorMessage: String? = null,
    val selectingFor: Int = 0,
    val isSelectingHardware: Boolean = false
)

class BenchmarkViewModel : ViewModel() {
    private val repository = BenchmarkRepository()

    private val _uiState = MutableStateFlow(BenchmarkUiState())
    val uiState: StateFlow<BenchmarkUiState> = _uiState.asStateFlow()

    fun setQueryMode(mode: QueryMode) {
        _uiState.update { 
            it.copy(
                queryMode = mode, 
                selectedHardware = emptyList(), 
                benchmarkScores = emptyList(), 
                searchQuery = "", 
                searchResults = emptyList(),
                isSelectingHardware = false
            ) 
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun search() {
        val query = _uiState.value.searchQuery.trim()
        if (query.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true, errorMessage = null) }
            val result = repository.searchGpu(query)
            result.fold(
                onSuccess = { items -> _uiState.update { it.copy(searchResults = items, isSearching = false) } },
                onFailure = { error -> _uiState.update { it.copy(isSearching = false, errorMessage = "搜索失败: ${error.message}") } }
            )
        }
    }

    fun selectHardware(item: SearchItem) {
        _uiState.update { state ->
            val updatedHardware = state.selectedHardware.toMutableList()
            if (state.queryMode == QueryMode.SINGLE) {
                updatedHardware.clear()
                updatedHardware.add(item)
            } else {
                if (state.selectingFor < updatedHardware.size) {
                    updatedHardware[state.selectingFor] = item
                } else {
                    updatedHardware.add(item)
                }
            }
            state.copy(
                selectedHardware = updatedHardware,
                searchResults = emptyList(),
                searchQuery = "",
                isSelectingHardware = false,
                selectingFor = if (state.queryMode == QueryMode.COMPARE && updatedHardware.size < 2) 1 else 0
            )
        }
        fetchBenchmarkScores()
    }

    fun selectScoreType(scoreType: ScoreType) {
        _uiState.update { it.copy(selectedScoreType = scoreType) }
        fetchBenchmarkScores()
    }

    fun fetchBenchmarkScores() {
        val hardwareList = _uiState.value.selectedHardware
        val scoreType = _uiState.value.selectedScoreType
        if (hardwareList.isEmpty() || scoreType == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingScores = true, errorMessage = null) }
            val scores = hardwareList.map { repository.getBenchmarkScores(scoreType, it.id).getOrNull() }
            _uiState.update { it.copy(benchmarkScores = scores, isLoadingScores = false) }
        }
    }

    fun setSelectingFor(index: Int) {
        _uiState.update { 
            it.copy(
                selectingFor = index, 
                searchQuery = "", 
                searchResults = emptyList(),
                isSelectingHardware = true
            ) 
        }
    }

    fun removeHardware(index: Int) {
        _uiState.update { state ->
            val updatedHardware = state.selectedHardware.toMutableList()
            val updatedScores = state.benchmarkScores.toMutableList()
            if (index < updatedHardware.size) {
                updatedHardware.removeAt(index)
            }
            if (index < updatedScores.size) {
                updatedScores.removeAt(index)
            }
            state.copy(
                selectedHardware = updatedHardware, 
                benchmarkScores = updatedScores,
                isSelectingHardware = false
            )
        }
    }

    fun resetSelection() {
        _uiState.update { 
            it.copy(
                selectedHardware = emptyList(),
                selectedScoreType = null,
                benchmarkScores = emptyList(),
                errorMessage = null,
                selectingFor = 0,
                searchQuery = "",
                searchResults = emptyList(),
                isSelectingHardware = false
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}