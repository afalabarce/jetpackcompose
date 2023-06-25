package io.github.afalabarce.jetpackcompose

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.afalabarce.jetpackcompose.authmanager.entities.BiometricCapabilities
import io.github.afalabarce.jetpackcompose.networking.NetworkStatus
import io.github.afalabarce.jetpackcompose.networking.NetworkStatusTracker

fun ComponentActivity.setUiContent(content: @Composable () -> Unit){
    setContent {
        val currentNetworkStatus by NetworkStatusTracker(this@setUiContent)
            .networkStatus.collectAsStateWithLifecycle(initialValue = NetworkStatus.Available)
        val currentBiometricManager = BiometricManager.from(this@setUiContent)
        val canBiometricAuth = currentBiometricManager.canAuthenticate(BiometricManager.Authenticators.DEVICE_CREDENTIAL) == BIOMETRIC_SUCCESS ||
                currentBiometricManager.canAuthenticate(
                    BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.BIOMETRIC_STRONG
                ) == BIOMETRIC_SUCCESS

        CompositionLocalProvider (
            LocalNetworkStatus provides currentNetworkStatus,
            LocalBiometricCapabilities provides BiometricCapabilities(
                canDevicePattern = currentBiometricManager.canAuthenticate(BiometricManager.Authenticators.DEVICE_CREDENTIAL) == BIOMETRIC_SUCCESS,
                canBiometric = currentBiometricManager.canAuthenticate(
                    BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.BIOMETRIC_STRONG
                ) == BIOMETRIC_SUCCESS,
                context = if (canBiometricAuth) this@setUiContent else null,
                biometricManager = if (canBiometricAuth) currentBiometricManager else null
            )
        ){
            content()
        }
    }
}