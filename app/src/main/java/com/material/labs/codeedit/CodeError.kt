package com.material.labs.codeedit

import android.util.Log
import java.lang.Exception

class CodeError {

    companion object {
        private const val logTag = "CodeEdit"

        fun logE(e: Exception) {
            Log.e(logTag, e.toString())
        }

        fun logI(s: String) {
            Log.i(logTag, s)
        }
    }

}