package com.example.ui.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Mood
import com.example.ui.theme.LifeOsPrimary

@Composable
fun JournalDialog(
    onDismiss: () -> Unit,
    onSave: (mood: String, gratitude: String, wins: String, improvements: String, text: String) -> Unit
) {
    var selectedMood by remember { mutableStateOf(Mood.GOOD.name) }
    var wins by remember { mutableStateOf("") }
    var gratitude by remember { mutableStateOf("") }
    var improvements by remember { mutableStateOf("") }
    var freeText by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "📔", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Daily Reflection & Journal",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("How are you feeling today?", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Mood.entries.forEach { mood ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selectedMood == mood.name) LifeOsPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .clickable { selectedMood = mood.name }
                                .padding(8.dp)
                        ) {
                            Text(text = mood.emoji, fontSize = 22.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = mood.title, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                OutlinedTextField(
                    value = wins,
                    onValueChange = { wins = it },
                    label = { Text("What did you accomplish today?") },
                    placeholder = { Text("Key wins, tasks completed, obstacles overcome...") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = gratitude,
                    onValueChange = { gratitude = it },
                    label = { Text("What are you grateful for?") },
                    placeholder = { Text("3 things that brought joy or peace today...") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = improvements,
                    onValueChange = { improvements = it },
                    label = { Text("What should you improve tomorrow?") },
                    placeholder = { Text("Adjustments to routine, focus, or habits...") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = freeText,
                    onValueChange = { freeText = it },
                    label = { Text("Freeform Notes / Ideas (optional)") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(selectedMood, gratitude, wins, improvements, freeText)
                }
            ) {
                Text("Save Reflection")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
