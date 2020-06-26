package com.github.kwasow.codeedit.utils

import android.content.Context
import java.io.*

data class RemoteInfoManager(
    var alias: String,
    var hostname: String,
    var username: String,
    var os: String = "Not checked",
    var port: Int = 22
) : Serializable {

    // All these functions are static for ease of use
    companion object {

        // Read saved servers from storage
        fun get(context: Context) : MutableList<RemoteInfoManager> {
            val directory = File(context.filesDir.toString() + "/servers/")
            val fileList = directory.listFiles()
            val serverList = mutableListOf<RemoteInfoManager>()

            fileList?.forEach { file ->
                val inputStream = FileInputStream(file.toString())
                val objectInputStream = ObjectInputStream(inputStream)
                val tmp: RemoteInfoManager = objectInputStream.readObject() as RemoteInfoManager

                serverList.add(tmp)

                objectInputStream.close()
                inputStream.close()
            }

            return serverList
        }
    }

    // The following functions return false if something failed and there was an error

    // Save the given server info to a file
    fun save(context: Context) : Boolean {
        val storageDir = File(context.filesDir.toString() + "/servers/")
        val file = File(storageDir.toString() + "/" + toFilename(alias))

        storageDir.mkdirs()

        // Do not attempt to save the info if a server with the same alias already exists
        if (file.exists()) throw IOException("Server with this alias already exists")

        try {
            val outputStream = FileOutputStream(file)
            val objectOutputStream = ObjectOutputStream(outputStream)
            objectOutputStream.writeObject(this)
            objectOutputStream.close()
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }

        return true
    }

    // Delete a server entry
    fun delete(context: Context) : Boolean {
        val file = File(context.filesDir.toString() + "/servers/" + toFilename(alias))

        // Just make sure it exists
        if (!file.exists()) return false

        return file.delete()
    }

    fun update(info: RemoteInfoManager, context: Context) : Boolean {
        val fileOld = File(context.filesDir.toString() + "/servers/" + toFilename(alias))
        val fileNew = File(context.filesDir.toString() + "/servers/" + toFilename(info.alias))

        // Create new file and save
        // This may overwrite the old file if alias stays the same
        val outputStream = FileOutputStream(fileNew)
        val objectOutputStream = ObjectOutputStream(outputStream)
        objectOutputStream.writeObject(info)

        // Check if it saved correctly and delete old file
        return if (fileNew.exists()) {
            // Only if alias has changed
            if (toFilename(alias) != toFilename(info.alias)) {
                fileOld.delete()
            }
            true
        } else {
            false
        }
    }

    private fun toFilename(dir: String): String? {
        return dir
            .replace(" ", "")
            .replace(".", "")
            .replace("?", "")
            .replace("'", "")
            .replace("/", "")
            .replace(":", "")
            .replace("-", "") + ".remoteInfo"
    }
}