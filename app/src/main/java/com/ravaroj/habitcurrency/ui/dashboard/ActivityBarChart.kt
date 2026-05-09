package com.ravaroj.habitcurrency.ui.dashboard

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ravaroj.habitcurrency.ui.theme.RewardsOrange
import com.ravaroj.habitcurrency.ui.theme.TasksBlue
import com.ravaroj.habitcurrency.util.DateUtils
import com.ravaroj.habitcurrency.util.calculateNiceScale
import kotlin.math.max

@Composable
fun ActivityBarChart(
    earned: List<Int>,
    spent: List<Int>,
    labels: List<String>,
    chartHeight: Dp = 180.dp,
    modifier: Modifier = Modifier
) {
    val topPadding = 16.dp    // As per Milestone 8.3 Step 1
    val bottomPadding = 28.dp // As per Milestone 8.3 Step 1
    val leftPadding = 32.dp   // Space for grid labels

    val maxDataValue = max((earned + spent).maxOrNull() ?: 0, 0)
    val scale = calculateNiceScale(maxDataValue)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
        ) {
            val width = size.width
            val height = size.height
            
            val chartTop = topPadding.toPx()
            val chartBottom = height - bottomPadding.toPx()
            val chartLeft = leftPadding.toPx()
            val chartRight = width
            val chartWidth = chartRight - chartLeft
            val chartHeightPx = chartBottom - chartTop

            // 1. Draw Grid Lines & Y-Axis Labels
            val gridPaint = Paint().apply {
                color = android.graphics.Color.WHITE
                alpha = (255 * 0.3f).toInt()
                textSize = 10.sp.toPx()
                textAlign = Paint.Align.RIGHT
            }

            scale.gridValues.forEach { value ->
                val y = chartBottom - (value.toFloat() / scale.maxValue) * chartHeightPx
                
                // Horizontal Grid Line
                drawLine(
                    color = Color.White.copy(alpha = 0.08f),
                    start = Offset(chartLeft, y),
                    end = Offset(chartRight, y),
                    strokeWidth = 1.dp.toPx()
                )
                
                // Y-Axis Label
                drawContext.canvas.nativeCanvas.drawText(
                    "$${value}",
                    chartLeft - 8.dp.toPx(),
                    y + 4.dp.toPx(),
                    gridPaint
                )
            }

            if (labels.isEmpty()) return@Canvas

            // 2. Draw Bars and Labels
            val barSpacing = chartWidth / labels.size
            val barWidth = 10.dp.toPx()
            val groupSpacing = 4.dp.toPx()

            val valueLabelPaint = Paint().apply {
                textSize = 9.sp.toPx()
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            labels.forEachIndexed { index, date ->
                val xCenter = chartLeft + (index * barSpacing) + (barSpacing / 2)
                
                val eValue = earned.getOrElse(index) { 0 }.coerceAtLeast(0)
                val sValue = spent.getOrElse(index) { 0 }.coerceAtLeast(0)

                // Earned Bar
                val normalizedEHeight = (eValue.toFloat() / scale.maxValue) * chartHeightPx
                val eHeight = if (eValue > 0) {
                    max(normalizedEHeight, 6.dp.toPx()) // Milestone 8.3 Step 3
                } else 0f
                
                val eBarLeft = xCenter - barWidth - (groupSpacing / 2)
                val eBarCenterX = eBarLeft + barWidth / 2
                if (eValue > 0) {
                    val barTop = chartBottom - eHeight
                    drawRoundRect(
                        color = TasksBlue,
                        topLeft = Offset(eBarLeft, barTop),
                        size = Size(barWidth, eHeight),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )

                    // Milestone 8.6: Smart Value Label
                    valueLabelPaint.color = TasksBlue.toArgb()
                    val labelY = (barTop - 4.dp.toPx()).coerceAtLeast(chartTop)
                    drawContext.canvas.nativeCanvas.drawText(
                        "$eValue",
                        eBarCenterX,
                        labelY,
                        valueLabelPaint
                    )
                }

                // Spent Bar
                val normalizedSHeight = (sValue.toFloat() / scale.maxValue) * chartHeightPx
                val sHeight = if (sValue > 0) {
                    max(normalizedSHeight, 6.dp.toPx()) // Milestone 8.3 Step 3
                } else 0f
                
                val sBarLeft = xCenter + (groupSpacing / 2)
                val sBarCenterX = sBarLeft + barWidth / 2
                if (sValue > 0) {
                    val barTop = chartBottom - sHeight
                    drawRoundRect(
                        color = RewardsOrange,
                        topLeft = Offset(sBarLeft, barTop),
                        size = Size(barWidth, sHeight),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )

                    // Milestone 8.6: Smart Value Label
                    valueLabelPaint.color = RewardsOrange.toArgb()
                    val labelY = (barTop - 4.dp.toPx()).coerceAtLeast(chartTop)
                    drawContext.canvas.nativeCanvas.drawText(
                        "$sValue",
                        sBarCenterX,
                        labelY,
                        valueLabelPaint
                    )
                }

                // Date Label
                val datePaint = Paint().apply {
                    color = android.graphics.Color.GRAY
                    textSize = 10.sp.toPx()
                    textAlign = Paint.Align.CENTER
                }
                drawContext.canvas.nativeCanvas.drawText(
                    DateUtils.displayShort(date),
                    xCenter,
                    height - 4.dp.toPx(),
                    datePaint
                )
            }
        }
    }
}
