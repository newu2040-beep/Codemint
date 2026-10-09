package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.data.auth.AuthManager
import com.example.data.model.BuildJob
import com.example.data.model.FileChangeProposal
import com.example.data.model.ProjectFile
import com.example.data.model.ProjectItem
import com.example.data.repository.AiRepository
import com.example.data.repository.BuildRepository
import com.example.data.repository.ProjectRepository
import com.example.ui.aistudio.AiStudioScreen
import com.example.ui.auth.SignInScreen
import com.example.ui.builds.BuildCenterScreen
import com.example.ui.editor.CodeEditorScreen
import com.example.ui.home.HomeScreen
import com.example.ui.navigation.NavScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.CodeMintTheme
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.MintPrimary
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CodeMintTheme {
                CodeMintAppRoot()
            }
        }
    }
}

@Composable
fun CodeMintAppRoot() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    var currentUser by remember { mutableStateOf<FirebaseUser?>(Firebase.auth.currentUser) }

    // Listen to Firebase Auth state changes
    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    // Attempt silent auto-sign in on launch if not signed in
    LaunchedEffect(Unit) {
        if (currentUser == null) {
            AuthManager.attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = { user -> currentUser = user },
                onUnauthenticated = { /* Remains unauthenticated */ },
                scope = coroutineScope
            )
        }
    }

    if (currentUser == null) {
        SignInScreen(
            onAuthSuccess = { user -> currentUser = user }
        )
    } else {
        CodeMintWorkspace(
            currentUser = currentUser!!,
            onSignOut = {
                AuthManager.signOut(
                    context = context,
                    credentialManager = credentialManager,
                    onSignOutComplete = { currentUser = null },
                    scope = coroutineScope
                )
            }
        )
    }
}

