package io.github.afalabarce.jetpackcompose

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun <T> SpinnerSelector(
    modifier: Modifier,
    readOnly: Boolean = false,
    label: @Composable () -> Unit = {},
    height: Dp = 160.dp,
    items: List<T>,
    onSelectedItem: (T) -> Unit = {},
    itemComposable: @Composable (T) -> Unit = { i -> Text(i.toString())}
) {
    var expandedDropDown by remember { mutableStateOf(false) }
    var spinnerValue: T? by remember { mutableStateOf(null) }


    Button(
        onClick = {
            if (!readOnly)
                expandedDropDown = !expandedDropDown
        },
        modifier = modifier.height(58.dp),
        shape = MaterialTheme.shapes.small,
        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.background),
        border = BorderStroke(2.dp, MaterialTheme.colors.primary)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Column(
                modifier = Modifier
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
            Column(
                modifier = Modifier
                    .width(24.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center
            ) {
                Icon(imageVector = Icons.Rounded.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                        .focusable(false)
                        .clickable(true) {
                            if (!readOnly)
                                expandedDropDown = !expandedDropDown
                        }
                )
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
                    Column(modifier = Modifier.clickable {
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
}