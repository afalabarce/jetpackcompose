package io.github.afalabarce.jetpackcompose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview
@Composable
fun ToolButton(){
    Box(modifier = Modifier.size(48.dp)) {
        Button(
            onClick = { /*TODO*/ },
            modifier = Modifier.fillMaxSize(),
            shape = MaterialTheme.shapes.large ,
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent, contentColor = Color.Transparent),
            border = null,
            elevation = null,
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.5f)
                    .clip(
                        AbsoluteRoundedCornerShape(
                            topLeftPercent = 0,
                            topRightPercent = 0,
                            bottomLeftPercent = 100,
                            bottomRightPercent = 100
                        )
                    )
                    .background(Color.Blue)
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ){
                    Icon(Icons.Filled.Home, contentDescription = null, tint = Color.White)
                }
            }
        }
    }
    
}