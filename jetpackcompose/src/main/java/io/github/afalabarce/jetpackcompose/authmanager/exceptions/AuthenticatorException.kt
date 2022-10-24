package io.github.afalabarce.jetpackcompose.authmanager.exceptions

import android.accounts.AccountManager
import android.content.Context
import android.os.Bundle

open class AuthenticatorException(): Exception() {
    companion object{
        private val serialVersionUUID: Long = 1L
    }

    private lateinit var mFailureBundle: Bundle
    val failureBundle: Bundle
        get() = this.mFailureBundle

    protected constructor(ctx: Context, errorCode: Int, errorMessageStringResourceId: Int) : this() {
        this.mFailureBundle = Bundle().apply {
            putInt(AccountManager.KEY_ERROR_CODE, errorCode)
            putString(AccountManager.KEY_ERROR_MESSAGE, ctx.getString(errorMessageStringResourceId))
        }
    }

}