package com.mes.feature.catalog.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mes.core.designsystem.component.AvailabilityChip
import com.mes.core.designsystem.theme.MesColor
import com.mes.core.domain.Product
import com.mes.core.domain.ProductCategory
import com.mes.core.domain.ProductImage

import androidx.compose.material3.CircularProgressIndicator
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.mes.feature.catalog.presentation.SellerDetailViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerDetailScreen(
    sellerId: String,
    onBackClick: () -> Unit,
    onProductClick: (String) -> Unit,
    viewModel: SellerDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(sellerId) {
        viewModel.loadSeller(sellerId)
    }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MesColor.PrimaryTeal)
        }
        return
    }

    val merchant = uiState.merchant
    if (merchant == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = uiState.error ?: "Seller not found")
        }
        return
    }

    val products = uiState.products

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seller Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Seller Header Info
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                SellerHeader(merchant = merchant)
            }

            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Equipment Listings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(products) { product ->
                SellerProductCard(product = product, onClick = { onProductClick(product.id) })
            }
        }
    }
}

@Composable
private fun SellerHeader(merchant: com.mes.core.domain.User) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MesColor.PrimaryTealContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (merchant.firstName.take(1) + merchant.lastName.take(1)).uppercase(),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MesColor.PrimaryTeal,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = merchant.businessName ?: "${merchant.firstName} ${merchant.lastName}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (merchant.isVerifiedMerchant) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = MesColor.AccentAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Verified Supplier",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MesColor.Success
                        )
                    } else {
                        Text(
                            text = "Pending Verification",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MesColor.Warning
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (merchant.productCount > 0) {
            Text(
                text = "${merchant.productCount} equipment listed",
                style = MaterialTheme.typography.bodyMedium,
                color = MesColor.PrimaryTeal,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Text(
            text = merchant.businessName ?: "Verified Medical Equipment Supplier",
            style = MaterialTheme.typography.bodyMedium,
            color = MesColor.Ink600
        )

        Spacer(modifier = Modifier.height(16.dp))

        InfoRow(icon = Icons.Filled.LocationOn, text = "Tanzania")
        InfoRow(icon = Icons.Filled.Phone, text = merchant.phone ?: "No phone")
        InfoRow(icon = Icons.Filled.Email, text = merchant.email)

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = MesColor.Ink100)
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MesColor.PrimaryTeal,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MesColor.Ink600
        )
    }
}

@Composable
private fun SellerProductCard(product: Product, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            AsyncImage(
                model = product.images.firstOrNull()?.url,
                contentDescription = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "TZS ${product.dailyRateTzs}/day",
                    style = MaterialTheme.typography.bodySmall,
                    color = MesColor.PrimaryTeal,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                AvailabilityChip(isAvailable = product.isActive, availableFrom = null)
            }
        }
    }
}

private data class SellerFullData(
    val id: String,
    val name: String,
    val location: String,
    val rating: Float,
    val reviewCount: Int,
    val bio: String,
    val phone: String,
    val email: String,
    val imageUrl: String
)
