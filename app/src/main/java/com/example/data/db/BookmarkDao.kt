package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE surahNumber = :surahNumber ORDER BY ayahNumber ASC")
    fun getBookmarksForSurah(surahNumber: Int): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE uniqueId = :uniqueId")
    suspend fun deleteBookmark(uniqueId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE uniqueId = :uniqueId)")
    suspend fun isBookmarkedSync(uniqueId: String): Boolean
}
