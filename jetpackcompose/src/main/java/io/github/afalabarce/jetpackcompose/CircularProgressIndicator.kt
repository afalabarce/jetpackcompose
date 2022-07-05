package io.github.afalabarce.jetpackcompose

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CircularProgressIndicatorWithBackground(progress: Float,
                                            modifier: Modifier = Modifier,
                                            progressColor: Color = MaterialTheme.colors.primary,
                                            backgroundColor: Color = MaterialTheme.colors.onPrimary,
                                            strokeWidth: Dp = ProgressIndicatorDefaults.StrokeWidth,
                                            content: @Composable () -> Unit
){
    Box(modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
                .padding(16.dp)
        ) {
            drawCircle(
                color = backgroundColor,
                center = Offset(size.width / 2f, 24 + size.height / 2f),
                style = Stroke(width = strokeWidth.toPx()),

                )
        }
        val animatedProgress = animateFloatAsState(
            targetValue = progress,
            animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
        ).value

        CircularProgressIndicator(
            animatedProgress,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            color = progressColor,
            strokeWidth = strokeWidth
        )
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            content()
        }
    }
}