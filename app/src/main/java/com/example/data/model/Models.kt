package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class UserProfile(
    val userId: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val lastActive: Timestamp? = null
)

data class ProjectItem(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val description: String = "",
    val projectType: String = "flutter", // flutter, kotlin, web, etc.
    val storageMode: String = "local", // local, cloud, synced
    val lastBuildStatus: String = "none", // success, failed, running, none
    val filesCount: Int = 0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class ProjectFile(
    val id: String = "",
    val projectId: String = "",
    val userId: String = "",
    val filePath: String = "",
    val content: String = "",
    val language: String = "dart", // dart, kotlin, javascript, html, css, yaml, json
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class BuildJob(
    val id: String = "",
    val userId: String = "",
    val projectId: String = "",
    val projectName: String = "",
    val buildVariant: String = "debug", // debug, release
    val buildEngine: String = "cloud", // cloud, local
    val status: String = "queued", // queued, running, success, failed
    val logs: String = "",
    val artifactName: String = "",
    val artifactSize: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class FileChangeProposal(
    val filePath: String = "",
    val addedLines: Int = 0,
    val deletedLines: Int = 0,
    val summary: String = "",
    val updatedContent: String = ""
)

data class AiMessage(
    val id: String = "",
    val userId: String = "",
    val projectId: String = "",
    val role: String = "assistant", // user, assistant
    val mode: String = "chat", // chat, generate, edit, debug
    val content: String = "",
    val fileModifications: String = "", // serialized JSON array of FileChangeProposal
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class AiProviderConfig(
    val id: String = "gemini",
    val name: String = "Google Gemini",
    val model: String = "gemini-2.5-flash",
    val isConnected: Boolean = true,
    val statusText: String = "Connected"
)
