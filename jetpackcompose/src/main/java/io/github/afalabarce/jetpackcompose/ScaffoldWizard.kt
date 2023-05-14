package io.github.afalabarce.jetpackcompose

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.compose.foundation.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.LaunchedEffect

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScaffoldWizard(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    snackBarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    previousButtonColors: ButtonColors = ButtonDefaults.buttonColors(),
    previousButtonShape: Shape = MaterialTheme.shapes.medium,
    previousButtonElevation: ButtonElevation? = null,
    previousButtonBorder: BorderStroke? = null,
    previousButtonContent: @Composable () -> Unit,
    nextButtonColors: ButtonColors = ButtonDefaults.buttonColors(),
    nextButtonShape: Shape = MaterialTheme.shapes.medium,
    nextButtonElevation: ButtonElevation? = null,
    nextButtonBorder: BorderStroke? = null,
    nextButtonContent: @Composable () -> Unit,
    finishButtonContent: @Composable () -> Unit,
    pagerIndicatorActiveColor: Color = MaterialTheme.colorScheme.primary,
    pagerIndicatorInactiveColor: Color = MaterialTheme.colorScheme.secondary,
    floatingActionButtonPosition : FabPosition = FabPosition . End,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = contentColorFor(containerColor),
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
    bottomBarPaddingValues: PaddingValues = PaddingValues(),
    contentPaddingValues: PaddingValues = PaddingValues(),
    pages: List<@Composable () -> Unit>,
){
    var previousEnabled by remember { mutableStateOf(false) }
    var nextEnabled by remember { mutableStateOf(true) }
    var addPage by remember { mutableStateOf(0) }
    val pagerState = rememberPagerState(0)

    previousEnabled = pagerState.currentPage != 0
    nextEnabled = pagerState.currentPage != pages.size - 1

    Scaffold(
        modifier = modifier,
        topBar = topBar,
        snackbarHost = snackBarHost,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        containerColor = containerColor,
        contentColor = contentColor,
        contentWindowInsets = contentWindowInsets,
        bottomBar = {
            ConstraintLayout(modifier = Modifier
                .fillMaxWidth()
                .padding(bottomBarPaddingValues)) {
                val (previous, next) = createRefs()
                Button(
                    modifier = Modifier.constrainAs(previous){
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        bottom.linkTo(parent.bottom)
                        width = Dimension.percent(0.45f)
                    },
                    enabled = previousEnabled,
                    colors = previousButtonColors,
                    shape = previousButtonShape,
                    elevation = previousButtonElevation,
                    border = previousButtonBorder,
                    onClick = {
                        if (previousEnabled) {
                            addPage = -1
                        }
                    }
                ) {
                    previousButtonContent()
                }

                Button(
                    modifier = Modifier.constrainAs(next){
                        top.linkTo(parent.top)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                        width = Dimension.percent(0.45f)
                    },
                    colors = nextButtonColors,
                    shape = nextButtonShape,
                    elevation = nextButtonElevation,
                    border = nextButtonBorder,
                    onClick = {
                        if (nextEnabled) {
                            addPage = 1
                        }
                    }
                ) {
                    if (nextEnabled)
                        nextButtonContent()
                    else
                        finishButtonContent()
                }
            }
        }
    ) { containerPaddingValues ->
        ConstraintLayout(modifier = Modifier
            .fillMaxSize()
            .padding(containerPaddingValues)
            .padding(contentPaddingValues),
        ) {
            val (horizontalPager, pagerIndicator) = createRefs()

            HorizontalPagerIndicator(
                pagerState = pagerState,
                modifier = Modifier.constrainAs(pagerIndicator){
                    start.linkTo(parent.start, 16.dp)
                    end.linkTo(parent.end, 16.dp)
                    bottom.linkTo(parent.bottom, 4.dp)
                },
                pageCount = pages.size,
                activeColor = pagerIndicatorActiveColor,
                inactiveColor = pagerIndicatorInactiveColor,
            )

            HorizontalPager(
                modifier = Modifier.constrainAs(horizontalPager){
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(pagerIndicator.top, 4.dp)
                    width = Dimension.fillToConstraints
                    height = Dimension.fillToConstraints
                },
                userScrollEnabled = true,
                pageCount = pages.size,
                state = pagerState,
            ){
                if (!pagerState.isScrollInProgress)
                    addPage = 0

                pages[pagerState.currentPage]()
            }

            LaunchedEffect(addPage){
                pagerState.animateScrollToPage(pagerState.currentPage + addPage )
            }
        }
    }
}