package com.mes.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mes.core.designsystem.theme.MesColor
import com.mes.core.domain.UserRole
import com.mes.feature.cart.CartScreen
import com.mes.feature.catalog.CatalogScreen
import com.mes.feature.catalog.presentation.SellersScreen
import com.mes.feature.profile.ProfileScreen

import androidx.compose.ui.unit.dp
import com.mes.app.Routes

import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Receipt

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    // Buyer items
    object Home : BottomNavItem(Routes.CATALOG, Icons.Filled.Home, "Home")
    object Sellers : BottomNavItem("sellers_tab", Icons.Filled.Storefront, "Sellers")
    object Cart : BottomNavItem(Routes.CART, Icons.Filled.ShoppingCart, "Cart")
    object Profile : BottomNavItem(Routes.PROFILE, Icons.Filled.Person, "Profile")

    // Merchant items
    object MerchantDashboard : BottomNavItem(Routes.MERCHANT_DASHBOARD, Icons.Filled.Dashboard, "Home")
    object MerchantOrders : BottomNavItem("merchant_orders_tab", Icons.Filled.Receipt, "Orders")
    object MerchantListings : BottomNavItem(Routes.MANAGE_LISTINGS, Icons.Filled.ListAlt, "Listings")
}

@Composable
fun MainScreen(
    rootNavController: NavHostController,
    currentUserRole: UserRole?
) {
    val navController = rememberNavController()
    val items = if (currentUserRole == UserRole.MERCHANT) {
        listOf(
            BottomNavItem.MerchantDashboard,
            BottomNavItem.MerchantOrders,
            BottomNavItem.MerchantListings,
            BottomNavItem.Profile
        )
    } else {
        listOf(
            BottomNavItem.Home,
            BottomNavItem.Sellers,
            BottomNavItem.Cart,
            BottomNavItem.Profile
        )
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MesColor.PrimaryTeal,
                            selectedTextColor = MesColor.PrimaryTeal,
                            indicatorColor = MesColor.PrimaryTealContainer
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = items.first().route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Buyer Routes
            composable(BottomNavItem.Home.route) {
                CatalogScreen(
                    onProductClick = { productId ->
                        rootNavController.navigate("product/$productId")
                    },
                    onCartClick = {
                        navController.navigate(BottomNavItem.Cart.route)
                    },
                    onNotificationsClick = {
                        rootNavController.navigate(Routes.NOTIFICATIONS)
                    },
                    onProfileClick = {
                        navController.navigate(BottomNavItem.Profile.route)
                    },
                    onSellerClick = {
                        navController.navigate(BottomNavItem.Sellers.route)
                    }
                )
            }
            composable(BottomNavItem.Sellers.route) {
                SellersScreen(
                    onSellerClick = { sellerId ->
                        rootNavController.navigate("seller/$sellerId")
                    },
                    onNotificationsClick = {
                        rootNavController.navigate(Routes.NOTIFICATIONS)
                    },
                    onRegisterSellerClick = {
                        rootNavController.navigate(Routes.REGISTER)
                    }
                )
            }
            composable(BottomNavItem.Cart.route) {
                CartScreen(
                    onCheckout = { rootNavController.navigate(Routes.CHECKOUT) },
                    onContinueShopping = {
                        navController.navigate(BottomNavItem.Home.route)
                    },
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            // Merchant Routes
            composable(BottomNavItem.MerchantDashboard.route) {
                com.mes.feature.merchant.MerchantDashboardScreen(
                    onNotificationsClick = { rootNavController.navigate(Routes.NOTIFICATIONS) },
                    onProfileClick = { navController.navigate(BottomNavItem.Profile.route) },
                    onAddListingClick = { rootNavController.navigate(Routes.ADD_LISTING) },
                    onManageListingsClick = { navController.navigate(BottomNavItem.MerchantListings.route) },
                    onViewOrdersClick = { navController.navigate(BottomNavItem.MerchantOrders.route) },
                    onOrderClick = { orderId -> rootNavController.navigate("order/$orderId") }
                )
            }
            composable(BottomNavItem.MerchantOrders.route) {
                com.mes.feature.orders.OrdersScreen(
                    onOrderClick = { orderId -> rootNavController.navigate("order/$orderId") },
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(BottomNavItem.MerchantListings.route) {
                com.mes.feature.merchant.ManageListingsScreen(
                    onBackClick = { navController.popBackStack() },
                    onAddListingClick = { rootNavController.navigate(Routes.ADD_LISTING) },
                    onEditListingClick = { rootNavController.navigate(Routes.ADD_LISTING) }
                )
            }

            // Shared Route
            composable(BottomNavItem.Profile.route) {
                ProfileScreen(
                    currentRole = currentUserRole ?: UserRole.BUYER,
                    onBackClick = { /* No back from main profile tab */ },
                    onLogout = {
                        rootNavController.navigate(Routes.ONBOARDING) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onSwitchRole = {},
                    onNavigateToOrders = { rootNavController.navigate(Routes.ORDERS) },
                    onNavigateToAddresses = { rootNavController.navigate(Routes.ADDRESSES) },
                    onNavigateToSettings = { rootNavController.navigate(Routes.SETTINGS) }
                )
            }
        }
    }
}
