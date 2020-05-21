package com.material.labs.codeedit

import android.content.Context
import java.io.File
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

data class RemoteInfo(var alias: String, var hostname: String, var username: String, var port: Int = 22) {

    // All these functions are static for ease of use
    companion object {

        // Read saved servers from storage
        fun get(context: Context) : MutableList<RemoteInfo> {
            val directory = File(context.filesDir.toString() + "/servers/")
            val fileList = directory.listFiles()
            val serverList = mutableListOf<RemoteInfo>()

            fileList?.forEach { file ->
                val inputStream = context.openFileInput(file.toString())
                val objectInputStream = ObjectInputStream(inputStream)
                val tmp: RemoteInfo = objectInputStream.readObject() as RemoteInfo

                serverList.add(tmp)
            }

            return serverList
        }

        // The following functions return false if something failed and there was an error

        // Save the given server info to a file
        fun save(info: RemoteInfo, context: Context) : Boolean {
            val file = File(context.filesDir.toString() + "/servers/" + info.alias)

            // Do not attempt to save the info if a server with the same alias already exists
            if (file.exists()) throw IOException("Server with this alias already exists")

            try {
                val outputStream = context.openFileOutput(file.toString(), Context.MODE_PRIVATE)
                val objectOutputStream = ObjectOutputStream(outputStream)
                objectOutputStream.writeObject(info)
            } catch (e: IOException) {
                e.printStackTrace()
                return false
            }

            return true
        }

        // Delete a server entry
        fun delete(alias: String, context: Context) : Boolean {
            val file = File(context.filesDir.toString() + "/servers/" + alias)

            // Just make sure it exists
            if (!file.exists()) return false

            return file.delete()
        }

        fun update(aliasOld: String, info: RemoteInfo, context: Context) : Boolean {
            val fileOld = File(context.filesDir.toString() + "/servers/" + aliasOld)
            val fileNew = File(context.filesDir.toString() + "/servers/" + info.alias)

            // Create new file and save
            val outputStream = context.openFileOutput(fileNew.toString(), Context.MODE_PRIVATE)
            val objectOutputStream = ObjectOutputStream(outputStream)
            objectOutputStream.writeObject(info)

            // Check if it saved correctly and delete old file
            return if (fileNew.exists()) {
                fileOld.delete()
                true
            } else {
                false
            }
        }

    }

}