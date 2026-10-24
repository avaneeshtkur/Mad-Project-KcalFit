package com.kcalfit.app.ui.auth

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kcalfit.app.R
import com.kcalfit.app.ui.nakafit.*

@Composable
fun WelcomeScreen(onNavigateToLogin: () -> Unit, onNavigateToRegister: () -> Unit,
    onNavigateToGoogle: () -> Unit = {}, modifier: Modifier = Modifier) {
    com.kcalfit.app.ui.reference.ReferenceWelcome(onNavigateToLogin, onNavigateToRegister, modifier)
}

@Composable
private fun AuthPhotoShell(title: String, subtitle: String, image: Int, modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).imePadding().verticalScroll(rememberScrollState())) {
        Box(Modifier.fillMaxWidth().height(235.dp)) {
            NakaPhoto(image, "Fitness studio", Modifier.fillMaxSize())
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = .8f)))))
            Column(Modifier.align(Alignment.BottomStart).padding(24.dp)) {
                Text("kcalfit", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(10.dp))
                Text(title, color = Color.White, fontSize = 30.sp, lineHeight = 35.sp)
            }
        }
        Column(Modifier.fillMaxWidth().padding(24.dp).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(subtitle, color = Naka.Muted, fontSize = 14.sp)
            content()
        }
    }
}

@Composable
private fun AuthField(label: String, value: String, onChange: (String) -> Unit,
    password: Boolean = false, type: KeyboardType = KeyboardType.Text) {
    var visible by rememberSaveable { mutableStateOf(false) }
    OutlinedTextField(value, onChange, Modifier.fillMaxWidth(), label = { Text(label) }, singleLine = true,
        shape = RoundedCornerShape(28.dp), keyboardOptions = KeyboardOptions(keyboardType = type),
        visualTransformation = if (password && !visible) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = if (password) { { IconButton(onClick = { visible = !visible }) {
            Icon(if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                if (visible) "Hide password" else "Show password")
        } } } else null,
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Naka.Violet, unfocusedBorderColor = Naka.Line,
            focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface))
}

@Composable
fun LoginScreen(onLoginSuccess: (String, String) -> Unit, onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit, errorMessage: String? = null, isLoading: Boolean = false,
    onGoogleSignIn: () -> Unit = {}, modifier: Modifier = Modifier, retryAfterSeconds: Int = 0) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    AuthPhotoShell("Welcome back", "Sign in to continue your fitness journey.", R.drawable.nakafit_runner, modifier) {
        AuthField("Email address", email, { email = it; error = null }, type = KeyboardType.Email)
        AuthField("Password", password, { password = it; error = null }, password = true, type = KeyboardType.Password)
        TextButton(onNavigateToForgotPassword, Modifier.align(Alignment.End)) { Text("Forgot password?", color = MaterialTheme.colorScheme.onSurface) }
        (errorMessage ?: error)?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp) }
        NakaButton(if (retryAfterSeconds > 0) "Retry after $retryAfterSeconds seconds" else if (isLoading) "Signing in…" else "Sign In", {
            when {
                !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> error = "Enter a valid email address"
                password.isBlank() -> error = "Enter your password"
                else -> onLoginSuccess(email.trim(), password)
            }
        }, enabled = !isLoading && retryAfterSeconds == 0)
        OutlinedButton(onGoogleSignIn, Modifier.fillMaxWidth().height(50.dp), enabled = !isLoading,
            shape = RoundedCornerShape(28.dp)) { Text("Continue with Google") }
        Row(Modifier.align(Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically) {
            Text("New to kcalfit?", color = Naka.Muted, fontSize = 13.sp)
            TextButton(onNavigateToRegister) { Text("Sign Up", color = Naka.Violet) }
        }
    }
}

@Composable
fun RegisterScreen(onRegisterSuccess: (String, String, String) -> Unit, onNavigateToLogin: () -> Unit,
    errorMessage: String? = null, isLoading: Boolean = false, onGoogleSignIn: () -> Unit = {}, modifier: Modifier = Modifier) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    AuthPhotoShell("Create your account", "Start building healthy habits with kcalfit.", R.drawable.nakafit_gym_motivation, modifier) {
        AuthField("Full name", name, { name = it; error = null })
        AuthField("Email address", email, { email = it; error = null }, type = KeyboardType.Email)
        AuthField("Password", password, { password = it; error = null }, true, KeyboardType.Password)
        AuthField("Confirm password", confirm, { confirm = it; error = null }, true, KeyboardType.Password)
        (errorMessage ?: error)?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp) }
        NakaButton(if (isLoading) "Creating account…" else "Sign Up", {
            when {
                name.isBlank() -> error = "Enter your name"
                !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> error = "Enter a valid email address"
                password.length < 6 -> error = "Use at least 6 characters for your password"
                password != confirm -> error = "Passwords do not match"
                else -> onRegisterSuccess(name.trim(), email.trim(), password)
            }
        }, enabled = !isLoading)
        OutlinedButton(onGoogleSignIn, Modifier.fillMaxWidth().height(50.dp), enabled = !isLoading,
            shape = RoundedCornerShape(28.dp)) { Text("Continue with Google") }
        Row(Modifier.align(Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically) {
            Text("Already have an account?", color = Naka.Muted, fontSize = 13.sp)
            TextButton(onNavigateToLogin) { Text("Sign In", color = Naka.Violet) }
        }
    }
}

@Composable
fun ResetPasswordScreen(onSubmit: (String, (String?) -> Unit) -> Unit, onBack: () -> Unit,
    modifier: Modifier = Modifier) {
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    NakaPage("Choose a new password", onBack) {
        Spacer(Modifier.height(24.dp))
        Text("Secure your account", color = MaterialTheme.colorScheme.onSurface, fontSize = 26.sp)
        Text("Choose a new password to finish resetting your account.", color = Naka.Muted)
        AuthField("New password", password, { password = it; error = null }, true, KeyboardType.Password)
        AuthField("Confirm new password", confirm, { confirm = it; error = null }, true, KeyboardType.Password)
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        NakaButton(if (loading) "Updating…" else "Update Password", {
            when {
                password.length < 6 -> error = "Use at least 6 characters for your password"
                password != confirm -> error = "Passwords do not match"
                else -> { loading = true; onSubmit(password) { result -> loading = false; error = result } }
            }
        }, enabled = !loading)
    }
}

@Composable
fun ForgotPasswordScreen(onRequestReset: (String, (String?) -> Unit) -> Unit,
    onNavigateBackToLogin: () -> Unit, modifier: Modifier = Modifier) {
    var email by rememberSaveable { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    NakaPage("Reset Password", onNavigateBackToLogin) {
        Spacer(Modifier.height(30.dp))
        Text("Let's get you back in", color = MaterialTheme.colorScheme.onSurface, fontSize = 28.sp)
        Text("Enter your email and we'll send you a reset link.", color = Naka.Muted)
        AuthField("Email address", email, { email = it }, type = KeyboardType.Email)
        message?.let { Text(it, color = Naka.Muted) }
        NakaButton(if (loading) "Sending…" else "Send Reset Link", {
            if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) message = "Enter a valid email address"
            else { loading = true; onRequestReset(email.trim()) { loading = false; message = it ?: "If an account exists, a reset email is on its way." } }
        }, enabled = !loading)
    }
}
