package io.github.afalabarce.jetpackcompose

import android.media.Image
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.ArrowDropUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.constraintlayout.compose.ConstraintLayout
import io.github.afalabarce.jetpackcompose.utilities.iif

@Composable
fun <T> SpinnerSelector(
    modifier: Modifier,
    readOnly: Boolean = false,
    hintText: String = "",
    label: @Composable () -> Unit = {},
    colors: ButtonColors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background),
    border: BorderStroke = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
    shape: Shape = MaterialTheme.shapes.small,
    componentHeight: Dp = 58.dp,
    height: Dp = 160.dp,
    selectedItem: T? = null,
    expandedTrailingIcon: ImageVector? = null,
    collapsedTrailingIcon: ImageVector? = null,
    trailingIconTint: Color = MaterialTheme.colorScheme.onBackground,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    onBackgroundColor: Color = MaterialTheme.colorScheme.background,
    items: List<T>,
    onSelectedItem: (T) -> Unit = {},
    itemComposable: @Composable (T) -> Unit = { i -> Text(i.toString())}
) {
    var expandedDropDown by remember { mutableStateOf(false) }
    var spinnerValue: T? by remember { mutableStateOf(null) }

    if (selectedItem != null)
        spinnerValue = selectedItem
    Box(modifier = modifier.height((spinnerValue != null).iif(componentHeight.plus(6.dp), componentHeight))){
        Button(
            onClick = {
                if (!readOnly)
                    expandedDropDown = !expandedDropDown
            },
            modifier = modifier
                .height(componentHeight)
                .align(Alignment.BottomStart),
            shape = shape,
            colors = colors,
            border = border,
        ) {
            ConstraintLayout(
                modifier = Modifier.fillMaxSize(),
            ) {
                val (spinnerItem, spinnerIcon) = createRefs()

                Icon(
                    imageVector = if (expandedDropDown)
                        expandedTrailingIcon ?: Icons.Rounded.ArrowDropUp
                    else
                        collapsedTrailingIcon ?: Icons.Rounded.ArrowDropDown,
                    contentDescription = null,
                    tint = trailingIconTint,
                    modifier = Modifier.size(32.dp).constrainAs(spinnerIcon){
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end)
                    }.focusable(false)
                    .clickable(true) {
                        if (!readOnly)
                            expandedDropDown = !expandedDropDown
                    }
                )

                Column(
                    modifier = Modifier
                        .constrainAs(spinnerItem){
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(spinnerIcon.start)
                        }
                        .fillMaxWidth(0.93f)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    if (spinnerValue == null)
                        label()
                    else
                        itemComposable(spinnerValue!!)
                }
            }

            DropdownMenu(expanded = expandedDropDown,
                onDismissRequest = { expandedDropDown = false }
            ) {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .padding(8.dp)
                    .verticalScroll(rememberScrollState())) {
                    items.forEach { item ->
                        Column(modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                spinnerValue = item
                                expandedDropDown = false
                                onSelectedItem(item)
                            }) {
                            itemComposable(item)
                        }
                    }
                }
            }
        }

        if (spinnerValue != null && hintText.isNotEmpty()){
            Column(
                modifier = modifier
                    .height((spinnerValue != null).iif(componentHeight.plus(6.dp), componentHeight))
                    .padding(start = 16.dp)
                    .background(Color.Transparent),
                verticalArrangement = Arrangement.Top

            ) {
                Text(
                    text = hintText,
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor,
                    modifier = Modifier
                        .background(onBackgroundColor)
                        .padding(horizontal = 4.dp)
                )
            }
        }
    }
}