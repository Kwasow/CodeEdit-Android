package com.github.kwasow.codeedit.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build

class NetworkState {

    enum class State {
        ETHERNET, WIFI, MOBILE, OFFLINE, ERROR
    }

    companion object {

        @Suppress("DEPRECATION")
        // NetworkInfo is deprecated, but only on SDK 23+, where the new method is being used
        fun type(context: Context): State {
            val connectivityManager: ConnectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // The function returns null if there are no active networks
                val network = connectivityManager.activeNetwork
                    ?: return State.OFFLINE
                // Something must have gone wrong if this is null
                val capabilities = connectivityManager.getNetworkCapabilities(network)
                    ?: return State.ERROR

                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    return State.ETHERNET
                }

                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    return State.WIFI
                }

                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    return State.MOBILE
                }

                return State.ERROR
            } else {
                // Return offline if null
                val info: NetworkInfo = connectivityManager.activeNetworkInfo
                    ?: return State.OFFLINE
                return when (info.type) {
                    ConnectivityManager.TYPE_ETHERNET -> State.ETHERNET
                    ConnectivityManager.TYPE_WIFI or ConnectivityManager.TYPE_VPN -> State.WIFI
                    ConnectivityManager.TYPE_MOBILE -> State.MOBILE
                    // I don't know what to do with these networks
                    else -> State.ERROR
                }
            }
        }
    }
}
