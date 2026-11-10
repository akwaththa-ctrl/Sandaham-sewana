package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.PlayerState
import com.example.data.model.SermonEntity
import com.example.ui.components.CategoryChipsRow
import com.example.ui.components.FeaturedHeroCard
import com.example.ui.components.SermonListItem
import com.example.ui.theme.AmberGold
import com.example.ui.theme.DeepMaroon
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.SaffronPrimary

@Composable
fun LibraryScreen(
    featuredSermon: SermonEntity?,
    sermons: List<SermonEntity>,
    selectedCategory: String,
    searchQuery: String,
    isCloudSyncing: Boolean,
    currentSermon: SermonEntity?,
    playerState: PlayerState,
    isAdminUnlocked: Boolean,
    onSelectCategory: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onPlaySermon: (SermonEntity) -> Unit,
    onToggleFavorite: (SermonEntity) -> Unit,
    onEditSermon: (SermonEntity) -> Unit,
    onDeleteSermon: (SermonEntity) -> Unit,
    onSyncCloud: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Prominent Main Screen Search Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text(
                        text = "දේශනා, පිරිත් හෝ ස්වාමීන්වහන්සේගේ නම සොයන්න...",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "සොයන්න",
                        tint = SaffronPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { onSearchQueryChange("") },
                            modifier = Modifier.testTag("clear_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "මකන්න",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SaffronPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("main_screen_search_bar")
            )
        }

        // Category Filter Chips
        CategoryChipsRow(
            selectedCategory = selectedCategory,
            onSelectCategory = onSelectCategory
        )

        // Cloud Firestore Status Banner
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isCloudSyncing) Icons.Default.CloudSync else Icons.Default.CloudDone,
                        contentDescription = null,
                        tint = if (isCloudSyncing) AmberGold else ForestGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isCloudSyncing) "Cloud Firestore සමඟ සමමුහුර්ත වෙමින්..." else "Cloud Firestore සජීවීව සම්බන්ධයි",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isCloudSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = SaffronPrimary
                    )
                } else {
                    Text(
                        text = "සජීවී දත්ත",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen
                    )
                }
            }
        }

        // Religious Content List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("sermons_list"),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Hero card displayed when not actively searching
            if (searchQuery.isBlank() && selectedCategory == "සියල්ල") {
                item {
                    FeaturedHeroCard(
                        featuredSermon = featuredSermon,
                        onPlayFeatured = onPlaySermon
                    )
                }
            }

            // Section Header with Results Count
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank()) "සෙවුම් ප්‍රතිඵල (${sermons.size})"
                        else if (selectedCategory != "සියල්ල") "$selectedCategory (${sermons.size})"
                        else "බෞද්ධ දේශනා සහ පිරිත් එකතුව (${sermons.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (searchQuery.isNotBlank() || selectedCategory != "සියල්ල") {
                        Text(
                            text = "සියල්ල පෙන්වන්න",
                            fontSize = 12.sp,
                            color = SaffronPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable {
                                    onSearchQueryChange("")
                                    onSelectCategory("සියල්ල")
                                }
                                .padding(4.dp)
                        )
                    }
                }
            }

            // Empty state
            if (sermons.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SearchOff,
                                    contentDescription = null,
                                    tint = SaffronPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Text(
                                text = "දේශනා හෝ පිරිත් හමු නොවීය",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = "ඔබ සෙවූ නමට අදාළ දේශනාවක් නොමැත. වෙනත් සෙවුම් පදයක් හෝ කාණ්ඩයක් තෝරා බලන්න.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )

                            OutlinedButton(
                                onClick = {
                                    onSearchQueryChange("")
                                    onSelectCategory("සියල්ල")
                                }
                            ) {
                                Text("සියලු දේශනා පෙන්වන්න")
                            }
                        }
                    }
                }
            } else {
                items(sermons, key = { it.id }) { sermon ->
                    SermonListItem(
                        sermon = sermon,
                        isPlayingThis = currentSermon?.id == sermon.id,
                        playerState = playerState,
                        isAdminUnlocked = isAdminUnlocked,
                        onPlayClick = { onPlaySermon(sermon) },
                        onToggleFavorite = { onToggleFavorite(sermon) },
                        onEditClick = { onEditSermon(sermon) },
                        onDeleteClick = { onDeleteSermon(sermon) }
                    )
                }
            }

            // Bottom space for floating mini-player
            item {
                Spacer(modifier = Modifier.height(96.dp))
            }
        }
    }
}
