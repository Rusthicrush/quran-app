package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Surah(
    val number: Int,
    val name: String,
    val englishName: String,
    val englishNameTranslation: String,
    val numberOfAyahs: Int,
    val revelationType: String
)

@JsonClass(generateAdapter = true)
data class SurahListResponse(
    val code: Int,
    val status: String,
    val data: List<Surah>
)

@JsonClass(generateAdapter = true)
data class Ayah(
    val number: Int,
    val audio: String? = null,
    val text: String,
    val numberInSurah: Int
)

@JsonClass(generateAdapter = true)
data class SurahDetail(
    val number: Int,
    val name: String,
    val englishName: String,
    val numberOfAyahs: Int,
    val ayahs: List<Ayah>
)

@JsonClass(generateAdapter = true)
data class SurahDetailResponse(
    val code: Int,
    val status: String,
    val data: SurahDetail
)

@JsonClass(generateAdapter = true)
data class TamilAyah(
    val number: Int,
    val text: String,
    val numberInSurah: Int
)

@JsonClass(generateAdapter = true)
data class TamilSurahDetail(
    val number: Int,
    val name: String,
    val ayahs: List<TamilAyah>
)

@JsonClass(generateAdapter = true)
data class TamilSurahResponse(
    val code: Int,
    val status: String,
    val data: TamilSurahDetail
)

// This combined model is used in the UI layer
data class CombinedAyah(
    val numberInSurah: Int,
    val textArabic: String,
    val textTamil: String,
    val audioUrl: String?,
    val isBookmarked: Boolean = false,
    val isPlaying: Boolean = false
)
