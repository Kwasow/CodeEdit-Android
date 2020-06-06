package com.material.labs.codeedit.views

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import android.widget.ScrollView
import com.material.labs.codeedit.CodeError

import com.material.labs.codeedit.R
import com.trilead.ssh2.Connection
import com.trilead.ssh2.ConnectionInfo
import com.trilead.ssh2.Session

import kotlinx.android.synthetic.main.terminal_view.view.*

import java.io.IOException
import java.io.InputStream

// The terminal view manages everything about the ssh session
// Both input, running commands and throwing their output
class TerminalView(context: Context, attrs: AttributeSet) : ScrollView(context, attrs) {
    private lateinit var connectionInfo: ConnectionInfo

    // Open view
    init {
        inflate(context, R.layout.terminal_view, this)
        this.rootView.setBackgroundColor(Color.BLACK)
    }

    // Connect to new ssh session
    fun connect(hostname: String, port: Int = 22) {
        val thread = Thread {
            val connection = Connection(hostname, port)
            val currentSession: Session
            val sessionOut: InputStream
            try {
                connectionInfo = connection.connect()
                connection.authenticateWithPassword("test", "test_password")
                currentSession = connection.openSession()
                sessionOut = currentSession.stdout
                currentSession.execCommand("ping -c 20 www.google.com")

                var x = sessionOut.read()
                while (x != -1) {
                    val c = x.toChar()
                    Handler(Looper.getMainLooper()).post {
                        this.rootView.textView.append(c.toString())
                        this.rootView.post {
                            fullScroll(View.FOCUS_DOWN)
                        }
                    }

                    x = sessionOut.read()
                }

                currentSession.close()
                connection.close()
            } catch (e: IOException) {
                CodeError.logE(e)
                Handler(Looper.getMainLooper()).post {
                    this.rootView.textView.text = e.toString()
                    this.rootView.post {
                        fullScroll(View.FOCUS_DOWN)
                    }
                }
            }
        }

        thread.start()
    }

}
