package com.tacke.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tacke.myapplication.data.model.CpuBenchmarkScores
import com.tacke.myapplication.data.model.SearchItem
import com.tacke.myapplication.data.repository.CpuBenchmarkRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CpuBenchmarkUiState(
    val queryMode: QueryMode = QueryMode.SINGLE,
    val searchQuery: String = "",
    val searchResults: List<SearchItem> = emptyList(),
    val isSearching: Boolean = false,
    val showSearchSheet: Boolean = false,
    val selectedHardware: List<SearchItem> = emptyList(),
    val benchmarkScores: List<CpuBenchmarkScores?> = emptyList(),
    val isLoadingData: Boolean = true,
    val errorMessage: String? = null
)

class CpuBenchmarkViewModel : ViewModel() {
    private val repository = CpuBenchmarkRepository()

    private val _uiState = MutableStateFlow(CpuBenchmarkUiState())
    val uiState: StateFlow<CpuBenchmarkUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        fetchCpuData()
    }

    private fun fetchCpuData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingData = true) }
            val result = repository.fetchCpuScores()
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoadingData = false) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(isLoadingData = false, errorMessage = "数据加载失败: ${error.message}")
                    }
                }
            )
        }
    }

    fun setQueryMode(mode: QueryMode) {
        searchJob?.cancel()
        _uiState.update {
            it.copy(
                queryMode = mode,
                selectedHardware = emptyList(),
                benchmarkScores = emptyList(),
                searchQuery = "",
                searchResults = emptyList(),
                errorMessage = null
            )
        }
    }

    fun openSearchSheet() {
        _uiState.update {
            it.copy(showSearchSheet = true, searchQuery = "", searchResults = emptyList(), errorMessage = null)
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

    private fun performSearch(query: String) {
        _uiState.update { it.copy(isSearching = true) }
        val results = repository.searchCpu(query)
        _uiState.update {
            it.copy(
                searchResults = results.map { SearchItem(it.ranking, it.cpu) },
                isSearching = false
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
                if (updatedHardware.size < 2) {
                    updatedHardware.add(item)
                }
            }
            state.copy(
                selectedHardware = updatedHardware,
                showSearchSheet = false,
                searchQuery = "",
                searchResults = emptyList()
            )
        }
        fetchBenchmarkScores()
    }

    fun removeHardware(index: Int) {
        _uiState.update { state ->
            val updatedHardware = state.selectedHardware.toMutableList()
            val updatedScores = state.benchmarkScores.toMutableList()
            if (index < updatedHardware.size) updatedHardware.removeAt(index)
            if (index < updatedScores.size) updatedScores.removeAt(index)
            state.copy(selectedHardware = updatedHardware, benchmarkScores = updatedScores)
        }
        fetchBenchmarkScores()
    }

    private fun fetchBenchmarkScores() {
        val hardwareList = _uiState.value.selectedHardware
        if (hardwareList.isEmpty()) return

        val scores = hardwareList.map { hw ->
            repository.getScores(hw.label)
        }
        _uiState.update { it.copy(benchmarkScores = scores) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
