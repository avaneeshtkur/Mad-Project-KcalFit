package com.kcalfit.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun EmailVerificationScreen(
    email: String,
    onVerifyCode: (String, (String?) -> Unit) -> Unit,
    onSuccess: () -> Unit,
    onResendCode: ((String?) -> Unit) -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var code by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var cooldown by remember { mutableIntStateOf(60) }
    LaunchedEffect(cooldown) { if (cooldown > 0) { delay(1000); cooldown-- } }

    Column(
        modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(Modifier.size(76.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(24.dp)), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.MarkEmailRead, null, Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.height(24.dp))
        Text("Check your inbox", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Enter the 6-digit code sent to $email", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(28.dp))
        OutlinedTextField(
            value = code,
            onValueChange = { code = it.filter(Char::isDigit).take(6); error = null },
            label = { Text("Verification code") },
            placeholder = { Text("000000") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )
        error?.let { Spacer(Modifier.height(8.dp)); Text(it, color = MaterialTheme.colorScheme.error) }
        message?.let { Spacer(Modifier.height(8.dp)); Text(it, color = MaterialTheme.colorScheme.primary) }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                if (code.length != 6) error = "Enter all 6 digits"
                else {
                    loading = true
                    onVerifyCode(code) { result ->
                        loading = false
                        if (result == null) onSuccess() else error = result
                    }
                }
            },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp)
        ) { if (loading) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp) else Text("Verify email") }
        TextButton(
            onClick = {
                onResendCode { result ->
                    if (result == null) { cooldown = 60; message = "A new code has been sent" }
                    else error = result
                }
            }, enabled = cooldown == 0
        ) { Text(if (cooldown > 0) "Resend in ${cooldown}s" else "Resend code") }
        TextButton(onClick = onNavigateToLogin) { Text("Back to login") }
    }
}
