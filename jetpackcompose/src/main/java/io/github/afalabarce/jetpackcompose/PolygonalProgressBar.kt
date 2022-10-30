package io.github.afalabarce.jetpackcompose

import android.graphics.PointF
import android.util.Log
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import kotlin.math.*

fun Float.round(decimals: Int): Float {
    var multiplier = 1.0f
    repeat(decimals) { multiplier *= 10 }
    return round(this * multiplier) / multiplier
}

private fun pointByDistanceInsideTwoPoints(pointA: PointF, pointB: PointF, newPointDistance: Float): PointF{
    val preAngle = (pointB.y - pointA.y).absoluteValue / (pointB.x - pointA.x).absoluteValue
    val angle =  (atan(preAngle) * 180 / PI).absoluteValue
    val angleCA = (180 - 90 - angle) / 180 * PI
    val co = newPointDistance * sin(angleCA)
    val ca = newPointDistance * cos(angleCA)
    val multiplierY = if (pointB.y > pointA.y) 1 else -1
    val multiplierX = if (pointB.x > pointA.x) 1 else -1

    Log.i("Polygonal Angle", angle.toString())

    return if (pointB.y == pointA.y)
        PointF(pointA.x + newPointDistance * multiplierX, pointA.y)
    else if (pointB.x == pointA.x)
        PointF(pointA.x, pointA.y + multiplierY * newPointDistance)
    else
        PointF(pointA.x + co.toFloat() * multiplierX, pointA.y + ca.toFloat() * multiplierY)
}

private fun polygonPerimeter(vertexNumber: Int, containerCircleDiameter: Float) = polygonEdgeLength(vertexNumber, containerCircleDiameter) * vertexNumber

private fun polygonEdgeLength(vertexNumber: Int, containerCircleDiameter: Float): Float {
    val radius = containerCircleDiameter / 2f
    val point1 = PointF(
        radius * cos(2 * Math.PI * 1 / vertexNumber).toFloat(),
        radius * sin(2 * Math.PI * 1 / vertexNumber).toFloat()
    )
    val point2 = PointF(
        radius * cos(2 * Math.PI * 2 / vertexNumber).toFloat(),
        radius * sin(2 * Math.PI * 2 / vertexNumber).toFloat()
    )

    return sqrt((point2.x - point1.x).pow(2) + (point2.y - point1.y).pow(2))
}

