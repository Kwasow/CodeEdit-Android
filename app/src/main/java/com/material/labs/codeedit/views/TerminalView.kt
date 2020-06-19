package com.material.labs.codeedit.views

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import com.material.labs.codeedit.utils.CodeLogger

import com.material.labs.codeedit.R
import com.material.labs.codeedit.utils.NetworkState

import com.trilead.ssh2.Connection
import com.trilead.ssh2.ConnectionInfo
import com.trilead.ssh2.Session

import kotlinx.android.synthetic.main.terminal_view.view.*

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

// The terminal view manages everything about the ssh session
// Both input, running commands and throwing their output
class TerminalView(context: Context, attrs: AttributeSet) : ScrollView(context, attrs) {
    private lateinit var connection: Connection
    private lateinit var connectionInfo: ConnectionInfo
    private lateinit var session: Session

    private lateinit var stdout: InputStream
    private lateinit var stdin: OutputStream
    private lateinit var stderr: InputStream

    // Open view
    init {
        inflate(context, R.layout.terminal_view, this)
        this.rootView.setBackgroundColor(Color.BLACK)

        this.rootView.buttonDisconnect.setOnClickListener {
            Thread {
                session.close()
                connection.close()
            }.start()

            // Inform the user
            this.rootView.textView.append("\nConnection terminated")
            this.rootView.post {
                fullScroll(View.FOCUS_DOWN)
            }

            this.rootView.buttonCommand.isEnabled = false
            this.rootView.buttonDisconnect.isEnabled = false
        }

        this.rootView.buttonCommand.setOnClickListener {
            val alert = Dialog(context)
            alert.setContentView(R.layout.dialog_command)
            val commandInput = alert.findViewById<EditText>(R.id.commandInput)
            val buttonExecute = alert.findViewById<Button>(R.id.buttonExecute)
            buttonExecute.setOnClickListener {
                val command = commandInput.text.toString()
                Thread {
                    stdin.write(command.toByteArray())
                    stdin.write(30)
                }.start()
                alert.hide()
            }
            alert.show()
        }
    }

    // Connect to new ssh session
    fun connect(hostname: String, user: String, password: String, port: Int = 22) {

        // This thread listens to updates on the sessions output
        val readThread = Thread {
            var x = stdout.read()
            while (x != -1) {
                val c = x.toChar()
                Handler(Looper.getMainLooper()).post {
                    this.rootView.textView.append(c.toString())
                    this.rootView.post {
                        fullScroll(View.FOCUS_DOWN)
                    }
                }

                x = stdout.read()
            }
        }

        // The main network (session) thread
        val connectionThread = Thread {
            connection = Connection(hostname, port)
            try {

                connectionInfo = connection.connect()
                connection.authenticateWithPassword(user, password)
                session = connection.openSession()

                // TODO: Try xterm-256color in the future
                session.requestDumbPTY()
                session.startShell()

                stdout = session.stdout // InputStream
                stdin = session.stdin // OutputStreams
                stderr = session.stderr // InputStream

                CodeLogger.logD("Connected")

                readThread.start()

                // UI stuff has to run on the main (UI) thread
                Handler(Looper.getMainLooper()).post {
                    this.rootView.buttonCommand.isEnabled = true
                    this.rootView.buttonDisconnect.isEnabled = true
                }

            } catch (e: IOException) {
                CodeLogger.logE(e)
                Handler(Looper.getMainLooper()).post {
                    this.rootView.textView.text = e.toString()
                    this.rootView.post {
                        fullScroll(View.FOCUS_DOWN)
                    }
                }
            }
        }

        // Check if we are on a WIFI/Ethernet network
        if ((NetworkState.type(context) == NetworkState.State.WIFI) or
            (NetworkState.type(context) == NetworkState.State.ETHERNET)) {
            connectionThread.start()
        } else {
            val builder = AlertDialog.Builder(context)
            var message = resources.getString(R.string.network_not_wifi)
            message += when (NetworkState.type(context)) {
                NetworkState.State.MOBILE ->
                    " " + resources.getString(R.string.on_mobile)
                NetworkState.State.OFFLINE ->
                    " " + resources.getString(R.string.offline)
                // If error or something else
                else ->
                    " " + resources.getString(R.string.something_wrong)
            }

            builder.setMessage(message)
            builder.setTitle(R.string.alert)
            builder.setPositiveButton(R.string.button_continue) { _: DialogInterface, _: Int ->
                connectionThread.start()
            }
            builder.setNegativeButton(R.string.button_cancel) { _: DialogInterface, _: Int ->
                this.rootView.textView.append(
                    resources.getString(R.string.connection_interrupted_user))
            }

            builder.show()
        }
    }
}
