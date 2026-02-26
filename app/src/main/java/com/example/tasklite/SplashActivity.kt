package com.example.tasklite

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

// 👉 change these to your theme colors if different
import com.example.tasklite.ui.theme.Blue
import com.example.tasklite.ui.theme.White
import com.example.tasklite.ui.theme.TaskLiteTheme

class SplashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TaskLiteTheme {
                SplashBody()
            }
        }
    }
}

@Composable
fun SplashBody() {

    val context = LocalContext.current
    val activity = context.findActivity()

    LaunchedEffect(Unit) {
        delay(1500) // wait 1.5 seconds

        // Check Firebase login state
        val currentUser = FirebaseAuth.getInstance().currentUser

        // Decide which screen to go to
        val intent = if (currentUser != null) {
            Intent(context, DashboardActivity::class.java)
        } else {
            Intent(context, LoginActivity::class.java)
        }

        context.startActivity(intent)
        activity?.finish() // close SplashActivity so user cannot go back
    }

    Scaffold { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Blue),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = "App Logo",
                modifier = Modifier.size(90.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "TaskLite",
                color = White,
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(30.dp))

            CircularProgressIndicator(color = White)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashPreview() {
    TaskLiteTheme {
        SplashBody()
    }
}

/**
 * Finds the activity from a given context.
 *
 * This extension function is used to safely find the activity from a given context. It's
 * especially useful in Jetpack Compose previews, where the context is not an activity.
 *
 * @return The activity if found, or null otherwise.
 */
fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}