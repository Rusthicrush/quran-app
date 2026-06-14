package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CombinedAyah
import com.example.ui.theme.ArabicColor
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.TamilColor

@Composable
fun AyahCard(
    ayah: CombinedAyah,
    playingAyahNumber: Int?,
    onPlayClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCurrentlyPlaying = playingAyahNumber == ayah.numberInSurah

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ayah_card_${ayah.numberInSurah}")
            .padding(vertical = 8.dp, horizontal = 14.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentlyPlaying) {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            width = if (isCurrentlyPlaying) 1.5.dp else 1.dp,
            color = if (isCurrentlyPlaying) GoldAccent else MaterialTheme.colorScheme.outline
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Verse Header (Index Badge & Player controls)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Verse Index Rounded Indicator
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(GoldAccent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = ayah.numberInSurah.toString(),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent,
                            fontSize = 13.sp
                        )
                    )
                }

                // Interactive Audio & Bookmark actions
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!ayah.audioUrl.isNullOrEmpty()) {
                        IconButton(
                            onClick = onPlayClick,
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("play_button_${ayah.numberInSurah}")
                        ) {
                            Icon(
                                imageVector = if (isCurrentlyPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = if (isCurrentlyPlaying) "Pause" else "Play",
                                tint = GoldAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    IconButton(
                        onClick = onBookmarkClick,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("bookmark_button_${ayah.numberInSurah}")
                    ) {
                        Icon(
                            imageVector = if (ayah.isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            contentDescription = if (ayah.isBookmarked) "Delete Bookmark" else "Add Bookmark",
                            tint = GoldAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Arabic text (Large, Gold ArabicColor, RTL, fontSize 24)
            Text(
                text = ayah.textArabic,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 24.sp,
                    color = ArabicColor,
                    lineHeight = 38.sp,
                    textAlign = TextAlign.Right
                ),
                modifier = Modifier.fillMaxWidth(),
                softWrap = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tamil Translation text (Blue/Sky TamilColor, fontSize 15)
            Text(
                text = ayah.textTamil,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    color = TamilColor,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Start
                ),
                modifier = Modifier.fillMaxWidth(),
                softWrap = true
            )
        }
    }
}
