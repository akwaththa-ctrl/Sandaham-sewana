package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SermonEntity
import com.example.ui.components.GoogleSignInButton
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberGoldLight
import com.example.ui.theme.DeepMaroon
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.SaffronPrimary
import com.google.firebase.auth.FirebaseUser
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.ExitToApp

@Composable
fun AdminManagementScreen(
    isAdminUnlocked: Boolean,
    currentUser: FirebaseUser?,
    sermons: List<SermonEntity>,
    pinInput: String,
    pinError: String?,
    onPinChange: (String) -> Unit,
    onVerifyPin: (String) -> Boolean,
    onLockAdmin: () -> Unit,
    onAddNewSermon: () -> Unit,
    onEditSermon: (SermonEntity) -> Unit,
    onDeleteSermon: (SermonEntity) -> Unit,
    onRestoreDefaults: () -> Unit,
    onSignOut: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var sermonToDelete by remember { mutableStateOf<SermonEntity?>(null) }
    var showRestoreConfirmDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("admin_back_button")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "ආපසු"
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ඇඩ්මින් කළමනාකරණය",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = SaffronPrimary
                        )
                    )
                    Text(
                        text = if (isAdminUnlocked) "පිරිත් සහ බණ දේශනා ඇතුළත් කිරීමේ පුවරුව" else "කරුණාකර PIN අංකය ඇතුළත් කරන්න",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    )
                }

                if (isAdminUnlocked) {
                    IconButton(onClick = onLockAdmin, modifier = Modifier.testTag("lock_admin_button")) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "වසන්න",
                            tint = SaffronPrimary
                        )
                    }
                }
            }
        }

        if (!isAdminUnlocked) {
            // PIN Entry Screen
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(AmberGoldLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = DeepMaroon,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Text(
                            text = "ඇඩ්මින් ප්‍රවේශය (Admin Access)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "දේශනා ඇතුළත් කිරීම සහ කළමනාකරණය සඳහා රහස්‍ය PIN අංකය ඇතුළත් කරන්න. (පෙරනිමි PIN: 1234)",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )

                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = onPinChange,
                            label = { Text("PIN අංකය") },
                            placeholder = { Text("1234") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            isError = pinError != null,
                            modifier = Modifier.fillMaxWidth().testTag("admin_pin_input")
                        )

                        if (pinError != null) {
                            Text(
                                text = pinError,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp
                            )
                        }

                        Button(
                            onClick = { onVerifyPin(pinInput) },
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().testTag("admin_unlock_button")
                        ) {
                            Icon(imageVector = Icons.Default.LockOpen, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ඇතුළු වන්න", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // Unlocked Admin Management Dashboard
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(6.dp))

                    // Cloud Firestore & Google Auth Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (currentUser != null) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (currentUser != null) Icons.Default.CloudDone else Icons.Default.CloudQueue,
                                        contentDescription = null,
                                        tint = if (currentUser != null) Color(0xFF2D6A4F) else Color(0xFFC86400)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (currentUser != null) "Cloud Firestore සක්‍රියයි" else "Cloud Firestore සබඳතාවය",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                if (currentUser != null) {
                                    IconButton(onClick = onSignOut, modifier = Modifier.size(28.dp)) {
                                        Icon(
                                            imageVector = Icons.Default.ExitToApp,
                                            contentDescription = "Sign out",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            if (currentUser != null) {
                                Text(
                                    text = "ගිණුම: ${currentUser.email ?: currentUser.displayName ?: "සම්බන්ධිතයි"}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "ඔබ ඇතුළත් කරන සියලුම පිරිත් සහ දේශනා Cloud Firestore දත්ත ගබඩාවට ආරක්ෂිතව සුරැකේ.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Text(
                                    text = "දේශනා Cloud Firestore දත්ත ගබඩාවට ස්ථිරව සුරැකීමට Google ගිණුමෙන් ඇතුළු වන්න:",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                GoogleSignInButton(
                                    onAuthSuccess = {},
                                    onAuthError = {}
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Metrics row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricCard(
                            title = "මුළු දේශනා",
                            count = sermons.size.toString(),
                            modifier = Modifier.weight(1f),
                            color = SaffronPrimary
                        )
                        MetricCard(
                            title = "මහා පිරිත්",
                            count = sermons.count { it.category == "මහා පිරිත්" }.toString(),
                            modifier = Modifier.weight(1f),
                            color = AmberGold
                        )
                        MetricCard(
                            title = "ධර්ම දේශනා",
                            count = sermons.count { it.category == "ධර්ම දේශනා" }.toString(),
                            modifier = Modifier.weight(1f),
                            color = ForestGreen
                        )
                    }
                }

                item {
                    // Action Buttons: Add Sermon & Restore Defaults
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onAddNewSermon,
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().testTag("add_sermon_button")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("නව දේශනාවක් / පිරිතක් ඇතුළත් කරන්න", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { showRestoreConfirmDialog = true },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().testTag("restore_defaults_button")
                        ) {
                            Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null, tint = SaffronPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("මූලික පිරිත් එකතුව නැවත පිහිටුවන්න")
                        }
                    }
                }

                item {
                    Text(
                        text = "ඇතුළත් කර ඇති සියලු දේශනා (${sermons.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(sermons, key = { it.id }) { sermon ->
                    AdminSermonCard(
                        sermon = sermon,
                        onEdit = { onEditSermon(sermon) },
                        onDelete = { sermonToDelete = sermon }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (sermonToDelete != null) {
        val s = sermonToDelete!!
        AlertDialog(
            onDismissRequest = { sermonToDelete = null },
            title = { Text("දේශනාව ඉවත් කිරීම", fontWeight = FontWeight.Bold) },
            text = { Text("'${s.title}' දේශනාව යෙදුමෙන් සදහටම ඉවත් කිරීමට ඔබට විශ්වාසද?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteSermon(s)
                        sermonToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("ඔව්, ඉවත් කරන්න", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { sermonToDelete = null }) {
                    Text("නැත")
                }
            }
        )
    }

    // Restore Defaults Confirmation Dialog
    if (showRestoreConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirmDialog = false },
            title = { Text("මූලික දේශනා එකතුව නැවත පිහිටුවීම", fontWeight = FontWeight.Bold) },
            text = { Text("කරණීය මෙත්ත, මංගල, රතන ආදී මූලික පිරිත් සහ දේශනා නැවත එක් කිරීමට අවශ්‍යද?") },
            confirmButton = {
                Button(
                    onClick = {
                        onRestoreDefaults()
                        showRestoreConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                ) {
                    Text("නැවත එක් කරන්න", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirmDialog = false }) {
                    Text("අවලංගුයි")
                }
            }
        )
    }
}

@Composable
private fun MetricCard(
    title: String,
    count: String,
    modifier: Modifier = Modifier,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = count, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = color)
            Text(text = title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AdminSermonCard(
    sermon: SermonEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = sermon.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (sermon.isFeatured) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AmberGoldLight
                        ) {
                            Text(
                                text = "විශේෂ",
                                fontSize = 10.sp,
                                color = DeepMaroon,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${sermon.thero} • ${sermon.category} • මිනි. ${sermon.durationSeconds / 60}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onEdit, modifier = Modifier.testTag("admin_item_edit_${sermon.id}")) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "සංස්කරණය",
                    tint = AmberGold,
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(onClick = onDelete, modifier = Modifier.testTag("admin_item_delete_${sermon.id}")) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "ඉවත් කරන්න",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
