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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tasklite.ui.theme.TaskLiteTheme
import com.example.tasklite.viewmodel.AuthViewModel

class SignupActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TaskLiteTheme {
                SignupScreen()
            }
        }
    }
}

@Composable
fun SignupScreen(viewModel: AuthViewModel = viewModel()) {

    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Observe ViewModel state
    LaunchedEffect(viewModel.isLoading, viewModel.errorMessage) {
        isLoading = viewModel.isLoading
        errorMessage = viewModel.errorMessage
    }

    // This is the stateful composable that holds the logic
    // and passes state and callbacks to the stateless composable.
    SignupScreenContent(
        email = email,
        onEmailChange = { email = it },
        password = password,
        onPasswordChange = { password = it },
        confirmPassword = confirmPassword,
        onConfirmPasswordChange = { confirmPassword = it },
        isLoading = isLoading,
        errorMessage = errorMessage,
        onSignupClick = {
            if (email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else if (password != confirmPassword) {
                Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.signup(email, password) {
                    Toast.makeText(context, "Signup Successful", Toast.LENGTH_SHORT).show()
                    context.startActivity(Intent(context, DashboardActivity::class.java))
                    (context as? ComponentActivity)?.finish()
                }
            }
        },
        onLoginClick = {
            context.startActivity(Intent(context, LoginActivity::class.java))
        }
    )
}

// This is the new stateless composable that only displays the UI.
@Composable
fun SignupScreenContent(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    onSignupClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Sign Up",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onSignupClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Sign Up")
            }
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onLoginClick
        ) {
            Text("Already have an account? Login")
        }
    }
}


@Preview(showBackground = true)
@Composable
fun SignupPreview() {
    TaskLiteTheme {
        // The preview now calls the stateless composable with dummy data.
        // This avoids instantiating the AuthViewModel, which was causing the crash.
        SignupScreenContent(
            email = "user@example.com",
            onEmailChange = {},
            password = "password",
            onPasswordChange = {},
            confirmPassword = "password",
            onConfirmPasswordChange = {},
            isLoading = false,
            errorMessage = null,
            onSignupClick = {},
            onLoginClick = {}
        )
    }
}