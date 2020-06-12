package com.material.labs.codeedit.utils

import android.util.Log
import java.lang.Exception

// Used for easier management of logtags
class CodeLogger {

    // Everything is static
    companion object {
        private const val logTag = "CodeEdit"
        private const val logTagSSH = logTag + "SSH"

        fun logD(s: String) {
            Log.d(logTag, s)
        }

        fun logE(e: Exception) {
            Log.e(logTag, e.toString())
            e.printStackTrace()
        }

        fun logI(s: String) {
            Log.i(logTag, s)
        }

        fun logSSH(s: String) {
            Log.i(logTagSSH, s)
        }
    }

}