package com.mes.feature.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AssignmentReturn
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mes.core.designsystem.component.StatusChip
import com.mes.core.designsystem.theme.MesColor
import com.mes.core.domain.OrderStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: String,
    onBackClick: () -> Unit,
    viewModel: OrdersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(orderId) {
        viewModel.selectOrder(orderId)
    }

    val order = uiState.selectedOrder

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (order == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Order not found")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                // Order header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = order.id,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = order.merchantName ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MesColor.Ink600
                            )
                        }
                        StatusChip(
                            status = order.status.name.lowercase().capitalize(),
                            statusColor = when (order.status) {
                                OrderStatus.CONFIRMED -> MesColor.PrimaryTeal
                                OrderStatus.DISPATCHED -> MesColor.Warning
                                OrderStatus.DELIVERED -> MesColor.Success
                                OrderStatus.RETURNED -> MesColor.Ink400
                                else -> MesColor.Ink400
                            }
                        )
                    }
                }

                // Return countdown
                if (order.status == OrderStatus.DELIVERED) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MesColor.WarningLight
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Return Due In",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MesColor.Warning
                                )
                                Text(
                                    text = "4 days",
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = MesColor.Warning,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Please ensure equipment is ready for return",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MesColor.Ink600
                                )
                            }
                        }
                    }
                }

                // Fulfillment timeline
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Fulfillment Status",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            val timelineSteps = listOf(
                                Triple("Confirmed", Icons.Filled.CheckCircle, true),
                                Triple("Dispatched", Icons.Filled.LocalShipping, 
                                    order.status != OrderStatus.CONFIRMED),
                                Triple("Delivered", Icons.Filled.LocationOn,
                                    order.status == OrderStatus.DELIVERED ||
                                    order.status == OrderStatus.RETURNED),
                                Triple("Returned", Icons.Filled.AssignmentReturn,
                                    order.status == OrderStatus.RETURNED)
                            )

                            timelineSteps.forEachIndexed { index, (label, icon, isCompleted) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isCompleted) MesColor.Success
                                                else MesColor.Ink100
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = label,
                                            tint = if (isCompleted) MesColor.Surface0 else MesColor.Ink400,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isCompleted) MesColor.Ink900 else MesColor.Ink400,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                if (index < timelineSteps.size - 1) {
                                    Row(
                                        modifier = Modifier
                                            .padding(start = 15.dp)
                                            .height(24.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(2.dp)
                                                .height(24.dp)
                                                .background(
                                                    if (timelineSteps[index + 1].third) MesColor.Success
                                                    else MesColor.Ink100
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Order items
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Order Items",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            order.lines.forEach { line ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = line.productName,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "${line.rentalStart} - ${line.rentalEnd} × ${line.quantity}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MesColor.Ink400
                                        )
                                    }
                                    Text(
                                        text = "TZS ${"%,d".format(line.lineTotalTzs)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Total",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "TZS ${"%,d".format(order.subtotalTzs)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MesColor.PrimaryTeal,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}
