package com.material.labs.codeedit.views

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.widget.ScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.material.labs.codeedit.R
import com.material.labs.codeedit.adapters.FilesAdapter
import com.material.labs.codeedit.models.FileDetails
import com.material.labs.codeedit.utils.CodeLogger
import com.material.labs.codeedit.utils.NetworkState

import com.trilead.ssh2.Connection
import com.trilead.ssh2.ConnectionInfo
import com.trilead.ssh2.Session
import kotlinx.android.synthetic.main.view_terminal.view.*
import java.io.IOException

import java.io.InputStream
import java.io.OutputStream

// The files view manages getting the files list, navigating through
// the filesystem and managing it's own ssh session
class FilesView(context: Context, attrs: AttributeSet) : ScrollView(context, attrs) {
    private var connection: Connection? = null
    private var connectionInfo: ConnectionInfo? = null
    private var session: Session? = null

    private var stdout: InputStream? = null
    private var stdin: OutputStream? = null
    private var stderr: InputStream? = null

    private lateinit var recyclerView: RecyclerView
    private var viewManager: RecyclerView.LayoutManager

    init {
        inflate(context, R.layout.view_files, this)

        viewManager = LinearLayoutManager(context)
    }

    fun connect(hostname: String, user: String, password: String, port: Int = 22) {

        val readThread = Thread {
            var returnString = ""

            var x = stdout?.read()
            while (x != -1) {
                returnString += x?.toChar()
                x = stdout?.read()
            }

            // Update files list when the command output ends
            updateAdapter(returnString)
        }

        val connectionThread = Thread {
            connection = Connection(hostname, port)
            try {

                connectionInfo = connection?.connect()
                connection?.authenticateWithPassword(user, password)
                session = connection?.openSession()

                stdout = session?.stdout // InputStream
                stdin = session?.stdin // OutputStreams
                stderr = session?.stderr // InputStream

                CodeLogger.logD("Connected")
                // This will list all files as well as their type
                session?.execCommand("file .* *")

                readThread.start()

            } catch (e: IOException) {
                CodeLogger.logE(e)
                CodeLogger.errorDialog(context, e)
            }
        }

        // Check if we are on a WIFI/Ethernet network
        if ((NetworkState.type(context) == NetworkState.State.WIFI) or
            (NetworkState.type(context) == NetworkState.State.ETHERNET)) {
            connectionThread.start()
        } else {
            val builder = AlertDialog.Builder(context)
            var message = resources.getString(R.string.network_not_wifi)
            message += " " + when (NetworkState.type(context)) {
                NetworkState.State.MOBILE ->
                    resources.getString(R.string.on_mobile)
                NetworkState.State.OFFLINE ->
                    resources.getString(R.string.offline)
                // If error or something else
                else ->
                    resources.getString(R.string.something_wrong)
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

    private fun updateAdapter(files: String) {
        val filesArray = mutableListOf<FileDetails>()
        files.split(Regex("\n")).forEach foreach@{
            if (it.isNotEmpty()) {
                val tmp = it.split(Regex(":[ ]+"), 2)

                // Ignore current folder
                if (tmp[0] == ".") {
                    return@foreach
                }

                // If one of the "files" is * then it means that there was an error
                // `*: cannot open '*' (No such file or directory)`
                if (tmp[1].contains("*")) {
                    return@foreach
                }

                // TODO: Can this somehow be a when statement?
                val tmpType: FileDetails.Type = if (tmp[1].contains("directory")) {
                    FileDetails.Type.DIRECTORY
                } else if (tmp[1].contains("text")) {
                    FileDetails.Type.TEXT
                } else if (tmp[1].contains("executable") || tmp[1].contains("script")) {
                    FileDetails.Type.BINARY
                } else {
                    FileDetails.Type.OTHER
                }
                filesArray.add(FileDetails(tmp[0]/*.removeSuffix(":")*/, tmpType))
            }
        }

        // Run on UI thread (this function is called from the readThread)
        Handler(Looper.getMainLooper()).post {
            recyclerView = findViewById<RecyclerView>(R.id.filesRecyclerView).apply {
                layoutManager = viewManager
                adapter = FilesAdapter(filesArray)
            }
        }
    }

    fun onDestroy() {
        Thread {
            session?.close()
            connection?.close()
        }.start()
        stdin?.close()
        stdout?.close()
        stderr?.close()
    }

}