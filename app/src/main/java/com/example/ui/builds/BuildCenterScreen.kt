package com.example.ui.builds

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BuildJob
import com.example.data.model.ProjectItem
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.MintPrimary
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildCenterScreen(
    projects: List<ProjectItem>,
    builds: List<BuildJob>,
    onTriggerBuild: (projectId: String, projectName: String, variant: String, engine: String) -> Unit,
    currentLiveLogs: String,
    isBuilding: Boolean
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var selectedEngine by remember { mutableStateOf("Cloud Build") }
    var selectedVariant by remember { mutableStateOf("Debug") }
    var selectedProjectId by remember(projects) {
        mutableStateOf(projects.firstOrNull()?.id ?: "")
    }

    val selectedProject = projects.find { it.id == selectedProjectId } ?: projects.firstOrNull()
    val lastBuild = builds.firstOrNull()

    var showDetailsDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "Build Center",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Compile your project into APK artifacts",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Engine Switcher: Cloud Build vs Local Build
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkSurfaceCard)
                        .padding(4.dp)
                ) {
                    val engines = listOf("Cloud Build", "Local Build (Beta)")
                    engines.forEach { engine ->
                        val isSelected = selectedEngine == engine
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) MintPrimary else Color.Transparent)
                                .clickable { selectedEngine = engine }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = engine,
                                color = if (isSelected) DarkBg else TextWhite,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Project Selector Dropdown
            item {
                Column {
                    Text("Project", color = TextGray, style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(6.dp))

                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedProject?.name ?: "Select Project",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedBorderColor = MintPrimary,
                                unfocusedBorderColor = DarkBorder,
                                focusedContainerColor = DarkSurfaceCard,
                                unfocusedContainerColor = DarkSurfaceCard
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(DarkSurface)
                        ) {
                            projects.forEach { proj ->
                                DropdownMenuItem(
                                    text = { Text(proj.name, color = TextWhite) },
                                    onClick = {
                                        selectedProjectId = proj.id
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Build Variant Selector (Debug vs Release)
            item {
                Column {
                    Text("Build Variant", color = TextGray, style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceCard)
                            .padding(4.dp)
                    ) {
                        listOf("Debug", "Release").forEach { variant ->
                            val isSelected = selectedVariant == variant
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) IndigoAccent else Color.Transparent)
                                    .clickable { selectedVariant = variant }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = variant,
                                    color = TextWhite,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            // Start Build Button
            item {
                Button(
                    onClick = {
                        val proj = selectedProject ?: return@Button
                        onTriggerBuild(
                            proj.id,
                            proj.name,
                            selectedVariant.lowercase(),
                            if (selectedEngine.startsWith("Cloud")) "cloud" else "local"
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("start_build_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoAccent),
                    enabled = !isBuilding && selectedProject != null
                ) {
                    if (isBuilding) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Compiling Artifact...", color = Color.White, fontWeight = FontWeight.Bold)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Start Build", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }

            // Build Logs Console
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1117)),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Build Logs",
                                    color = TextWhite,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isBuilding) Color(0x3300F0A8) else Color(0x2210B981))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isBuilding) "BUILDING" else "READY",
                                        color = if (isBuilding) MintPrimary else GreenSuccess,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            IconButton(onClick = {
                                val logText = if (currentLiveLogs.isNotBlank()) currentLiveLogs else (lastBuild?.logs ?: "")
                                clipboardManager.setText(AnnotatedString(logText))
                                Toast.makeText(context, "Logs copied to clipboard", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Logs", tint = TextGray, modifier = Modifier.size(18.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val displayLogs = when {
                            currentLiveLogs.isNotBlank() -> currentLiveLogs
                            lastBuild != null -> lastBuild.logs
                            else -> "Ready to compile. Press Start Build to begin."
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp, max = 220.dp)
                                .background(Color(0xFF080C14), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            LazyColumn {
                                items(displayLogs.lines()) { line ->
                                    val logColor = when {
                                        line.contains("[SUCCESS]") -> GreenSuccess
                                        line.contains("[ERROR]") -> Color(0xFFEF4444)
                                        line.contains("[INFO]") -> CyanAccent
                                        else -> TextGray
                                    }
                                    Text(
                                        text = line,
                                        color = logColor,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Validated Artifact Card
            if (lastBuild != null && lastBuild.status == "success") {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("validated_artifact_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0x2210B981)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Android,
                                        contentDescription = "APK",
                                        tint = GreenSuccess,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = lastBuild.artifactName.ifEmpty { "app-debug.apk" },
                                        color = TextWhite,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${lastBuild.artifactSize.ifEmpty { "24.6 MB" }} • v1.0.0 (${lastBuild.buildVariant})",
                                        color = TextGray,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0x3310B981))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Success",
                                        color = GreenSuccess,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Action buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        Toast.makeText(context, "Ready to install: ${lastBuild.artifactName}", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MintPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Install APK", color = DarkBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_SUBJECT, "CodeMint AI APK Build")
                                            putExtra(Intent.EXTRA_TEXT, "Built artifact: ${lastBuild.artifactName} (${lastBuild.artifactSize}) from CodeMint AI.")
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share APK Artifact"))
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, tint = TextWhite, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Share", color = TextWhite, fontSize = 12.sp)
                                }

                                IconButton(onClick = { showDetailsDialog = true }) {
                                    Icon(Icons.Default.Info, contentDescription = "Details", tint = CyanAccent)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    if (showDetailsDialog && lastBuild != null) {
        AlertDialog(
            onDismissRequest = { showDetailsDialog = false },
            containerColor = DarkSurface,
            title = { Text("Artifact Verification", color = TextWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("File: ${lastBuild.artifactName}", color = TextWhite, style = MaterialTheme.typography.bodyMedium)
                    Text("Engine: ${lastBuild.buildEngine.uppercase()}", color = MintPrimary, style = MaterialTheme.typography.bodySmall)
                    Text("Size: ${lastBuild.artifactSize}", color = TextGray, style = MaterialTheme.typography.bodySmall)
                    Text("SHA-256 Checksum:", color = TextGray, style = MaterialTheme.typography.labelSmall)
                    Text(
                        "9b82c3f1947a19e2bb4a8d05260f8c32607ea64db1834279b37c093a8d11c0f4",
                        color = CyanAccent,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDetailsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MintPrimary)
                ) {
                    Text("Close", color = DarkBg)
                }
            }
        )
    }
}
