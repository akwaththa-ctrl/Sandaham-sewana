package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SermonEntity
import com.example.ui.theme.SaffronPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAddEditDialog(
    sermonToEdit: SermonEntity?,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        thero: String,
        category: String,
        audioUrl: String,
        videoUrl: String,
        durationMinutes: Int,
        description: String,
        paliVerses: String,
        sinhalaMeaning: String,
        isFeatured: Boolean
    ) -> Unit
) {
    val isEditing = sermonToEdit != null

    var title by remember { mutableStateOf(sermonToEdit?.title ?: "") }
    var thero by remember { mutableStateOf(sermonToEdit?.thero ?: "") }
    var category by remember { mutableStateOf(sermonToEdit?.category ?: "ධර්ම දේශනා") }
    var audioUrl by remember { mutableStateOf(sermonToEdit?.audioUrl ?: "") }
    var videoUrl by remember { mutableStateOf(sermonToEdit?.videoUrl ?: "") }
    var durationMinutesStr by remember {
        mutableStateOf(
            if (sermonToEdit != null) (sermonToEdit.durationSeconds / 60).toString() else "30"
        )
    }
    var description by remember { mutableStateOf(sermonToEdit?.description ?: "") }
    var paliVerses by remember { mutableStateOf(sermonToEdit?.paliVerses ?: "") }
    var sinhalaMeaning by remember { mutableStateOf(sermonToEdit?.sinhalaMeaning ?: "") }
    var isFeatured by remember { mutableStateOf(sermonToEdit?.isFeatured ?: false) }

    var categoryExpanded by remember { mutableStateOf(false) }
    var titleError by remember { mutableStateOf(false) }

    val categories = listOf("මහා පිරිත්", "ධර්ම දේශනා", "භාවනා", "සෙත් පිරිත් සහ ගාථා")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditing) "දේශනාව සංස්කරණය" else "නව දේශනාවක් / පිරිතක් ඇතුළත් කිරීම",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = SaffronPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        titleError = it.isBlank()
                    },
                    label = { Text("දේශනාවේ / පිරිතේ නම *") },
                    placeholder = { Text("උදා: කරණීය මෙත්ත සූත්‍රය") },
                    isError = titleError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("admin_input_title")
                )
                if (titleError) {
                    Text("කරුණාකර නම ඇතුළත් කරන්න", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                }

                // Thero
                OutlinedTextField(
                    value = thero,
                    onValueChange = { thero = it },
                    label = { Text("දේශනා කළ ස්වාමීන්වහන්සේගේ නම") },
                    placeholder = { Text("උදා: පූජ්‍ය මන්කඩවල සුදස්සන හිමි / මහා සංඝරත්නය") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("admin_input_thero")
                )

                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("වර්ගය (කාණ්ඩය)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("admin_input_category")
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                // Audio URL
                OutlinedTextField(
                    value = audioUrl,
                    onValueChange = { audioUrl = it },
                    label = { Text("ශ්‍රව්‍ය (Audio / MP3) සබැඳිය") },
                    placeholder = { Text("https://example.com/audio.mp3 (හිස්ව තැබුවහොත් සාමාන්‍ය හඬ වාදනය වේ)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("admin_input_audio_url")
                )

                // Video URL
                OutlinedTextField(
                    value = videoUrl,
                    onValueChange = { videoUrl = it },
                    label = { Text("වීඩියෝ / YouTube සබැඳිය (විකල්ප)") },
                    placeholder = { Text("https://youtube.com/watch?v=...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("admin_input_video_url")
                )

                // Duration in Minutes
                OutlinedTextField(
                    value = durationMinutesStr,
                    onValueChange = { durationMinutesStr = it.filter { char -> char.isDigit() } },
                    label = { Text("කාලය (මිනිත්තු වලින්)") },
                    placeholder = { Text("30") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("admin_input_duration")
                )

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("දේශනා විස්තරය") },
                    placeholder = { Text("දේශනාවේ සාරාංශය සහ අරමුණ...") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("admin_input_description")
                )

                // Pali Verses
                OutlinedTextField(
                    value = paliVerses,
                    onValueChange = { paliVerses = it },
                    label = { Text("පාළි ගාථා / පිරිත් පද (විකල්ප)") },
                    placeholder = { Text("කරණීයමත්ථ කුසලේන...") },
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth().testTag("admin_input_pali")
                )

                // Sinhala Meaning
                OutlinedTextField(
                    value = sinhalaMeaning,
                    onValueChange = { sinhalaMeaning = it },
                    label = { Text("සිංහල අර්ථය (විකල්ප)") },
                    placeholder = { Text("ශාන්ත වූ නිවන් පදය අවබෝධ කරගනු කැමති...") },
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth().testTag("admin_input_meaning")
                )

                // Featured Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("දවසේ ප්‍රධාන දේශනාව ලෙස පෙන්වන්න", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text("මුල් පිටුවේ විශාල බැනරයේ දිස්වේ", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = isFeatured,
                        onCheckedChange = { isFeatured = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = SaffronPrimary),
                        modifier = Modifier.testTag("admin_toggle_featured")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                        return@Button
                    }
                    val durationMin = durationMinutesStr.toIntOrNull() ?: 30
                    val resolvedThero = if (thero.isBlank()) "මහා සංඝරත්නය" else thero

                    onSave(
                        title.trim(),
                        resolvedThero.trim(),
                        category,
                        audioUrl.trim(),
                        videoUrl.trim(),
                        durationMin,
                        description.trim(),
                        paliVerses.trim(),
                        sinhalaMeaning.trim(),
                        isFeatured
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                modifier = Modifier.testTag("admin_save_button")
            ) {
                Text(if (isEditing) "යාවත්කාලීන කරන්න" else "සුරකින්න", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("අවලංගුයි")
            }
        }
    )
}
