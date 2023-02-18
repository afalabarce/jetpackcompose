package io.github.afalabarce.jetpackcompose

import android.content.res.Configuration
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlin.math.cos
import kotlin.math.sin

enum class SatellitePosition (private val value: Int){
    NORTH(0),
    NORTH_EAST(1),
    EAST(2),
    SOUTH_EAST(3),
    SOUTH(4),
    SOUTH_WEST(5),
    WEST(6),
    NORTH_WEST(7);

    operator fun inc(): SatellitePosition = SatellitePosition.values().first { x -> x.value == (this.value + 1) % SatellitePosition.values().size }
    operator fun plus(intValue: Int): SatellitePosition = SatellitePosition.values().first { x -> x.value == (this.value + intValue) % SatellitePosition.values().size }
}

data class Satellite(
    val satelliteKey: String,
    val orbit: Int,
    val satellitePosition: SatellitePosition,
    val content: @Composable BoxScope.() -> Unit,
)

@Composable
fun OrbitalMenu(
    modifier: Modifier,
    isExpanded: Boolean,
    orbitColor: Color = MaterialTheme.colorScheme.primary,
    orbitWidth: Dp = 2.dp,
    orbitStyle: DrawStyle? = null,
    core: @Composable BoxScope.() -> Unit,
    satellites: List<Satellite>,
    onCorePositioned: () -> Unit,
    onClickCore: () -> Unit,
    onClickSatellite: (Satellite) -> Unit
){
    val orbitsNumber = satellites.maxOfOrNull { satellite -> satellite.orbit } ?: 0
    val configuration = LocalConfiguration.current
    var fullScreenSize by remember { mutableStateOf(Size.Zero) }
    var screenSize by remember { mutableStateOf(Size.Zero) }
    var coreSize by remember { mutableStateOf(0.dp) }
    var isComposableExpanded by remember { mutableStateOf(false) }
    var coreCenterPosition: IntOffset by remember { mutableStateOf(IntOffset.Zero) }
    var orbitRadius: Map<Int, Float>  by remember {
        mutableStateOf(
            mapOf(*IntRange(1, orbitsNumber).map { orbit -> orbit to 0f }.toTypedArray())
        )
    }

    Box(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                screenSize = Size(
                    coordinates.size.width.toFloat(),
                    coordinates.size.height.toFloat(),
                )

                fullScreenSize = Size(
                    coordinates.size.width.toFloat(),
                    coordinates.size.height.toFloat(),
                )
            }
            .drawBehind {
                if (isExpanded) {
                    for (orbit in orbitRadius) {
                        drawCircle(
                            color = orbitColor,
                            style = orbitStyle ?: Stroke(orbitWidth.value),
                            radius = orbit.value
                        )
                    }
                }
            }
    ){
        Box( // Container for the core
            modifier = Modifier.zIndex(10f)
                .onGloballyPositioned { coreCoordinates ->
                    coreSize = Dp(
                        listOf(
                            coreCoordinates.size.width.toFloat(),
                            coreCoordinates.size.width.toFloat()
                        ).maxOf { x -> x }
                    )
                    coreCenterPosition = coreCoordinates
                        .positionInParent()
                        .let { old ->
                            IntOffset(
                                x = (old.x + coreCoordinates.size.width* 0.5f).toInt(),
                                y = (old.y + coreCoordinates.size.height* 0.5f).toInt()
                            )
                        }
                    screenSize = Size(
                        fullScreenSize.width - coreSize.value,
                        fullScreenSize.height - coreSize.value
                    )
                    orbitRadius = orbitRadius
                        .map { (orbit, _) ->
                            val multiplier = (orbit.toFloat() / orbitsNumber)

                            orbit to (
                                    (if (orbit == 1) coreSize.value else coreSize.value* 0.5f) + (
                                            if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
                                                screenSize.height
                                            else
                                                screenSize.width
                                            )* 0.5f) * multiplier
                        }
                        .toMap()
                    isComposableExpanded = isExpanded && (coreCenterPosition.x != 0 || coreCenterPosition.y != 0)
                    onCorePositioned()
                }
                .align(alignment = Alignment.Center)
                .clickable { onClickCore() }
        ){
            core()
        }

        if (coreCenterPosition != IntOffset.Zero)
            satellites.forEach { satellite ->
                SatelliteComposable(
                    modifier = Modifier.size(48.dp).zIndex(9f),
                    isVisible = isComposableExpanded,
                    satellite = satellite,
                    centerPosition = coreCenterPosition,
                    orbitRadius = orbitRadius[satellite.orbit]!!,
                    onClick = onClickSatellite
                )
            }
    }
}

