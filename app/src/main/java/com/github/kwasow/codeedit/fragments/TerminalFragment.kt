package com.github.kwasow.codeedit.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

import com.github.kwasow.codeedit.databinding.FragmentTerminalBinding
import com.github.kwasow.codeedit.views.TerminalView

class TerminalFragment : Fragment() {
    private lateinit var layoutBinding: FragmentTerminalBinding
    private lateinit var terminalView: TerminalView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        layoutBinding = FragmentTerminalBinding.inflate(layoutInflater)
        terminalView = layoutBinding.terminalView
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        terminalView.open()

        return layoutBinding.root
    }

    override fun onDestroy() {
        super.onDestroy()

        terminalView.onDestroy()
    }
}