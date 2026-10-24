package com.example.shopp.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shopp.components.BrandWordmark
import com.example.shopp.components.PrimaryButton
import com.example.shopp.ui.theme.Spacing
import kotlinx.coroutines.launch

private data class OnboardingPage(val title: String, val body: String)

private val pages = listOf(
    OnboardingPage("Find what you love, fast", "Search the whole catalogue, filter by price and category, and save favourites for later."),
    OnboardingPage("Checkout in a few taps", "Save your addresses, apply coupons and pay online or when it arrives."),
    OnboardingPage("Track every step", "Watch your order move from packed to delivered. Got something to sell? Open your own shop from Profile.")
)

/** Three-page first-run walkthrough. [onFinish] is called for both Skip and the last "Get started". */
@Composable
fun OnboardingScreen(onFinish: () -> Unit, modifier: Modifier = Modifier) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val last = pagerState.currentPage == pages.lastIndex

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .padding(horizontal = Spacing.xl, vertical = Spacing.lg)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(44.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BrandWordmark(
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary,
                dotColor = MaterialTheme.colorScheme.secondary
            )
            if (!last) {
                TextButton(onClick = onFinish) {
                    Text("Skip", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { index ->
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    when (index) {
                        0 -> DiscoverIllustration()
                        1 -> CheckoutIllustration()
                        else -> TrackIllustration()
                    }
                }
                Text(
                    text = pages[index].title,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(Spacing.sm))
                Text(
                    text = pages[index].body,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(Spacing.lg))
            }
        }

        PageDots(
            count = pages.size,
            selected = pagerState.currentPage,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(Spacing.xl))
        PrimaryButton(
            text = if (last) "Get started" else "Next",
            onClick = {
                if (last) onFinish() else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
            }
        )
    }
}

@Composable
private fun PageDots(count: Int, selected: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(count) { index ->
            Box(
                Modifier
                    .height(8.dp)
                    .width(if (index == selected) 24.dp else 8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (index == selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant
                    )
            )
        }
    }
}

// Illustrations are simple shapes standing in for photos; the real app shows real product data.

@Composable
private fun IllustrationBackdrop(main: Color, accent: Color, content: @Composable () -> Unit) {
    Box(Modifier.size(width = 320.dp, height = 380.dp)) {
        Box(Modifier.padding(start = 20.dp, top = 30.dp).size(280.dp).clip(CircleShape).background(main))
        Box(Modifier.size(84.dp).clip(CircleShape).background(accent))
        content()
    }
}

@Composable
private fun FloatingCard(modifier: Modifier = Modifier, radius: Int = 22, content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(radius.dp)
    Box(
        modifier = modifier
            .shadow(14.dp, shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest, shape)
    ) { content() }
}

