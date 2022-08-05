package io.github.afalabarce.jetpackcompose

import android.graphics.Bitmap
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.scale
import java.io.ByteArrayOutputStream

private enum class DrawAction{
    Idle,
    Down,
    Up,
    Move
}

@Composable
fun DrawCanvas(
    modifier: Modifier,
    penColor: Color = Color.Black,
    penWidth: Dp = 2.dp,
    erase: Boolean = false,
    waterMark: Bitmap? = null,
    waterMarkOnFront: Boolean = false,
    onErase: () -> Unit = {},
    onDraw: (ByteArray) -> Unit = {}
){
    val path by remember { mutableStateOf(Path()) }

    if (erase){
        path.reset()
        onErase()
    }

    var motionEvent by remember { mutableStateOf(DrawAction.Idle) }
    var currentPosition by remember { mutableStateOf(Offset.Unspecified) }
    var previousPosition by remember { mutableStateOf(Offset.Unspecified) }

    val painter = Paint().apply {
        style = PaintingStyle.Stroke
        color = penColor
        strokeWidth = penWidth.value * LocalContext.current.resources.displayMetrics.density
        strokeCap = StrokeCap.Round
        strokeJoin = StrokeJoin.Round
    }

    val canvasModifier = modifier
        .pointerInput(Unit) {
            forEachGesture {
                awaitPointerEventScope {
                    // Wait for at least one pointer to press down, and set first contact position
                    awaitFirstDown().also {
                        motionEvent = DrawAction.Down
                        currentPosition = it.position
                        previousPosition = currentPosition
                    }

                    do {
                        // This PointerEvent contains details including events, id, position and more
                        val event: PointerEvent = awaitPointerEvent()
                        if (currentPosition == Offset.Unspecified) {
                            motionEvent = DrawAction.Down
                            currentPosition = event.changes.first().position
                            previousPosition = currentPosition
                        }
                        event.changes
                            .forEachIndexed { _: Int, pointerInputChange: PointerInputChange ->

                                // This necessary to prevent other gestures or scrolling
                                // when at least one pointer is down on canvas to draw
                                pointerInputChange.consumePositionChange()
                            }
                        motionEvent = DrawAction.Move
                        currentPosition = event.changes.first().position
                    } while (event.changes.any { it.pressed })

                    motionEvent = DrawAction.Up
                }
            }
        }

    Box(modifier = canvasModifier.drawBehind {
        when(motionEvent){
            DrawAction.Down -> {
                if (currentPosition.x != 0f && currentPosition.y != 0f)
                    path.moveTo(currentPosition.x, currentPosition.y)
                previousPosition = currentPosition
            }
            DrawAction.Move -> {
                if (currentPosition != Offset.Unspecified && currentPosition.x != 0f && currentPosition.y != 0f) {
                    //path.lineTo(currentPosition.x, currentPosition.y)
                    path.quadraticBezierTo(
                        previousPosition.x,
                        previousPosition.y,
                        (previousPosition.x + currentPosition.x) / 2,
                        (previousPosition.y + currentPosition.y) / 2
                    )
                }
                previousPosition = currentPosition
            }
            DrawAction.Up -> {
                path.lineTo(currentPosition.x, currentPosition.y)
                // Change state to idle to not draw in wrong position if recomposition happens
                currentPosition = Offset.Unspecified
                previousPosition = currentPosition
                motionEvent = DrawAction.Idle
            }
            else -> Unit
        }
        val drawingBitmap = ImageBitmap(size.width.toInt(), size.height.toInt(), ImageBitmapConfig.Argb8888).asAndroidBitmap()
        val drawingCanvas = Canvas(drawingBitmap.asImageBitmap())

        if (!erase){
            if (waterMark != null && !waterMarkOnFront){
                drawingCanvas.drawImage(waterMark.scale(size.width.toInt(), size.height.toInt()).asImageBitmap(), Offset(0f, 0f), painter )
            }

            drawingCanvas.drawPath(
                path,
                painter
                //style = Stroke(width = penWidth.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            if (waterMark != null && waterMarkOnFront){
                drawingCanvas.drawImage(waterMark.scale(size.width.toInt(), size.height.toInt()).asImageBitmap(), Offset(0f, 0f), painter )
            }

        }

        drawImage(drawingBitmap.asImageBitmap())
        val msBmp = ByteArrayOutputStream()
        drawingBitmap.compress(Bitmap.CompressFormat.PNG, 100, msBmp)
        onDraw(msBmp.toByteArray())
    })
}