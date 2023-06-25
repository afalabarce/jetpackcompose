package io.github.afalabarce.jetpackcompose.authmanager.entities

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.os.CancellationSignal
import androidx.fragment.app.FragmentActivity


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
     * @param negativeText negativeText for cancel biometric authentication
     * @param onBiometricAuthentication lambda function with the result of authentication
     */
    fun showBiometricPrompt(
        title: String,
        subTitle: String = "",
        description: String,
        negativeText: String,
        onBiometricAuthentication: (isSuccess: Boolean, errorCode: Int, errorDescription: String) -> Unit = { _, _, _ ->  }
    ){
        if (biometricManager != null && context != null){
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
                .setNegativeButtonText(negativeText)
                .build()
            val mainExecutor = ContextCompat.getMainExecutor(this.context)
            val biometricPrompt = BiometricPrompt (
                this.context as FragmentActivity,
                mainExecutor,
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

    private fun getCancellationSignal(onCancel: () -> Unit): CancellationSignal {
        val cancellationSignal = CancellationSignal()
        cancellationSignal.setOnCancelListener (onCancel)

        return cancellationSignal
    }
}

