package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProjectFile
import com.example.data.model.ProjectItem
import com.example.ui.components.CodeSyntaxHighlighter
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeEditorScreen(
    project: ProjectItem,
    files: List<ProjectFile>,
    onSaveFile: (ProjectFile) -> Unit,
    onAddNewFile: (filePath: String, language: String) -> Unit,
    onRunBuild: () -> Unit
) {
    var activeFileId by remember(files) {
        mutableStateOf(files.firstOrNull()?.id ?: "")
    }

    val activeFile = files.find { it.id == activeFileId } ?: files.firstOrNull()

    var codeContent by remember(activeFile?.id) {
        mutableStateOf(activeFile?.content ?: "")
    }

    var showDrawer by remember { mutableStateOf(false) }
    var showPreviewModal by remember { mutableStateOf(false) }
    var showAddFileDialog by remember { mutableStateOf(false) }
    var saveFeedback by remember { mutableStateOf(false) }

    val openTabIds = remember { mutableStateListOf<String>() }
    LaunchedEffect(files) {
        if (openTabIds.isEmpty()) {
            files.take(3).forEach { openTabIds.add(it.id) }
        }
    }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            Column(modifier = Modifier.background(DarkSurface)) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = project.name,
                                color = TextWhite,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { showDrawer = !showDrawer }) {
                            Icon(Icons.Default.Menu, contentDescription = "Files Menu", tint = MintPrimary)
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            if (activeFile != null) {
                                onSaveFile(activeFile.copy(content = codeContent))
                                saveFeedback = true
                            }
                        }) {
                            Icon(
                                if (saveFeedback) Icons.Default.Check else Icons.Default.Save,
                                contentDescription = "Save",
                                tint = if (saveFeedback) GreenSuccess else TextWhite
                            )
                        }
                        IconButton(onClick = { showPreviewModal = true }) {
                            Icon(Icons.Default.Visibility, contentDescription = "Preview", tint = CyanAccent)
                        }
                        IconButton(onClick = onRunBuild) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(MintPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = "Run",
                                    tint = DarkBg,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
                )

                // Breadcrumb
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F1420))
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Folder,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = activeFile?.filePath?.replace("/", " > ") ?: "No file open",
                        color = TextGray,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // File Tabs
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurface)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(openTabIds) { fileId ->
                        val file = files.find { it.id == fileId }
                        if (file != null) {
                            val isSelected = file.id == activeFileId
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFF1E283D) else Color.Transparent)
                                    .clickable {
                                        activeFileId = file.id
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val dotColor = when (file.language.lowercase()) {
                                    "dart" -> MintPrimary
                                    "kotlin" -> Color(0xFF818CF8)
                                    "html", "javascript" -> Color(0xFFFBBF24)
                                    else -> CyanAccent
                                }
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(dotColor)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = file.filePath.substringAfterLast("/"),
                                    color = if (isSelected) TextWhite else TextGray,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                                if (openTabIds.size > 1) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Close Tab",
                                        tint = TextMuted,
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clickable {
                                                openTabIds.remove(file.id)
                                                if (activeFileId == file.id) {
                                                    activeFileId = openTabIds.firstOrNull() ?: ""
                                                }
                                            }
                                    )
                                }
                            }
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
            ) {
                // Quick Symbols Bar
                val symbols = listOf("{", "}", "(", ")", "[", "]", ";", "=", "<", ">", "\"", "'", "$", ".")
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(symbols) { sym ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF1E2638))
                                .clickable {
                                    codeContent += sym
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = sym,
                                color = MintPrimary,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // Status Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F1420))
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${activeFile?.language?.uppercase() ?: "DART"} • UTF-8 • LF",
                        color = TextMuted,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = "Lines: ${codeContent.lines().size}",
                        color = TextMuted,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Sliding File Drawer
            if (showDrawer) {
                Card(
                    modifier = Modifier
                        .width(260.dp)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Project Tree",
                                color = TextWhite,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { showAddFileDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = "Add File", tint = MintPrimary)
                            }
                        }

                        Divider(color = DarkBorder, modifier = Modifier.padding(vertical = 8.dp))

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(files) { file ->
                                val isSelected = file.id == activeFileId
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) Color(0xFF1E283D) else Color.Transparent)
                                        .clickable {
                                            activeFileId = file.id
                                            if (!openTabIds.contains(file.id)) {
                                                openTabIds.add(file.id)
                                            }
                                            showDrawer = false
                                        }
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Description,
                                        contentDescription = null,
                                        tint = if (isSelected) MintPrimary else TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = file.filePath,
                                        color = if (isSelected) TextWhite else TextGray,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Editor Canvas with Line Numbers
            val lines = codeContent.lines()
            val scrollState = rememberScrollState()

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkBg)
                    .horizontalScroll(scrollState)
            ) {
                // Line Numbers Gutter
                Column(
                    modifier = Modifier
                        .background(Color(0xFF0F1420))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    for (i in 1..maxOf(lines.size, 1)) {
                        Text(
                            text = i.toString(),
                            color = TextMuted,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(min = 600.dp)
                        .padding(8.dp)
                ) {
                    BasicTextField(
                        value = codeContent,
                        onValueChange = {
                            codeContent = it
                            saveFeedback = false
                        },
                        textStyle = TextStyle(
                            color = TextWhite,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        ),
                        cursorBrush = SolidColor(MintPrimary),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("code_editor_text_field")
                    )
                }
            }
        }
    }

    // Add New File Dialog
    if (showAddFileDialog) {
        var newFilePath by remember { mutableStateOf("") }
        var newLanguage by remember { mutableStateOf("dart") }

        AlertDialog(
            onDismissRequest = { showAddFileDialog = false },
            containerColor = DarkSurface,
            title = { Text("New File", color = TextWhite) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newFilePath,
                        onValueChange = { newFilePath = it },
                        label = { Text("File Path (e.g. lib/services/api.dart)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = MintPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFilePath.isNotBlank()) {
                            val lang = when {
                                newFilePath.endsWith(".dart") -> "dart"
                                newFilePath.endsWith(".kt") -> "kotlin"
                                newFilePath.endsWith(".js") -> "javascript"
                                newFilePath.endsWith(".html") -> "html"
                                newFilePath.endsWith(".css") -> "css"
                                else -> "text"
                            }
                            onAddNewFile(newFilePath.trim(), lang)
                            showAddFileDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MintPrimary)
                ) {
                    Text("Add File", color = DarkBg)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddFileDialog = false }) {
                    Text("Cancel", color = TextGray)
                }
            }
        )
    }

    // Preview Modal (for Web / Flutter UI simulation)
    if (showPreviewModal) {
        AlertDialog(
            onDismissRequest = { showPreviewModal = false },
            containerColor = DarkSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Visibility, contentDescription = null, tint = MintPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Runtime Preview: ${project.name}", color = TextWhite)
                }
            },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F1420))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0x1F00F0A8)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = MintPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "${project.name} is running in preview sandbox",
                            color = TextWhite,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Project type: ${project.projectType.uppercase()} • Status: Active",
                            color = TextGray,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPreviewModal = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MintPrimary)
                ) {
                    Text("Close Preview", color = DarkBg)
                }
            }
        )
    }
}
