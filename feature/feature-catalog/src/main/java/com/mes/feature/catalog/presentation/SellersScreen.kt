package com.mes.feature.catalog.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mes.core.designsystem.component.MerchantTrustCard
import com.mes.core.designsystem.theme.MesColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellersScreen(
    onSellerClick: (String) -> Unit,
    onNotificationsClick: () -> Unit,
    onRegisterSellerClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Verified Sellers",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onRegisterSellerClick) {
                        Icon(
                            imageVector = Icons.Filled.Storefront,
                            contentDescription = "Register as Seller",
                            tint = MesColor.PrimaryTeal
                        )
                    }
                    IconButton(onClick = onNotificationsClick) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = "Notifications"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("Search sellers...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp)
            )

            val demoSellers = listOf(
                SellerData("1", "MedTech Supplies", "Dar es Salaam", 4.8f, "https://images.unsplash.com/photo-1576091160550-2173dad99901?auto=format&fit=crop&q=80&w=200"),
                SellerData("2", "Kariakoo Medical", "Dar es Salaam", 4.5f, "https://images.unsplash.com/photo-1585421514738-ee1b3bb6fb98?auto=format&fit=crop&q=80&w=200"),
                SellerData("3", "Arusha Diagnostics", "Arusha", 4.9f, "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?auto=format&fit=crop&q=80&w=200"),
                SellerData("4", "Lake Zone Health", "Mwanza", 4.2f, "https://images.unsplash.com/photo-1581594658553-35942489435b?auto=format&fit=crop&q=80&w=200")
            )

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(demoSellers) { seller ->
                    SellerCard(seller = seller, onClick = { onSellerClick(seller.id) })
                }
            }
        }
    }
}

@Composable
private fun SellerCard(seller: SellerData, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = seller.imageUrl,
                contentDescription = seller.name,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = seller.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = seller.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = MesColor.Ink400
                )
                Spacer(modifier = Modifier.height(4.dp))
                MerchantTrustCard(merchantName = seller.name, isVerified = true)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "★ ${seller.rating}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MesColor.AccentAmber,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private data class SellerData(
    val id: String,
    val name: String,
    val location: String,
    val rating: Float,
    val imageUrl: String
)
