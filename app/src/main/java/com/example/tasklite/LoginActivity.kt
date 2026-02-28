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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tasklite.ui.theme.TaskLiteTheme
import com.example.tasklite.viewmodel.AuthViewModel

class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TaskLiteTheme {
                LoginScreen(viewModel = viewModel())
            }
        }
    }
}

@Composable
fun LoginScreen(viewModel: AuthViewModel?) {

    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    if (viewModel != null) {
        LaunchedEffect(viewModel.isLoading, viewModel.errorMessage) {
            isLoading = viewModel.isLoading
            errorMessage = viewModel.errorMessage
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Login",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("email")
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("password")
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                viewModel?.login(email, password) {

                    Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()

                    // ✅ Go to Dashboard after login
                    val intent = Intent(context, DashboardActivity::class.java)
                    context.startActivity(intent)

                    (context as? ComponentActivity)?.finish()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("login_button")
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Login")
            }
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = {
                context.startActivity(Intent(context, SignupActivity::class.java))
            }
        ) {
            Text("Don't have an account? Sign Up")
        }

        TextButton(
            onClick = {
                context.startActivity(Intent(context, ForgotPasswordActivity::class.java))
            }
        ) {
            Text("Forgot Password?")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    TaskLiteTheme {
        LoginScreen(viewModel = null)
    }
}