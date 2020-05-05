package com.amazon.market.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amazon.market.R
import com.amazon.market.etc.BaseHelper
import kotlinx.android.synthetic.main.address_fragment.*
import java.lang.StringBuilder


class AddressFragment : BaseFragment(),View.OnClickListener {


    private var regPage2Fragment: RegistrationPage2Fragment? = null
    fun setAddRecipientFragment(addRecipientPage2Fragment: RegistrationPage2Fragment) {
        this.regPage2Fragment = addRecipientPage2Fragment
    }
    var address = ""
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.address_fragment, container, false)
        return v
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cancel.setOnClickListener(this)
        submit.setOnClickListener(this)

    }
    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.cancel -> {
                home().proceedDoOnBackPressed()
            }
            R.id.submit -> {
                address()
                regPage2Fragment?.setFromAddress(address)
                regPage2Fragment?.isBackFromAddress = true

                home().proceedDoOnBackPressed()
            }
        }
    }
    fun address() {
        val stringBuilder = StringBuilder()
        if(!BaseHelper.isEmpty(area.text.toString())) {
            stringBuilder.append(area.text.toString())
        }
        if(!BaseHelper.isEmpty(street_name.text.toString())) {
            stringBuilder.append(",")
            stringBuilder.append(street_name.text.toString())
        }
        if(!BaseHelper.isEmpty(city.text.toString())) {
            stringBuilder.append(",")
            stringBuilder.append(city.text.toString())
        }
        if(!BaseHelper.isEmpty(state.text.toString())) {
            stringBuilder.append(",")
            stringBuilder.append(state.text.toString())
        }
        if(!BaseHelper.isEmpty(landmark.text.toString())) {
            stringBuilder.append(",")
            stringBuilder.append(landmark.text.toString())
        }
        if(!BaseHelper.isEmpty(pincode.text.toString())) {
            stringBuilder.append(",")
            stringBuilder.append(pincode.text.toString())
        }
        address = stringBuilder.toString()

    }
}