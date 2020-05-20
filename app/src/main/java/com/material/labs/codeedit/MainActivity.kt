package com.material.labs.codeedit

import androidx.appcompat.app.AppCompatActivity

import android.os.Bundle
import android.util.Base64

import com.material.labs.codeedit.databinding.ActivityMainBinding

import com.trilead.ssh2.Connection
import com.trilead.ssh2.ConnectionInfo
import com.trilead.ssh2.KnownHosts
import com.trilead.ssh2.Session
import java.io.IOException
import java.io.InputStream

class MainActivity : AppCompatActivity() {
    lateinit var layoutBinding: ActivityMainBinding
    var outString = ""
    lateinit var connectionInfo: ConnectionInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        layoutBinding = ActivityMainBinding.inflate(layoutInflater)
        layoutBinding.text.text = ""

        layoutBinding.buttonConect.setOnClickListener {
            var thread = Thread {
                var connection = Connection("192.168.0.10")
                var currentSession: Session
                var sessionOut: InputStream
                try {
                    connectionInfo = connection.connect()
                    print(connectionInfo.serverHostKey)
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
        }

        // Is this really needed? It should exist for security, but the default implementation doesn't seem to check for known hosts
        layoutBinding.buttonAddKnown.setOnClickListener {
            var knownHosts = KnownHosts()
            knownHosts.addHostkey(arrayOf("192.168.0.10"), connectionInfo.serverHostKeyAlgorithm, connectionInfo.serverHostKey);
        }

        setContentView(layoutBinding.root)
    }
}
