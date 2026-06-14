package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.db.BookmarkEntity
import com.example.data.model.Surah
import com.example.data.repository.QuranRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Success(val surahs: List<Surah>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

class QuranViewModel(private val repository: QuranRepository) : ViewModel() {

    private val _rawSurahList = MutableStateFlow<List<Surah>>(emptyList())
    private val _homeUiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val homeUiState: StateFlow<HomeUiState> = _homeUiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTab = MutableStateFlow(0) // 0: All Surahs, 1: Bookmarked
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // Real-time flow of all bookmarked verses stored locally in the database
    val bookmarkedVerses: StateFlow<List<BookmarkEntity>> = repository.getAllBookmarksFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Dynamic clean filtering based on name, English translation description, or index number
    val filteredSurahs: StateFlow<List<Surah>> = combine(_rawSurahList, _searchQuery) { list, query ->
        if (query.isBlank()) {
            list
        } else {
            list.filter { surah ->
                surah.englishName.contains(query, ignoreCase = true) ||
                surah.name.contains(query) ||
                surah.number.toString() == query.trim()
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadSurahList()
    }

    fun loadSurahList() {
        viewModelScope.launch {
            _homeUiState.value = HomeUiState.Loading
            try {
                val data = repository.getSurahs()
                _rawSurahList.value = data
                _homeUiState.value = HomeUiState.Success(data)
            } catch (e: Exception) {
                _homeUiState.value = HomeUiState.Error(e.localizedMessage ?: "Unknown error loading Surahs")
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSelectedTab(index: Int) {
        _selectedTab.value = index
    }

    fun toggleBookmarkRemoval(surahNumber: Int, ayahNumber: Int) {
        viewModelScope.launch {
            repository.removeBookmark(surahNumber, ayahNumber)
        }
    }
}

class QuranViewModelFactory(private val repository: QuranRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuranViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QuranViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
