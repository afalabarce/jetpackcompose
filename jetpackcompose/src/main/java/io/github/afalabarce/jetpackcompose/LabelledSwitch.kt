package io.github.afalabarce.jetpackcompose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@Composable
fun LabelledSwitch(
    modifier: Modifier = Modifier,
    checked: Boolean,
    label: String,
    leadingIcon: @Composable () -> Unit = {},
    enabled: Boolean = true,
    colors: SwitchColors = SwitchDefaults.colors(),
    onCheckedChange: ((Boolean) -> Unit)
) {

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .toggleable(
                value = checked,
                onValueChange = onCheckedChange,
                role = Role.Switch,
                enabled = enabled
            )
            .padding(horizontal = 8.dp)

    ) {
        CompositionLocalProvider(
            LocalContentAlpha provides
                    if (enabled) ContentAlpha.high else ContentAlpha.disabled
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(0.9f).align(Alignment.CenterStart),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                leadingIcon()
                Text(
                    text = label,
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier
                        .padding(end = 16.dp)
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = null,
            enabled = enabled,
            colors = colors,
            modifier = Modifier.align(Alignment.CenterEnd).padding(start = 8.dp)
        )
    }
}