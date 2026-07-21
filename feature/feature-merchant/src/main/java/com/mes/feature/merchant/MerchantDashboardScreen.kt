package com.mes.feature.merchant

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mes.core.designsystem.component.StatusChip
import com.mes.core.designsystem.theme.MesColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantDashboardScreen(
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAddListingClick: () -> Unit,
    onManageListingsClick: () -> Unit,
    onViewOrdersClick: () -> Unit,
    onOrderClick: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Business,
                            contentDescription = null,
                            tint = MesColor.PrimaryTeal,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Merchant Dashboard",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNotificationsClick) {
                        Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                    }
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Filled.Business, contentDescription = "Profile")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddListingClick,
                containerColor = MesColor.AccentAmber
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Listing")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stats cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Active Rentals",
                        value = "12",
                        icon = Icons.Filled.Inventory,
                        containerColor = MesColor.PrimaryTealContainer,
                        contentColor = MesColor.PrimaryTeal,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "This Month",
                        value = "TZS 2.4M",
                        icon = Icons.Filled.TrendingUp,
                        containerColor = MesColor.AccentAmberContainer,
                        contentColor = MesColor.AccentAmber,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Pending orders
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Incoming Orders",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "See All",
                        style = MaterialTheme.typography.labelLarge,
                        color = MesColor.PrimaryTeal,
                        modifier = Modifier.clickable { onViewOrdersClick() }
                    )
                }
            }

            items(demoOrders) { order ->
                OrderRequestCard(
                    orderNumber = order.first,
                    product = order.second,
                    customer = order.third,
                    status = order.fourth,
                    onClick = { onOrderClick(order.first) }
                )
            }

            // Quick actions
            item {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                QuickActionCard(
                    icon = Icons.Filled.Inventory,
                    title = "Manage Listings",
                    subtitle = "Add, edit, or remove equipment",
                    onClick = onManageListingsClick
                )
            }

            item {
                QuickActionCard(
                    icon = Icons.Filled.Receipt,
                    title = "View All Orders",
                    subtitle = "See incoming and past orders",
                    onClick = onViewOrdersClick
                )
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}


@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = contentColor,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MesColor.Ink600
            )
        }
    }
}

@Composable
private fun OrderRequestCard(
    orderNumber: String,
    product: String,
    customer: String,
    status: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MesColor.PrimaryTealContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Receipt,
                    contentDescription = null,
                    tint = MesColor.PrimaryTeal,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$customer • $orderNumber",
                    style = MaterialTheme.typography.bodySmall,
                    color = MesColor.Ink400
                )
            }

            StatusChip(
                status = status,
                statusColor = MesColor.Warning
            )

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = null,
                tint = MesColor.Ink400,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MesColor.PrimaryTealContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MesColor.PrimaryTeal
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MesColor.Ink400
                )
            }

            Icon(
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = null,
                tint = MesColor.Ink400
            )
        }
    }
}

private data class OrderData(
    val first: String,
    val second: String,
    val third: String,
    val fourth: String
)

private val demoOrders = listOf(
    OrderData("ORD-001", "Portable Ventilator Pro", "Muhimbili Hospital", "Pending"),
    OrderData("ORD-002", "Patient Monitor 12-Lead", "Kilimanjaro Medical", "Pending"),
    OrderData("ORD-003", "Infusion Pump Set", "Temeke Hospital", "Confirmed")
)
