package com.mes.feature.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mes.core.designsystem.component.MerchantTrustCard
import com.mes.core.designsystem.theme.MesColor
import com.mes.core.domain.Product
import com.mes.core.domain.ProductImage
import kotlinx.datetime.*
import kotlinx.serialization.json.JsonPrimitive

import androidx.compose.material3.CircularProgressIndicator
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.mes.feature.catalog.presentation.ProductDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    onBackClick: () -> Unit,
    onAddToCart: (Int, String, String) -> Unit,
    onNavigateToCart: () -> Unit,
    onMerchantClick: (String) -> Unit,
    viewModel: ProductDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(productId) {
        viewModel.loadProduct(productId)
    }

    LaunchedEffect(uiState.addToCartSuccess) {
        if (uiState.addToCartSuccess) {
            viewModel.resetAddToCart()
            onNavigateToCart()
        }
    }

    val product = uiState.product

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MesColor.PrimaryTeal)
        }
        return
    }

    if (product == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = uiState.error ?: "Product not found")
        }
        return
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.addToCartSuccess) {
        if (uiState.addToCartSuccess) {
            viewModel.resetAddToCart()
        }
    }
    
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError() // I need to add this to ViewModel too
        }
    }

    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    var startDate by remember { mutableStateOf(today) }
    var endDate by remember { mutableStateOf(today.plus(7, DateTimeUnit.DAY)) }
    val numberOfDays = remember(startDate, endDate) {
        maxOf(1, (endDate.toEpochDays() - startDate.toEpochDays()))
    }
    val totalCost = remember(numberOfDays, product) {
        product.dailyRateTzs * numberOfDays
    }

    var quantity by remember { mutableIntStateOf(1) }
    val pagerState = rememberPagerState(pageCount = { product.images.size })

    var showAuthPrompt by remember { mutableStateOf(false) }

    if (showAuthPrompt) {
        AlertDialog(
            onDismissRequest = { showAuthPrompt = false },
            title = { Text("Sign in Required") },
            text = { Text("Please sign in or create an account to add items to your cart and proceed with rentals.") },
            confirmButton = {
                Button(
                    onClick = {
                        showAuthPrompt = false
                        // In a real app, navigate to Login
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MesColor.PrimaryTeal)
                ) {
                    Text("Sign In")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAuthPrompt = false }) {
                    Text("Cancel")
                }
            }
        )
    }

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
                        onClick = { onAddToCart(quantity, startDate.toString(), endDate.toString()) },
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
                        model = product.images[page].url,
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
                    repeat(product.images.size) { index ->
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
                        modifier = Modifier.fillMaxWidth().clickable { 
                            onMerchantClick(product.merchant ?: "") 
                        }
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
                            val displayValue = if (value is JsonPrimitive) value.content else value.toString()
                            Text(
                                text = displayValue,
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
