package com.github.kwasow.codeedit.views

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.AttributeSet
import android.widget.EditText
import android.widget.LinearLayout

import com.github.kwasow.codeedit.R
import com.github.kwasow.codeedit.utils.ConnectionService
import com.trilead.ssh2.Session

import kotlinx.android.synthetic.main.view_editor.view.*

import java.io.InputStream
import java.io.OutputStream

// TODO: Add loading indicator
class EditorView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    private var session: Session? = null

    private var stdout: InputStream? = null
    private var stdin: OutputStream? = null
    private var stderr: InputStream? = null

    private var editText: EditText

    private var currentFile = ""

    private var serviceIntent: Intent

    init {
        // Open view
        inflate(context, R.layout.view_editor, this)

        editText = this.rootView.mainTextEditor
        this.rootView.editorSaveButton.setOnClickListener {
            save()
        }
        this.rootView.editorCloseButton.setOnClickListener {
            close()
        }
        serviceIntent = Intent(context, ConnectionService::class.java)
    }

    private var connectionService: ConnectionService? = null
    private var isBound = false

    private var serviceConnectionOpen = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as ConnectionService.LocalBinder
            connectionService = binder.getService()
            isBound = true

            val readThread = Thread {
                var returnString = ""

                var x = stdout?.read()
                while (x != -1) {
                    returnString += x?.toChar()
                    x = stdout?.read()
                }

                // Close session after read is done and unbind service
                session?.close()
                context.unbindService(this)

                // Update edit text
                Handler(Looper.getMainLooper()).post {
                    editText.text.clear()
                    editText.text.append(returnString)
                }
            }

            Thread {
                session = connectionService?.newSession()

                stdout = session?.stdout // InputStream
                stdin = session?.stdin // OutputStreams
                stderr = session?.stderr // InputStream

                // This will print out the contents of the file
                session?.execCommand("cat $currentFile")

                readThread.start()
            }.start()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            connectionService = null
            isBound = false
        }
    }

    private var serviceConnectionSave = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as ConnectionService.LocalBinder
            connectionService = binder.getService()
            isBound = true

            Thread {
                session = connectionService?.newSession()

                // TODO: Escape characters
                // This will print the given string to file
                val newContents = editText.text.toString()
                val command = """printf "$newContents" > $currentFile"""
                session?.execCommand(command)

                session?.close()
                context.unbindService(this)
            }.start()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            connectionService = null
            isBound = false
        }
    }

    fun openFile(path: String) {
        // Only open if is not yet open
        if (currentFile != path) {
            currentFile = path
            context.bindService(serviceIntent, serviceConnectionOpen, Context.BIND_AUTO_CREATE)
            // Make editor visible
            this.rootView.noFileOpenText.visibility = GONE
            editText.visibility = VISIBLE
            this.rootView.buttonEditorLinear.visibility = VISIBLE
        }
    }

    private fun close() {
        currentFile = ""
        editText.text.clear()
        this.rootView.noFileOpenText.visibility = VISIBLE
        editText.visibility = GONE
        this.rootView.buttonEditorLinear.visibility = GONE
    }

    private fun save() {
        if (currentFile.isNotEmpty()) {
            context.bindService(serviceIntent, serviceConnectionSave, Context.BIND_AUTO_CREATE)
        }
    }

}