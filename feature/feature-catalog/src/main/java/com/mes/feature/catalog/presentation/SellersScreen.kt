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

import androidx.compose.foundation.background
import androidx.compose.material3.CircularProgressIndicator
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.mes.feature.catalog.presentation.SellersViewModel
import androidx.compose.runtime.getValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellersScreen(
    onSellerClick: (String) -> Unit,
    onNotificationsClick: () -> Unit,
    onRegisterSellerClick: () -> Unit,
    viewModel: SellersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MesColor.PrimaryTeal)
                }
            } else if (uiState.sellers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = uiState.error ?: "No sellers found")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.sellers) { seller ->
                        SellerCard(seller = seller, onClick = { onSellerClick(seller.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun SellerCard(seller: com.mes.core.domain.User, onClick: () -> Unit) {
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
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MesColor.PrimaryTealContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (seller.firstName.take(1) + seller.lastName.take(1)).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MesColor.PrimaryTeal,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = seller.businessName ?: "${seller.firstName} ${seller.lastName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = seller.facilityName ?: "Verified Supplier",
                    style = MaterialTheme.typography.bodySmall,
                    color = MesColor.Ink400
                )
                Spacer(modifier = Modifier.height(4.dp))
                MerchantTrustCard(merchantName = seller.businessName ?: seller.firstName, isVerified = seller.isVerifiedMerchant)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "★ 4.8",
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
