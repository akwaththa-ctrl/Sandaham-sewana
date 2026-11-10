package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AmberGold
import com.example.ui.theme.DeepMaroon
import com.example.ui.theme.SaffronPrimary
import com.example.ui.viewmodel.AppScreen

@Composable
fun TopDhammaBar(
    currentScreen: AppScreen,
    isAdminUnlocked: Boolean,
    favoritesCount: Int,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onNavigate: (AppScreen) -> Unit,
    onOpenAdmin: () -> Unit
) {
    var isSearchExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Lotus emblem
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(AmberGold, SaffronPrimary, DeepMaroon)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_buddhist_dhamma),
                        contentDescription = "සදහම් සෙවණ ලාංඡනය",
                        modifier = Modifier.size(38.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "සදහම් සෙවණ",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = "බෞද්ධ දේශනා සහ පිරිත්",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    )
                }

                // Search button
                IconButton(
                    onClick = {
                        isSearchExpanded = !isSearchExpanded
                        if (!isSearchExpanded) onSearchChange("")
                    },
                    modifier = Modifier.testTag("toggle_search_button")
                ) {
                    Icon(
                        imageVector = if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = "දේශනා සොයන්න",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Favorites button
                IconButton(
                    onClick = {
                        if (currentScreen == AppScreen.FAVORITES) onNavigate(AppScreen.LIBRARY)
                        else onNavigate(AppScreen.FAVORITES)
                    },
                    modifier = Modifier.testTag("nav_favorites_button")
                ) {
                    if (favoritesCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(containerColor = SaffronPrimary) {
                                    Text(
                                        text = favoritesCount.toString(),
                                        color = Color.White,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "ප්‍රියතම දේශනා",
                                tint = if (currentScreen == AppScreen.FAVORITES) SaffronPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "ප්‍රියතම දේශනා",
                            tint = if (currentScreen == AppScreen.FAVORITES) SaffronPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Admin Button
                IconButton(
                    onClick = {
                        if (currentScreen == AppScreen.ADMIN) onNavigate(AppScreen.LIBRARY)
                        else onOpenAdmin()
                    },
                    modifier = Modifier.testTag("nav_admin_button")
                ) {
                    Icon(
                        imageVector = if (isAdminUnlocked) Icons.Default.AdminPanelSettings else Icons.Default.Lock,
                        contentDescription = "ඇඩ්මින් කළමනාකරණය",
                        tint = if (currentScreen == AppScreen.ADMIN || isAdminUnlocked) AmberGold else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Expandable Search Bar
            AnimatedVisibility(visible = isSearchExpanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = { Text("දේශනාව, ස්වාමීන්වහන්සේගේ නම සොයන්න...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = SaffronPrimary
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchChange("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "මකන්න"
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_text_input"),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SaffronPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}
