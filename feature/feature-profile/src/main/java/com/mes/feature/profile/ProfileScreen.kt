package com.mes.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mes.core.designsystem.theme.MesColor
import com.mes.core.domain.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    isLoggedIn: Boolean,
    currentRole: UserRole,
    onBackClick: () -> Unit,
    onLogout: () -> Unit,
    onLoginClick: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToAddresses: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    if (onBackClick != {}) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading && isLoggedIn) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MesColor.PrimaryTeal)
            }
        } else {
            val user = uiState.user
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Profile header
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isLoggedIn) MesColor.PrimaryTealContainer else MesColor.Ink100
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(if (isLoggedIn) MesColor.PrimaryTeal else MesColor.Ink300),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLoggedIn) {
                                    Text(
                                        text = user?.let { (it.firstName.take(1) + it.lastName.take(1)).uppercase() } ?: "??",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MesColor.Surface0,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Icon(Icons.Filled.Person, contentDescription = null, tint = MesColor.Surface0)
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isLoggedIn) (user?.let { "${it.firstName} ${it.lastName}" } ?: "Loading...") else "Guest User",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                if (isLoggedIn) {
                                    user?.facilityName?.let {
                                        Text(
                                            text = it,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MesColor.Ink600
                                        )
                                    }
                                    user?.businessName?.let {
                                        Text(
                                            text = it,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MesColor.Ink600
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (currentRole == UserRole.BUYER) "Buyer Account" else "Merchant Account",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MesColor.PrimaryTeal,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Text(
                                        text = "Login to manage your orders and profile",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MesColor.Ink600
                                    )
                                }
                            }
                        }
                    }
                }

                if (isLoggedIn) {
                    // Account settings
                    item {
                        Text(
                            text = "Account Settings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column {
                                ProfileMenuItem(
                                    icon = Icons.Filled.Phone,
                                    title = "Phone Number",
                                    subtitle = user?.phone ?: "Not verified",
                                    onClick = onNavigateToSettings
                                )
                                HorizontalDivider()
                                ProfileMenuItem(
                                    icon = Icons.Filled.Receipt,
                                    title = "Order History",
                                    subtitle = "View all past orders",
                                    onClick = onNavigateToOrders
                                )
                                if (currentRole == UserRole.BUYER) {
                                    HorizontalDivider()
                                    ProfileMenuItem(
                                        icon = Icons.Filled.LocationOn,
                                        title = "Delivery Addresses",
                                        subtitle = "Manage where your rentals go",
                                        onClick = onNavigateToAddresses
                                    )
                                }
                            }
                        }
                    }
                }

                // Logout / Login
                item {
                    val containerColor = if (isLoggedIn) MesColor.DangerLight else MesColor.PrimaryTealContainer
                    val contentColor = if (isLoggedIn) MesColor.Danger else MesColor.PrimaryTeal
                    val icon = if (isLoggedIn) Icons.AutoMirrored.Filled.Logout else Icons.Filled.Login
                    val label = if (isLoggedIn) "Logout" else "Sign In / Register"

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = {
                                if (isLoggedIn) {
                                    viewModel.logout()
                                    onLogout()
                                } else {
                                    onLoginClick()
                                }
                            }),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = containerColor
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = contentColor
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                color = contentColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MesColor.PrimaryTeal,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MesColor.Ink400
            )
        }

        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MesColor.Ink300
        )
    }
}