@Composable
private fun SatelliteComposable(
    modifier: Modifier,
    isVisible: Boolean,
    satellite: Satellite,
    centerPosition: IntOffset,
    orbitRadius: Float,
    onClick: (Satellite) -> Unit
) {
    var composableVisibility by remember { mutableStateOf(true) }
    var satelliteSize by remember { mutableStateOf(IntSize.Zero) }
    var animatedCenterPosition by remember { mutableStateOf(centerPosition) }
    var composablePosition by remember { mutableStateOf(animatedCenterPosition) }
    val position = when (satellite.satellitePosition) {
        SatellitePosition.NORTH -> Offset(
            x = centerPosition.x - satelliteSize.width.toFloat() * 0.5f,
            y = centerPosition.y - (orbitRadius + satelliteSize.height * 0.5f)
        )
        SatellitePosition.EAST -> Offset(
            x = centerPosition.x + orbitRadius - satelliteSize.width * 0.5f,
            y = centerPosition.y - satelliteSize.height.toFloat() * 0.5f
        )
        SatellitePosition.WEST -> Offset(
            x = centerPosition.x - (orbitRadius + satelliteSize.width * 0.5f),
            y = centerPosition.y - satelliteSize.height.toFloat() * 0.5f
        )
        SatellitePosition.SOUTH -> Offset(
            x = centerPosition.x - satelliteSize.width * 0.5f,
            y = centerPosition.y + (orbitRadius - satelliteSize.height * 0.5f)
        )
        SatellitePosition.NORTH_EAST -> Offset(
            x = centerPosition.x + orbitRadius * cos(45f),
            y = centerPosition.y - orbitRadius * sin(45f)
        )
        SatellitePosition.SOUTH_EAST -> Offset(
            x = centerPosition.x + orbitRadius * cos(45f),
            y = centerPosition.y + orbitRadius * sin(45f) - satelliteSize.height * 0.9f
        )

        SatellitePosition.SOUTH_WEST -> Offset(
            x = centerPosition.x + (orbitRadius * cos(135f) + satelliteSize.width * 0.9f),
            y = centerPosition.y + orbitRadius * sin(45f) - satelliteSize.height * 0.9f
        )

        SatellitePosition.NORTH_WEST -> Offset(
            x = centerPosition.x + (orbitRadius * cos(135f) + satelliteSize.width * 0.9f),
            y = centerPosition.y - (orbitRadius * sin(45f))
        )
    }

    val animatedOffsetEffect by animateIntOffsetAsState(
        targetValue = composablePosition,
        label = satellite.satelliteKey,
        animationSpec = tween(700, easing = LinearOutSlowInEasing)
    ) {
        composableVisibility = isVisible
    }

    if (composableVisibility) {
        Box(
            modifier = modifier
                .offset { animatedOffsetEffect }
                .onGloballyPositioned { satelliteCoordinates ->
                    satelliteSize = satelliteCoordinates.size
                    animatedCenterPosition = IntOffset(
                        (centerPosition.x - (satelliteSize.width * 0.5f)).toInt(),
                        (centerPosition.y - (satelliteSize.height * 0.5f)).toInt()
                    )
                    composablePosition = if (isVisible)
                        IntOffset(position.x.toInt(), position.y.toInt())
                    else
                        animatedCenterPosition
                }
                .background(Color.Red)
                .clickable { onClick(satellite) }
        ) {
            satellite.content(this)
        }
    }

    composablePosition = if (!isVisible)
        animatedCenterPosition
    else
        IntOffset(position.x.toInt(), position.y.toInt()).also { composableVisibility = true }
}
