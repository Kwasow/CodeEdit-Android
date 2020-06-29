package com.github.kwasow.codeedit.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2

import com.github.kwasow.codeedit.databinding.FragmentFilesBinding
import com.github.kwasow.codeedit.views.EditorView
import com.github.kwasow.codeedit.views.FilesView

class FilesFragment(val ideViewPager: ViewPager2) : Fragment() {
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
        files.open(ideViewPager)

        return layoutBinding.root
    }

    fun setEditor(editor: EditorView) {
        files.editorView = editor
    }

    override fun onDestroy() {
        super.onDestroy()

        files.onDestroy()
    }

}