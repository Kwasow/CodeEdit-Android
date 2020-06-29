package com.github.kwasow.codeedit.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

import com.github.kwasow.codeedit.R
import com.github.kwasow.codeedit.models.FileDetails
import com.github.kwasow.codeedit.views.EditorView
import com.github.kwasow.codeedit.views.FilesView

class FilesAdapter(
    private val dataset: MutableList<FileDetails>,
    private val filesView: FilesView,
    private val editorView: EditorView?,
    private val ideViewPager: ViewPager2
) : RecyclerView.Adapter<FilesAdapter.ViewHolder>() {

    init {
        // Sort to be grouped by types (directories, text, binary, other)
        dataset.sort()
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
            when (dataset[position].type) {
                FileDetails.Type.DIRECTORY ->
                    filesView.updatePath(dataset[position].name)
                FileDetails.Type.TEXT -> {
                    editorView?.openFile(filesView.path + dataset[position].name)
                    // Move to editor page
                    ideViewPager.currentItem = 1
                }
                else -> {}
            }
        }
    }

    override fun getItemCount(): Int {
        return dataset.size
    }
}