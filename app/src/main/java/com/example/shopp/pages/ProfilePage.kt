package com.example.shopp.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shopp.components.ErrorView
import com.example.shopp.components.LoadingView
import com.example.shopp.components.ShoppCard
import com.example.shopp.ui.showToast
import com.example.shopp.ui.theme.Spacing
import com.example.shopp.viewmodel.ProfileData
import com.example.shopp.viewmodel.ProfileEvent
import com.example.shopp.viewmodel.ProfileViewModel
import com.example.shopp.viewmodel.UiState

@Composable
fun ProfilePage(
    modifier: Modifier = Modifier,
    onOrdersClick: () -> Unit,
    onAddressesClick: () -> Unit,
    onSellerClick: () -> Unit,
    onSignedOut: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val darkChoice by viewModel.darkTheme.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ProfileEvent.Message -> context.showToast(event.text)
                ProfileEvent.SignedOut -> onSignedOut()
            }
        }
    }

    when (val state = uiState) {
        UiState.Loading -> LoadingView(modifier)
        is UiState.Error -> ErrorView(state.message, onRetry = viewModel::retry, modifier = modifier)
        is UiState.Success -> ProfileContent(
            data = state.data,
            darkTheme = darkChoice ?: isSystemInDarkTheme(),
            onDarkThemeChange = viewModel::setDarkTheme,
            onOrdersClick = onOrdersClick,
            onAddressesClick = onAddressesClick,
            onSellerClick = onSellerClick,
            onSignOut = viewModel::signOut,
            modifier = modifier
        )
    }
}

@Composable
private fun ProfileContent(
    data: ProfileData,
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    onOrdersClick: () -> Unit,
    onAddressesClick: () -> Unit,
    onSellerClick: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val isSeller = "seller" in data.user.roles

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Identity header
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF0A8F63), Color(0xFF066B4A))))
        ) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 30.dp, y = (-30).dp)
                    .size(130.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
            )
            Row(
                Modifier.padding(22.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    Modifier.size(64.dp).clip(CircleShape).background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = data.user.name.trim().firstOrNull()?.uppercase() ?: "S",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF0A8F63)
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        text = data.user.name.ifBlank { "Shopper" },
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (data.user.email.isNotBlank()) {
                        Text(
                            text = data.user.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Counts
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("${data.orderCount}", "Orders", Modifier.weight(1f))
            StatCard("${data.favoriteCount}", "Favorites", Modifier.weight(1f))
            StatCard("${data.cartItemCount}", "In cart", Modifier.weight(1f))
        }

        // Menu
        ShoppCard(Modifier.fillMaxWidth()) {
            MenuRow(Icons.Default.Receipt, colors.primaryContainer, colors.onPrimaryContainer, "My orders", onOrdersClick)
            HorizontalDivider(color = colors.surfaceContainer)
            MenuRow(Icons.Default.LocationOn, colors.tertiaryContainer, colors.onTertiaryContainer, "Saved addresses", onAddressesClick)
            HorizontalDivider(color = colors.surfaceContainer)
            Row(
                Modifier.fillMaxWidth().height(60.dp).padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                IconTile(Icons.Default.DarkMode, colors.surfaceContainerHigh, colors.onSurfaceVariant)
                Text("Dark mode", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), modifier = Modifier.weight(1f))
                Switch(checked = darkTheme, onCheckedChange = onDarkThemeChange)
            }
        }

        // Seller
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(colors.secondaryContainer)
                .clickable(role = Role.Button, onClick = onSellerClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(colors.secondary),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.Storefront, null, tint = colors.onSecondary) }
            Column(Modifier.weight(1f)) {
                Text(
                    if (isSeller) "Seller dashboard" else "Become a seller",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = colors.onSecondaryContainer
                )
                Text(
                    if (isSeller) "Manage your products and orders" else "Open your own shop in minutes",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onSecondaryContainer.copy(alpha = 0.85f)
                )
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = colors.onSecondaryContainer)
        }

        // Sign out
        Row(
            Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(18.dp))
                .border(1.5.dp, colors.outline, RoundedCornerShape(18.dp))
                .clickable(role = Role.Button, onClick = onSignOut),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, null, tint = colors.error, modifier = Modifier.size(20.dp))
            Spacer(Modifier.size(10.dp))
            Text("Sign out", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = colors.error)
        }
        Spacer(Modifier.height(Spacing.sm))
    }
}

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    ShoppCard(modifier) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun MenuRow(icon: ImageVector, container: Color, content: Color, label: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(60.dp).clickable(role = Role.Button, onClick = onClick).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        IconTile(icon, container, content)
        Text(label, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.outline)
    }
}

@Composable
private fun IconTile(icon: ImageVector, container: Color, content: Color) {
    Box(Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(container), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = content, modifier = Modifier.size(20.dp))
    }
}
