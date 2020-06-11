package com.material.labs.codeedit.views

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import com.material.labs.codeedit.CodeLogger

import com.material.labs.codeedit.R

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

    private var update = true

    // Open view
    init {
        inflate(context, R.layout.terminal_view, this)
        this.rootView.setBackgroundColor(Color.BLACK)

        this.rootView.buttonDisconnect.setOnClickListener {
            Thread {
                session.close()
                connection.close()
            }.start()
            update = false

            // Inform the user
            this.rootView.textView.append("\nConnection terminated")
            this.rootView.post {
                fullScroll(View.FOCUS_DOWN)
            }
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
    fun connect(hostname: String, port: Int = 22) {

        val readThread = Thread {
            while (update) {
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
        }

        val connectionThread = Thread {
            connection = Connection(hostname, port)
            try {
                connectionInfo = connection.connect()
                connection.authenticateWithPassword("test", "test_password")
                session = connection.openSession()

                // TODO: Try xterm-256color in the future
                session.requestDumbPTY()
                session.startShell()

                stdout = session.stdout // InputStream
                stdin = session.stdin // OutputStreams
                stderr = session.stderr // InputStream

                CodeLogger.logD("Connected")

                readThread.start()

                //newPrompt()
                //execute("exit")
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

        // TODO: check if wifi

        connectionThread.start()
    }

    // TODO: Execute `echo $USER@$HOSTNAME: ` somehow
    private fun newPrompt() {
        /*
        val sessionOut = session.stdout
        val sessionIn = session.stdin

        PrintWriter(sessionIn).println("echo test")

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

         */

        //val inputField = EditText(this.context)
        //inputField.textCursorDrawable = this.context.getDrawable(R.drawable.terminal_cursor)
    }

    private fun execute(command: String) {
        session.execCommand(command)
    }

    private fun update(s: String) {
        this.rootView.textView.append(s)
    }
}
