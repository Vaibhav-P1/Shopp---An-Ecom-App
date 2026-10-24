package com.example.shopp.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shopp.pages.CartPage
import com.example.shopp.pages.FavoritePage
import com.example.shopp.pages.HomePage
import com.example.shopp.pages.ProfilePage
import com.example.shopp.viewmodel.CartViewModel
import com.example.shopp.viewmodel.UiState

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onCategoryClick: (String) -> Unit,
    onProductClick: (String) -> Unit,
    onCheckout: () -> Unit,
    onOrdersClick: () -> Unit,
    onSellerClick: () -> Unit,
    onSearchClick: () -> Unit,
    onAddressesClick: () -> Unit,
    onSignedOut: () -> Unit
){

    val navItemList = listOf(
        NavItem("Home", Icons.Default.Home),
        NavItem("Favorite", Icons.Default.Favorite),
        NavItem("Cart", Icons.Default.ShoppingCart, showsCartCount = true),
        NavItem("Profile", Icons.Default.Person)
    )

    var selectedIndex by rememberSaveable {
        mutableStateOf(0)
    }



    val cartViewModel: CartViewModel = hiltViewModel()
    val cartState by cartViewModel.uiState.collectAsStateWithLifecycle()
    val cartCount = (cartState as? UiState.Success)?.data?.sumOf { it.quantity.toInt() } ?: 0

    // Phones get a bottom bar, tablets and landscape get a rail on the side
    val isWide = LocalConfiguration.current.screenWidthDp >= WIDE_LAYOUT_MIN_WIDTH_DP

    val tabColors = NavigationBarItemDefaults.colors(
        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer
    )
    val tabIcon: @Composable (NavItem) -> Unit = { navItem ->
        if (navItem.showsCartCount) {
            CartIcon(navItem, cartCount)
        } else {
            Icon(imageVector = navItem.icon, contentDescription = navItem.label)
        }
    }

    Scaffold(
        bottomBar = {
            if (!isWide) {
                NavigationBar {
                    navItemList.forEachIndexed { index, navItem ->
                        NavigationBarItem(
                            selected = index == selectedIndex,
                            onClick = { selectedIndex = index },
                            colors = tabColors,
                            icon = { tabIcon(navItem) },
                            label = { Text(text = navItem.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Row(Modifier.padding(innerPadding)) {
            if (isWide) {
                NavigationRail(windowInsets = WindowInsets(0)) {
                    navItemList.forEachIndexed { index, navItem ->
                        NavigationRailItem(
                            selected = index == selectedIndex,
                            onClick = { selectedIndex = index },
                            colors = NavigationRailItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            icon = { tabIcon(navItem) },
                            label = { Text(text = navItem.label) }
                        )
                    }
                }
            }
            Crossfade(targetState = selectedIndex, label = "tabs") { tab ->
                ContentScreen(
                    selectedIndex = tab,
                    onCategoryClick = onCategoryClick,
                    onProductClick = onProductClick,
                    onCheckout = onCheckout,
                    onOrdersClick = onOrdersClick,
                    onSellerClick = onSellerClick,
                    onSearchClick = onSearchClick,
                    onAddressesClick = onAddressesClick,
                    onSignedOut = onSignedOut
                )
            }
        }
    }
}

private const val WIDE_LAYOUT_MIN_WIDTH_DP = 600

@Composable
fun ContentScreen(
    modifier: Modifier = Modifier,
    selectedIndex: Int,
    onCategoryClick: (String) -> Unit,
    onProductClick: (String) -> Unit,
    onCheckout: () -> Unit,
    onOrdersClick: () -> Unit,
    onSellerClick: () -> Unit,
    onSearchClick: () -> Unit,
    onAddressesClick: () -> Unit,
    onSignedOut: () -> Unit
) {
    when (selectedIndex) {
        0-> HomePage(modifier = modifier, onCategoryClick = onCategoryClick, onSearchClick = onSearchClick)
        1-> FavoritePage(modifier = modifier, onProductClick = onProductClick)
        2-> CartPage(modifier = modifier, onCheckout = onCheckout, onProductClick = onProductClick)
        3-> ProfilePage(
            modifier = modifier,
            onOrdersClick = onOrdersClick,
            onAddressesClick = onAddressesClick,
            onSellerClick = onSellerClick,
            onSignedOut = onSignedOut
        )
    }
}

data class NavItem(
    val label : String,
    val icon : ImageVector,
    val showsCartCount: Boolean = false
)

/** Cart tab icon with a badge that pops in and animates when the item count changes. */
@Composable
private fun CartIcon(navItem: NavItem, count: Int) {
    BadgedBox(
        badge = {
            AnimatedVisibility(visible = count > 0, enter = scaleIn(), exit = scaleOut()) {
                Badge(containerColor = MaterialTheme.colorScheme.secondary) {
                    AnimatedContent(targetState = count, label = "cartCount") { value ->
                        Text(text = if (value > 99) "99+" else value.toString())
                    }
                }
            }
        }
    ) {
        Icon(
            imageVector = navItem.icon,
            contentDescription = if (count > 0) "${navItem.label}, $count items" else navItem.label
        )
    }
}


