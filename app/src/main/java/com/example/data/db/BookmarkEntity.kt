package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    val surahNumber: Int,
    val ayahNumber: Int, // numberInSurah
    val surahEnglishName: String,
    val surahArabicName: String,
    val textArabic: String,
    val textTamil: String,
    val audioUrl: String?,
    val timestamp: Long = System.currentTimeMillis(),
    @PrimaryKey val uniqueId: String = "${surahNumber}_${ayahNumber}"
)
