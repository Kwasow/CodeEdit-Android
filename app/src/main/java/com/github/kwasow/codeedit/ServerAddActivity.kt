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
            layoutBinding.inputPortSSH.text.clear()
            layoutBinding.inputPortSamba.text.clear()
            layoutBinding.inputPortSSH.append(details!!.sshPort.toString())
            layoutBinding.inputPortSamba.append(details!!.sambaPort.toString())
        }

        setContentView(layoutBinding.root)
    }

    fun save(v: View) {
        // Get details, save them and connect
        val info = RemoteInfoManager(
            layoutBinding.inputAlias.text.toString(),
            layoutBinding.inputHostname.text.toString(),
            layoutBinding.inputUsername.text.toString(),
            layoutBinding.inputPortSSH.text.toString().toInt(),
            layoutBinding.inputPortSamba.text.toString().toInt(),
        )
        if (details == null) {
            info.save(this)
        } else {
            info.os = details!!.os

            details!!.update(info, this)
        }
        finish()
    }

    fun cancel(v: View) {
        // End this activity and go back to server list
        finish()
    }
}
