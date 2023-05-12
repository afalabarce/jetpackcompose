package io.github.afalabarce.jetpackcompose

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.afalabarce.jetpackcompose.networking.NetworkStatus
import io.github.afalabarce.jetpackcompose.networking.NetworkStatusTracker

fun ComponentActivity.setConnectionAvailableContent(content: @Composable (NetworkStatus) -> Unit){
    setContent {
        val networkStatusTracker by remember { mutableStateOf(NetworkStatusTracker(this)) }
        val connectionStatus by networkStatusTracker.networkStatus.collectAsStateWithLifecycle(NetworkStatus.Available)

        content(connectionStatus)
    }
}