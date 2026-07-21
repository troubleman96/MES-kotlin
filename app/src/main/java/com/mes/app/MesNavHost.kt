package com.mes.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mes.core.domain.UserRole
import com.mes.feature.auth.LoginScreen
import com.mes.feature.auth.RegisterScreen
import com.mes.feature.cart.CartScreen
import com.mes.feature.catalog.ProductDetailScreen
import com.mes.feature.catalog.presentation.SellerDetailScreen
import com.mes.feature.checkout.CheckoutScreen
import com.mes.feature.merchant.AddListingScreen
import com.mes.feature.merchant.ManageListingsScreen
import com.mes.feature.merchant.MerchantDashboardScreen
import com.mes.feature.notifications.NotificationsScreen
import com.mes.feature.onboarding.OnboardingScreen
import com.mes.feature.orders.OrderDetailScreen
import com.mes.feature.orders.OrdersScreen
import com.mes.feature.profile.AddressesScreen
import com.mes.feature.profile.ProfileScreen
import com.mes.feature.profile.SettingsScreen
import com.mes.feature.catalog.presentation.SellerDetailScreen

object Routes {
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val MAIN = "main"
    const val CATALOG = "catalog"
    const val PRODUCT_DETAIL = "product/{productId}"
    const val CART = "cart"
    const val CHECKOUT = "checkout"
    const val ORDERS = "orders"
    const val ORDER_DETAIL = "order/{orderId}"
    const val NOTIFICATIONS = "notifications"
    const val PROFILE = "profile"
    const val MERCHANT_DASHBOARD = "merchant_dashboard"
    const val MANAGE_LISTINGS = "manage_listings"
    const val ADD_LISTING = "add_listing"
    const val ADDRESSES = "addresses"
    const val SETTINGS = "settings"
    const val SELLER_DETAIL = "seller/{sellerId}"
}

@Composable
fun MesNavHost() {
    val navController = rememberNavController()
    var hasSeenOnboarding by remember { mutableStateOf(false) }
    var currentUserRole by remember { mutableStateOf<UserRole?>(null) }
    var isLoggedIn by remember { mutableStateOf(false) } // This should ideally come from a ViewModel

    val startDestination = if (hasSeenOnboarding) {
        Routes.MAIN
    } else {
        Routes.ONBOARDING
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onFinished = { role ->
                    hasSeenOnboarding = true
                    currentUserRole = role
                    if (role == UserRole.MERCHANT) {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Routes.MAIN) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Routes.MAIN) {
            MainScreen(
                rootNavController = navController,
                currentUserRole = currentUserRole
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    isLoggedIn = true
                    if (navController.previousBackStackEntry?.destination?.route == Routes.ONBOARDING) {
                        navController.navigate(Routes.MAIN) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    } else {
                        navController.popBackStack()
                    }
                },
                onRegisterClick = { navController.navigate(Routes.REGISTER) }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                },
                onLoginClick = { navController.popBackStack() }
            )
        }

        composable(Routes.PRODUCT_DETAIL) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: return@composable
            ProductDetailScreen(
                productId = productId,
                onBackClick = { navController.popBackStack() },
                onAddToCart = {
                    navController.navigate(Routes.CART)
                },
                onMerchantClick = { sellerId: String ->
                    navController.navigate("seller/$sellerId")
                }
            )
        }

        composable(Routes.CART) {
            CartScreen(
                onCheckout = { 
                    if (isLoggedIn) {
                        navController.navigate(Routes.CHECKOUT)
                    } else {
                        navController.navigate(Routes.LOGIN)
                    }
                },
                onContinueShopping = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.CHECKOUT) {
            CheckoutScreen(
                onOrderConfirmed = {
                    navController.navigate(Routes.ORDERS) {
                        popUpTo(Routes.CATALOG)
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.ORDERS) {
            OrdersScreen(
                onOrderClick = { orderId ->
                    navController.navigate("order/$orderId")
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.ORDER_DETAIL) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: return@composable
            OrderDetailScreen(
                orderId = orderId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.NOTIFICATIONS) {
            NotificationsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                currentRole = currentUserRole ?: UserRole.BUYER,
                onBackClick = { navController.popBackStack() },
                onLogout = {
                    hasSeenOnboarding = false
                    currentUserRole = null
                    navController.navigate(Routes.ONBOARDING) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onSwitchRole = {
                    currentUserRole = if (currentUserRole == UserRole.BUYER) UserRole.MERCHANT else UserRole.BUYER
                    val nextRoute = if (currentUserRole == UserRole.BUYER) Routes.CATALOG else Routes.MERCHANT_DASHBOARD
                    navController.navigate(nextRoute) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToOrders = { navController.navigate(Routes.ORDERS) },
                onNavigateToAddresses = { navController.navigate(Routes.ADDRESSES) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(Routes.ADDRESSES) {
            AddressesScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.MERCHANT_DASHBOARD) {
            MerchantDashboardScreen(
                onNotificationsClick = { navController.navigate(Routes.NOTIFICATIONS) },
                onProfileClick = { navController.navigate(Routes.PROFILE) },
                onAddListingClick = { navController.navigate(Routes.ADD_LISTING) },
                onManageListingsClick = { navController.navigate(Routes.MANAGE_LISTINGS) },
                onViewOrdersClick = { navController.navigate(Routes.ORDERS) },
                onOrderClick = { orderId ->
                    navController.navigate("order/$orderId")
                }
            )
        }

        composable(Routes.MANAGE_LISTINGS) {
            ManageListingsScreen(
                onBackClick = { navController.popBackStack() },
                onAddListingClick = { navController.navigate(Routes.ADD_LISTING) },
                onEditListingClick = { listingId ->
                    // For now, reuse ADD_LISTING or create an EDIT route
                    navController.navigate(Routes.ADD_LISTING)
                }
            )
        }

        composable(Routes.ADD_LISTING) {
            AddListingScreen(
                onBackClick = { navController.popBackStack() },
                onListingAdded = { navController.popBackStack() }
            )
        }

        composable(Routes.SELLER_DETAIL) { backStackEntry ->
            val sellerId = backStackEntry.arguments?.getString("sellerId") ?: return@composable
            SellerDetailScreen(
                sellerId = sellerId,
                onBackClick = { navController.popBackStack() },
                onProductClick = { productId ->
                    navController.navigate("product/$productId")
                }
            )
        }
    }
}
