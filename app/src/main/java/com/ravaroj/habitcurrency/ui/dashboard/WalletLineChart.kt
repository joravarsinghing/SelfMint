package com.ravaroj.habitcurrency.ui.dashboard

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ravaroj.habitcurrency.util.DateUtils
import com.ravaroj.habitcurrency.util.calculateNiceScale
import kotlin.math.max

@Composable
fun WalletLineChart(
    values: List<Int>,
    labels: List<String>,
    chartHeight: Dp = 180.dp,
    modifier: Modifier = Modifier
) {
    val topPadding = 24.dp
    val bottomPadding = 28.dp // Space for date labels
    val leftPadding = 32.dp   // Space for grid labels

    val maxDataValue = values.maxOrNull() ?: 0
    val scale = calculateNiceScale(maxDataValue)

    Column(modifier = modifier.fillMaxWidth().padding(8.dp)) {
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

            if (values.isEmpty()) return@Canvas
            
            val points = if (scale.maxValue == 0) {
                val y = chartBottom
                val spacing = if (values.size > 1) chartWidth / (values.size - 1) else 0f
                values.mapIndexed { index, _ -> Offset(chartLeft + index * spacing, y) }
            } else if (values.size == 1) {
                val normalized = values[0].toFloat() / scale.maxValue
                val y = chartBottom - (normalized * chartHeightPx)
                listOf(Offset(chartLeft + chartWidth / 2, y))
            } else {
                val spacing = chartWidth / (values.size - 1)
                values.mapIndexed { index, value ->
                    val x = chartLeft + index * spacing
                    val normalized = value.toFloat() / scale.maxValue
                    val y = chartBottom - (normalized * chartHeightPx)
                    Offset(x, y)
                }
            }

            if (points.size > 1) {
                val strokePath = Path().apply {
                    moveTo(points[0].x, points[0].y)
                    for (i in 1 until points.size) {
                        lineTo(points[i].x, points[i].y)
                    }
                }

                val fillPath = Path().apply {
                    moveTo(points[0].x, chartBottom)
                    for (point in points) {
                        lineTo(point.x, point.y)
                    }
                    lineTo(points.last().x, chartBottom)
                    close()
                }

                // Draw area fill
                drawPath(
                    path = fillPath,
                    color = Color.White.copy(alpha = 0.15f)
                )

                // Draw line
                drawPath(
                    path = strokePath,
                    color = Color.White,
                    style = Stroke(width = 3.dp.toPx())
                )
            } else if (points.size == 1) {
                // Just draw a point
                drawCircle(color = Color.White, radius = 4.dp.toPx(), center = points[0])
            }

            val valueLabelPaint = Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 9.sp.toPx()
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            // Draw Dots and Date Labels
            points.forEachIndexed { index, point ->
                // Draw dot
                drawCircle(
                    color = Color.White,
                    radius = 4.dp.toPx(),
                    center = point
                )

                // Milestone 8.6: Intelligent Value Labels
                val value = values[index]
                val isFirst = index == 0
                val isLast = index == values.lastIndex
                val isChange = index > 0 && value != values[index - 1]

                if (isFirst || isLast || isChange) {
                    val labelY = (point.y - 8.dp.toPx()).coerceAtLeast(chartTop)
                    drawContext.canvas.nativeCanvas.drawText(
                        "$value",
                        point.x,
                        labelY,
                        valueLabelPaint
                    )
                }
                
                // Date Label
                val datePaint = Paint().apply {
                    color = android.graphics.Color.WHITE
                    alpha = (255 * 0.6f).toInt()
                    textSize = 10.sp.toPx()
                    textAlign = Paint.Align.CENTER
                }
                drawContext.canvas.nativeCanvas.drawText(
                    DateUtils.displayShort(labels[index]),
                    point.x,
                    height - 4.dp.toPx(),
                    datePaint
                )
            }
        }
    }
}
