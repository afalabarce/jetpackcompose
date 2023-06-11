package io.github.afalabarce.jetpackcompose.networking

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.telephony.TelephonyManager
import android.telephony.TelephonyManager.NETWORK_TYPE_CDMA
import android.telephony.TelephonyManager.NETWORK_TYPE_HSDPA
import android.telephony.TelephonyManager.NETWORK_TYPE_HSPA
import android.telephony.TelephonyManager.NETWORK_TYPE_HSUPA
import android.telephony.TelephonyManager.NETWORK_TYPE_LTE
import android.telephony.TelephonyManager.NETWORK_TYPE_NR
import android.util.Log
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class NetworkStatusTracker(
    context: Context
) {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    val networkStatus = callbackFlow {
        val networkStatusCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onUnavailable() {
                trySend(NetworkStatus.Unavailable).isSuccess
            }

            override fun onAvailable(network: Network) {
                val availableNetwork = NetworkStatus.Available
                trySend(availableNetwork).isSuccess
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                this@callbackFlow.launch {
                    val isWifi = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)

                    val availableNetwork = NetworkStatus.Available.apply {
                        this.connectionType = if (isWifi)
                            NetworkConnectionType.ConnectionWifi
                        else {
                            if (
                                ActivityCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.READ_BASIC_PHONE_STATE
                                ) != PackageManager.PERMISSION_GRANTED &&
                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                NetworkConnectionType.ConnectionCellular
                            }else {
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU &&
                                    ActivityCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.READ_PHONE_STATE
                                    ) != PackageManager.PERMISSION_GRANTED){
                                    NetworkConnectionType.ConnectionCellular
                                }else {
                                    try {
                                        when (telephonyManager.dataNetworkType) {
                                            NETWORK_TYPE_CDMA, NETWORK_TYPE_HSDPA, NETWORK_TYPE_HSPA, NETWORK_TYPE_HSUPA -> NetworkConnectionType.Connection3G
                                            NETWORK_TYPE_LTE -> NetworkConnectionType.Connection4G
                                            NETWORK_TYPE_NR -> NetworkConnectionType.Connection5G
                                            else -> NetworkConnectionType.Unknown
                                        }
                                    } catch (ex: Exception) {
                                        Log.e("NetworkTracker", "${ex.message ?: ""}\n\t${ex.stackTraceToString()}")
                                        NetworkConnectionType.ConnectionCellular
                                    }
                                }
                            }
                        }
                    }

                    trySend(availableNetwork).isSuccess
                }
            }

            override fun onLost(network: Network) {
                trySend(NetworkStatus.Unavailable).isSuccess
            }
        }

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, networkStatusCallback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(networkStatusCallback)
        }
    }
}