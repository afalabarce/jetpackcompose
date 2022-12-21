package io.github.afalabarce.jetpackcompose

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.verticalDrag
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@SuppressLint("ReturnFromAwaitPointerEventScope")
private fun Modifier.swipeToDismiss(
    onDismissed: () -> Unit
): Modifier = composed {
    val offsetY = remember { Animatable(0f) }
    pointerInput(Unit) {
        // Used to calculate fling decay.
        val decay = splineBasedDecay<Float>(this)
        // Use suspend functions for touch events and the Animatable.
        coroutineScope {
            while (true) {
                // Detect a touch down event.
                val pointerId = awaitPointerEventScope { awaitFirstDown().id }
                val velocityTracker = VelocityTracker()
                // Stop any ongoing animation.
                offsetY.stop()
                awaitPointerEventScope {
                    verticalDrag(pointerId) { change ->
                        // Update the animation value with touch events.
                        launch {
                            offsetY.snapTo(
                                offsetY.value + change.positionChange().y
                            )
                        }
                        velocityTracker.addPosition(
                            change.uptimeMillis,
                            change.position
                        )
                    }
                }
                // No longer receiving touch events. Prepare the animation.
                val velocity = velocityTracker.calculateVelocity().x
                val targetOffsetY = decay.calculateTargetValue(
                    offsetY.value,
                    velocity
                )
                // The animation stops when it reaches the bounds.
                offsetY.updateBounds(
                    lowerBound = -size.height.toFloat(),
                    upperBound = size.height.toFloat()
                )
                launch {
                    if (targetOffsetY.absoluteValue <= size.height) {
                        // Not enough velocity; Slide back.
                        offsetY.animateTo(
                            targetValue = 0f,
                            initialVelocity = velocity
                        )
                    } else {
                        // The element was swiped away.
                        offsetY.animateDecay(velocity, decay)
                        onDismissed()
                    }
                }
            }
        }
    }.offset { IntOffset(0, offsetY.value.roundToInt()) }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BottomSheetDialogMaterial3(
    isVisible: Boolean,
    slideTimeInMillis: Int = 800,
    backDropColor: Color = Color(0x44444444),
    dialogShape: Shape = MaterialTheme.shapes.medium.copy(
        bottomEnd = CornerSize(0),
        bottomStart = CornerSize(0)
    ),
    dialogElevation: CardElevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    dialogBorderStroke: BorderStroke? = BorderStroke(2.dp, MaterialTheme.colorScheme.onBackground),
    cardColors: CardColors = CardDefaults.cardColors(contentColor = MaterialTheme.colorScheme.background),
    onDismissRequest: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val transitionState = remember { MutableTransitionState(initialState = false) }
    val bottomPadding by remember { mutableStateOf(0.dp) }
    val resources = context.resources
    val withNavBar =
        resources.getBoolean(resources.getIdentifier("config_showNavigationBar", "bool", "android"))
    if (withNavBar) {
        //bottomPadding = 24.dp
    }

    if (!isVisible)
        transitionState.targetState = false
    else {
        Dialog(
            onDismissRequest = {
                transitionState.targetState = false
                onDismissRequest()
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backDropColor)
                    .padding(bottom = bottomPadding)
                    .clickable {
                        transitionState.targetState = false
                        onDismissRequest()
                    },
                contentAlignment = Alignment.BottomCenter,
            ) {
                AnimatedVisibility(
                    visibleState = transitionState,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = TweenSpec(
                            durationMillis = slideTimeInMillis,
                            delay = 0,
                            easing = LinearEasing
                        )
                    ),
                    exit = slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = TweenSpec(
                            durationMillis = slideTimeInMillis,
                            delay = 0,
                            easing = LinearEasing
                        )
                    )
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { }.swipeToDismiss {
                                transitionState.targetState = false
                                onDismissRequest()
                            },
                        shape = dialogShape,
                        elevation = dialogElevation,
                        border = dialogBorderStroke,
                        colors = cardColors,
                    ) {
                        content()
                    }
                }

                BackHandler {
                    transitionState.targetState = false
                    onDismissRequest()
                }
            }
        }
        transitionState.targetState = true
    }
}