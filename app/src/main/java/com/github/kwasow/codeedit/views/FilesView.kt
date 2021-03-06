package com.github.kwasow.codeedit.views

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.AttributeSet
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.github.kwasow.codeedit.R
import com.github.kwasow.codeedit.adapters.FilesAdapter
import com.github.kwasow.codeedit.models.FileDetails
import com.github.kwasow.codeedit.utils.ConnectionService
import com.trilead.ssh2.Session
import kotlinx.android.synthetic.main.view_files.view.*
import kotlinx.android.synthetic.main.view_terminal.view.*
import java.io.InputStream
import java.io.OutputStream

// The files view manages getting the files list, navigating through
// the filesystem and managing it's own ssh session
class FilesView(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {
    private var session: Session? = null

    private var stdout: InputStream? = null
    private var stdin: OutputStream? = null
    private var stderr: InputStream? = null

    private var recyclerView: RecyclerView? = null
    private var viewManager: RecyclerView.LayoutManager
    private var loadingIndicator: ProgressBar
    var editorView: EditorView? = null

    var path = "./"

    private var serviceIntent: Intent

    init {
        inflate(context, R.layout.view_files, this)

        loadingIndicator = this.rootView.loadingIndicator
        viewManager = LinearLayoutManager(context)
        serviceIntent = Intent(context, ConnectionService::class.java)
    }

    private var connectionService: ConnectionService? = null
    private var isBound = false

    private val serviceConnection = object : ServiceConnection {
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

                // Update files list when the command output ends
                updateAdapter(returnString)
            }

            Thread {
                session = connectionService?.newSession()

                stdout = session?.stdout // InputStream
                stdin = session?.stdin // OutputStreams
                stderr = session?.stderr // InputStream

                // This will list all files as well as their type
                session?.execCommand("cd $path && file .* *")

                readThread.start()
            }.start()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            connectionService = null
            isBound = false
            this@FilesView.rootView.textView.append("[DEBUG]: Connection service unbound")
        }
    }

    private lateinit var ideViewPager: ViewPager2

    fun open(pager: ViewPager2) {
        ideViewPager = pager
        path = "./"
        cdFile()
    }

    private fun cdFile() {
        // Display loading indicator and hide file list
        // It'll be made visible again in `updateAdapter()`
        recyclerView?.visibility = GONE
        loadingIndicator.visibility = VISIBLE

        Thread {
            context.bindService(serviceIntent, serviceConnection, Context.BIND_IMPORTANT)
        }.start()
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

                val tmpType: FileDetails.Type = when {
                    tmp[1].contains("directory") -> FileDetails.Type.DIRECTORY
                    tmp[1].contains("text") -> FileDetails.Type.TEXT
                    tmp[1].contains("executable") -> FileDetails.Type.BINARY
                    else -> FileDetails.Type.OTHER
                }
                filesArray.add(FileDetails(tmp[0], tmpType))
            }
        }

        // Run on UI thread (this function is called from the readThread)
        Handler(Looper.getMainLooper()).post {
            recyclerView = findViewById<RecyclerView>(R.id.filesRecyclerView).apply {
                layoutManager = viewManager
                adapter = FilesAdapter(
                    filesArray,
                    this@FilesView,
                    editorView,
                    ideViewPager
                )
            }
            loadingIndicator.visibility = GONE
            recyclerView?.visibility = VISIBLE
        }
    }

    fun updatePath(folderName: String) {
        path += "$folderName/"
        cdFile()
    }

    fun goBack() {
        val pathList = path.split("/")
        path = ""
        pathList.subList(0, pathList.lastIndex - 1).forEach {
            path += "$it/"
        }
        cdFile()
    }

    fun onDestroy() {
        Thread {
            session?.close()
            stdin?.close()
            stdout?.close()
            stderr?.close()
        }.start()
    }
}
