package io.github.afalabarce.jetpackcompose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
inline fun <reified T>RadioButtonGroup(
    modifier: Modifier = Modifier,
    radioButtonLabel: @Composable (T) -> Unit = { },
    radioButtonValues: Array<T>,
    selectedValue: T?,
    itemVerticalAlignment: Alignment.Vertical = CenterVertically,
    borderStroke: BorderStroke? = null,
    dividerHeight: Dp = 4.dp,
    excludedValues: Array<T> = emptyArray(),
    radioButtonItemShape: Shape = MaterialTheme.shapes.medium,
    crossinline onCheckedChanged: (T) -> Unit
) {
    Column(
        modifier = modifier
    ) {
        radioButtonValues
            .filter { notExcluded -> !excludedValues.any { excluded -> excluded == notExcluded } }
            .forEachIndexed{ index,  item ->
                if (index > 0)
                    Spacer(modifier = Modifier.size(dividerHeight))
                Row(
                    modifier = Modifier
                        .clip(radioButtonItemShape)
                        .border(borderStroke ?: BorderStroke(0.dp, Color.Unspecified), radioButtonItemShape)
                        .fillMaxWidth()
                        .clickable { if (item == selectedValue) onCheckedChanged(item) },
                    verticalAlignment = itemVerticalAlignment,
                ) {
                    RadioButton(
                        selected = item == selectedValue,
                        onClick = { if (item == selectedValue) onCheckedChanged(item) }
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    radioButtonLabel(item)
                }
            }
    }
}