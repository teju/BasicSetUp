package com.amazon.market.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amazon.market.R
import kotlinx.android.synthetic.main.registration_fragment.*

class RegistrationFragment : BaseFragment() ,View.OnClickListener{


    val BLANKFRAGMENT_LAYOUT = R.layout.registration_fragment

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(BLANKFRAGMENT_LAYOUT, container, false)
        return v
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        register.setOnClickListener(this)
        login.setOnClickListener(this)

    }
    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.register -> {
                home().setFragment(RegistrationPage2Fragment())
            }
            R.id.login -> {
                home().proceedDoOnBackPressed()
            }
        }
    }
}