package com.mes.feature.merchant

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mes.core.designsystem.theme.MesColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageListingsScreen(
    onBackClick: () -> Unit,
    onAddListingClick: () -> Unit,
    onEditListingClick: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Listings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddListingClick,
                containerColor = MesColor.AccentAmber
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Listing")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(demoListings) { listing ->
                ListingItem(
                    listing = listing,
                    onEditClick = { onEditListingClick(listing.id) }
                )
            }
        }
    }
}

@Composable
private fun ListingItem(
    listing: ListingData,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = listing.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "TZS ${listing.price}/day • ${listing.stock} in stock",
                    style = MaterialTheme.typography.bodySmall,
                    color = MesColor.Ink400
                )
            }
            
            IconButton(onClick = onEditClick) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = MesColor.PrimaryTeal)
            }
            
            IconButton(onClick = { /* menu */ }) {
                Icon(Icons.Filled.MoreVert, contentDescription = "Options")
            }
        }
    }
}

private data class ListingData(
    val id: String,
    val name: String,
    val price: String,
    val stock: Int
)

private val demoListings = listOf(
    ListingData("1", "Portable Ventilator Pro", "150,000", 5),
    ListingData("2", "Patient Monitor 12-Lead", "85,000", 8),
    ListingData("3", "Infusion Pump Set", "45,000", 12),
    ListingData("4", "Wheelchair Standard", "15,000", 20),
    ListingData("5", "Oxygen Concentrator", "120,000", 3)
)
