package com.example.tasklite

import com.example.tasklite.repository.AuthRepository
import com.example.tasklite.viewmodel.AuthViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class AuthViewModelTest {

    @Test
    fun login_success_test() {
        val repo = mock<AuthRepository>()
        val viewModel = AuthViewModel(repo)

        // Mock login behavior
        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String?) -> Unit>(2)
            callback(true, null)
            null
        }.`when`(repo).login(eq("test@gmail.com"), eq("123456"), any())

        var success = false
        viewModel.login("test@gmail.com", "123456") { success = true }

        assertTrue(success)
        verify(repo).login(eq("test@gmail.com"), eq("123456"), any())
    }

    @Test
    fun login_failure_test() {
        val repo = mock<AuthRepository>()
        val viewModel = AuthViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String?) -> Unit>(2)
            callback(false, "Invalid credentials")
            null
        }.`when`(repo).login(eq("test@gmail.com"), eq("wrongpass"), any())

        var success = false
        viewModel.login("test@gmail.com", "wrongpass") { success = true }

        assertEquals("Invalid credentials", viewModel.errorMessage)
        assertTrue(!success)
    }
}