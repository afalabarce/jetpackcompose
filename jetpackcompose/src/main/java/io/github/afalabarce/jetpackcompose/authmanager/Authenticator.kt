package io.github.afalabarce.jetpackcompose.authmanager

import android.accounts.*
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import com.google.gson.GsonBuilder
import io.github.afalabarce.jetpackcompose.authmanager.entities.IUser
import io.github.afalabarce.jetpackcompose.authmanager.enums.LoginType
import io.github.afalabarce.jetpackcompose.authmanager.enums.Operation
import io.github.afalabarce.jetpackcompose.authmanager.exceptions.UnsupportedAccountTypeException

/**
 * With this class you can manage the creation and update of your app user accounts.
 *
 * Usage:
 *
 * 1. Create your own class YourOwnAuthenticatorService inherits from Service.
 *
 *    1.1. Override onBind method, with Authenticator implementation:
 *
 *         override fun onBind(intent: Intent?) = Authenticator(
 *                this,
 *                YourLoginActivity::class.java,
 *                R.string.your_account_type_name_resource_id,
 *                R.string.your_custom_message_on_single_account_error,
 *                R.string.your_custom_message_on_unsupported_account_error,
 *                R.string.your_custom_message_on_unsupported_token_error,
 *                R.string.your_custom_message_on_unsupported_features_error,
 *                true/false // true if only one account is allowed, false if app are designed to multiple accounts
 *         )
 *
 * 2. Create your own LoginActivity. This activity need to handle some extras from intent:
 *
 *      2.1. At your onCreate method, handle some values from activity's intent, to prepare for account creation, from app or from account manager:
 *
 *             2.1.1. Instantiate is own Authenticator (like 1.1).
 *          2.1.2. Load all data from this.intent.extras:
 *                this.fromApp = try{ (this.intent.extras!![Authenticator.ACTION_LOGIN_TYPE] as AuthenticatorLoginType) == AuthenticatorLoginType.App} catch(_: Exception){ false }
 *                this.loginUser = try{ this.intent.getSerializableExtra(Authenticator.KEY_ACCOUNT) as YourAppAccount<b>IUser</b> }catch(_: Exception { null }
 *          2.1.3. Usually, if you create your login data from app, you need to clean all fields.
 *          2.1.4. Usually, if you update your login data from app, you need to load persisted data at this.loginUser (2.1.2 extracted data)
 *          2.1.5. If login process is successful, you can persist your user account info:
 *                  this.authenticator.saveUserAccount(this, R.string.your_account_type_name_resource_id, this.loginUser)
 *
 * 3. At your AndroidManifest.xml
 *
 *      3.1. Add some needed permissions:
 *
 *                 <uses-permission android:name="android.permission.MANAGE_ACCOUNTS" />
 *              <uses-permission android:name="android.permission.AUTHENTICATE_ACCOUNTS" />
 *              <uses-permission android:name="android.permission.USE_CREDENTIALS" />
 *              <uses-permission android:name="android.permission.GET_ACCOUNTS" />
 *              <uses-permission android:name="android.permission.READ_PROFILE" />
 *
 *      3.2. Add a reference to your YourOwnAuthenticatorService into <application> section
 *              <service android:name=".auth.YourOwnAuthenticator">
 *                  <intent-filter>
 *                      <action android:name="android.accounts.AccountAuthenticator" />
 *                  </intent-filter>
 *
 *                  <meta-data
 *                      android:name="android.accounts.AccountAuthenticator"
 *                      android:resource="@xml/authenticator" />
 *              </service>
 *
 * 4. Add a resource to xml/authenticator.xml, with this content:
 *
 *      &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 *
 *      &lt;account-authenticator xmlns:android="http://schemas.android.com/apk/res/android"
 *
 *              android:accountType="@string/your_account_type_name_resource_id"
 *              android:icon="@mipmap/ic_launcher"
 *              android:label="@string/app_name"
 *              android:smallIcon="@mipmap/ic_launcher"&gt;
 *      &lt;/account-authenticator&gt;
 *
 * AND THAT'S ALL!!
 */
