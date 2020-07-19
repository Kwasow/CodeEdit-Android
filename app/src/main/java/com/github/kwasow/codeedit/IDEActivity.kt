package com.github.kwasow.codeedit

import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.github.kwasow.codeedit.adapters.IDEPagerAdapter
import com.github.kwasow.codeedit.databinding.ActivityIdeBinding
import com.github.kwasow.codeedit.interfaces.ConnectionCallbacks
import com.github.kwasow.codeedit.utils.ConnectionService

class IDEActivity : FragmentActivity() {
    private lateinit var layoutBinding: ActivityIdeBinding
    private lateinit var pagerAdapter: IDEPagerAdapter
    private lateinit var ideViewPager: ViewPager2

    private lateinit var serviceIntent: Intent

    private lateinit var connectionCallbacks: ConnectionCallbacks
    private var connectionService: ConnectionService? = null
    private var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as ConnectionService.LocalBinder
            connectionService = binder.getService()
            isBound = true

            connectionService?.addCallback(connectionCallbacks)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            connectionService = null
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        layoutBinding = ActivityIdeBinding.inflate(layoutInflater)

        connectionCallbacks = object : ConnectionCallbacks {
            override fun onDisconnected() {
                notifyClose()
            }

            override fun onError(error: String) {
                notifyClose()
            }
        }

        ideViewPager = layoutBinding.ideViewPager
        pagerAdapter = IDEPagerAdapter(this, ideViewPager)
        ideViewPager.apply {
            adapter = pagerAdapter
            currentItem = 1
        }

        serviceIntent = Intent(this, ConnectionService::class.java)
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)

        setContentView(layoutBinding.root)
    }

    fun notifyClose() {
        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle("Connection lost")
                .setMessage("The connection was interrupted")
                .setPositiveButton("OK") { dialogInterface: DialogInterface, _: Int ->
                    dialogInterface.dismiss()
                    finish()
                }
                .show()
        }
    }

    override fun onBackPressed() {
        // 0 - Files
        // 1 - (default) Editor
        // 2 - Terminal
        when (ideViewPager.currentItem) {
            0 -> {
                if (pagerAdapter.filesFragment.files.path == "./") {
                    ideViewPager.currentItem = 1
                } else {
                    pagerAdapter.filesFragment.files.goBack()
                }
            }
            1 -> super.onBackPressed()
            2 -> ideViewPager.currentItem = 1
            else -> super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (!isBound) {
            bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        }

        connectionService?.removeCallback(connectionCallbacks)
        unbindService(serviceConnection)
    }
}