@Composable
private fun ProductTile(
    label: String,
    price: String,
    colors: List<Color>,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    FloatingCard(modifier.size(width = 128.dp, height = 168.dp)) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(colors)),
                contentAlignment = Alignment.Center
            ) { Icon(icon, null, tint = Color.White, modifier = Modifier.size(48.dp)) }
            Text(label, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
            Text(
                price,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun DiscoverIllustration() {
    val cs = MaterialTheme.colorScheme
    IllustrationBackdrop(main = cs.primaryContainer, accent = cs.secondaryContainer) {
        FloatingCard(Modifier.padding(start = 30.dp, top = 40.dp).size(width = 260.dp, height = 52.dp), radius = 26) {
            Row(
                Modifier.fillMaxSize().padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Default.Search, null, tint = cs.primary, modifier = Modifier.size(20.dp))
                Text("Search sneakers, phones...", style = MaterialTheme.typography.bodySmall, color = cs.outline, maxLines = 1)
            }
        }
        ProductTile(
            "Court Sneakers", "₹6,495", listOf(cs.tertiary, cs.tertiaryContainer), Icons.Default.Checkroom,
            Modifier.padding(start = 24.dp, top = 118.dp)
        )
        ProductTile(
            "Smartphone", "₹13,999", listOf(cs.secondary, cs.secondaryContainer), Icons.Default.PhoneAndroid,
            Modifier.padding(start = 168.dp, top = 150.dp)
        )
        Box(
            Modifier
                .padding(start = 230.dp, top = 296.dp)
                .height(36.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(cs.secondary)
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("40% OFF", color = cs.onSecondary, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
        }
    }
}

@Composable
private fun CheckoutIllustration() {
    val cs = MaterialTheme.colorScheme
    IllustrationBackdrop(main = cs.secondaryContainer, accent = cs.primaryContainer) {
        FloatingCard(Modifier.padding(start = 34.dp, top = 58.dp).width(252.dp), radius = 24) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        Modifier.size(34.dp).clip(RoundedCornerShape(12.dp)).background(cs.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.LocationOn, null, tint = cs.onPrimaryContainer, modifier = Modifier.size(18.dp)) }
                    Column {
                        Text("Home", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
                        Text("12 Test Street, Bengaluru", style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                    }
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(cs.surfaceVariant))
                SummaryRow("Subtotal", "₹3,000", cs.onSurfaceVariant)
                SummaryRow("WELCOME10", "-₹300", cs.primary)
                SummaryRow("Total", "₹3,051", cs.onSurface, bold = true)
                Box(
                    Modifier.fillMaxWidth().height(42.dp).clip(RoundedCornerShape(14.dp)).background(cs.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Place order", color = cs.onPrimary, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold))
                }
            }
        }
        IllustrationChip("Cash on delivery", Icons.Default.Check, Modifier.padding(start = 12.dp, top = 312.dp))
        IllustrationChip("Pay online", Icons.Default.CreditCard, Modifier.padding(start = 176.dp, top = 336.dp))
    }
}

@Composable
private fun SummaryRow(label: String, value: String, color: Color, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        val style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
        Text(label, style = style, color = color)
        Text(value, style = style, color = color)
    }
}

@Composable
private fun IllustrationChip(label: String, icon: ImageVector, modifier: Modifier = Modifier) {
    FloatingCard(modifier.height(40.dp), radius = 20) {
        Row(
            Modifier.padding(horizontal = 16.dp).height(40.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            Text(label, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
        }
    }
}

@Composable
private fun TrackIllustration() {
    val cs = MaterialTheme.colorScheme
    IllustrationBackdrop(main = cs.tertiaryContainer, accent = cs.primaryContainer) {
        FloatingCard(Modifier.padding(start = 34.dp, top = 50.dp).width(252.dp), radius = 24) {
            Column(Modifier.padding(horizontal = 18.dp, vertical = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "ORD_51D15B4C73",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = cs.onSurfaceVariant
                )
                Row(Modifier.fillMaxWidth()) {
                    // 2 = done, 1 = current, 0 = upcoming
                    listOf("Placed" to 2, "Packed" to 2, "Shipped" to 1, "Delivered" to 0).forEach { (label, state) ->
                        Column(
                            Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val dot = when (state) {
                                2 -> cs.primary
                                1 -> cs.secondary
                                else -> cs.surfaceContainerHighest
                            }
                            Box(Modifier.size(24.dp).clip(CircleShape).background(dot), contentAlignment = Alignment.Center) {
                                if (state == 2) Icon(Icons.Default.Check, null, tint = cs.onPrimary, modifier = Modifier.size(14.dp))
                            }
                            Text(
                                label,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                maxLines = 1,
                                softWrap = false,
                                color = if (state == 0) cs.outline else cs.onSurface
                            )
                        }
                    }
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(cs.surfaceVariant))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.linearGradient(listOf(cs.tertiary, cs.tertiaryContainer)))
                    )
                    Column {
                        Text("Court Sneakers", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
                        Text("Arrives tomorrow", style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                    }
                }
            }
        }
        FloatingCard(Modifier.padding(start = 150.dp, top = 300.dp).size(width = 170.dp, height = 58.dp), radius = 20) {
            Row(
                Modifier.padding(horizontal = 14.dp).fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    Modifier.size(34.dp).clip(RoundedCornerShape(11.dp)).background(cs.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.Storefront, null, tint = cs.onSecondaryContainer, modifier = Modifier.size(18.dp)) }
                Column {
                    Text("Sell on Shopp", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
                    Text("List in minutes", style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                }
            }
        }
    }
}
