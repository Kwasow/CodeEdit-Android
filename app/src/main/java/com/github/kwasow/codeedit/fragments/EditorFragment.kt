package com.github.kwasow.codeedit.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.kwasow.codeedit.databinding.FragmentEditorBinding

// TODO: Code editor
// This is the fragment that contains one more ViewPager that holds all the open files in a browser-like
// tab view
class EditorFragment : Fragment() {
    private lateinit var layoutBinding: FragmentEditorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        layoutBinding = FragmentEditorBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return layoutBinding.root
    }
}