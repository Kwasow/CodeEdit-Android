package com.github.kwasow.codeedit

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import com.github.kwasow.codeedit.databinding.ActivityFilesBinding
import com.github.kwasow.codeedit.views.FilesView

import kotlinx.android.synthetic.main.activity_files.*

class FilesActivity : AppCompatActivity() {
    private lateinit var layoutBinding: ActivityFilesBinding
    private lateinit var files: FilesView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        layoutBinding = ActivityFilesBinding.inflate(layoutInflater)
        files = layoutBinding.filesView
        files.open()

        setContentView(layoutBinding.root)
    }

    override fun onBackPressed() {
        // If we are not at ./ then navigate back in the files tree
        if (filesView.path == "./") {
            super.onBackPressed()
        } else {
            filesView.goBack()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        files.onDestroy()
    }

}