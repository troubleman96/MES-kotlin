package com.mes.feature.catalog.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mes.core.designsystem.component.MerchantTrustCard
import com.mes.core.domain.User
import com.mes.core.designsystem.theme.MesColor

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
                value = uiState.searchQuery,
                onValueChange = { viewModel.search(it) },
                placeholder = { Text("Search sellers...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.search("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MesColor.PrimaryTeal)
                    }
                }
                uiState.error != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = uiState.error ?: "Something went wrong",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MesColor.Ink400
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = { viewModel.refresh() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                uiState.sellers.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.Storefront,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MesColor.Ink200
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (uiState.searchQuery.isNotEmpty()) "No sellers found" else "No verified sellers yet",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MesColor.Ink400
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.sellers, key = { it.id }) { seller ->
                            SellerCard(seller = seller, onClick = { onSellerClick(seller.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SellerCard(seller: User, onClick: () -> Unit) {
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
                    text = "${seller.productCount} equipment listed",
                    style = MaterialTheme.typography.bodySmall,
                    color = MesColor.Ink400
                )
                Spacer(modifier = Modifier.height(4.dp))
                MerchantTrustCard(
                    merchantName = seller.businessName ?: seller.firstName,
                    isVerified = seller.isVerifiedMerchant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (seller.isVerifiedMerchant) "Verified" else "Pending",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (seller.isVerifiedMerchant) MesColor.Success else MesColor.Warning,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
