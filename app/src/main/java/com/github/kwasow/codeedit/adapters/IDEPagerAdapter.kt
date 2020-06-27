package com.github.kwasow.codeedit.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.github.kwasow.codeedit.fragments.EditorFragment
import com.github.kwasow.codeedit.fragments.FilesFragment
import com.github.kwasow.codeedit.fragments.SomethingWentWrongFragment
import com.github.kwasow.codeedit.fragments.TerminalFragment

class IDEPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    var filesFragment: FilesFragment = FilesFragment()
    var editorFragment: EditorFragment = EditorFragment()
    var terminalFragment: TerminalFragment = TerminalFragment()

    // 0 - Files
    // 1 - (default) Editor
    // 2 - Terminal
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> filesFragment
            1 -> editorFragment
            2 -> terminalFragment
            else -> SomethingWentWrongFragment()
        }
    }
}