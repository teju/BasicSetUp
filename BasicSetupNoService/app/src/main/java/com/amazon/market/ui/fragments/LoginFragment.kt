package com.amazon.market.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amazon.market.R
import kotlinx.android.synthetic.main.login_fragment.*

class LoginFragment : BaseFragment(),View.OnClickListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.login_fragment, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        register.setOnClickListener(this)
        login.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.register -> {
                home().setFragment(RegistrationFragment())
            }
            R.id.login -> {
                home().setFragment(MainFragment())
            }
        }
    }
}