package com.example.ui.aistudio

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiMessage
import com.example.data.model.FileChangeProposal
import com.example.data.model.ProjectItem
import com.example.ui.components.DiffViewerDialog
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.MintPrimary
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun AiStudioScreen(
    currentProject: ProjectItem,
    messages: List<AiMessage>,
    onSendMessage: (prompt: String, mode: String) -> Unit,
    onApplyProposal: (FileChangeProposal) -> Unit,
    parseProposals: (String) -> List<FileChangeProposal>
) {
    var selectedMode by remember { mutableStateOf("Chat") }
    var inputPrompt by remember { mutableStateOf("") }
    var inspectingProposal by remember { mutableStateOf<FileChangeProposal?>(null) }
    var appliedFeedback by remember { mutableStateOf<String?>(null) }

    val modes = listOf("Chat", "Generate", "Edit", "Debug")

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "AI Studio",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Your AI coding assistant • ${currentProject.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Mode Tabs (Chat, Generate, Edit, Debug)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(modes) { mode ->
                        val isSelected = selectedMode == mode
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) MintPrimary else DarkSurfaceCard)
                                .clickable { selectedMode = mode }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .testTag("ai_mode_${mode.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode,
                                color = if (isSelected) DarkBg else TextWhite,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputPrompt,
                        onValueChange = { inputPrompt = it },
                        placeholder = {
                            Text(
                                text = "Ask anything about your project...",
                                color = TextMuted,
                                fontSize = 14.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = MintPrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedContainerColor = DarkSurfaceCard,
                            unfocusedContainerColor = DarkSurfaceCard
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ai_prompt_input_field"),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (inputPrompt.isNotBlank()) {
                                onSendMessage(inputPrompt.trim(), selectedMode)
                                inputPrompt = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MintPrimary)
                            .testTag("ai_send_button")
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Send",
                            tint = DarkBg,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Powered by Google Gemini (gemini-2.5-flash)",
                        color = TextMuted,
                        style = MaterialTheme.typography.labelSmall
                    )
                    if (appliedFeedback != null) {
                        Text(
                            text = appliedFeedback!!,
                            color = GreenSuccess,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(messages) { message ->
                if (message.role == "user") {
                    // User Message Bubble (Right aligned)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Card(
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E283D)),
                            modifier = Modifier.widthIn(max = 300.dp)
                        ) {
                            Text(
                                text = message.content,
                                color = TextWhite,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                } else {
                    // Assistant Message (Left aligned with avatar and multi-file proposal card)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        // AI Avatar
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0x1F00F0A8)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "</>",
                                color = MintPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Card(
                                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                                border = CardDefaults.outlinedCardBorder()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = message.content,
                                        color = TextWhite,
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    val proposals = parseProposals(message.fileModifications)
                                    if (proposals.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(14.dp))

                                        // Proposals Card
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1420)),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                proposals.forEach { prop ->
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(vertical = 4.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Icon(
                                                                Icons.Default.Description,
                                                                contentDescription = null,
                                                                tint = CyanAccent,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text(
                                                                text = prop.filePath,
                                                                color = TextWhite,
                                                                style = MaterialTheme.typography.bodySmall,
                                                                fontFamily = FontFamily.Monospace
                                                            )
                                                        }
                                                        Text(
                                                            text = "+${prop.addedLines}",
                                                            color = MintPrimary,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Action buttons (Apply Changes, View Diff)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    proposals.forEach { onApplyProposal(it) }
                                                    appliedFeedback = "✓ Changes applied to ${proposals.size} files"
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = MintPrimary),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f).testTag("apply_changes_button")
                                            ) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = DarkBg,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Apply Changes", color = DarkBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    inspectingProposal = proposals.firstOrNull()
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                                    brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
                                                ),
                                                modifier = Modifier.weight(1f).testTag("view_diff_button")
                                            ) {
                                                Icon(
                                                    Icons.Default.Visibility,
                                                    contentDescription = null,
                                                    tint = TextWhite,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("View Diff", color = TextWhite, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Diff inspection modal
    if (inspectingProposal != null) {
        DiffViewerDialog(
            proposal = inspectingProposal!!,
            onDismiss = { inspectingProposal = null },
            onApply = {
                onApplyProposal(inspectingProposal!!)
                appliedFeedback = "✓ Applied to ${inspectingProposal!!.filePath}"
                inspectingProposal = null
            }
        )
    }
}
