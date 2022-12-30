package io.github.afalabarce.jetpackcompose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState

@OptIn(ExperimentalPagerApi::class)
@Composable
fun CardPager(
    modifier: Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    backgroundColor: Color = MaterialTheme.colors.surface,
    contentColor: Color = contentColorFor(backgroundColor),
    showPagerIndicator: Boolean = true,
    pagerIndicatorActiveColor: Color = Color.Black,
    pagerIndicatorInactiveColor: Color = Color.Gray,
    border: BorderStroke? = null,
    elevation: Dp = 1.dp,
    pageComposables: Array<@Composable () -> Unit>
){
    var selectedPage by remember { mutableStateOf(0) }
    Card(
        modifier = modifier,
        shape = shape,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        border = border,
        elevation = elevation,

        ){
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter,
        ) {
            val pagerState = rememberPagerState(selectedPage)

            HorizontalPager(
                modifier = Modifier.fillMaxSize(),
                count = pageComposables.size,
                state = pagerState,

                ) { pagerScope ->
                if (pagerScope in pageComposables.indices){
                    pageComposables[pagerScope]()
                }
            }
            if (showPagerIndicator){
                HorizontalPagerIndicator(
                    pagerState = pagerState,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    pageCount = pageComposables.size,
                    activeColor = pagerIndicatorActiveColor,
                    inactiveColor = pagerIndicatorInactiveColor,
                )
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun CardPagerPreview(){
    MaterialTheme{
        Surface{
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.Top) {
                CardPager(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp)
                        .padding(vertical = 4.dp),
                    border = BorderStroke(1.dp, Color.Black),
                    pageComposables = arrayOf(
                        {
                            Text(text = "Page 1.1")
                        },
                        {
                            Text(text = "Page 1.2")
                        },
                        {
                            Text(text = "Page 1.3")
                        }
                    )
                )

                CardPager(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp)
                        .padding(vertical = 4.dp),
                    border = BorderStroke(1.dp, Color.Black),
                    pagerIndicatorActiveColor = Color.Red,
                    pagerIndicatorInactiveColor = Color.Cyan,
                    pageComposables = arrayOf(
                        {
                            Text(text = "Page 2.1")
                        },
                        {
                            Text(text = "Page 2.2")
                        },
                        {
                            Text(text = "Page 2.3")
                        }
                    )
                )
            }

        }
    }
}