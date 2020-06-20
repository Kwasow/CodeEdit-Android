package com.material.labs.codeedit.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView

import com.material.labs.codeedit.R
import com.material.labs.codeedit.models.FileDetails
import com.material.labs.codeedit.utils.CodeLogger
import com.material.labs.codeedit.views.FilesView

class FilesAdapter(private val dataset: MutableList<FileDetails>, private val filesView: FilesView) :
    RecyclerView.Adapter<FilesAdapter.ViewHolder>() {

    init {
        // Sort to be grouped by types (directories, text, binary, other)
        dataset.sort()

        // Use unique IDs
        setHasStableIds(true)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var filename: TextView = itemView.findViewById(R.id.fileName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val fileView = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_file, parent, false)

        return ViewHolder(fileView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.filename.text = dataset[position].name
        when (dataset[position].type) {
            FileDetails.Type.DIRECTORY -> holder.filename
                .setCompoundDrawablesRelativeWithIntrinsicBounds(
                    R.drawable.ic_file_directory,
                    0, 0, 0)
            FileDetails.Type.TEXT -> holder.filename
                .setCompoundDrawablesRelativeWithIntrinsicBounds(
                    R.drawable.ic_file_text,
                    0, 0, 0)
            FileDetails.Type.BINARY -> holder.filename
                .setCompoundDrawablesRelativeWithIntrinsicBounds(
                    R.drawable.ic_file_executable,
                    0, 0, 0)
            FileDetails.Type.OTHER -> holder.filename
                .setCompoundDrawablesRelativeWithIntrinsicBounds(
                    R.drawable.ic_file_other,
                    0, 0, 0)
        }
        holder.itemView.setOnClickListener {
            if (dataset[position].type == FileDetails.Type.DIRECTORY) {
                filesView.updatePath(dataset[position].name)
            }
        }
    }

    override fun getItemCount(): Int {
        return dataset.size
    }
}