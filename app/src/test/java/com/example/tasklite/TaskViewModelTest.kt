package com.example.tasklite

import com.example.tasklite.viewmodel.TaskViewModel
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import org.junit.Test
import org.mockito.kotlin.*

class TaskViewModelTest {

    @Test
    fun addTask_callsFirestoreAdd() {
        // Mock Firestore and collection
        val mockCollection = mock<CollectionReference>()
        val mockDb = mock<FirebaseFirestore> {
            on { collection("tasks") } doReturn mockCollection
        }

        val viewModel = TaskViewModel(mockDb)

        // Call addTask
        viewModel.addTask("Test Task", "Description", "2026-02-27", "Pending", "Personal")

        // Verify Firestore add was called with correct map
        verify(mockCollection).add(
            argThat<Map<String, Any>> {
                this["title"] == "Test Task" &&
                        this["description"] == "Description" &&
                        this["dueDate"] == "2026-02-27" &&
                        this["status"] == "Pending" &&
                        this["category"] == "Personal"
            }
        )
    }

    @Test
    fun updateTask_callsFirestoreSet() {
        val mockDoc = mock<DocumentReference>()
        val mockCollection = mock<CollectionReference> {
            on { document("123") } doReturn mockDoc
        }
        val mockDb = mock<FirebaseFirestore> {
            on { collection("tasks") } doReturn mockCollection
        }

        val viewModel = TaskViewModel(mockDb)

        viewModel.updateTask("123", "Updated", "New Desc", "2026-03-01", "Completed", "Work")

        verify(mockCollection).document("123")
        verify(mockDoc).set(
            argThat<Map<String, Any>> {
                this["title"] == "Updated" &&
                        this["description"] == "New Desc" &&
                        this["dueDate"] == "2026-03-01" &&
                        this["status"] == "Completed" &&
                        this["category"] == "Work"
            }
        )
    }

    @Test
    fun deleteTask_callsFirestoreDelete() {
        val mockDoc = mock<DocumentReference>()
        val mockCollection = mock<CollectionReference> {
            on { document("123") } doReturn mockDoc
        }
        val mockDb = mock<FirebaseFirestore> {
            on { collection("tasks") } doReturn mockCollection
        }

        val viewModel = TaskViewModel(mockDb)

        viewModel.deleteTask("123")

        verify(mockCollection).document("123")
        verify(mockDoc).delete()
    }
}
