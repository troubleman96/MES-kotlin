package com.mes.feature.orders

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mes.core.designsystem.component.StatusChip
import com.mes.core.designsystem.theme.MesColor
import com.mes.core.domain.Order
import com.mes.core.domain.OrderStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    onOrderClick: (String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: OrdersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Active", "Completed", "Cancelled")

    val filteredOrders = remember(uiState.orders, selectedTab) {
        when (selectedTab) {
            0 -> uiState.orders.filter {
                it.status in listOf(
                    OrderStatus.CONFIRMED, OrderStatus.DISPATCHED,
                    OrderStatus.IN_TRANSIT, OrderStatus.DELIVERED,
                    OrderStatus.IN_USE, OrderStatus.RETURN_DUE
                )
            }
            1 -> uiState.orders.filter {
                it.status in listOf(OrderStatus.RETURNED)
            }
            2 -> uiState.orders.filter {
                it.status == OrderStatus.CANCELLED
            }
            else -> emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Orders") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MesColor.PrimaryTeal,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MesColor.PrimaryTeal
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            if (filteredOrders.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.Receipt,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MesColor.Ink200
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No orders in this category",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MesColor.Ink400
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
                ) {
                    items(filteredOrders) { order ->
                        OrderCard(
                            order = order,
                            onClick = { onOrderClick(order.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderCard(
    order: Order,
    onClick: () -> Unit
) {
    val firstSubOrder = order.subOrders.firstOrNull()
    val statusColor = when (order.status) {
        OrderStatus.CONFIRMED -> MesColor.PrimaryTeal
        OrderStatus.DISPATCHED -> MesColor.Warning
        OrderStatus.DELIVERED -> MesColor.Success
        OrderStatus.RETURNED -> MesColor.Ink400
        OrderStatus.CANCELLED -> MesColor.Danger
        OrderStatus.RETURN_DUE -> MesColor.Warning
        else -> MesColor.Ink400
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.id,
                    style = MaterialTheme.typography.labelMedium,
                    color = MesColor.Ink400
                )
                StatusChip(
                    status = order.status.displayName,
                    statusColor = statusColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            firstSubOrder?.let { subOrder ->
                Text(
                    text = subOrder.merchantName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                subOrder.items.forEach { item ->
                    Text(
                        text = "${item.productName} × ${item.quantity}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MesColor.Ink600
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.bodySmall,
                    color = MesColor.Ink400
                )
                Text(
                    text = "TZS ${"%,d".format(firstSubOrder?.totalTzs ?: 0)}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MesColor.PrimaryTeal,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
