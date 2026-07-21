package com.mes.feature.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mes.core.designsystem.theme.MesColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressesScreen(
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Addresses") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Add address */ },
                containerColor = MesColor.PrimaryTeal
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Address", tint = MesColor.Surface0)
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
            items(demoAddresses) { address ->
                AddressCard(address)
            }
        }
    }
}

@Composable
private fun AddressCard(address: AddressData) {
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
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                tint = MesColor.PrimaryTeal
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = address.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (address.isDefault) {
                        Spacer(modifier = Modifier.width(8.dp))
                        SuggestionChip(
                            onClick = { },
                            label = { Text("Default", style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                Text(
                    text = address.address,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MesColor.Ink600
                )
            }
            
            IconButton(onClick = { /* Edit */ }) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = MesColor.Ink400)
            }
            
            IconButton(onClick = { /* Delete */ }) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MesColor.Danger)
            }
        }
    }
}

private data class AddressData(
    val label: String,
    val address: String,
    val isDefault: Boolean = false
)

private val demoAddresses = listOf(
    AddressData("Main Receiving", "Bagamoyo Road, Ilala, Dar es Salaam", true),
    AddressData("Pharmacy Stores", "Veta Street, Dar es Salaam"),
    AddressData("Home", "Kijitonyama, Dar es Salaam")
)
