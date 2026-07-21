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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerDetailScreen(
    sellerId: String,
    onBackClick: () -> Unit,
    onProductClick: (String) -> Unit
) {
    // Demo data for the seller
    val seller = remember(sellerId) {
        SellerFullData(
            id = sellerId,
            name = "MedTech Supplies Ltd",
            location = "Bagamoyo Road, Dar es Salaam",
            rating = 4.8f,
            reviewCount = 124,
            bio = "Premier provider of high-quality medical imaging and diagnostic equipment in Tanzania. We specialize in MRI, CT Scans, and ICU ventilators.",
            phone = "+255 712 345 678",
            email = "info@medtech.co.tz",
            imageUrl = "https://images.unsplash.com/photo-1576091160550-2173dad99901?auto=format&fit=crop&q=80&w=400"
        )
    }

    val products = remember(sellerId) {
        listOf(
            Product(
                id = "p1",
                name = "Portable Ventilator Pro",
                category = ProductCategory.LIFE_SUPPORT,
                description = "High quality ventilator",
                merchantName = seller.name,
                dailyRateTzs = 50000,
                images = listOf(ProductImage("i1", "https://via.placeholder.com/300?text=Ventilator")),
                isActive = true
            ),
            Product(
                id = "p2",
                name = "Diagnostic Ultrasound",
                category = ProductCategory.DIAGNOSTIC,
                description = "High quality ultrasound",
                merchantName = seller.name,
                dailyRateTzs = 75000,
                images = listOf(ProductImage("i2", "https://via.placeholder.com/300?text=Ultrasound")),
                isActive = true
            ),
            Product(
                id = "p3",
                name = "Patient Monitor G30",
                category = ProductCategory.MONITORING,
                description = "High quality monitor",
                merchantName = seller.name,
                dailyRateTzs = 30000,
                images = listOf(ProductImage("i3", "https://via.placeholder.com/300?text=Monitor")),
                isActive = true
            ),
            Product(
                id = "p4",
                name = "Digital X-Ray Unit",
                category = ProductCategory.DIAGNOSTIC,
                description = "High quality x-ray",
                merchantName = seller.name,
                dailyRateTzs = 120000,
                images = listOf(ProductImage("i4", "https://via.placeholder.com/300?text=X-Ray")),
                isActive = false
            )
        )
    }

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
                SellerHeader(seller = seller)
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
private fun SellerHeader(seller: SellerFullData) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = seller.imageUrl,
                contentDescription = seller.name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = seller.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = MesColor.AccentAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${seller.rating} (${seller.reviewCount} reviews)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MesColor.Ink600
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = seller.bio,
            style = MaterialTheme.typography.bodyMedium,
            color = MesColor.Ink600
        )

        Spacer(modifier = Modifier.height(16.dp))

        InfoRow(icon = Icons.Filled.LocationOn, text = seller.location)
        InfoRow(icon = Icons.Filled.Phone, text = seller.phone)
        InfoRow(icon = Icons.Filled.Email, text = seller.email)

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
