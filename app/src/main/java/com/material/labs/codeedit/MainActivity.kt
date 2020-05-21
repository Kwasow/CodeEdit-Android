package com.material.labs.codeedit

import androidx.appcompat.app.AppCompatActivity

import android.os.Bundle

import com.material.labs.codeedit.databinding.ActivityMainBinding

import com.trilead.ssh2.ConnectionInfo

class MainActivity : AppCompatActivity() {
    lateinit var layoutBinding: ActivityMainBinding
    var outString = ""
    lateinit var connectionInfo: ConnectionInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        layoutBinding = ActivityMainBinding.inflate(layoutInflater)

        val actionBar = supportActionBar
        actionBar?.title = "Servers"

        layoutBinding.addServerFab.setOnClickListener {
            // Open a add server activity
        }
/*
        var thread = Thread {
            var connection = Connection("192.168.0.10")
            var currentSession: Session
            var sessionOut: InputStream
            try {
                connectionInfo = connection.connect()
                connection.authenticateWithPassword("test", "test_password")
                currentSession = connection.openSession()
                sessionOut = currentSession.stdout
                currentSession.execCommand("ping -c 3 www.google.com")

                var x = sessionOut.read()
                while (x != -1) {
                    var c  = x.toChar()
                    outString += c

                    x = sessionOut.read()
                }

                currentSession.close()
                connection.close()
            } catch (e: IOException) {
                CodeError.logE(e)
                outString = e.toString()
            }
        }

        thread.start()

        layoutBinding.text.text = outString
*/

        setContentView(layoutBinding.root)
    }
}
