package com.material.labs.codeedit

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.material.labs.codeedit.adapters.ServersAdapter

import com.material.labs.codeedit.databinding.ActivityMainBinding
import com.material.labs.codeedit.utils.RemoteInfoManager

class MainActivity : AppCompatActivity() {
    private lateinit var layoutBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        layoutBinding = ActivityMainBinding.inflate(layoutInflater)

        // Load servers
        updateServerList()

        setContentView(layoutBinding.root)
    }

    override fun onResume() {
        super.onResume()

        // Check if server list changed
        updateServerList()
    }

    fun updateServerList() {
        val servers = RemoteInfoManager.get(this)
        // Check if there are any servers saved
        if (servers.isEmpty()) {
            layoutBinding.noServersText.visibility = View.VISIBLE
        } else {
            layoutBinding.serverListRecyclerView.apply {
                layoutManager = LinearLayoutManager(this@MainActivity)
                adapter = ServersAdapter(servers)
                visibility = View.VISIBLE
            }
        }
    }

    fun addServer(v: View) {
        val intent = Intent(this, ServerAddActivity::class.java)
        startActivity(intent)
    }
}
