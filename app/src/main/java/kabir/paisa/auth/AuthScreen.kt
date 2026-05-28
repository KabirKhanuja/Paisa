package kabir.paisa.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kabir.paisa.data.AuthRepository
import kabir.paisa.ui.theme.PaisaColors
import kabir.paisa.ui.theme.PaisaTextStyles
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(onSignedIn: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { if (AuthRepository.isSignedIn) onSignedIn() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PaisaColors.Background)
    ) {
        // Top blue hero with logo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(PaisaColors.Primary)
                .padding(top = 56.dp, bottom = 48.dp, start = 20.dp, end = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(PaisaColors.SurfaceContainerLowest, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("₹", style = PaisaTextStyles.AmountDisplay, color = PaisaColors.Primary)
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "Paisa",
                    color = PaisaColors.OnPrimary,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Every rupee has a job.",
                    color = PaisaColors.OnPrimaryContainer,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                if (isSignUp) "Create your account" else "Welcome back",
                style = MaterialTheme.typography.headlineSmall,
                color = PaisaColors.OnSurface,
                fontWeight = FontWeight.SemiBold
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Email") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PaisaColors.Primary,
                    unfocusedBorderColor = PaisaColors.OutlineVariant,
                    focusedContainerColor = PaisaColors.SurfaceContainerLowest,
                    unfocusedContainerColor = PaisaColors.SurfaceContainerLowest,
                )
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PaisaColors.Primary,
                    unfocusedBorderColor = PaisaColors.OutlineVariant,
                    focusedContainerColor = PaisaColors.SurfaceContainerLowest,
                    unfocusedContainerColor = PaisaColors.SurfaceContainerLowest,
                )
            )

            if (error != null) {
                Text(error!!, color = PaisaColors.Error, style = MaterialTheme.typography.labelMedium)
            }

            Button(
                onClick = {
                    error = null
                    loading = true
                    scope.launch {
                        val result = if (isSignUp) AuthRepository.signUp(email.trim(), password)
                        else AuthRepository.signIn(email.trim(), password)
                        loading = false
                        result.fold(
                            onSuccess = { onSignedIn() },
                            onFailure = { error = it.message ?: "Something went wrong" }
                        )
                    }
                },
                enabled = !loading && email.isNotBlank() && password.length >= 6,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PaisaColors.Primary,
                    contentColor = PaisaColors.OnPrimary
                )
            ) {
                Text(
                    if (loading) "Please wait…" else if (isSignUp) "Create account" else "Sign in",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            TextButton(
                onClick = { isSignUp = !isSignUp; error = null },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (isSignUp) "Already have an account? Sign in"
                    else "New here? Create an account",
                    color = PaisaColors.Primary
                )
            }

            Spacer(Modifier.height(8.dp))

            // Skip for now — lets users open the app without Firebase configured.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, PaisaColors.OutlineVariant, RoundedCornerShape(14.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                TextButton(onClick = onSignedIn) {
                    Text("Continue without an account", color = PaisaColors.OnSurfaceVariant)
                }
            }
        }
    }
}
