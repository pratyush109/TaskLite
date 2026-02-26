package com.example.tasklite.viewmodel

import androidx.lifecycle.ViewModel
import com.example.tasklite.repository.AuthRepository

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    var isLoading = false
    var errorMessage: String? = null

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
    fun logout() {
        repository.logout()
    }

    // ✅ GET CURRENT USER EMAIL
    fun getCurrentUserEmail(): String? {
        return repository.getCurrentUser()?.email
    }
}