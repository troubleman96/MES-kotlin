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
import com.mes.feature.catalog.CatalogScreen
import com.mes.feature.catalog.ProductDetailScreen
import com.mes.feature.checkout.CheckoutScreen
import com.mes.feature.merchant.MerchantDashboardScreen
import com.mes.feature.notifications.NotificationsScreen
import com.mes.feature.onboarding.OnboardingScreen
import com.mes.feature.orders.OrderDetailScreen
import com.mes.feature.orders.OrdersScreen
import com.mes.feature.profile.ProfileScreen

object Routes {
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val CATALOG = "catalog"
    const val PRODUCT_DETAIL = "product/{productId}"
    const val CART = "cart"
    const val CHECKOUT = "checkout"
    const val ORDERS = "orders"
    const val ORDER_DETAIL = "order/{orderId}"
    const val NOTIFICATIONS = "notifications"
    const val PROFILE = "profile"
    const val MERCHANT_DASHBOARD = "merchant_dashboard"
}

@Composable
fun MesNavHost() {
    val navController = rememberNavController()
    var hasSeenOnboarding by remember { mutableStateOf(false) }
    var currentUserRole by remember { mutableStateOf<UserRole?>(null) }

    val startDestination = if (hasSeenOnboarding && currentUserRole != null) {
        when (currentUserRole) {
            UserRole.BUYER -> Routes.CATALOG
            UserRole.MERCHANT -> Routes.MERCHANT_DASHBOARD
            null -> Routes.ONBOARDING
        }
    } else {
        Routes.ONBOARDING
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onFinished = { role ->
                    hasSeenOnboarding = true
                    currentUserRole = role
                    when (role) {
                        UserRole.BUYER -> navController.navigate(Routes.CATALOG) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                        UserRole.MERCHANT -> navController.navigate(Routes.MERCHANT_DASHBOARD) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    when (currentUserRole) {
                        UserRole.BUYER -> navController.navigate(Routes.CATALOG) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                        UserRole.MERCHANT -> navController.navigate(Routes.MERCHANT_DASHBOARD) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                        else -> navController.navigate(Routes.CATALOG)
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

        composable(Routes.CATALOG) {
            CatalogScreen(
                onProductClick = { productId ->
                    navController.navigate("product/$productId")
                },
                onCartClick = { navController.navigate(Routes.CART) },
                onNotificationsClick = { navController.navigate(Routes.NOTIFICATIONS) },
                onProfileClick = { navController.navigate(Routes.PROFILE) }
            )
        }

        composable(Routes.PRODUCT_DETAIL) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: return@composable
            ProductDetailScreen(
                productId = productId,
                onBackClick = { navController.popBackStack() },
                onAddToCart = { navController.navigate(Routes.CART) },
                onMerchantClick = { /* open merchant profile sheet */ }
            )
        }

        composable(Routes.CART) {
            CartScreen(
                onCheckout = { navController.navigate(Routes.CHECKOUT) },
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
                onBackClick = { navController.popBackStack() },
                onLogout = {
                    hasSeenOnboarding = false
                    currentUserRole = null
                    navController.navigate(Routes.ONBOARDING) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.MERCHANT_DASHBOARD) {
            MerchantDashboardScreen(
                onNotificationsClick = { navController.navigate(Routes.NOTIFICATIONS) },
                onProfileClick = { navController.navigate(Routes.PROFILE) }
            )
        }
    }
}
