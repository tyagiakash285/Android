package com.example.ui.coach

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.AgentAction
import com.example.ai.AgentRole
import com.example.ai.CoachMessage
import com.example.ui.MainViewModel
import com.example.ui.components.AreaBadge
import com.example.ui.theme.LifeOsPrimary
import com.example.ui.theme.LifeOsSecondary
import kotlinx.coroutines.launch

@Composable
fun AiCoachScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.coachMessages.collectAsState()
    val isLoading by viewModel.isCoachLoading.collectAsState()
    val selectedRole by viewModel.selectedAgentRole.collectAsState()

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // --- 1. Agent Role Selector ---
        Text(
            text = "SPECIALIZED AI AGENTS",
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp,
            color = LifeOsPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(AgentRole.entries.toTypedArray()) { role ->
                FilterChip(
                    selected = selectedRole == role,
                    onClick = { viewModel.selectAgentRole(role) },
                    label = { Text("${role.emoji} ${role.title}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- 2. Quick Prompt Chips ---
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            val suggestions = when (selectedRole) {
                AgentRole.PLANNER -> listOf("Plan my day today", "I have 2 hours tonight. What should I do?", "Plan my weekend")
                AgentRole.GOAL_BREAKDOWN -> listOf("Break down: Build an AI Chatbot", "Break down: Run 5km", "Break down: Learn LangChain")
                AgentRole.HABIT_ANALYST -> listOf("Analyze my habit consistency", "Why do I fail my evening reading?", "How to stack habits?")
                AgentRole.WEEKLY_REVIEW -> listOf("Generate my Weekly Review", "Audit my productivity index", "Next week priorities")
                else -> listOf("What should I focus on today?", "How am I progressing toward my goals?", "Give me a productivity boost")
            }

            items(suggestions) { prompt ->
                SuggestionChip(
                    onClick = {
                        inputText = prompt
                        viewModel.sendCoachMessage(prompt)
                        inputText = ""
                    },
                    label = { Text(prompt, fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- 3. Chat Messages Stream ---
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                CoachMessageBubble(
                    message = msg,
                    onExecuteAction = { action -> viewModel.executeAgentAction(action) }
                )
            }

            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = LifeOsPrimary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "${selectedRole.title} is analyzing your LifeOS data...",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // --- 4. Input Bar ---
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 80.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ask ${selectedRole.title}...", fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    singleLine = true
                )

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendCoachMessage(inputText)
                            inputText = ""
                        }
                    },
                    enabled = inputText.isNotBlank() && !isLoading,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (inputText.isNotBlank()) LifeOsPrimary else MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (inputText.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CoachMessageBubble(
    message: CoachMessage,
    onExecuteAction: (AgentAction) -> Unit
) {
    val isUser = message.sender == "USER"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (!isUser) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Text(text = message.agentRole.emoji, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = message.agentRole.title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = LifeOsPrimary
                )
            }
        }

        Box(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(
                    if (isUser) LifeOsPrimary
                    else MaterialTheme.colorScheme.surface
                )
                .border(
                    width = if (isUser) 0.dp else 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(14.dp)
        ) {
            Text(
                text = message.text,
                color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
        }

        // Actionable Tool Call executions generated by the Agent
        if (message.actions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier.widthIn(max = 320.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                message.actions.forEach { action ->
                    AgentActionCard(action = action, onExecute = { onExecuteAction(action) })
                }
            }
        }
    }
}

@Composable
fun AgentActionCard(
    action: AgentAction,
    onExecute: () -> Unit
) {
    var executed by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                val (badge, label) = when (action) {
                    is AgentAction.CreateTask -> Pair("ADD TASK", "${action.title} (${action.estimatedMins}m)")
                    is AgentAction.CreateHabit -> Pair("ADD HABIT", "${action.icon} ${action.name}")
                    is AgentAction.CreateProject -> Pair("ADD PROJECT", action.title)
                }
                Text(
                    text = badge,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = LifeOsPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Button(
                onClick = {
                    if (!executed) {
                        onExecute()
                        executed = true
                    }
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (executed) LifeOsSecondary else LifeOsPrimary
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                enabled = !executed
            ) {
                Text(
                    text = if (executed) "Added ✅" else "Execute Action",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
