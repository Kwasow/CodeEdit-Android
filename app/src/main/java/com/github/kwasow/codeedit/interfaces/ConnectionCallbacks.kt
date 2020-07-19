package com.github.kwasow.codeedit.interfaces

interface ConnectionCallbacks {
    fun onConnected() {}
    fun onDisconnected() {}
    fun onError(error: String) {}
    fun onServerOSUpdated(newOS: String) {}
}
