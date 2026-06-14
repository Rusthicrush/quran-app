package com.example.data.repository

import com.example.data.api.QuranApiService
import com.example.data.db.BookmarkDao
import com.example.data.db.BookmarkEntity
import com.example.data.model.CombinedAyah
import com.example.data.model.Surah
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class QuranRepository(
    private val apiService: QuranApiService,
    private val bookmarkDao: BookmarkDao
) {
    // 1. Fetch Surah List
    suspend fun getSurahs(): List<Surah> {
        return apiService.getSurahs().data
    }

    // 2. Fetch and combine Arabic verses (including Aafasy audio) & Tamil translations
    suspend fun getCombinedSurah(surahNumber: Int): List<CombinedAyah> {
        // Fetch Arabic (with audio) first
        val arabicResponse = apiService.getSurahArabicWithAudio(surahNumber)
        // Fetch Tamil translation
        val tamilResponse = apiService.getSurahTamil(surahNumber)

        val arabicAyahs = arabicResponse.data.ayahs
        val tamilAyahs = tamilResponse.data.ayahs

        // Find Bookmarks for this surah to see which ones are already saved
        val bookmarks = bookmarkDao.getAllBookmarks().first()
        val bookmarkedAyahNumbers = bookmarks
            .filter { it.surahNumber == surahNumber }
            .map { it.ayahNumber }
            .toSet()

        return arabicAyahs.mapIndexed { index, arabicAyah ->
            val tamilTranslation = tamilAyahs.getOrNull(index)?.text ?: ""
            CombinedAyah(
                numberInSurah = arabicAyah.numberInSurah,
                textArabic = arabicAyah.text,
                textTamil = tamilTranslation,
                audioUrl = arabicAyah.audio,
                isBookmarked = bookmarkedAyahNumbers.contains(arabicAyah.numberInSurah)
            )
        }
    }

    // Listen to reactive updates on all bookmarks
    fun getAllBookmarksFlow(): Flow<List<BookmarkEntity>> = bookmarkDao.getAllBookmarks()

    // Add verse to bookmarks
    suspend fun addBookmark(bookmark: BookmarkEntity) {
        bookmarkDao.insertBookmark(bookmark)
    }

    // Remove verse from bookmarks
    suspend fun removeBookmark(surahNumber: Int, ayahNumber: Int) {
        bookmarkDao.deleteBookmark("${surahNumber}_${ayahNumber}")
    }
}
