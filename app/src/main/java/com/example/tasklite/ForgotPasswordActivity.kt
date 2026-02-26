package com.example.tasklite

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tasklite.ui.theme.TaskLiteTheme
import com.example.tasklite.viewmodel.AuthViewModel

class ForgotPasswordActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TaskLiteTheme {
                // Changed the composable to a stateful version that handles the ViewModel
                ForgotPasswordRoute()
            }
        }
    }
}

/**
 * This is a stateful composable that manages the state and logic for the ForgotPasswordScreen.
 * It connects to the AuthViewModel and passes down state and event handlers to the stateless
 * ForgotPasswordScreen.
 */
@Composable
fun ForgotPasswordRoute(viewModel: AuthViewModel = viewModel()) {

    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Observe ViewModel state
    LaunchedEffect(viewModel.isLoading, viewModel.errorMessage) {
        isLoading = viewModel.isLoading
        errorMessage = viewModel.errorMessage
    }
    
    ForgotPasswordScreen(
        isLoading = isLoading,
        errorMessage = errorMessage,
        onResetPassword = { email ->
            viewModel.resetPassword(email) {
                Toast.makeText(context, "Reset email sent!", Toast.LENGTH_LONG).show()
            }
        },
        onBackToLogin = {
            context.startActivity(Intent(context, LoginActivity::class.java))
        }
    )
}

/**
 * This is a stateless composable that only displays the UI for the Forgot Password screen.
 * It receives all its state and callbacks as parameters, making it easy to preview and test.
 */
@Composable
fun ForgotPasswordScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onResetPassword: (email: String) -> Unit,
    onBackToLogin: () -> Unit
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Reset Password",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (email.isBlank()) {
                    Toast.makeText(context, "Please enter your email", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                onResetPassword(email)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Send Reset Email")
            }
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onBackToLogin
        ) {
            Text("Back to Login")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ForgotPasswordPreview() {
    TaskLiteTheme {
        // The preview now calls the stateless composable with dummy data,
        // which avoids instantiating the ViewModel.
        ForgotPasswordScreen(
            isLoading = false,
            errorMessage = null,
            onResetPassword = {},
            onBackToLogin = {}
        )
    }
}