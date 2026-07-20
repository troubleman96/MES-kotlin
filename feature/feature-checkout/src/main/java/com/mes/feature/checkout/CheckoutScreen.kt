package com.mes.feature.checkout

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mes.core.designsystem.component.MerchantTrustCard
import com.mes.core.designsystem.component.StatusChip
import com.mes.core.designsystem.theme.MesColor
import com.mes.core.domain.CartLine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onOrderConfirmed: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: CheckoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.orderConfirmed) {
        OrderConfirmedView(onContinue = onOrderConfirmed)
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (uiState.currentStep) {
                            CheckoutStep.REVIEW -> "Review Order"
                            CheckoutStep.ADDRESS -> "Delivery Address"
                            CheckoutStep.PAYMENT -> "Payment"
                            CheckoutStep.CONFIRMED -> "Confirmed"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Step progress indicator
            LinearProgressIndicator(
                progress = { uiState.stepProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                color = MesColor.PrimaryTeal,
                trackColor = MesColor.Ink100
            )

            // Step indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("Review", "Address", "Payment").forEachIndexed { index, label ->
                    val isActive = CheckoutStep.entries.indexOf(uiState.currentStep) >= index
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isActive) MesColor.PrimaryTeal else MesColor.Ink300,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedContent(
                targetState = uiState.currentStep,
                transitionSpec = {
                    slideInHorizontally { it } + fadeIn() togetherWith
                    slideOutHorizontally { -it } + fadeOut()
                },
                modifier = Modifier.weight(1f)
            ) { step ->
                when (step) {
                    CheckoutStep.REVIEW -> ReviewStep(
                        cart = uiState.cart
                    )
                    CheckoutStep.ADDRESS -> AddressStep(
                        selectedAddressId = uiState.selectedAddressId,
                        onSelectAddress = viewModel::selectAddress,
                        onAddAddress = { /* open address form */ }
                    )
                    CheckoutStep.PAYMENT -> PaymentStep(
                        phone = uiState.paymentPhone,
                        network = uiState.paymentNetwork,
                        total = uiState.cart.grandTotalTzs,
                        isProcessing = uiState.isProcessingPayment,
                        onPay = viewModel::processPayment
                    )
                    CheckoutStep.CONFIRMED -> { /* handled by orderConfirmed */ }
                }
            }

            // Bottom navigation
            if (uiState.currentStep != CheckoutStep.CONFIRMED && !uiState.isProcessingPayment) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (uiState.currentStep != CheckoutStep.REVIEW) {
                            Button(
                                onClick = viewModel::previousStep,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Text("Back")
                            }
                        }

                        Button(
                            onClick = viewModel::nextStep,
                            modifier = Modifier.weight(if (uiState.currentStep == CheckoutStep.REVIEW) 1f else 2f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (uiState.currentStep == CheckoutStep.PAYMENT)
                                    MesColor.AccentAmber else MesColor.PrimaryTeal
                            )
                        ) {
                            Text(
                                text = if (uiState.currentStep == CheckoutStep.PAYMENT) "Place Order"
                                else "Continue"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewStep(cart: com.mes.core.domain.Cart) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "Order Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        cart.groupedByMerchant.forEach { (merchantId, lines) ->
            val merchantName = lines.first().merchantName
            val merchantSubtotal = lines.sumOf { it.lineTotalTzs }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MesColor.PrimaryTealContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        MerchantTrustCard(
                            merchantName = merchantName,
                            isVerified = true
                        )
                    }
                }
            }

            items(lines) { line ->
                CartLineItem(line = line)
            }
        }

        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Grand Total",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "TZS ${"%,d".format(cart.grandTotalTzs)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MesColor.PrimaryTeal,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AddressStep(
    selectedAddressId: String?,
    onSelectAddress: (String) -> Unit,
    onAddAddress: () -> Unit
) {
    val addresses = remember {
        listOf(
            com.mes.core.domain.Address(
                id = "addr-1",
                label = "Main Receiving",
                facilityName = "Muhimbili National Hospital",
                addressLine1 = "Bagamoyo Road",
                ward = "Ilala",
                district = "Ilala",
                city = "Dar es Salaam",
                contactName = "Dr. John Mwakasege",
                contactPhone = "+255 712 345 678",
                isDefault = true
            ),
            com.mes.core.domain.Address(
                id = "addr-2",
                label = "Pharmacy Stores",
                facilityName = "Muhimbili Pharmacy",
                addressLine1 = "Veta Street",
                city = "Dar es Salaam",
                contactName = "Sarah Kimaro",
                contactPhone = "+255 754 123 456"
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Select Delivery Address",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(addresses) { address ->
            AddressCard(
                address = address,
                isSelected = address.id == selectedAddressId,
                onSelect = { onSelectAddress(address.id) }
            )
        }
    }
}

@Composable
private fun AddressCard(
    address: com.mes.core.domain.Address,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MesColor.PrimaryTealContainer
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        ),
        onClick = onSelect
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = address.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (address.isDefault) {
                    StatusChip(status = "Default", statusColor = MesColor.PrimaryTeal)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = address.facilityName,
                style = MaterialTheme.typography.bodyMedium,
                color = MesColor.Ink600
            )

            Text(
                text = "${address.addressLine1}, ${address.city}",
                style = MaterialTheme.typography.bodySmall,
                color = MesColor.Ink400
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${address.contactName} • ${address.contactPhone}",
                style = MaterialTheme.typography.bodySmall,
                color = MesColor.Ink400
            )
        }
    }
}

@Composable
private fun CartLineItem(line: CartLine) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = line.productName,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${line.rentalPeriod.numberOfDays} days",
                style = MaterialTheme.typography.bodySmall,
                color = MesColor.Ink400
            )
        }
        Text(
            text = "TZS ${"%,d".format(line.lineTotalTzs)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PaymentStep(
    phone: String,
    network: String,
    total: Long,
    isProcessing: Boolean,
    onPay: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isProcessing) {
            CircularProgressIndicator(
                modifier = Modifier.size(64.dp),
                color = MesColor.PrimaryTeal
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Check your phone",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "A USSD prompt has been sent to your $network number",
                style = MaterialTheme.typography.bodyMedium,
                color = MesColor.Ink600,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = phone,
                style = MaterialTheme.typography.titleMedium,
                color = MesColor.PrimaryTeal,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Enter your PIN on your phone to authorize the payment",
                style = MaterialTheme.typography.bodySmall,
                color = MesColor.Ink400,
                textAlign = TextAlign.Center
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Phone,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MesColor.PrimaryTeal
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Mobile Money Payment",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = phone,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = network,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MesColor.Ink400
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Amount to pay",
                style = MaterialTheme.typography.bodyMedium,
                color = MesColor.Ink600
            )

            Text(
                text = "TZS ${"%,d".format(total)}",
                style = MaterialTheme.typography.displaySmall,
                color = MesColor.PrimaryTeal,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onPay,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MesColor.AccentAmber
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Pay TZS ${"%,d".format(total)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun OrderConfirmedView(onContinue: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MesColor.Success
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Order Confirmed!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your rental order has been placed successfully",
            style = MaterialTheme.typography.bodyLarge,
            color = MesColor.Ink600,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onContinue,
            colors = ButtonDefaults.buttonColors(
                containerColor = MesColor.PrimaryTeal
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        ) {
            Text("View My Orders")
        }
    }
}
