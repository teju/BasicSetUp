package com.amazon.market.ui.fragments

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.core.view.OneShotPreDrawListener.add
import androidx.lifecycle.Observer
import com.amazon.market.R
import com.amazon.market.etc.Helper
import androidx.databinding.DataBindingUtil
import kotlinx.android.synthetic.main.main_fragment.*

class MainFragment : BaseFragment() {

    val MAINFRAGMENT_LAYOUT = R.layout.main_fragment
    private var currentTab = ID_HOME

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(MAINFRAGMENT_LAYOUT, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }


    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)

    }



    companion object {
        private const val ID_STORE = 1
        private const val ID_ORDER_MANAGER = 2
        private const val ID_HOME = 3
        private const val ID_ME = 4
        private const val ID_TUTORIALS = 5
    }

}
