package com.mes.feature.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mes.core.designsystem.theme.MesColor
import com.mes.core.domain.UserRole
import kotlinx.coroutines.launch

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

@Composable
fun OnboardingScreen(
    onFinished: (UserRole) -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(pageCount = { 6 })
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = true,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> SplashPage()
                1 -> LanguageSelectPage(
                    currentLanguage = uiState.selectedLanguage,
                    onSelect = viewModel::setLanguage
                )
                2 -> ValuePropPage(
                    imageUrl = "https://images.unsplash.com/photo-1516549655169-df83a0774514?auto=format&fit=crop&q=80&w=1000",
                    icon = Icons.Filled.MedicalServices,
                    title = if (uiState.selectedLanguage == "sw") "Ganga vifaa kwa dakika, si siku" else "Discover equipment in minutes, not days",
                    subtitle = if (uiState.selectedLanguage == "sw")
                        "Vinjari orodha yetu ya vifaa vya matibabu vilivyoidhinishwa na upate bei bora"
                    else
                        "Browse our curated catalogue of certified medical equipment and get the best rates",
                    gradientColors = listOf(MesColor.PrimaryTeal.copy(alpha = 0.8f), MesColor.PrimaryTealDark.copy(alpha = 0.9f))
                )
                3 -> ValuePropPage(
                    imageUrl = "https://images.unsplash.com/photo-1563013544-824ae1b704d3?auto=format&fit=crop&q=80&w=1000",
                    icon = Icons.Filled.Payment,
                    title = if (uiState.selectedLanguage == "sw") "Lipa kama unavyolipa sasa" else "Pay the way you already do",
                    subtitle = if (uiState.selectedLanguage == "sw")
                        "M-Pesa, Tigo Pesa, Airtel Money, Mixx by Yas — malipo ya simu yako"
                    else
                        "M-Pesa, Tigo Pesa, Airtel Money, Mixx by Yas — right from your phone",
                    gradientColors = listOf(MesColor.AccentAmber.copy(alpha = 0.8f), MesColor.AccentAmberDark.copy(alpha = 0.9f))
                )
                4 -> ValuePropPage(
                    imageUrl = "https://images.unsplash.com/photo-1450101499163-c8848c66ca85?auto=format&fit=crop&q=80&w=1000",
                    icon = Icons.Filled.Description,
                    title = if (uiState.selectedLanguage == "sw") "Kila upangaji, mkataba halisi" else "Every rental, a real contract",
                    subtitle = if (uiState.selectedLanguage == "sw")
                        "Mikataba ya moja kwa moja inayotolewa kwa kila upangaji"
                    else
                        "Auto-generated rental agreements for every order — formalized, signed, stored",
                    gradientColors = listOf(MesColor.Success.copy(alpha = 0.8f), MesColor.PrimaryTeal.copy(alpha = 0.9f))
                )
                5 -> RoleSelectPage(
                    language = uiState.selectedLanguage,
                    onRoleChosen = { role ->
                        viewModel.markOnboardingComplete(role)
                        onFinished(role)
                    }
                )
            }
        }

        // Progress dots for value prop pages (2-4)
        AnimatedVisibility(
            visible = pagerState.currentPage in 2..4,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp)
        ) {
            OnboardingProgressDots(
                pageCount = 3,
                currentPage = pagerState.currentPage - 2
            )
        }

        // Skip button for value prop pages
        AnimatedVisibility(
            visible = pagerState.currentPage in 2..4,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            TextButton(onClick = {
                scope.launch {
                    pagerState.animateScrollToPage(5)
                }
            }) {
                Text(
                    text = if (uiState.selectedLanguage == "sw") "Ruka" else "Skip",
                    color = MesColor.Ink600,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun SplashPage() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = "https://images.unsplash.com/photo-1581093588401-fbb62a02f120?auto=format&fit=crop&q=80&w=1000",
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(MesColor.PrimaryTeal.copy(alpha = 0.7f), MesColor.PrimaryTealDark.copy(alpha = 0.9f))
                    )
                )
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.MedicalServices,
                contentDescription = "MES Logo",
                tint = MesColor.Surface0,
                modifier = Modifier.size(100.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "MES",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MesColor.Surface0,
                    letterSpacing = 8.sp
                )
            )
            Text(
                text = "Medical Equipment Supply",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MesColor.Surface0.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
private fun LanguageSelectPage(
    currentLanguage: String,
    onSelect: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (currentLanguage == "sw") "Chagua Lugha" else "Choose Language",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (currentLanguage == "sw")
                "Tafadhali chagua lugha unayopendelea"
            else
                "Select your preferred language",
            style = MaterialTheme.typography.bodyLarge,
            color = MesColor.Ink600
        )

        Spacer(modifier = Modifier.height(48.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            LanguageCard(
                language = "English",
                preview = "Discover equipment...",
                isSelected = currentLanguage == "en",
                onClick = { onSelect("en") },
                modifier = Modifier.weight(1f)
            )
            LanguageCard(
                language = "Kiswahili",
                preview = "Ganga vifaa...",
                isSelected = currentLanguage == "sw",
                onClick = { onSelect("sw") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun LanguageCard(
    language: String,
    preview: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(120.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MesColor.PrimaryTeal else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = language,
                style = MaterialTheme.typography.titleLarge,
                color = if (isSelected) MesColor.Surface0 else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = preview,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) MesColor.Surface0.copy(alpha = 0.8f) else MesColor.Ink400,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ValuePropPage(
    imageUrl: String,
    icon: ImageVector,
    title: String,
    subtitle: String,
    gradientColors: List<Color>
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Brush.verticalGradient(gradientColors)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MesColor.Surface0,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    lineHeight = 32.sp,
                    color = Color.White
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(120.dp)) // Leave space for progress dots
        }
    }
}

@Composable
private fun RoleSelectPage(
    language: String,
    onRoleChosen: (UserRole) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (language == "sw") "Chagua Jukumu Lako" else "Choose Your Role",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (language == "sw")
                "Jinsi unavyotumia MES inategemea jukumu lako"
            else
                "How you use MES depends on your role",
            style = MaterialTheme.typography.bodyLarge,
            color = MesColor.Ink600,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        RoleCard(
            icon = Icons.Filled.LocalHospital,
            title = if (language == "sw") "Ninahitaji vifaa" else "I need equipment",
            subtitle = if (language == "sw")
                "Vinjari, weka kwenye kikapu, na upange vifaa vya matibabu"
            else
                "Browse, cart, and rent medical equipment for your facility",
            onClick = { onRoleChosen(UserRole.BUYER) },
            containerColor = MesColor.PrimaryTealContainer,
            contentColor = MesColor.PrimaryTeal,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        RoleCard(
            icon = Icons.Filled.Business,
            title = if (language == "sw") "Ninauza vifaa" else "I supply equipment",
            subtitle = if (language == "sw")
                "Orodhesha vifaa vyako na upokee maagizo"
            else
                "List your equipment and receive rental orders",
            onClick = { onRoleChosen(UserRole.MERCHANT) },
            containerColor = MesColor.AccentAmberContainer,
            contentColor = MesColor.AccentAmber,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun RoleCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(contentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MesColor.Ink600
                )
            }
        }
    }
}

@Composable
private fun OnboardingProgressDots(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(pageCount) { index ->
            Box(
                modifier = Modifier
                    .size(if (index == currentPage) 12.dp else 8.dp)
                    .clip(CircleShape)
                    .background(
                        if (index == currentPage) MesColor.PrimaryTeal
                        else MesColor.Ink200
                    )
            )
        }
    }
}
