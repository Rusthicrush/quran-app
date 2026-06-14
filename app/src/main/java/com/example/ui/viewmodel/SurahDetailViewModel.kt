package com.example.ui.viewmodel

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.db.BookmarkEntity
import com.example.data.model.CombinedAyah
import com.example.data.repository.QuranRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SurahDetailUiState {
    object Loading : SurahDetailUiState
    data class Success(val ayahs: List<CombinedAyah>) : SurahDetailUiState
    data class Error(val message: String) : SurahDetailUiState
}

class SurahDetailViewModel(
    private val repository: QuranRepository,
    private val surahNumber: Int,
    private val surahEnglishName: String,
    private val surahArabicName: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<SurahDetailUiState>(SurahDetailUiState.Loading)
    val uiState: StateFlow<SurahDetailUiState> = _uiState.asStateFlow()

    private val _playingAyahNumber = MutableStateFlow<Int?>(null)
    val playingAyahNumber: StateFlow<Int?> = _playingAyahNumber.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null

    init {
        loadSurahData()
    }

    fun loadSurahData() {
        viewModelScope.launch {
            _uiState.value = SurahDetailUiState.Loading
            try {
                val data = repository.getCombinedSurah(surahNumber)
                _uiState.value = SurahDetailUiState.Success(data)
            } catch (e: Exception) {
                _uiState.value = SurahDetailUiState.Error(e.localizedMessage ?: "Failed to load Ayahs")
            }
        }
    }

    fun toggleBookmark(ayah: CombinedAyah) {
        viewModelScope.launch {
            if (ayah.isBookmarked) {
                repository.removeBookmark(surahNumber, ayah.numberInSurah)
            } else {
                val bookmark = BookmarkEntity(
                    surahNumber = surahNumber,
                    ayahNumber = ayah.numberInSurah,
                    surahEnglishName = surahEnglishName,
                    surahArabicName = surahArabicName,
                    textArabic = ayah.textArabic,
                    textTamil = ayah.textTamil,
                    audioUrl = ayah.audioUrl
                )
                repository.addBookmark(bookmark)
            }

            // Immediately reflect bookmark toggle in UI State seamlessly
            val currentState = _uiState.value
            if (currentState is SurahDetailUiState.Success) {
                val updated = currentState.ayahs.map {
                    if (it.numberInSurah == ayah.numberInSurah) {
                        it.copy(isBookmarked = !ayah.isBookmarked)
                    } else {
                        it
                    }
                }
                _uiState.value = SurahDetailUiState.Success(updated)
            }
        }
    }

    fun playAudio(ayahNumber: Int, audioUrl: String?) {
        if (audioUrl.isNullOrEmpty()) return

        if (_playingAyahNumber.value == ayahNumber) {
            // Under user rules: Pressing same ayah pauses/stops it
            pauseAndStopAudio()
            _playingAyahNumber.value = null
        } else {
            // Stop prior audio first & update current playing index
            pauseAndStopAudio()
            _playingAyahNumber.value = ayahNumber

            try {
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(audioUrl)
                    setOnPreparedListener { mp ->
                        mp.start()
                    }
                    setOnCompletionListener {
                        _playingAyahNumber.value = null
                        cleanUpMediaPlayer()

                        val state = _uiState.value
                        if (state is SurahDetailUiState.Success) {
                            val currentList = state.ayahs
                            val currentIndex = currentList.indexOfFirst { it.numberInSurah == ayahNumber }
                            if (currentIndex != -1 && currentIndex + 1 < currentList.size) {
                                val nextAyah = currentList[currentIndex + 1]
                                playAudio(nextAyah.numberInSurah, nextAyah.audioUrl)
                            }
                        }
                    }
                    setOnErrorListener { _, what, extra ->
                        Log.e("QuranAudioPlayer", "Error loading ayah audio: what=$what, extra=$extra")
                        _playingAyahNumber.value = null
                        cleanUpMediaPlayer()
                        true
                    }
                    prepareAsync()
                }
            } catch (e: Exception) {
                Log.e("QuranAudioPlayer", "Failed playing audio stream: ${e.message}")
                _playingAyahNumber.value = null
                cleanUpMediaPlayer()
            }
        }
    }

    private fun pauseAndStopAudio() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {
            Log.e("QuranAudioPlayer", "Error stopping MediaPlayer safely: ${e.message}")
        } finally {
            mediaPlayer = null
        }
    }

    private fun cleanUpMediaPlayer() {
        try {
            mediaPlayer?.release()
        } catch (e: Exception) {
            Log.e("QuranAudioPlayer", "Error releasing MediaPlayer: ${e.message}")
        } finally {
            mediaPlayer = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        pauseAndStopAudio()
    }
}

class SurahDetailViewModelFactory(
    private val repository: QuranRepository,
    private val surahNumber: Int,
    private val surahEnglishName: String,
    private val surahArabicName: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SurahDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SurahDetailViewModel(repository, surahNumber, surahEnglishName, surahArabicName) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