@Composable
fun CodeMintWorkspace(
    currentUser: FirebaseUser,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val projectRepo = remember { ProjectRepository(context) }
    val buildRepo = remember { BuildRepository(context) }
    val aiRepo = remember { AiRepository(context) }

    var currentScreen by remember { mutableStateOf(NavScreen.Home) }

    val projectsState by projectRepo.observeProjects().collectAsState(
        initial = projectRepo.getDefaultProjects(currentUser.uid)
    )

    var selectedProject by remember(projectsState) {
        mutableStateOf(projectsState.firstOrNull() ?: projectRepo.getDefaultProjects(currentUser.uid).first())
    }

    var projectFiles by remember { mutableStateOf<List<ProjectFile>>(emptyList()) }

    LaunchedEffect(selectedProject.id) {
        projectFiles = projectRepo.getProjectFiles(selectedProject.id)
    }

    val aiMessages by aiRepo.observeAiMessages().collectAsState(initial = emptyList())
    val builds by buildRepo.observeBuilds().collectAsState(initial = emptyList())

    var liveLogs by remember { mutableStateOf("") }
    var isBuilding by remember { mutableStateOf(false) }

    // Handle back button on sub-screens
    if (currentScreen != NavScreen.Home) {
        BackHandler {
            currentScreen = NavScreen.Home
        }
    }

    Scaffold(
        containerColor = DarkBg,
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                contentColor = TextWhite,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("main_bottom_nav")
            ) {
                val navItems = listOf(
                    NavScreen.Home,
                    NavScreen.Projects,
                    NavScreen.AIStudio,
                    NavScreen.Builds,
                    NavScreen.Settings
                )

                navItems.forEach { screen ->
                    val isSelected = currentScreen == screen
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentScreen = screen },
                        icon = {
                            Icon(
                                screen.icon,
                                contentDescription = screen.title,
                                tint = if (isSelected) MintPrimary else TextGray
                            )
                        },
                        label = {
                            Text(
                                screen.title,
                                color = if (isSelected) MintPrimary else TextGray,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color(0x2200F0A8)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            when (currentScreen) {
                NavScreen.Home -> {
                    HomeScreen(
                        currentUser = currentUser,
                        projects = projectsState,
                        onSelectProject = { proj ->
                            selectedProject = proj
                            currentScreen = NavScreen.Editor
                        },
                        onNavigate = { dest -> currentScreen = dest },
                        onCreateProject = { name, desc, type ->
                            coroutineScope.launch {
                                val newProj = projectRepo.createProject(name, desc, type)
                                selectedProject = newProj
                                currentScreen = NavScreen.Editor
                            }
                        }
                    )
                }

                NavScreen.Projects -> {
                    ProjectsListScreen(
                        projects = projectsState,
                        onSelectProject = { proj ->
                            selectedProject = proj
                            currentScreen = NavScreen.Editor
                        },
                        onCreateNew = {
                            coroutineScope.launch {
                                val newProj = projectRepo.createProject("NewApp", "Created via CodeMint", "flutter")
                                selectedProject = newProj
                                currentScreen = NavScreen.Editor
                            }
                        }
                    )
                }

                NavScreen.Editor -> {
                    CodeEditorScreen(
                        project = selectedProject,
                        files = projectFiles,
                        onSaveFile = { updatedFile ->
                            coroutineScope.launch {
                                projectRepo.saveProjectFile(updatedFile)
                                projectFiles = projectFiles.map { if (it.id == updatedFile.id) updatedFile else it }
                            }
                        },
                        onAddNewFile = { filePath, lang ->
                            val newFile = ProjectFile(
                                id = "file_${System.currentTimeMillis()}",
                                projectId = selectedProject.id,
                                userId = currentUser.uid,
                                filePath = filePath,
                                content = "// $filePath\n",
                                language = lang
                            )
                            coroutineScope.launch {
                                projectRepo.saveProjectFile(newFile)
                                projectFiles = projectFiles + newFile
                            }
                        },
                        onRunBuild = {
                            currentScreen = NavScreen.Builds
                        }
                    )
                }

                NavScreen.AIStudio -> {
                    AiStudioScreen(
                        currentProject = selectedProject,
                        messages = aiMessages,
                        onSendMessage = { prompt, mode ->
                            coroutineScope.launch {
                                val summary = projectFiles.joinToString("\n") { "${it.filePath} (${it.content.lines().size} lines)" }
                                aiRepo.sendUserPrompt(
                                    prompt = prompt,
                                    mode = mode,
                                    projectId = selectedProject.id,
                                    projectName = selectedProject.name,
                                    currentFilesSummary = summary
                                )
                            }
                        },
                        onApplyProposal = { proposal ->
                            coroutineScope.launch {
                                val existing = projectFiles.find { it.filePath == proposal.filePath }
                                if (existing != null) {
                                    val updated = existing.copy(content = proposal.updatedContent.ifEmpty { existing.content + "\n// Applied proposal\n" })
                                    projectRepo.saveProjectFile(updated)
                                    projectFiles = projectFiles.map { if (it.id == updated.id) updated else it }
                                } else {
                                    val newFile = ProjectFile(
                                        id = "file_${System.currentTimeMillis()}",
                                        projectId = selectedProject.id,
                                        userId = currentUser.uid,
                                        filePath = proposal.filePath,
                                        content = proposal.updatedContent,
                                        language = if (proposal.filePath.endsWith(".dart")) "dart" else "kotlin"
                                    )
                                    projectRepo.saveProjectFile(newFile)
                                    projectFiles = projectFiles + newFile
                                }
                            }
                        },
                        parseProposals = { json -> aiRepo.parseProposals(json) }
                    )
                }

                NavScreen.Builds -> {
                    BuildCenterScreen(
                        projects = projectsState,
                        builds = builds,
                        onTriggerBuild = { projId, projName, variant, engine ->
                            coroutineScope.launch {
                                isBuilding = true
                                liveLogs = ""
                                buildRepo.startBuild(
                                    projectId = projId,
                                    projectName = projName,
                                    variant = variant,
                                    engine = engine,
                                    onLogUpdate = { updated -> liveLogs = updated }
                                )
                                isBuilding = false
                            }
                        },
                        currentLiveLogs = liveLogs,
                        isBuilding = isBuilding
                    )
                }

                NavScreen.Settings -> {
                    SettingsScreen(
                        currentUser = currentUser,
                        onSignOut = onSignOut
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsListScreen(
    projects: List<ProjectItem>,
    onSelectProject: (ProjectItem) -> Unit,
    onCreateNew: () -> Unit
) {
    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = { Text("All Projects", color = TextWhite, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface),
                actions = {
                    IconButton(onClick = onCreateNew) {
                        Icon(Icons.Default.Add, contentDescription = "New Project", tint = MintPrimary)
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(projects) { project ->
                com.example.ui.home.ProjectCardItem(
                    project = project,
                    onClick = { onSelectProject(project) }
                )
            }
        }
    }
}
