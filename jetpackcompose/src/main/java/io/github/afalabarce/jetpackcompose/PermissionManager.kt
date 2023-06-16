package io.github.afalabarce.jetpackcompose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionManager(
    vararg permission: String,
    modifier: Modifier = Modifier,
    showDeniedIfNeeded: Boolean = true,
    onDenied: @Composable BoxScope.(Array<String>) -> Unit,
    onGranted: @Composable BoxScope.(Array<String>) -> Unit,
){
    val deniedPermissions = remember { mutableStateListOf<String>() }
    val grantedPermissions = remember { mutableStateListOf<String>() }
    var launchedPermissionRequest by remember {
        mutableStateOf(false)
    }
    val permissions = rememberMultiplePermissionsState(permissions = permission.toList()){ permissionResults ->

        launchedPermissionRequest = true
        deniedPermissions.addAll(permissionResults.filterValues { isGranted -> !isGranted }.keys)
        grantedPermissions.addAll(permissionResults.filterValues { isGranted -> isGranted }.keys)
    }

    Box(
        modifier = modifier
    ){
        if (launchedPermissionRequest) {
            if (grantedPermissions.isNotEmpty()) {
                this.onGranted(grantedPermissions.toTypedArray())
            }

            if (!permissions.allPermissionsGranted && showDeniedIfNeeded) {
                this.onDenied(deniedPermissions.toTypedArray())
            }
        }else{
            LaunchedEffect(permissions){
                permissions.launchMultiplePermissionRequest()
            }
        }
    }
}