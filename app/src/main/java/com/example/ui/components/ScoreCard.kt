package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun ScoreCard(
    scorePercentage: Int,
    totalEvaluated: Int,
    compliantCount: Int,
    nonCompliantCount: Int,
    branchName: String = "تقييم الجودة الميداني",
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (scorePercentage / 100f).coerceIn(0f, 1f),
        label = "scoreProgress"
    )

    val (scoreTextColor, scoreBgColor, scoreBorderColor) = when {
        scorePercentage >= 85 -> Triple(ColorOkText, ColorOkBg, ColorOkBorder)
        scorePercentage >= 65 -> Triple(ColorWarningText, ColorWarningBg, ColorWarningBorder)
        else -> Triple(ColorBadText, ColorBadBg, ColorBadBorder)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Top row: Section title / Branch & Score Badge Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "النتيجة والتقييم",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = branchName,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = RaneenNavy
                    )
                }

                // Sleek Score Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(scoreBgColor)
                        .border(1.dp, scoreBorderColor, RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "$scorePercentage%",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = scoreTextColor
                        )
                        Text(
                            text = when {
                                totalEvaluated == 0 -> "بدون تقييم"
                                scorePercentage >= 90 -> "ممتاز"
                                scorePercentage >= 75 -> "مطابقة"
                                scorePercentage >= 60 -> "مقبول"
                                else -> "مخالفة"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = scoreTextColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sleek Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFF1F5F9))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress)
                        .clip(RoundedCornerShape(4.dp))
                        .background(scoreTextColor)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom 3-Column Stats Row with sleek border top
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatItem(
                    title = "مطابق",
                    value = compliantCount.toString(),
                    color = ColorOkText
                )

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(28.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                )

                StatItem(
                    title = "مخالف",
                    value = nonCompliantCount.toString(),
                    color = ColorBadText
                )

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(28.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                )

                StatItem(
                    title = "الإجمالي",
                    value = totalEvaluated.toString(),
                    color = RaneenNavy
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

