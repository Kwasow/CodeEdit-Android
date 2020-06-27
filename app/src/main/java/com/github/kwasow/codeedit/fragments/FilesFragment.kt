package com.github.kwasow.codeedit.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

import com.github.kwasow.codeedit.databinding.FragmentFilesBinding
import com.github.kwasow.codeedit.views.FilesView

class FilesFragment : Fragment() {
    private lateinit var layoutBinding: FragmentFilesBinding
    lateinit var files: FilesView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        layoutBinding = FragmentFilesBinding.inflate(layoutInflater)
        files = layoutBinding.filesView
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        files.open()

        return layoutBinding.root
    }

    // TODO: This has to move to the fragment activity
    /*
        override fun onBackPressed() {
            // If we are not at ./ then navigate back in the files tree
            if (filesView.path == "./") {
                super.onBackPressed()
            } else {
                filesView.goBack()
            }
        }
    */
    override fun onDestroy() {
        super.onDestroy()

        files.onDestroy()
    }

}