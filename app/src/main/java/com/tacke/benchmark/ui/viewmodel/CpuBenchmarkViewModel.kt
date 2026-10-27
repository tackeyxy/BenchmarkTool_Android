package com.tacke.benchmark.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tacke.benchmark.data.model.CpuBenchmarkProject
import com.tacke.benchmark.data.model.CpuBenchmarkScores
import com.tacke.benchmark.data.model.SearchItem
import com.tacke.benchmark.data.repository.CpuBenchmarkRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CpuBenchmarkUiState(
    val queryMode: QueryMode = QueryMode.SINGLE,
    val selectedProject: CpuBenchmarkProject = CpuBenchmarkProject.CINEBENCH_2026,
    val searchQuery: String = "",
    val searchResults: List<SearchItem> = emptyList(),
    val isSearching: Boolean = false,
    val showSearchSheet: Boolean = false,
    val selectedHardware: List<SearchItem> = emptyList(),
    val benchmarkScores: List<CpuBenchmarkScores?> = emptyList(),
    val isLoadingData: Boolean = true,
    val isDataAvailable: Boolean = false,
    /** 标题实际使用的年份；切换项目时立即跟随所选项目更新。 */
    val benchmarkYear: String? = CpuBenchmarkProject.CINEBENCH_2026.year,
    val errorMessage: String? = null
)

class CpuBenchmarkViewModel : ViewModel() {
    private val repository = CpuBenchmarkRepository()

    private val _uiState = MutableStateFlow(CpuBenchmarkUiState())
    val uiState: StateFlow<CpuBenchmarkUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var loadJob: Job? = null
    private var loadGeneration = 0

    init {
        fetchCpuData()
    }

    private fun fetchCpuData() {
        val project = _uiState.value.selectedProject
        val generation = ++loadGeneration
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingData = true,
                    errorMessage = null,
                    isDataAvailable = repository.isDataLoaded(project)
                )
            }
            val result = repository.fetchCpuScores(project)
            // 用户已切到其他项目时，忽略旧请求的结果。
            if (generation != loadGeneration) return@launch
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoadingData = false,
                            isDataAvailable = true,
                            benchmarkYear = repository.benchmarkYear(project)
                        )
                    }
                    fetchBenchmarkScores()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoadingData = false,
                            isDataAvailable = repository.isDataLoaded(project),
                            benchmarkYear = repository.benchmarkYear(project),
                            errorMessage = "${project.title} 数据加载失败: ${error.message}"
                        )
                    }
                }
            )
        }
    }

    /** 数据加载失败后由界面上的「重试」按钮调用。 */
    fun retryLoadData() {
        if (_uiState.value.isLoadingData) return
        fetchCpuData()
    }

    /** 选择 Cinebench 2026 / 2024，重新查询当前项目的数据。 */
    fun selectProject(project: CpuBenchmarkProject) {
        if (project == _uiState.value.selectedProject) return
        searchJob?.cancel()
        _uiState.update {
            it.copy(
                selectedProject = project,
                benchmarkYear = project.year,
                selectedHardware = emptyList(),
                benchmarkScores = emptyList(),
                searchQuery = "",
                searchResults = emptyList(),
                isSearching = false,
                showSearchSheet = false,
                isDataAvailable = repository.isDataLoaded(project),
                errorMessage = null
            )
        }
        fetchCpuData()
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
        val project = _uiState.value.selectedProject
        _uiState.update { it.copy(isSearching = true) }
        val results = repository.searchCpu(project, query)
        if (project != _uiState.value.selectedProject) return
        _uiState.update {
            it.copy(
                searchResults = results.map { SearchItem(it.ranking.toString(), it.cpu) },
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
        val state = _uiState.value
        val hardwareList = state.selectedHardware
        if (hardwareList.isEmpty()) return

        val scores = hardwareList.map { hw ->
            runCatching { repository.getScores(state.selectedProject, hw.label) }.getOrNull()
        }
        if (state.selectedProject != _uiState.value.selectedProject) return
        _uiState.update { it.copy(benchmarkScores = scores) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
