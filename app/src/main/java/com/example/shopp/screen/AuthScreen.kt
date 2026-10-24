package com.example.shopp.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shopp.R
import com.example.shopp.components.BrandLogo
import com.example.shopp.components.BrandWordmark
import com.example.shopp.components.LabeledDivider
import com.example.shopp.components.PrimaryButton
import com.example.shopp.components.SecondaryButton
import com.example.shopp.ui.theme.CoralContainerLight
import com.example.shopp.ui.theme.Spacing
import com.example.shopp.viewmodel.AuthViewModel

/** The welcome screen: brand header, then log in, create account or continue with Google. */
@Composable
fun AuthScreen(
    modifier: Modifier = Modifier,
    onLoginClick: () -> Unit,
    onSignupClick: () -> Unit,
    onAuthenticated: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authViewModel: AuthViewModel = hiltViewModel()
    val webClientId = context.getString(R.string.default_web_client_id)
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()
    AuthEventsEffect(authViewModel, onAuthenticated)

    Box(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .widthIn(max = 520.dp)
                .align(Alignment.TopCenter)
        ) {
            WelcomeHeader()

            Column(
                Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = Spacing.xl).padding(top = 28.dp, bottom = Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Text(
                    text = "Start your shopping journey",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Great prices, fast delivery, and everything in one place.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(Spacing.xs))
                PrimaryButton(text = "Log in", onClick = onLoginClick)
                SecondaryButton(text = "Create account", onClick = onSignupClick)
                LabeledDivider("or", Modifier.padding(vertical = Spacing.xs))
                GoogleButton(
                    onClick = { authViewModel.googleSignIn(context, scope, webClientId) },
                    enabled = !uiState.isLoading
                )
                Text(
                    text = "By continuing you agree to our Terms of Service and Privacy Policy.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun WelcomeHeader() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(430.dp)
            .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF12A878), Color(0xFF0A8F63), Color(0xFF066B4A))))
    ) {
        Box(Modifier.offset(x = (-110).dp, y = (-80).dp).size(300.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.07f)))
        Box(Modifier.align(Alignment.BottomEnd).offset(x = 70.dp, y = 60.dp).size(220.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.06f)))

        Row(
            Modifier.statusBarsPadding().padding(start = Spacing.xl, top = Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BrandLogo(size = 40.dp)
            BrandWordmark(fontSize = 26.sp, color = Color.White, dotColor = CoralContainerLight)
        }

        WelcomeTile(
            label = "Court Sneakers",
            icon = Icons.Default.Checkroom,
            colors = listOf(Color(0xFF3A6B7E), Color(0xFFA5CCDF)),
            modifier = Modifier.padding(start = 36.dp, top = 150.dp).rotate(-6f)
        )
        WelcomeTile(
            label = "Smartphone",
            icon = Icons.Default.PhoneAndroid,
            colors = listOf(Color(0xFFD94A33), Color(0xFFFFB4A6)),
            modifier = Modifier.padding(start = 196.dp, top = 190.dp).rotate(5f)
        )
    }
}

@Composable
private fun WelcomeTile(label: String, icon: ImageVector, colors: List<Color>, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(26.dp)
    Column(
        modifier = modifier
            .size(width = 150.dp, height = 190.dp)
            .shadow(16.dp, shape)
            .background(Color.White, shape)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Brush.linearGradient(colors)),
            contentAlignment = Alignment.Center
        ) { Icon(icon, null, tint = Color.White, modifier = Modifier.size(56.dp)) }
        Text(
            label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = Color(0xFF171D1A)
        )
    }
}

@Composable
fun GoogleButton(onClick: () -> Unit, enabled: Boolean, modifier: Modifier = Modifier) {
    SecondaryButton(
        text = "Continue with Google",
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        leadingContent = {
            Icon(
                painter = painterResource(id = R.drawable.ic_google),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = Color.Unspecified
            )
        }
    )
}

/**
 * Shared layout for the login and signup screens: top-aligned, scrolls when the content does not
 * fit (small screens, landscape, keyboard open) so the primary button is never hidden by the IME.
 */
@Composable
fun AuthLayout(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    BoxWithConstraints(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .heightIn(min = maxHeight)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.xl, vertical = Spacing.lg),
            content = content
        )
    }
}
