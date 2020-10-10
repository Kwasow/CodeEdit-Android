package com.github.kwasow.codeedit

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.github.kwasow.codeedit.databinding.ActivityServerAddBinding
import com.github.kwasow.codeedit.utils.RemoteInfoManager

class ServerAddActivity : AppCompatActivity() {
    private lateinit var layoutBinding: ActivityServerAddBinding

    var details: RemoteInfoManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent?.getSerializableExtra("details") != null) {
            details = intent?.getSerializableExtra("details") as RemoteInfoManager
        }

        layoutBinding = ActivityServerAddBinding.inflate(layoutInflater)

        if (details != null) {
            layoutBinding.inputAlias.append(details!!.alias)
            layoutBinding.inputHostname.append(details!!.hostname)
            layoutBinding.inputUsername.append(details!!.username)
            layoutBinding.inputPort.text.clear()
            layoutBinding.inputPort.append(details!!.sshPort.toString())
        }

        setContentView(layoutBinding.root)
    }

    fun save(v: View) {
        // Get details, save them and connect
        val info = RemoteInfoManager(
            layoutBinding.inputAlias.text.toString(),
            layoutBinding.inputHostname.text.toString(),
            layoutBinding.inputUsername.text.toString()
        )
        if (details == null) {
            info.save(this)
        } else {
            info.os = details!!.os
            info.sshPort = layoutBinding.inputPort.text.toString().toInt()

            details!!.update(info, this)
        }
        finish()
    }

    fun cancel(v: View) {
        // End this activity and go back to server list
        finish()
    }
}
