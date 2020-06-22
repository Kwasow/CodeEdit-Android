package com.material.labs.codeedit.interfaces

interface ConnectionCallbacks {
    fun onConnected()
    fun onDisconnected()
    fun onError(error: String)
}