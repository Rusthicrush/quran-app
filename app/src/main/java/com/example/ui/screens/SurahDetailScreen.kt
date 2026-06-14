package com.example.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AyahCard
import com.example.ui.theme.ArabicColor
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.DeepMidnight
import com.example.ui.theme.GoldAccent
import com.example.ui.viewmodel.SurahDetailUiState
import com.example.ui.viewmodel.SurahDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahDetailScreen(
    viewModel: SurahDetailViewModel,
    onBackClick: () -> Unit,
    surahNumber: Int,
    surahEnglishName: String,
    surahArabicName: String,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val playingAyahNumber by viewModel.playingAyahNumber.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = surahEnglishName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent
                            ),
                            modifier = Modifier.testTag("detail_title")
                        )
                        Text(
                            text = "Surah #$surahNumber",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = DarkTextSecondary
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to list",
                            tint = GoldAccent
                        )
                    }
                },
                actions = {
                    Text(
                        text = surahArabicName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ArabicColor,
                            fontSize = 18.sp,
                            textAlign = TextAlign.End
                        ),
                        modifier = Modifier.padding(end = 16.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepMidnight,
                    titleContentColor = GoldAccent
                )
            )
        },
        containerColor = DeepMidnight,
        modifier = modifier.statusBarsPadding()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is SurahDetailUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GoldAccent)
                    }
                }
                is SurahDetailUiState.Success -> {
                    val listState = rememberLazyListState()
                    LaunchedEffect(playingAyahNumber) {
                        playingAyahNumber?.let { ayahNum ->
                            val index = state.ayahs.indexOfFirst { it.numberInSurah == ayahNum }
                            if (index >= 0) {
                                listState.animateScrollToItem(index)
                            }
                        }
                    }
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("ayah_list")
                    ) {
                        items(
                            items = state.ayahs,
                            key = { it.numberInSurah }
                        ) { ayah ->
                            AyahCard(
                                ayah = ayah,
                                playingAyahNumber = playingAyahNumber,
                                onPlayClick = { viewModel.playAudio(ayah.numberInSurah, ayah.audioUrl) },
                                onBookmarkClick = { viewModel.toggleBookmark(ayah) }
                            )
                        }
                    }
                }
                is SurahDetailUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Failed to load verses",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.message,
                                color = DarkTextSecondary,
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
