package com.example.tasklite.viewmodel

import androidx.lifecycle.ViewModel
import com.example.tasklite.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

open class AuthViewModel(private val repository: AuthRepository = AuthRepository()) : ViewModel() {

    private val _isUserLoggedIn = MutableStateFlow(repository.getCurrentUser() != null)
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn

    var isLoading = false
    var errorMessage: String? = null

    init {
        repository.addAuthStateListener {
            _isUserLoggedIn.value = it
        }
    }

    // LOGIN FUNCTION
    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit
    ) {
        isLoading = true

        repository.login(email, password) { success, error ->
            isLoading = false
            if (success) {
                onSuccess()
            } else {
                errorMessage = error
            }
        }
    }

    // SIGNUP FUNCTION
    fun signup(
        email: String,
        password: String,
        onSuccess: () -> Unit
    ) {
        isLoading = true

        repository.signup(email, password) { success, error ->
            isLoading = false
            if (success) {
                onSuccess()
            } else {
                errorMessage = error
            }
        }
    }

    // RESET PASSWORD
    fun resetPassword(email: String, onSuccess: () -> Unit) {
        isLoading = true

        repository.resetPassword(email) { success, error ->
            isLoading = false
            if (success) {
                onSuccess()
            } else {
                errorMessage = error
            }
        }
    }

    // ✅ LOGOUT FUNCTION
    open fun logout() {
        repository.logout()
    }

    // ✅ GET CURRENT USER EMAIL
    open fun getCurrentUserEmail(): String? {
        return repository.getCurrentUser()?.email
    }
}