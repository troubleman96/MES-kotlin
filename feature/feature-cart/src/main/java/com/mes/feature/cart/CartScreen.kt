package com.mes.feature.cart

import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.mes.core.designsystem.theme.MesColor
import com.mes.core.domain.Cart
import com.mes.core.domain.CartLine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onCheckout: () -> Unit,
    onContinueShopping: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: CartViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cart") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            if (uiState.cart.isNotEmpty) {
                CartSummaryBottomBar(
                    cart = uiState.cart,
                    onCheckout = onCheckout
                )
            }
        }
    ) { padding ->
        if (uiState.cart.isEmpty) {
            EmptyCartView(onContinueShopping = onContinueShopping)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                uiState.cart.groupedByMerchant.forEach { (merchantId, lines) ->
                    val merchantName = lines.first().merchantName
                    val merchantSubtotal = lines.sumOf { it.lineTotalTzs }

                    item {
                        MerchantSection(
                            merchantName = merchantName,
                            subtotal = merchantSubtotal,
                            lines = lines,
                            onRemoveLine = viewModel::removeLine,
                            onQuantityChange = viewModel::updateQuantity
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}

@Composable
private fun MerchantSection(
    merchantName: String,
    subtotal: Long,
    lines: List<CartLine>,
    onRemoveLine: (String) -> Unit,
    onQuantityChange: (String, Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MesColor.PrimaryTealContainer)
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = merchantName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MesColor.PrimaryTeal
            )
            Text(
                text = "Subtotal: TZS ${"%,d".format(subtotal)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MesColor.PrimaryTeal
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        lines.forEach { line ->
            CartLineItem(
                line = line,
                onRemove = { onRemoveLine(line.id) },
                onQuantityChange = { onQuantityChange(line.id, it) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun CartLineItem(
    line: CartLine,
    onRemove: () -> Unit,
    onQuantityChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = line.thumbnailUrl,
                contentDescription = line.productName,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = line.productName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${line.numberOfDays} days × TZS ${"%,d".format(line.dailyRateTzs)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MesColor.Ink400
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "TZS ${"%,d".format(line.lineTotalTzs)}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MesColor.PrimaryTeal,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Remove",
                        tint = MesColor.Danger,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onQuantityChange(line.quantity - 1) }) {
                        Icon(
                            imageVector = Icons.Filled.Remove,
                            contentDescription = "Decrease",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = line.quantity.toString(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    IconButton(onClick = { onQuantityChange(line.quantity + 1) }) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Increase",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CartSummaryBottomBar(
    cart: Cart,
    onCheckout: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Delivery arranged directly with merchant",
                style = MaterialTheme.typography.bodySmall,
                color = MesColor.Ink400
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Grand Total",
                        style = MaterialTheme.typography.bodySmall,
                        color = MesColor.Ink600
                    )
                    Text(
                        text = "TZS ${"%,d".format(cart.grandTotalTzs)}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MesColor.PrimaryTeal,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onCheckout,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MesColor.AccentAmber
                    ),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("Proceed to Checkout")
                }
            }
        }
    }
}

@Composable
private fun EmptyCartView(onContinueShopping: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MesColor.Ink200
            )

            Text(
                text = "Your cart is empty",
                style = MaterialTheme.typography.headlineSmall,
                color = MesColor.Ink600
            )

            Text(
                text = "Browse equipment to get started",
                style = MaterialTheme.typography.bodyMedium,
                color = MesColor.Ink400
            )

            Button(
                onClick = onContinueShopping,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MesColor.PrimaryTeal
                )
            ) {
                Text("Browse Equipment")
            }
        }
    }
}
