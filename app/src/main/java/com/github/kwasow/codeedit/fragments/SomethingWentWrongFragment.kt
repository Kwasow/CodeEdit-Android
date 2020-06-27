package com.github.kwasow.codeedit.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.kwasow.codeedit.databinding.FragmentSomethingWrongBinding

class SomethingWentWrongFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentSomethingWrongBinding.inflate(inflater).root
    }

}