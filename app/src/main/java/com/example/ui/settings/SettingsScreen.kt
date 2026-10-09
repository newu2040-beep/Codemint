package com.example.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.google.firebase.auth.FirebaseUser

@Composable
fun SettingsScreen(
    currentUser: FirebaseUser?,
    onSignOut: () -> Unit
) {
    var showAiConfigDialog by remember { mutableStateOf<String?>(null) }
    var darkModeEnabled by remember { mutableStateOf(true) }

    val displayName = currentUser?.displayName ?: "Rahul Shah"
    val email = currentUser?.email ?: "rahul.shah@gmail.com"

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
                    text = "Settings",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Customize your experience",
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Card
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(MintPrimary, CyanAccent)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = displayName.take(1).uppercase(),
                                color = DarkBg,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = displayName,
                                color = TextWhite,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = email,
                                color = TextGray,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        TextButton(onClick = onSignOut) {
                            Text("Sign Out", color = Color(0xFFEF4444), fontSize = 12.sp)
                        }
                    }
                }
            }

            // Google Signed in Status Tile
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1626)),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4285F4)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("G", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Google Account", color = TextWhite, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("Signed in securely", color = TextGray, fontSize = 12.sp)
                        }
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Verified",
                            tint = GreenSuccess,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // AI Providers Section
            item {
                Text(
                    text = "AI Providers",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Manage your AI model providers",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        SettingItemRow(
                            icon = Icons.Default.SmartToy,
                            title = "Google Gemini",
                            subtitle = "Connected (gemini-2.5-flash)",
                            badgeText = "Connected",
                            badgeColor = GreenSuccess,
                            onClick = { showAiConfigDialog = "Google Gemini" }
                        )
                        Divider(color = DarkBorder, modifier = Modifier.padding(horizontal = 8.dp))
                        SettingItemRow(
                            icon = Icons.Default.AutoAwesome,
                            title = "OpenAI",
                            subtitle = "Not configured",
                            badgeText = "Optional",
                            badgeColor = TextMuted,
                            onClick = { showAiConfigDialog = "OpenAI" }
                        )
                        Divider(color = DarkBorder, modifier = Modifier.padding(horizontal = 8.dp))
                        SettingItemRow(
                            icon = Icons.Default.Build,
                            title = "Custom API",
                            subtitle = "Not configured",
                            badgeText = "Optional",
                            badgeColor = TextMuted,
                            onClick = { showAiConfigDialog = "Custom API" }
                        )
                    }
                }
            }

            // General Preferences
            item {
                Text(
                    text = "Preferences",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        SettingItemRow(
                            icon = Icons.Default.Folder,
                            title = "Project Settings",
                            subtitle = "Default SDK & build paths",
                            onClick = {}
                        )
                        Divider(color = DarkBorder, modifier = Modifier.padding(horizontal = 8.dp))
                        SettingItemRow(
                            icon = Icons.Default.Build,
                            title = "Build Environment",
                            subtitle = "Cloud (Recommended)",
                            onClick = {}
                        )
                        Divider(color = DarkBorder, modifier = Modifier.padding(horizontal = 8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Palette, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Appearance", color = TextWhite, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("Dark Mode", color = TextGray, fontSize = 12.sp)
                            }
                            Switch(
                                checked = darkModeEnabled,
                                onCheckedChange = { darkModeEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = DarkBg,
                                    checkedTrackColor = MintPrimary
                                )
                            )
                        }
                    }
                }
            }

            // Storage & Data
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Storage, contentDescription = null, tint = IndigoAccent, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Storage & Data", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("12.4 GB used of 64 GB", color = TextGray, fontSize = 12.sp)
                            Text("19% Used", color = MintPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { 0.19f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = MintPrimary,
                            trackColor = Color(0xFF1E2638)
                        )
                    }
                }
            }

            // Footer
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Made with love by Rahul Shah / Editingcells",
                        style = MaterialTheme.typography.bodySmall,
                        color = MintPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "CodeMint AI • Package: com.editingcells.codemint • v1.0.0",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // AI Provider Config Dialog
    if (showAiConfigDialog != null) {
        val provider = showAiConfigDialog!!
        AlertDialog(
            onDismissRequest = { showAiConfigDialog = null },
            containerColor = DarkSurface,
            title = { Text("Configure $provider", color = TextWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Manage credentials and model parameters for $provider.",
                        color = TextGray,
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = if (provider == "Google Gemini") "Configured via AI Studio Secrets" else "",
                        onValueChange = {},
                        label = { Text("API Key") },
                        readOnly = provider == "Google Gemini",
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = MintPrimary,
                            unfocusedBorderColor = DarkBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAiConfigDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MintPrimary)
                ) {
                    Text("Save", color = DarkBg)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAiConfigDialog = null }) {
                    Text("Close", color = TextGray)
                }
            }
        )
    }
}

@Composable
fun SettingItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badgeText: String? = null,
    badgeColor: Color = GreenSuccess,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextWhite, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(subtitle, color = TextGray, fontSize = 12.sp)
        }
        if (badgeText != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(badgeColor.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(badgeText, color = badgeColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted, modifier = Modifier.size(20.dp))
    }
}
