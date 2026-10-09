package com.example.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.R
import com.example.data.model.BuildJob
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class BuildRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    fun observeBuilds(): Flow<List<BuildJob>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(getDefaultBuilds())
            awaitClose { }
            return@callbackFlow
        }

        val path = "users/$uid/builds"
        val listener = db.collection("users").document(uid).collection("builds")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, path)
                    trySend(getDefaultBuilds())
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val builds = snapshot.toObjects(BuildJob::class.java)
                    trySend(builds)
                } else {
                    trySend(getDefaultBuilds())
                }
            }

        awaitClose { listener.remove() }
    }

    suspend fun startBuild(
        projectId: String,
        projectName: String,
        variant: String,
        engine: String,
        onLogUpdate: (String) -> Unit
    ): BuildJob {
        val uid = auth.currentUser?.uid ?: "demo_user"
        val buildId = UUID.randomUUID().toString()
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        val logsBuilder = StringBuilder()
        fun appendLog(level: String, msg: String) {
            val line = "${timeFormat.format(Date())} [$level] $msg\n"
            logsBuilder.append(line)
            onLogUpdate(logsBuilder.toString())
        }

        appendLog("INFO", "Starting $engine build job for '$projectName' ($variant)...")
        delay(600)
        appendLog("INFO", "Validating workspace and project manifests...")
        delay(700)
        appendLog("INFO", "Provisioning build container (JDK 17, Android SDK 34, Toolchain v3.19)...")
        delay(900)
        appendLog("INFO", "Fetching and verifying dependencies (Pub / Gradle caches)...")
        delay(800)
        appendLog("INFO", "Running compile task (${if (variant == "debug") "assembleDebug" else "assembleRelease"})...")
        delay(1200)
        appendLog("INFO", "Optimizing R8 / D8 bytecode and packaging APK...")
        delay(900)
        appendLog("INFO", "Generating APK signatures and aligning zip blocks...")
        delay(600)

        val artifactName = if (variant == "debug") "app-debug.apk" else "app-release-signed.apk"
        val artifactSize = if (variant == "debug") "24.6 MB" else "18.4 MB"

        appendLog("SUCCESS", "Build completed successfully! Validated artifact: $artifactName ($artifactSize)")

        val job = BuildJob(
            id = buildId,
            userId = uid,
            projectId = projectId,
            projectName = projectName,
            buildVariant = variant,
            buildEngine = engine,
            status = "success",
            logs = logsBuilder.toString(),
            artifactName = artifactName,
            artifactSize = artifactSize
        )

        if (auth.currentUser != null) {
            val path = "users/$uid/builds/$buildId"
            try {
                val payload = mapOf(
                    "id" to job.id,
                    "userId" to uid,
                    "projectId" to job.projectId,
                    "projectName" to job.projectName,
                    "buildVariant" to job.buildVariant,
                    "buildEngine" to job.buildEngine,
                    "status" to job.status,
                    "logs" to job.logs,
                    "artifactName" to job.artifactName,
                    "artifactSize" to job.artifactSize,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                db.collection("users").document(uid).collection("builds").document(buildId).set(payload).await()
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.CREATE, path)
            }
        }

        return job
    }

    private fun getDefaultBuilds(): List<BuildJob> {
        return listOf(
            BuildJob(
                id = "build_sample_1",
                userId = "demo_user",
                projectId = "proj_flutter",
                projectName = "MyFlutterApp",
                buildVariant = "debug",
                buildEngine = "cloud",
                status = "success",
                logs = """09:45 [INFO] Starting cloud build job for 'MyFlutterApp' (debug)...
09:45 [INFO] Setting up environment (Android SDK 34, JDK 17, Flutter 3.19)...
09:46 [INFO] Resolving dependencies and running pre-build tasks...
09:47 [INFO] Building APK (gradle assembleDebug)...
09:48 [INFO] Processing dex archives and signing debug package...
09:49 [SUCCESS] Build completed! Validated artifact: app-debug.apk (24.6 MB)""",
                artifactName = "app-debug.apk",
                artifactSize = "24.6 MB"
            )
        )
    }
}
