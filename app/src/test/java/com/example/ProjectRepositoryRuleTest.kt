package com.example

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.ProjectItem
import com.example.data.repository.ProjectRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

class ProjectRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun testAuthenticatedUserCanCreateAndReadProject() = runBlocking {
        val aliceUid = signInTestUser("alice@codemint.dev")
        val repo = ProjectRepository(firestore)

        val project = repo.createProject("TestProject", "Description", "flutter")
        assertNotNull(project.id)
        assertEquals("TestProject", project.name)
        assertEquals(aliceUid, project.userId)

        val defaultList = repo.getDefaultProjects(aliceUid)
        assertTrue(defaultList.isNotEmpty())
    }

    @Test
    fun testUnauthenticatedUserCannotCallRequireUserId() {
        auth.signOut()
        val repo = ProjectRepository(firestore)
        try {
            repo.requireUserId()
            fail("Expected IllegalStateException for unauthenticated user")
        } catch (e: IllegalStateException) {
            assertTrue(e.message!!.contains("User must be signed in with Google"))
        }
    }
}
