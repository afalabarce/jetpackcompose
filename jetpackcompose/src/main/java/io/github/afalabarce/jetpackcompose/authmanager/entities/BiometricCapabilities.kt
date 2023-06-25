package io.github.afalabarce.jetpackcompose.authmanager.entities

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat


class BiometricCapabilities(
    val canDevicePattern: Boolean = false,
    val canBiometric: Boolean = false,
    private val context: Context? = null,
    private val biometricManager: BiometricManager? = null
){
    /**
     * indicate if current device has biometric (or pattern, pin, etc) authentication capabilities
     */
    val canBiometricAuthentication
        get() = this.canBiometric || this.canDevicePattern

    /**
     * Show (if applicable) an authentication dialog
     * @param title title for biometric authentication dialog
     * @param subTitle subtitle for biometric authentication dialog
     * @param description description text for biometric authentication dialog
     * @param onBiometricAuthentication lambda function with the result of authentication
     */
    fun showBiometricPrompt(
        title: String = "",
        subTitle: String = "",
        description: String = "",
        onBiometricAuthentication: (isSuccess: Boolean, errorCode: Int, errorDescription: String) -> Unit = { _, _, _ ->  }
    ){
        if (biometricManager != null && context != null && context is AppCompatActivity){
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setAllowedAuthenticators(
                    if (canBiometric)
                        BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.BIOMETRIC_STRONG
                    else
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                .setTitle(title)
                .setSubtitle(subTitle)
                .setDescription(description)
                .build()
            val biometricExecutor = ContextCompat.getMainExecutor(this.context)
            val biometricPrompt = BiometricPrompt(
                this.context,
                biometricExecutor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        onBiometricAuthentication(false, errorCode, errString as String)
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        onBiometricAuthentication(false, -1, "")
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        onBiometricAuthentication(true, -1, "")
                    }
                }
            )

            biometricPrompt.authenticate(promptInfo)
        }else{
            onBiometricAuthentication(true, -1, "")
        }

    }
}

