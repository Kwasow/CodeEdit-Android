package com.github.kwasow.codeedit.views

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import android.widget.ScrollView
import com.github.kwasow.codeedit.R
import com.github.kwasow.codeedit.utils.ConnectionService
import com.trilead.ssh2.Session
import kotlinx.android.synthetic.main.view_terminal.view.*
import java.io.InputStream
import java.io.OutputStream

// The terminal view manages everything about the ssh session
// Both input, running commands and throwing their output
class TerminalView(context: Context, attrs: AttributeSet) : ScrollView(context, attrs) {
    private var session: Session? = null

    private var stdout: InputStream? = null
    private var stdin: OutputStream? = null
    private var stderr: InputStream? = null

    private var serviceIntent: Intent

    private var connectionService: ConnectionService? = null
    private var isBound = false

    // Open view
    init {
        inflate(context, R.layout.view_terminal, this)
        this.rootView.setBackgroundColor(Color.BLACK)

        serviceIntent = Intent(context, ConnectionService::class.java)
    }

    // This thread listens to updates on the sessions output
    val readThread = Thread {
        var x = stdout?.read()
        while (x != -1) {
            val c = x?.toChar()
            Handler(Looper.getMainLooper()).post {
                this@TerminalView.rootView.textView.append(c.toString())
                this@TerminalView.rootView.post {
                    fullScroll(View.FOCUS_DOWN)
                }
            }

            x = stdout?.read()
        }
        session?.close()
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as ConnectionService.LocalBinder
            connectionService = binder.getService()
            isBound = true

            // The main network (session) thread
            Thread {
                session = connectionService?.newSession()

                // TODO: Try xterm-256color in the future
                session?.requestDumbPTY()
                session?.startShell()

                stdout = session?.stdout // InputStream
                stdin = session?.stdin // OutputStreams
                stderr = session?.stderr // InputStream

                readThread.start()
            }.start()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            connectionService = null
            isBound = false
        }
    }

    // Connect to new ssh session
    fun open() {
        context.bindService(serviceIntent, serviceConnection, Context.BIND_IMPORTANT)
    }

    fun onDestroy() {
        Thread {
            session?.close()
            stdout?.close()
            stdin?.close()
            stderr?.close()
        }.start()

        if (isBound) {
            context.unbindService(serviceConnection)
        }
    }
}
