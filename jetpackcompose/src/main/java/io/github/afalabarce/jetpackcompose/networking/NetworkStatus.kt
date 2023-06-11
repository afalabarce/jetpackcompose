package io.github.afalabarce.jetpackcompose.networking

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

sealed class NetworkConnectionType {
    object Unknown: NetworkConnectionType()
    object ConnectionCellular: NetworkConnectionType()
    object Connection3G: NetworkConnectionType()
    object Connection4G: NetworkConnectionType()
    object Connection5G: NetworkConnectionType()
    object ConnectionWifi: NetworkConnectionType()
}
sealed class NetworkStatus{
    object Available : NetworkStatus(){
        var connectionType: NetworkConnectionType = NetworkConnectionType.Connection3G
    }
    object Unavailable : NetworkStatus()
}

inline fun <Result> Flow<NetworkStatus>.map(
    crossinline onUnavailable: suspend () -> Result,
    crossinline onAvailable: suspend () -> Result,
): Flow<Result> = map { status ->
    when (status) {
        NetworkStatus.Unavailable -> onUnavailable()
        NetworkStatus.Available -> onAvailable()
    }
}