package com.tacke.benchmark.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tacke.benchmark.data.model.BenchmarkScores
import com.tacke.benchmark.data.model.ScoreType
import com.tacke.benchmark.data.model.SearchItem
import com.tacke.benchmark.data.repository.BenchmarkRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    val showSearchSheet: Boolean = false,
    val selectedHardware: List<SearchItem> = emptyList(),
    val selectedScoreType: ScoreType? = null,
    val benchmarkScores: List<BenchmarkScores?> = emptyList(),
    val isLoadingScores: Boolean = false,
    val errorMessage: String? = null,
    val selectingFor: Int = 0
)

class BenchmarkViewModel : ViewModel() {
    private val repository = BenchmarkRepository()

    private val _uiState = MutableStateFlow(BenchmarkUiState())
    val uiState: StateFlow<BenchmarkUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun setQueryMode(mode: QueryMode) {
        searchJob?.cancel()
        _uiState.update { 
            it.copy(
                queryMode = mode, 
                selectedHardware = emptyList(), 
                selectedScoreType = null,
                benchmarkScores = emptyList(), 
                searchQuery = "", 
                searchResults = emptyList(),
                showSearchSheet = false
            ) 
        }
    }

    fun closeSearchSheet() {
        searchJob?.cancel()
        _uiState.update {
            it.copy(showSearchSheet = false, searchQuery = "", searchResults = emptyList(), isSearching = false)
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
            return
        }
        searchJob = viewModelScope.launch {
            delay(350)
            performSearch(query.trim())
        }
    }

    private suspend fun performSearch(query: String) {
        _uiState.update { it.copy(isSearching = true, errorMessage = null) }
        val result = repository.searchGpu(query)
        result.fold(
            onSuccess = { items -> _uiState.update { it.copy(searchResults = items, isSearching = false) } },
            onFailure = { error -> _uiState.update { it.copy(isSearching = false, errorMessage = "搜索失败: ${error.message}") } }
        )
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
                selectedScoreType = null,
                benchmarkScores = emptyList(),
                showSearchSheet = false,
                searchQuery = "",
                searchResults = emptyList(),
                selectingFor = if (state.queryMode == QueryMode.COMPARE && updatedHardware.size < 2) 1 else 0
            )
        }
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
                showSearchSheet = true
            ) 
        }
    }

    fun removeHardware(index: Int) {
        _uiState.update { state ->
            val updatedHardware = state.selectedHardware.toMutableList()
            if (index < updatedHardware.size) {
                updatedHardware.removeAt(index)
            }
            state.copy(
                selectedHardware = updatedHardware,
                selectedScoreType = null,
                benchmarkScores = emptyList(),
                showSearchSheet = false
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
                showSearchSheet = false
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
