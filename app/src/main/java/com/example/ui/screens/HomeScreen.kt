package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CombinedAyah
import com.example.ui.components.AyahCard
import com.example.ui.components.SurahCard
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.DeepMidnight
import com.example.ui.theme.GoldAccent
import com.example.ui.viewmodel.HomeUiState
import com.example.ui.viewmodel.QuranViewModel

@Composable
fun HomeScreen(
    viewModel: QuranViewModel,
    onSurahClick: (Int, String, String) -> Unit,
    onAboutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.homeUiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val bookmarkedVerses by viewModel.bookmarkedVerses.collectAsState()
    val filteredSurahs by viewModel.filteredSurahs.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepMidnight)
            .statusBarsPadding()
    ) {
        // App brand header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Al-Quran",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = GoldAccent,
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    text = "Arabic & Tamil (தமிழ்) Translation",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = DarkTextSecondary
                    )
                )
            }

            IconButton(
                onClick = onAboutClick,
                modifier = Modifier.testTag("about_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "About Developer",
                    tint = GoldAccent,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Search Bar (Filtered by index number or English/Arabic title)
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            placeholder = { Text("Search surah name or number...", color = DarkTextSecondary) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = GoldAccent.copy(alpha = 0.7f)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp)
                .testTag("search_field"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = GoldAccent,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Custom Gold / Slate Tab selection
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val tabs = listOf("All Surahs", "Bookmarked")
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTab == index
                val tabBgColor by animateColorAsState(
                    targetValue = if (isSelected) GoldAccent.copy(alpha = 0.14f) else Color.Transparent,
                    label = "tabBg"
                )
                val tabTextColor by animateColorAsState(
                    targetValue = if (isSelected) GoldAccent else DarkTextSecondary,
                    label = "tabText"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(tabBgColor)
                        .clickable { viewModel.updateSelectedTab(index) }
                        .padding(vertical = 10.dp)
                        .testTag("tab_button_$index"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = tabTextColor,
                            fontSize = 14.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Screen Content (Surah list or Bookmarked items)
        when (selectedTab) {
            0 -> {
                when (uiState) {
                    is HomeUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = GoldAccent)
                        }
                    }
                    is HomeUiState.Success -> {
                        if (filteredSurahs.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "No Surahs found for your query",
                                    color = DarkTextSecondary,
                                    textAlign = TextAlign.Center,
                                    fontSize = 15.sp
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("surah_list")
                            ) {
                                items(
                                    items = filteredSurahs,
                                    key = { it.number }
                                ) { surah ->
                                    SurahCard(
                                        surah = surah,
                                        onClick = { onSurahClick(surah.number, surah.englishName, surah.name) }
                                    )
                                }
                            }
                        }
                    }
                    is HomeUiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Failed to load Al-Quran metadata",
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = (uiState as HomeUiState.Error).message,
                                    color = DarkTextSecondary,
                                    textAlign = TextAlign.Center,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
            1 -> {
                if (bookmarkedVerses.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No Bookmarks Added",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = GoldAccent,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Save individual Ayahs in Surah detail mode to view them offline anytime.",
                                color = DarkTextSecondary,
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("bookmark_list")
                    ) {
                        items(
                            items = bookmarkedVerses,
                            key = { "${it.surahNumber}_${it.ayahNumber}" }
                        ) { bookmark ->
                            val combined = CombinedAyah(
                                numberInSurah = bookmark.ayahNumber,
                                textArabic = bookmark.textArabic,
                                textTamil = bookmark.textTamil,
                                audioUrl = bookmark.audioUrl,
                                isBookmarked = true,
                                isPlaying = false
                            )
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // Dynamic text headers showing Surah & ayah identifier
                                Text(
                                    text = "${bookmark.surahNumber}. ${bookmark.surahEnglishName} (${bookmark.surahArabicName}) • Ayah ${bookmark.ayahNumber}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = GoldAccent,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 2.dp)
                                )
                                AyahCard(
                                    ayah = combined,
                                    playingAyahNumber = null,
                                    onPlayClick = { }, // Detail activity performs full streaming
                                    onBookmarkClick = { viewModel.toggleBookmarkRemoval(bookmark.surahNumber, bookmark.ayahNumber) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
