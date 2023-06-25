package io.github.afalabarce.jetpackcompose

import androidx.compose.runtime.compositionLocalOf
import io.github.afalabarce.jetpackcompose.authmanager.entities.BiometricCapabilities
import io.github.afalabarce.jetpackcompose.networking.NetworkStatus

val LocalNetworkStatus = compositionLocalOf<NetworkStatus> {
    NetworkStatus.Available
}

val LocalBiometricCapabilities = compositionLocalOf { BiometricCapabilities() }