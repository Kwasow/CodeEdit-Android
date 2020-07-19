package com.github.kwasow.codeedit.utils

import android.app.Dialog
import android.content.Context
import android.util.Log
import android.widget.TextView
import com.github.kwasow.codeedit.R
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

        fun logE(s: String) {
            Log.e(logTag, s)
        }

        fun logI(s: String) {
            Log.i(logTag, s)
        }

        fun logSSH(s: String) {
            Log.i(logTagSSH, s)
        }

        fun errorDialog(context: Context, e: Exception) {
            val dialog = Dialog(context)
            dialog.setContentView(R.layout.dialog_error)
            dialog.findViewById<TextView>(R.id.errorText).text = e.toString()
            dialog.show()
        }
    }
}
