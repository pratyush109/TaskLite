package com.example.tasklite

import com.example.tasklite.repository.AuthRepository
import com.example.tasklite.viewmodel.AuthViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.*

class AuthViewModelTest {

    @Test
    fun login_success_test() {
        val repo = mock<AuthRepository>()
        val viewModel = AuthViewModel(repo)

        // Mock repository login callback
        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String?) -> Unit>(2)
            callback(true, null)
            null
        }.`when`(repo).login(eq("test@gmail.com"), eq("123456"), any())

        var callbackCalled = false

        viewModel.login("test@gmail.com", "123456") {
            callbackCalled = true
        }

        assertTrue(viewModel.isLoading == false)
        assertTrue(callbackCalled)
        assertEquals(null, viewModel.errorMessage)

        verify(repo).login(eq("test@gmail.com"), eq("123456"), any())
    }

    @Test
    fun login_failure_test() {
        val repo = mock<AuthRepository>()
        val viewModel = AuthViewModel(repo)

        // Mock login failure
        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String?) -> Unit>(2)
            callback(false, "Invalid credentials")
            null
        }.`when`(repo).login(eq("test@gmail.com"), eq("wrongpass"), any())

        var callbackCalled = false

        viewModel.login("test@gmail.com", "wrongpass") {
            callbackCalled = true
        }

        assertTrue(viewModel.isLoading == false)
        assertTrue(!callbackCalled) // onSuccess should not be called
        assertEquals("Invalid credentials", viewModel.errorMessage)
    }

    @Test
    fun signup_success_test() {
        val repo = mock<AuthRepository>()
        val viewModel = AuthViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String?) -> Unit>(2)
            callback(true, null)
            null
        }.`when`(repo).signup(eq("new@gmail.com"), eq("123456"), any())

        var successCalled = false

        viewModel.signup("new@gmail.com", "123456") {
            successCalled = true
        }

        assertTrue(successCalled)
        assertEquals(null, viewModel.errorMessage)
        assertTrue(viewModel.isLoading == false)
        verify(repo).signup(eq("new@gmail.com"), eq("123456"), any())
    }

    @Test
    fun resetPassword_success_test() {
        val repo = mock<AuthRepository>()
        val viewModel = AuthViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String?) -> Unit>(1)
            callback(true, null)
            null
        }.`when`(repo).resetPassword(eq("test@gmail.com"), any())

        var successCalled = false

        viewModel.resetPassword("test@gmail.com") {
            successCalled = true
        }

        assertTrue(successCalled)
        assertEquals(null, viewModel.errorMessage)
        assertTrue(viewModel.isLoading == false)
        verify(repo).resetPassword(eq("test@gmail.com"), any())
    }
}
