package com.kcalfit.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val FigmaNavy = Color(0xFF001B51)
private val FigmaDeepNavy = Color(0xFF000C24)
private val FigmaBlue = Color(0xFF003396)
private val FigmaGray = Color(0xFF888888)
private fun figmaBackground() = Brush.horizontalGradient(listOf(FigmaNavy, Color(0xFF0D100C)))

@Composable
private fun FigmaLogo() = Row(verticalAlignment = Alignment.CenterVertically) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(Color.White.copy(alpha = 0.15f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.FitnessCenter,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )
    }
    Spacer(Modifier.width(12.dp))
    Text("FitnessHub", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Medium)
}

@Composable
private fun FigmaButton(
    label: String,
    filled: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(25.dp))
            .background(
                if (filled) Brush.horizontalGradient(listOf(FigmaDeepNavy, FigmaBlue))
                else Brush.linearGradient(listOf(Color.White, Color.White))
            )
            .then(if (filled) Modifier.border(1.dp, Color.White, RoundedCornerShape(25.dp)) else Modifier)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label.uppercase(),
            color = if (filled) Color.White else FigmaNavy,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun WelcomeScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(figmaBackground())
            .padding(top = 100.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FigmaLogo()
            Spacer(Modifier.height(48.dp))
            Text("Welcome", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(54.dp))
            FigmaButton("Sign In", true, onNavigateToLogin, modifier = Modifier.width(261.dp))
            Spacer(Modifier.height(20.dp))
            FigmaButton("Sign Up", false, onNavigateToRegister, modifier = Modifier.width(261.dp))
        }
    }
}

@Composable
private fun FigmaField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    password: Boolean = false,
    visible: Boolean = false,
    onToggleVisibility: (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(Modifier.fillMaxWidth()) {
        Text(label, color = Color.Black, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(color = Color.Black, fontSize = 14.sp),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                visualTransformation = if (password && !visible) PasswordVisualTransformation() else VisualTransformation.None,
                decorationBox = { inner ->
                    Box(Modifier.weight(1f)) {
                        if (value.isEmpty()) Text(placeholder, color = FigmaGray, fontSize = 14.sp)
                        inner()
                    }
                },
                modifier = Modifier.weight(1f)
            )
            if (password && onToggleVisibility != null) {
                IconButton(onClick = onToggleVisibility, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = if (visible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (visible) "Hide password" else "Show password",
                        tint = FigmaGray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(Color.Black.copy(alpha = 0.3f)))
    }
}

@Composable
private fun AuthShell(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(figmaBackground())
            .imePadding()
            .navigationBarsPadding()
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 30.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 35.sp,
            modifier = Modifier.padding(start = 36.dp, top = 60.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 40.dp)
                .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 36.dp, vertical = 40.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun LoginScreen(
    onLoginSuccess: (String, String) -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    errorMessage: String? = null,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    AuthShell("Hello\nSign In", modifier) {
        FigmaField("Email", email, "john@email.com", { email = it; localError = null }, keyboardType = KeyboardType.Email)
        Spacer(Modifier.height(36.dp))
        FigmaField("Password", password, "", { password = it; localError = null }, true, visible, { visible = !visible })
        Spacer(Modifier.height(20.dp))
        TextButton(
            onClick = onNavigateToForgotPassword,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Forgot password?", color = Color.Black, fontSize = 13.sp)
        }
        Spacer(Modifier.height(24.dp))
        FigmaButton(
            label = "Sign In",
            filled = true,
            onClick = {
                when {
                    email.isBlank() -> localError = "Please enter your email"
                    password.isBlank() -> localError = "Please enter your password"
                    else -> onLoginSuccess(email.trim(), password)
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        )
        if (isLoading) {
            Spacer(Modifier.height(12.dp))
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp).align(Alignment.CenterHorizontally),
                color = FigmaBlue,
                strokeWidth = 2.dp
            )
        }
        (errorMessage ?: localError)?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        Spacer(Modifier.height(48.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Don't have an account? ", color = FigmaGray, fontSize = 14.sp)
            TextButton(onClick = onNavigateToRegister) {
                Text("Sign Up", color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun RegisterScreen(
    onRegisterSuccess: (String, String, String) -> Unit,
    onNavigateToLogin: () -> Unit,
    errorMessage: String? = null,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    AuthShell("Create Your\nAccount", modifier) {
        FigmaField("Full Name", name, "John Smith", { name = it; localError = null })
        Spacer(Modifier.height(16.dp))
        FigmaField("Email", email, "john@email.com", { email = it; localError = null }, keyboardType = KeyboardType.Email)
        Spacer(Modifier.height(16.dp))
        FigmaField("Password", password, "", { password = it; localError = null }, true, visible, { visible = !visible })
        Spacer(Modifier.height(16.dp))
        FigmaField("Confirm Password", confirm, "", { confirm = it; localError = null }, true, visible, { visible = !visible })
        Spacer(Modifier.height(28.dp))
        FigmaButton(
            label = "Sign Up",
            filled = true,
            onClick = {
                when {
                    name.isBlank() -> localError = "Please enter your name"
                    email.isBlank() || !email.contains("@") -> localError = "Please enter a valid email"
                    password.length < 6 -> localError = "Password must be at least 6 characters"
                    password != confirm -> localError = "Passwords do not match"
                    else -> onRegisterSuccess(name.trim(), email.trim(), password)
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        )
        if (isLoading) {
            Spacer(Modifier.height(12.dp))
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp).align(Alignment.CenterHorizontally),
                color = FigmaBlue,
                strokeWidth = 2.dp
            )
        }
        (errorMessage ?: localError)?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        Spacer(Modifier.height(28.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Already registered? ", color = FigmaGray, fontSize = 14.sp)
            TextButton(onClick = onNavigateToLogin) {
                Text("Sign In", color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun ForgotPasswordScreen(
    onRequestReset: (String, (String?) -> Unit) -> Unit,
    onNavigateBackToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Reset Password", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text(
            "Enter your email address and we'll send you a password reset link.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        FigmaField("Email Address", email, "john@email.com", { email = it }, keyboardType = KeyboardType.Email)
        Spacer(Modifier.height(16.dp))
        message?.let { Text(it, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp) }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                if (!email.contains("@")) {
                    message = "Enter a valid email address"
                } else {
                    loading = true
                    onRequestReset(email.trim()) {
                        loading = false
                        message = it ?: "If this account exists, a reset email is on its way."
                    }
                }
            },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Send Reset Link", fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onNavigateBackToLogin) {
            Text("Back to Login")
        }
    }
}
