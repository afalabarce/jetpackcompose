package io.github.afalabarce.jetpackcompose.authmanager.exceptions

import android.accounts.AccountManager
import android.content.Context


class UnsupportedAccountTypeException(context: Context, unsupportedAccountTypeResourceId: Int) :
    AuthenticatorException(
        context,
        AccountManager.ERROR_CODE_UNSUPPORTED_OPERATION,
        unsupportedAccountTypeResourceId
    ) {
    companion object{
        private val serialVersionUUID: Long = 2L
    }
}