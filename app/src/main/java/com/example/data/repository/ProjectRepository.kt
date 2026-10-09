package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.ProjectFile
import com.example.data.model.ProjectItem
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ProjectRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeProjects(): Flow<List<ProjectItem>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(getDefaultProjects("demo_user"))
            awaitClose { }
            return@callbackFlow
        }

        val path = "users/$uid/projects"
        val listener = db.collection("users").document(uid).collection("projects")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, path)
                    trySend(getDefaultProjects(uid))
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val projects = snapshot.toObjects(ProjectItem::class.java)
                    trySend(projects)
                } else {
                    // Seed initial projects if empty
                    val defaults = getDefaultProjects(uid)
                    trySend(defaults)
                }
            }

        awaitClose { listener.remove() }
    }

    suspend fun getProjectFiles(projectId: String): List<ProjectFile> {
        val uid = auth.currentUser?.uid ?: return getDefaultFiles(projectId, "demo_user")
        val path = "users/$uid/projects/$projectId/files"
        return try {
            val snapshot = db.collection("users").document(uid)
                .collection("projects").document(projectId)
                .collection("files").get().await()
            if (!snapshot.isEmpty) {
                snapshot.toObjects(ProjectFile::class.java)
            } else {
                getDefaultFiles(projectId, uid)
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            getDefaultFiles(projectId, uid)
        }
    }

    suspend fun saveProjectFile(file: ProjectFile) {
        val uid = auth.currentUser?.uid ?: return
        val path = "users/$uid/projects/${file.projectId}/files/${file.id}"
        try {
            val docRef = db.collection("users").document(uid)
                .collection("projects").document(file.projectId)
                .collection("files").document(file.id)

            val payload = mapOf(
                "id" to file.id,
                "projectId" to file.projectId,
                "userId" to uid,
                "filePath" to file.filePath,
                "content" to file.content,
                "language" to file.language,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
        }
    }

    suspend fun createProject(name: String, description: String, type: String): ProjectItem {
        val uid = auth.currentUser?.uid ?: "demo_user"
        val projectId = UUID.randomUUID().toString()
        val project = ProjectItem(
            id = projectId,
            userId = uid,
            name = name,
            description = description,
            projectType = type,
            storageMode = "cloud",
            lastBuildStatus = "none",
            filesCount = 3
        )

        if (auth.currentUser != null) {
            val path = "users/$uid/projects/$projectId"
            try {
                val payload = mapOf(
                    "id" to project.id,
                    "userId" to uid,
                    "name" to project.name,
                    "description" to project.description,
                    "projectType" to project.projectType,
                    "storageMode" to project.storageMode,
                    "lastBuildStatus" to project.lastBuildStatus,
                    "filesCount" to project.filesCount,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                db.collection("users").document(uid).collection("projects").document(projectId).set(payload).await()

                // Seed files
                val files = getDefaultFiles(projectId, uid)
                for (f in files) {
                    saveProjectFile(f)
                }
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.CREATE, path)
            }
        }
        return project
    }

    fun getDefaultProjects(userId: String): List<ProjectItem> {
        return listOf(
            ProjectItem(
                id = "proj_flutter",
                userId = userId,
                name = "MyFlutterApp",
                description = "Modern mobile app built with Flutter and Riverpod",
                projectType = "flutter",
                storageMode = "cloud",
                lastBuildStatus = "success",
                filesCount = 6
            ),
            ProjectItem(
                id = "proj_kotlin",
                userId = userId,
                name = "KotlinApp",
                description = "Native Android application using Jetpack Compose",
                projectType = "kotlin",
                storageMode = "cloud",
                lastBuildStatus = "success",
                filesCount = 4
            ),
            ProjectItem(
                id = "proj_web",
                userId = userId,
                name = "WebPortfolio",
                description = "Responsive developer portfolio site",
                projectType = "web",
                storageMode = "local",
                lastBuildStatus = "none",
                filesCount = 3
            ),
            ProjectItem(
                id = "proj_todo",
                userId = userId,
                name = "TodoApp",
                description = "Minimalist offline-first task tracker",
                projectType = "flutter",
                storageMode = "cloud",
                lastBuildStatus = "success",
                filesCount = 5
            )
        )
    }

    fun getDefaultFiles(projectId: String, userId: String): List<ProjectFile> {
        return when (projectId) {
            "proj_kotlin" -> listOf(
                ProjectFile(
                    id = "kf_1",
                    projectId = projectId,
                    userId = userId,
                    filePath = "app/src/main/java/MainActivity.kt",
                    language = "kotlin",
                    content = """package com.codemint.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    GreetingCard(name = "CodeMint Developer")
                }
            }
        }
    }
}

@Composable
fun GreetingCard(name: String) {
    Column(modifier = Modifier.padding(24.dp)) {
        Text(text = "Welcome to CodeMint AI", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Hello, ${'$'}name! Build and run Android apps natively.")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { /* Action */ }) {
            Text("Compile Project")
        }
    }
}
"""
                ),
                ProjectFile(
                    id = "kf_2",
                    projectId = projectId,
                    userId = userId,
                    filePath = "app/build.gradle.kts",
                    language = "kotlin",
                    content = """plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.codemint.demo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.codemint.demo"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }
}
"""
                ),
                ProjectFile(
                    id = "kf_3",
                    projectId = projectId,
                    userId = userId,
                    filePath = "app/src/main/AndroidManifest.xml",
                    language = "xml",
                    content = """<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application
        android:label="CodeMint Kotlin"
        android:theme="@android:style/Theme.Material.NoActionBar">
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
"""
                )
            )

            "proj_web" -> listOf(
                ProjectFile(
                    id = "wf_1",
                    projectId = projectId,
                    userId = userId,
                    filePath = "index.html",
                    language = "html",
                    content = """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Developer Portfolio</title>
  <link rel="stylesheet" href="styles.css">
</head>
<body>
  <div class="container">
    <header>
      <h1>Rahul Shah</h1>
      <p class="subtitle">Mobile & AI Software Engineer</p>
    </header>
    <main>
      <section class="card">
        <h2>About Me</h2>
        <p>Crafting high-performance mobile IDEs and developer tools with CodeMint AI.</p>
      </section>
      <button id="cta-btn">Connect With Me</button>
    </main>
  </div>
  <script src="app.js"></script>
</body>
</html>
"""
                ),
                ProjectFile(
                    id = "wf_2",
                    projectId = projectId,
                    userId = userId,
                    filePath = "styles.css",
                    language = "css",
                    content = """:root {
  --bg: #0b0f19;
  --surface: #161b26;
  --primary: #00f0a8;
  --text: #f8fafc;
}

body {
  margin: 0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  background-color: var(--bg);
  color: var(--text);
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
}

.container {
  width: 90%;
  max-width: 600px;
  background: var(--surface);
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 8px 32px rgba(0, 240, 168, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.08);
}

button {
  background: var(--primary);
  color: #0b0f19;
  font-weight: bold;
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  cursor: pointer;
}
"""
                ),
                ProjectFile(
                    id = "wf_3",
                    projectId = projectId,
                    userId = userId,
                    filePath = "app.js",
                    language = "javascript",
                    content = """document.addEventListener('DOMContentLoaded', () => {
  const btn = document.getElementById('cta-btn');
  btn.addEventListener('click', () => {
    alert('Thank you for connecting with CodeMint AI!');
  });
  console.log('CodeMint Web Runtime Initialized.');
});
"""
                )
            )

            else -> listOf(
                ProjectFile(
                    id = "ff_1",
                    projectId = projectId,
                    userId = userId,
                    filePath = "lib/main.dart",
                    language = "dart",
                    content = """import 'package:flutter/material.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'CodeMint',
      theme: ThemeData(
        brightness: Brightness.dark,
        primarySwatch: Colors.teal,
        scaffoldBackgroundColor: const Color(0xFF0B0F19),
      ),
      home: const HomeScreen(),
    );
  }
}

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  int _counter = 0;

  void _incrementCounter() {
    setState(() {
      _counter++;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('CodeMint AI Demo'),
        backgroundColor: const Color(0xFF161B26),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            const Text(
              'You have clicked the button this many times:',
              style: TextStyle(color: Colors.white70),
            ),
            Text(
              '${'$'}_counter',
              style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                    color: const Color(0xFF00F0A8),
                    fontWeight: FontWeight.bold,
                  ),
            ),
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: _incrementCounter,
        tooltip: 'Increment',
        backgroundColor: const Color(0xFF00F0A8),
        child: const Icon(Icons.add, color: Color(0xFF0B0F19)),
      ),
    );
  }
}
"""
                ),
                ProjectFile(
                    id = "ff_2",
                    projectId = projectId,
                    userId = userId,
                    filePath = "pubspec.yaml",
                    language = "yaml",
                    content = """name: my_flutter_app
description: "A new Flutter project created with CodeMint AI."
publish_to: 'none'
version: 1.0.0+1

environment:
  sdk: '>=3.0.0 <4.0.0'

dependencies:
  flutter:
    sdk: flutter
  cupertino_icons: ^1.0.6
  provider: ^6.1.1

dev_dependencies:
  flutter_test:
    sdk: flutter
  flutter_lints: ^3.0.0

flutter:
  uses-material-design: true
"""
                ),
                ProjectFile(
                    id = "ff_3",
                    projectId = projectId,
                    userId = userId,
                    filePath = "README.md",
                    language = "markdown",
                    content = """# MyFlutterApp

Created with CodeMint AI — the mobile AI coding IDE.

## Features
- Material 3 Design
- Google Sign-In with Firebase Auth
- Clean architecture
- Real cloud and on-device APK builds
"""
                ),
                ProjectFile(
                    id = "ff_4",
                    projectId = projectId,
                    userId = userId,
                    filePath = "lib/screens/login_screen.dart",
                    language = "dart",
                    content = """import 'package:flutter/material.dart';

class LoginScreen extends StatelessWidget {
  const LoginScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Container(
        padding: const EdgeInsets.all(24.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(Icons.code, size: 72, color: Color(0xFF00F0A8)),
            const SizedBox(height: 24),
            const Text(
              'Welcome to CodeMint',
              style: TextStyle(fontSize: 26, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 32),
            ElevatedButton.icon(
              onPressed: () {},
              icon: const Icon(Icons.login),
              label: const Text('Continue with Google'),
              style: ElevatedButton.styleFrom(
                minimumSize: const Size.fromHeight(50),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
"""
                )
            )
        }
    }
}
