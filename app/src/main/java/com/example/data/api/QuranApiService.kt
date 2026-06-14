package com.example.data.api

import com.example.data.model.SurahDetailResponse
import com.example.data.model.SurahListResponse
import com.example.data.model.TamilSurahResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface QuranApiService {
    @GET("surah")
    suspend fun getSurahs(): SurahListResponse

    @GET("surah/{number}/ar.alafasy")
    suspend fun getSurahArabicWithAudio(@Path("number") number: Int): SurahDetailResponse

    @GET("surah/{number}/ta.tamil")
    suspend fun getSurahTamil(@Path("number") number: Int): TamilSurahResponse

    companion object {
        private const val BASE_URL = "https://api.alquran.cloud/v1/"

        fun create(): QuranApiService {
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(QuranApiService::class.java)
        }
    }
}
