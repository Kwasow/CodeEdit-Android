package com.github.kwasow.codeedit

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.github.kwasow.codeedit.databinding.ActivityServerAddBinding
import com.github.kwasow.codeedit.utils.RemoteInfoManager

class ServerAddActivity : AppCompatActivity() {
    private lateinit var layoutBinding: ActivityServerAddBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        layoutBinding = ActivityServerAddBinding.inflate(layoutInflater)

        setContentView(layoutBinding.root)
    }

    fun save(v: View) {
        // Get details, save them and connect
        val info = RemoteInfoManager(
            layoutBinding.inputAlias.text.toString(),
            layoutBinding.inputHostname.text.toString(),
            layoutBinding.inputUsername.text.toString()
        )
        info.save(this)
        finish()
    }

    fun cancel(v: View) {
        // End this activity and go back to server list
        finish()
    }
}