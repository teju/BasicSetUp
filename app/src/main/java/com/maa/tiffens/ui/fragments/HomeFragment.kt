package com.maa.tiffens.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.maa.tiffens.R
import com.maa.tiffens.ui.fragments.BaseFragment


class HomeFragment : BaseFragment()  {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.home_fragment, container, false)
        return v
    }

    override fun onBackTriggered() {
        home().exitApp()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI();
    }

    private fun initUI() {

    }


}
