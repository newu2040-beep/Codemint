package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.ui.graphics.vector.ImageVector

enum class NavScreen(
    val title: String,
    val icon: ImageVector
) {
    Home("Home", Icons.Default.Home),
    Projects("Projects", Icons.Default.Folder),
    Editor("Editor", Icons.Default.Code),
    AIStudio("AI Studio", Icons.Default.SmartToy),
    Builds("Builds", Icons.Default.Build),
    Settings("Settings", Icons.Default.Settings)
}