class Authenticator @JvmOverloads constructor(
    val context: Context,
    val loginActivityClass: Class<*>,
    val accountTypeResourceId: Int,
    private val errorOnlyOneAccountAllowedMessageId: Int,
    private val unsupportedAccountTypeExceptionResId: Int,
    private val unsupportedAuthTokenTypeExceptionResId: Int,
    private val unsupportedFeaturesExceptionResId: Int,
    val onlyOneAccount: Boolean = false
) : AbstractAccountAuthenticator(context) {
    companion object{
        const val ACTION_OPERATION = "ActionOperation"
        const val ACTION_LOGIN_TYPE = "ActionLoginType"
        const val KEY_AUTH_TOKEN_TYPE = "AuthTokenType"
        const val KEY_REQUIRED_FEATURES = "RequiredFeatures"
        const val KEY_LOGIN_OPTIONS = "LoginOptions"
        const val KEY_ACCOUNT = "Account"

        /**
         * Get all user accounts register into the system of provided account type
         */
        fun loadUserAccounts(context: Context, accountTypeResId: Int): List<Account>{
            try {
                val manager = AccountManager.get(context)
                return manager.getAccountsByType(context.getString(accountTypeResId)).asList()
            }catch (_: Exception){

            }

            return listOf()
        }

        /**
         * Gets system user account identified by their account type and name
         */
        fun getUserAccount(context: Context, accountTypeResId: Int, accountName: String): Account?{
            try{
                return Authenticator.loadUserAccounts(
                    context = context,
                    accountTypeResId = accountTypeResId
                ).firstOrNull { x -> x.name.lowercase() == accountName.lowercase() }
            }catch (_: Exception){

            }

            return null
        }

        /**
         * Gets system user account identified by their account type and name, returns logic accound data
         */
        inline fun <reified T: IUser>getUserAccount(context: Context, accountTypeResId: Int, accountName: String): T?{
            try{
                val manager = AccountManager.get(context)
                val account = Authenticator.loadUserAccounts(
                    context = context,
                    accountTypeResId = accountTypeResId
                ).firstOrNull { x -> x.name.lowercase() == accountName.lowercase() }
                val accountData = Base64.decode(manager.getPassword(account), Base64.DEFAULT)
                return GsonBuilder().create().fromJson(String(accountData), T::class.java)
            }catch (_: Exception){

            }

            return null
        }

        /**
         * Saves user account into the android Account Manager
         */
        fun <T: IUser> saveUserAccount(context: Context, accountTypeResId: Int, userData: T): Boolean{

            try {
                val manager = AccountManager.get(context)
                val serializedUser = Base64.encodeToString(GsonBuilder().create().toJson(userData).toByteArray(Charsets.UTF_8), Base64.DEFAULT)
                val currentAccount = getUserAccount(
                    context = context,
                    accountTypeResId = accountTypeResId,
                    accountName = userData.userName
                )

                return if (currentAccount == null){
                    val newAccount = Account(userData.userName, context.getString(accountTypeResId))
                    manager.addAccountExplicitly(newAccount, serializedUser, Bundle.EMPTY)
                }else{
                    manager.setPassword(currentAccount, serializedUser)
                    true
                }

            }catch (_: Exception){

            }

            return false
        }
    }

    @Throws(UnsupportedAccountTypeException::class)
    private fun validateAccountType(accountType: String){
        if (accountType != this.context.getString(this.accountTypeResourceId))
            throw UnsupportedAccountTypeException(this.context, this.unsupportedAccountTypeExceptionResId)
    }

    override fun addAccount(
        accountAuthResponse: AccountAuthenticatorResponse?,
        pAccountType: String?,
        pAuthTokenType: String?,
        pRequiredFeatures: Array<out String>?,
        pLoginOptions: Bundle?
    ): Bundle {
        if (this.onlyOneAccount && loadUserAccounts(this.context, this.accountTypeResourceId).isNotEmpty()){
            Toast.makeText(this.context, this.errorOnlyOneAccountAllowedMessageId, Toast.LENGTH_LONG).show()
            return Bundle.EMPTY
        }

        try{
            validateAccountType(pAccountType ?: this.context.getString(this.accountTypeResourceId))

            val createIntent = Intent(
                this.context,
                this.loginActivityClass
            ).apply {
                putExtra(AccountManager.KEY_ACCOUNT_MANAGER_RESPONSE, accountAuthResponse)
                putExtra(AccountManager.KEY_ACCOUNT_TYPE, this@Authenticator.context.getString(this@Authenticator.accountTypeResourceId));
                putExtra(KEY_AUTH_TOKEN_TYPE, pAuthTokenType);
                putExtra(KEY_LOGIN_OPTIONS, pLoginOptions);
                putExtra(Authenticator.ACTION_OPERATION, Operation.NewAccount);
                putExtra(Authenticator.ACTION_LOGIN_TYPE, LoginType.Authenticator);
            }

            return Bundle().apply {  putParcelable(AccountManager.KEY_INTENT, createIntent) }
        }catch (_: Exception){
            
        }

        return Bundle.EMPTY
    }

    @Throws(NetworkErrorException::class)
    override fun updateCredentials(
        accountAuthResponse: AccountAuthenticatorResponse?,
        account: Account?,
        authTokenType: String?,
        loginOptions: Bundle?
    ): Bundle {
        try{
            val updateIntent = Intent(
                this.context,
                this.loginActivityClass
            ).apply {
                putExtra(AccountManager.KEY_ACCOUNT_MANAGER_RESPONSE, accountAuthResponse)
                putExtra(AccountManager.KEY_ACCOUNT_TYPE, this@Authenticator.context.getString(this@Authenticator.accountTypeResourceId));
                putExtra(KEY_AUTH_TOKEN_TYPE, authTokenType);
                putExtra(KEY_LOGIN_OPTIONS, loginOptions);
                putExtra(Authenticator.ACTION_OPERATION, Operation.UpdateAccount);
                putExtra(Authenticator.ACTION_LOGIN_TYPE, LoginType.Authenticator);
                putExtra(Authenticator.KEY_ACCOUNT, account);
            }

            return Bundle().apply {  putParcelable(AccountManager.KEY_INTENT, updateIntent) }
        }catch (_: Exception){

        }

        return Bundle.EMPTY
    }

    //region Unnecessary but overridable functions

    override fun editProperties(p0: AccountAuthenticatorResponse?, p1: String?): Bundle? = null

    override fun confirmCredentials(p0: AccountAuthenticatorResponse?, p1: Account?, p2: Bundle?): Bundle? = null

    override fun getAuthToken(p0: AccountAuthenticatorResponse?, p1: Account?, p2: String?, p3: Bundle?): Bundle? = null

    override fun getAuthTokenLabel(p0: String?): String? = null

    override fun hasFeatures(p0: AccountAuthenticatorResponse?, p1: Account?, p2: Array<out String>?): Bundle? = null

    //endregion
}