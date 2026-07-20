package com.mes.core.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mes.core.designsystem.theme.MesColor

@Composable
fun MesBadge(
    text: String,
    backgroundColor: Color = MesColor.PrimaryTeal,
    contentColor: Color = Color.White,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = contentColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun AvailabilityChip(
    isAvailable: Boolean,
    availableFrom: String? = null,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isAvailable) MesColor.SuccessLight else MesColor.WarningLight
    val textColor = if (isAvailable) MesColor.Success else MesColor.Warning
    val text = if (isAvailable) "Available now" else "From $availableFrom"

    MesBadge(
        text = text,
        backgroundColor = bgColor,
        contentColor = textColor,
        modifier = modifier
    )
}

@Composable
fun StatusChip(
    status: String,
    statusColor: Color,
    modifier: Modifier = Modifier
) {
    val bgColor = statusColor.copy(alpha = 0.12f)

    MesBadge(
        text = status,
        backgroundColor = bgColor,
        contentColor = statusColor,
        modifier = modifier
    )
}

@Composable
fun AnimatedCountBadge(
    count: Int,
    modifier: Modifier = Modifier
) {
    val animatedScale = remember { Animatable(1f) }

    LaunchedEffect(count) {
        if (count > 0) {
            animatedScale.animateTo(
                targetValue = 1.3f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            animatedScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }

    if (count > 0) {
        Box(modifier = modifier) {
            MesBadge(
                text = if (count > 99) "99+" else count.toString(),
                backgroundColor = MesColor.Danger,
                contentColor = Color.White
            )
        }
    }
}
