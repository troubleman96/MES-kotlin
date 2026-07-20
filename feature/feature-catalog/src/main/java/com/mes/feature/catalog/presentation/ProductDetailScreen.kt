package com.mes.feature.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mes.core.designsystem.component.MerchantTrustCard
import com.mes.core.designsystem.theme.MesColor
import com.mes.core.domain.Product
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    onBackClick: () -> Unit,
    onAddToCart: () -> Unit,
    onMerchantClick: () -> Unit
) {
    // In a real app, we'd fetch from API using productId
    val product = remember {
        Product(
            id = productId,
            name = "Portable Ventilator Pro 3000",
            description = "Professional-grade portable ventilator for ICU and emergency use. Features advanced tidal volume control, multiple ventilation modes, and long battery life. Suitable for adult and pediatric patients.",
            category = com.mes.core.domain.ProductCategory.LIFE_SUPPORT,
            merchantId = "merchant-1",
            merchantName = "MedTech Supplies Ltd",
            merchantIsVerified = true,
            dailyRateTzs = 50000,
            weeklyRateTzs = 280000,
            monthlyRateTzs = 1000000,
            imageUrls = listOf(
                "https://via.placeholder.com/600x400/0E7C7B/FFFFFF?text=Ventilator+1",
                "https://via.placeholder.com/600x400/5FBFBE/FFFFFF?text=Ventilator+2",
                "https://via.placeholder.com/600x400/0E7C7B/FFFFFF?text=Ventilator+3"
            ),
            specs = mapOf(
                "Model" to "VentPro 3000",
                "Manufacturer" to "MedTech Corp",
                "Weight" to "5.2 kg",
                "Power" to "AC 100-240V, 50/60Hz",
                "Battery" to "Lithium-ion, 4 hours",
                "Ventilation Modes" to "VCV, PCV, SIMV, CPAP",
                "Tidal Volume" to "50-2000 mL",
                "Display" to "10.1\" color touchscreen"
            ),
            isFeatured = true,
            isAvailable = true
        )
    }

    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    var startDate by remember { mutableStateOf(today) }
    var endDate by remember { mutableStateOf(today.plus(7, DateTimeUnit.DAY)) }
    val numberOfDays = remember(startDate, endDate) {
        maxOf(1, (endDate.year * 365 + endDate.dayOfYear) - (startDate.year * 365 + startDate.dayOfYear))
    }
    val totalCost = remember(numberOfDays, product) {
        product.dailyRateTzs.toLong() * numberOfDays
    }

    var quantity by remember { mutableIntStateOf(1) }
    val pagerState = rememberPagerState(pageCount = { product.imageUrls.size })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(product.name) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Total",
                            style = MaterialTheme.typography.bodySmall,
                            color = MesColor.Ink600
                        )
                        Text(
                            text = "TZS ${"%,d".format(totalCost * quantity)}",
                            style = MaterialTheme.typography.titleLarge,
                            color = MesColor.PrimaryTeal,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = onAddToCart,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MesColor.AccentAmber
                        ),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add to Cart")
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Image carousel
            item {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(3f / 2f)
                ) { page ->
                    AsyncImage(
                        model = product.imageUrls[page],
                        contentDescription = "${product.name} - Image ${page + 1}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Page indicator dots
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(product.imageUrls.size) { index ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(if (index == pagerState.currentPage) 8.dp else 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index == pagerState.currentPage) MesColor.PrimaryTeal
                                    else MesColor.Ink200
                                )
                        )
                    }
                }
            }

            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Merchant trust card
                    MerchantTrustCard(
                        merchantName = product.merchantName,
                        isVerified = product.merchantIsVerified,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title and price
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "TZS ${"%,d".format(product.dailyRateTzs)}",
                            style = MaterialTheme.typography.titleLarge,
                            color = MesColor.PrimaryTeal,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = " / day",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MesColor.Ink400
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Description
                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MesColor.Ink600
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Rental period picker
                    Text(
                        text = "Rental Period",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DateCard(
                            label = "Start Date",
                            date = startDate.toString(),
                            modifier = Modifier.weight(1f),
                            onClick = { /* Open date picker */ }
                        )
                        DateCard(
                            label = "End Date",
                            date = endDate.toString(),
                            modifier = Modifier.weight(1f),
                            onClick = { /* Open date picker */ }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick duration buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(7, 14, 30).forEach { days ->
                            OutlinedButton(
                                onClick = {
                                    endDate = startDate.plus(days.toLong(), DateTimeUnit.DAY)
                                }
                            ) {
                                Text("${days}D")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quantity
                    Text(
                        text = "Quantity",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (quantity > 1) quantity-- }) {
                            Icon(Icons.Filled.Remove, contentDescription = "Decrease")
                        }
                        Text(
                            text = quantity.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        IconButton(onClick = { quantity++ }) {
                            Icon(Icons.Filled.Add, contentDescription = "Increase")
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Specs table
                    Text(
                        text = "Specifications",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    product.specs.forEach { (key, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = key,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MesColor.Ink600
                            )
                            Text(
                                text = value,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(100.dp)) // Space for bottom bar
                }
            }
        }
    }
}

@Composable
private fun DateCard(
    label: String,
    date: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MesColor.Ink400
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = date,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}


