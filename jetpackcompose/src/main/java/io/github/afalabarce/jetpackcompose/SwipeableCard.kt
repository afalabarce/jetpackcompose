@file:OptIn(ExperimentalMaterialApi::class)

package io.github.afalabarce.jetpackcompose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

data class SwipeAction(val order: Int, val key: String, val title: String, val imageVector: ImageVector, val color: Color, val tint: Color, val dockLeft: Boolean)

/**
 * Swipeable Horizontal Card with custom actions
 * @param modifier Modifier to be applied to the layout of the card.
 * @param shape Defines the card's shape as well its shadow. A shadow is only
 *  displayed if the [elevation] is greater than zero.
 * @param backgroundColor The background color.
 * @param contentColor The preferred content color provided by this card to its children.
 * Defaults to either the matching content color for [backgroundColor], or if [backgroundColor]
 * is not a color from the theme, this will keep the same value set above this card.
 * @param border Optional border to draw on top of the card
 * @param elevation The z-coordinate at which to place this card. This controls
 *  the size of the shadow below the card.
 *  @param buttonWidth SwipeActions fixed Width
 *  @param swipeActions SwipeActions Array with left/right docking
 *  @param onClickSwipeAction Raised event on SwipeAction tap
 *  @param swipeBackColor Backcolor of swipe area
 *  @param content Card Content
 */
@Composable
fun SwipeableCard(modifier: Modifier = Modifier,
                  shape: Shape = MaterialTheme.shapes.medium,
                  backgroundColor: Color = MaterialTheme.colors.surface,
                  buttonWidth: Dp,
                  swipeBackColor: Color = Color.Transparent,
                  contentColor: Color = contentColorFor(backgroundColor),
                  border: BorderStroke? = null,
                  elevation: Dp = 1.dp,
                  swipeActions: Array<SwipeAction> = arrayOf(),
                  onClickSwipeAction: (SwipeAction) -> Unit = { },
                  content: @Composable () -> Unit) {
    val swipeableState = rememberSwipeableState(0)
    val withLeftActions = swipeActions.any { x -> x.dockLeft }
    val withRightActions = swipeActions.any { x -> !x.dockLeft }
    val actionState = if (withLeftActions && !withRightActions)
                            1
                        else if (withLeftActions && withRightActions)
                            2
                        else if (!withLeftActions && withRightActions)
                            3
                        else
                            0
    val sizePx = with(LocalDensity.current) {
        buttonWidth.times(swipeActions.count { x -> x.dockLeft }).toPx()
    }
    val sizePxR = with(LocalDensity.current) {
        buttonWidth.times(swipeActions.count { x -> !x.dockLeft }).toPx()
    }

    val anchors = when (actionState){
        1 ->  mapOf(0f to 0, sizePx to 1)
        2 ->  mapOf(0f to 0, sizePx to 1, -sizePxR to 2)
        3 -> mapOf(0f to 0, -sizePxR to 2)
        else -> mapOf(0f to 0)
    }

    Box(
        modifier = modifier.background(swipeBackColor)
            .padding(0.dp)
            .swipeable(
                state = swipeableState,
                anchors = anchors,
                orientation = Orientation.Horizontal,
                thresholds = { _, _ -> FractionalThreshold(0.3f) })
    ) {
        if (swipeActions.isNotEmpty()) {
            Row(modifier = Modifier.fillMaxSize()) {
                if (swipeActions.any { sw -> sw.dockLeft }) {
                    Row(modifier = Modifier.fillMaxHeight()) {
                        swipeActions.filter { x -> x.dockLeft }.sortedBy { o -> o.order }
                            .map { action ->
                                Button(
                                    onClick = { onClickSwipeAction(action) },
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(buttonWidth),
                                    colors = ButtonDefaults.buttonColors(backgroundColor = action.color)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = action.imageVector,
                                            contentDescription = null,
                                            modifier = Modifier.size(buttonWidth.div(2)),
                                            tint = action.tint
                                        )
                                        Text(text = action.title, fontSize = 10.sp, color = action.tint)
                                    }
                                }
                            }
                    }
                }

                if (swipeActions.any { sw -> !sw.dockLeft }) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        swipeActions.filter { x -> !x.dockLeft }.sortedBy { o -> o.order }
                            .map { action ->
                                Button(
                                    onClick = { onClickSwipeAction(action) },
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(buttonWidth),
                                    colors = ButtonDefaults.buttonColors(backgroundColor = action.color)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = action.imageVector,
                                            contentDescription = null,
                                            modifier = Modifier.size(buttonWidth.div(2)),
                                            tint = action.tint
                                        )
                                        Text(text = action.title, fontSize = 10.sp, color = action.tint)
                                    }
                                }
                            }
                    }
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp)
                .offset {
                    IntOffset(
                        if (swipeActions.isEmpty()) 0 else swipeableState.offset.value.roundToInt(),
                        0
                    )
                },
            shape = shape,
            backgroundColor = backgroundColor,
            contentColor = contentColor,
            border = border,
            elevation = elevation,
            content = content
        )
    }
}