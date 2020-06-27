package com.github.kwasow.codeedit

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.github.kwasow.codeedit.adapters.IDEPagerAdapter

import com.github.kwasow.codeedit.databinding.ActivityIdeBinding

class IDEActivity : FragmentActivity() {
    private lateinit var layoutBinding: ActivityIdeBinding
    private lateinit var pagerAdapter: IDEPagerAdapter

    // TODO: Connect to service and add callbacks
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        layoutBinding = ActivityIdeBinding.inflate(layoutInflater)

        pagerAdapter = IDEPagerAdapter(this)
        layoutBinding.ideViewPager.apply {
            adapter = pagerAdapter
        }

        setContentView(layoutBinding.root)
    }

    // TODO: Go back to default file view or quit if it's already the visible one, or if it's the
    //  files view, then navigate back in the file tree
    override fun onBackPressed() {
        super.onBackPressed()
    }
}