@Composable
fun PolygonalProgressBar(
    modifier: Modifier = Modifier,
    rotationDegress: Float = 0f,
    size: Dp,
    backgroundColor: Color = Color.Gray,
    barColor: Color = Color.Green,
    stroke: Float = 8f,
    vertexNumber: Int = 2,
    progress: Float = 0f,
    isInfinite: Boolean = false,
    isPulsation: Boolean = false,
    infiniteDelayInMillis: Int = 800,
    pulsationTimeInMillis: Int = 2000,
){
    check(progress in 0f..1f){
        throw IllegalArgumentException("progress is a number between 0 and 1")
    }

    //region animation objects definition

    val infiniteTransition = rememberInfiniteTransition()

    val vertexIdx by infiniteTransition.animateValue(
        initialValue = 1,
        targetValue = vertexNumber + 1,
        typeConverter = Int.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(infiniteDelayInMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val infiniteRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            tween(infiniteDelayInMillis, easing =  LinearEasing)
        )
    )

    val pulsationColor by infiniteTransition.animateColor(
        initialValue = backgroundColor,
        targetValue = barColor,
        animationSpec = infiniteRepeatable(
            animation = tween(pulsationTimeInMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    //endregion

    Box(
        modifier = modifier.rotate(if (isInfinite && vertexNumber == 0) infiniteRotation else rotationDegress),
        contentAlignment = Alignment.Center
    ){
        //region Background Canvas

        Canvas(modifier = Modifier
            .size(size)
            .align(Alignment.Center)){

            val polygonPath = Path().apply {
                val scope: DrawScope = this@Canvas
                val center = scope.center
                check(vertexNumber == 0 || vertexNumber >= 2)
                when (vertexNumber){
                    0 -> {
                        if (isInfinite){
                            drawCircle(color = backgroundColor, radius = (scope.size.width - stroke) / 2f, center = center, style = Stroke(stroke))
                            drawArc(
                                color = barColor,
                                startAngle = 0f,
                                sweepAngle = 90f,
                                useCenter = false,
                                style = Stroke(stroke),
                                size = Size(size.toPx() - stroke / 2, size.toPx() - stroke / 2)
                            )
                        }else {
                            drawArc(color = backgroundColor, startAngle = 0f, sweepAngle = 360f, useCenter = false, style = Stroke(stroke), size = Size(size.toPx() - stroke / 2, size.toPx() - stroke / 2))
                            drawArc(
                                color = barColor,
                                startAngle = 0f,
                                sweepAngle = 360f * progress,
                                useCenter = false,
                                style = Stroke(stroke),
                                size = Size(size.toPx() - stroke / 2, size.toPx() - stroke / 2)
                            )
                        }
                    }

                    2 -> {
                        moveTo(0f, stroke)
                        lineTo(scope.size.width, stroke)
                    }
                    else -> {
                        val radius = (scope.size.width - stroke) / 2f
                        val vertex = IntRange(1, vertexNumber).map { nVertex ->
                            PointF(
                                radius * cos(2 * Math.PI * nVertex/vertexNumber).toFloat() + center.x,
                                radius * sin(2 * Math.PI * nVertex/vertexNumber).toFloat() + center.y
                            )
                        }

                        val firstVertex = vertex.first()
                        moveTo(firstVertex.x, firstVertex.y)
                        vertex.filterIndexed { x, _ -> x > 0 }.forEach { p -> lineTo(p.x, p.y)  }

                    }
                }

                close()
            }

            // Background Path
            val pathColor = if (isPulsation)
                pulsationColor
            else
                backgroundColor

            this.drawPath(
                path = polygonPath,
                color = pathColor,
                style = Stroke(stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }

        //endregion

        if (isInfinite && vertexNumber > 0){
            //region Infinite non circular progressbar
            Canvas(
                modifier = Modifier
                    .size(size)
                    .align(Alignment.Center)
            ){
                val polygonInfinitePath = Path().apply {
                    val scope: DrawScope = this@Canvas
                    val center = scope.center
                    check(vertexNumber >= 2)
                    when (vertexNumber){
                        2 -> {
                            moveTo(0f, stroke)
                            lineTo(scope.size.width, stroke)
                        }
                        else -> {
                            val radius = (scope.size.width - stroke) / 2f
                            val vertex = IntRange(vertexIdx - 2 , vertexIdx).map { nVertex ->
                                PointF(
                                    radius * cos(2 * Math.PI * nVertex/vertexNumber).toFloat() + center.x,
                                    radius * sin(2 * Math.PI * nVertex/vertexNumber).toFloat() + center.y
                                )
                            }

                            val firstVertex = vertex.first()
                            moveTo(firstVertex.x, firstVertex.y)
                            vertex.filterIndexed { x, _ -> x > 0 }.forEach { p ->
                                lineTo(p.x, p.y)
                            }
                        }
                    }
                }

                this.drawPath(
                    path = polygonInfinitePath,
                    color = barColor,
                    style = Stroke(stroke, cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
            }

            //endregion

        }else if (!isInfinite && !isPulsation && vertexNumber > 0){

            //Deterministic progressBar
            var perimeter = polygonPerimeter(
                vertexNumber = vertexNumber,
                containerCircleDiameter = size.value
            )
            val edgeLength = polygonEdgeLength(
                vertexNumber = vertexNumber,
                containerCircleDiameter = size.value
            )

            val progressValue = perimeter * progress
            val edgeProgressNumber = ceil(progressValue / edgeLength) + 1
            val lengthOfLastEdge = ((edgeProgressNumber - 1) * edgeLength) - progressValue
            Log.i("PolygonalProgressBar", "Perimeter / Percentaje / Edge Number / Edge Length / Length Last edge: $perimeter / $progressValue (${progress * 100}) / $edgeProgressNumber / $edgeLength / $lengthOfLastEdge")
            Canvas(
                modifier = Modifier
                    .size(size)
                    .align(Alignment.Center)
            ){
                val polygonDeterministicPath = Path().apply {
                    val scope: DrawScope = this@Canvas
                    val center = scope.center
                    check(vertexNumber >= 2)
                    when (vertexNumber){
                        2 -> {
                            moveTo(0f, stroke)
                            lineTo(scope.size.width, stroke)
                        }
                        else -> {
                            val radius = (scope.size.width - stroke) / 2f
                            val vertex = IntRange(1 , edgeProgressNumber.toInt()).map { nVertex ->
                                if (nVertex < edgeProgressNumber.toInt())
                                    PointF(
                                        radius * cos(2 * Math.PI * nVertex/vertexNumber).toFloat() + center.x,
                                        radius * sin(2 * Math.PI * nVertex/vertexNumber).toFloat() + center.y
                                    )
                                else {
                                    val pointA = PointF(
                                        radius * cos(2 * Math.PI * (nVertex - 1) / vertexNumber).toFloat() + center.x,
                                        radius * sin(2 * Math.PI * (nVertex - 1) / vertexNumber).toFloat() + center.y
                                    )
                                    val pointB = PointF(
                                        radius * cos(2 * Math.PI * nVertex / vertexNumber).toFloat() + center.x,
                                        radius * sin(2 * Math.PI * nVertex / vertexNumber).toFloat() + center.y
                                    )

                                    Log.i("PolygonalProgressBar PreLast", pointA.toString() )
                                    Log.i("PolygonalProgressBar Last", pointB.toString() )

                                    val invertedSize = (pointB.y > pointA.y && pointB.x > pointA.x) ||
                                            (pointB.y > pointA.y && pointB.x < pointA.x) ||
                                            (pointB.y < pointA.y && pointB.x < pointA.x)
                                    //pointByDistanceInsideTwoPoints( pointA, pointB, if (invertedSize) edgeLength - lengthOfLastEdge else lengthOfLastEdge)
                                    pointByDistanceInsideTwoPoints( pointA, pointB, edgeLength - lengthOfLastEdge)
                                }
                            }

                            val firstVertex = vertex.first()
                            moveTo(firstVertex.x, firstVertex.y)
                            vertex.filterIndexed { x, _ -> x > 0 }.forEachIndexed { index, p ->
                                lineTo(p.x, p.y)
                            }

                            if (progress == 1f)
                                close()
                        }
                    }
                }

                this.drawPath(
                    path = polygonDeterministicPath,
                    color = barColor,
                    style = Stroke(stroke, cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
            }
        }
    }
}