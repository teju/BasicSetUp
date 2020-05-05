package com.amazon.market.ui.fragments.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amazon.market.R
import com.amazon.market.etc.callback.NoInternetListener

import kotlinx.android.synthetic.main.no_internet_dialog.*

class NoInternetDialogFragment : BaseDialogFragment() {


    companion object {
        val TAG = "NoInternetDialogFragment"
    }

    lateinit var listener: NoInternetListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.no_internet_dialog, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

       /* btn_positive.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                dismiss()
                listener.let {
                    listener.onReloadClicked()
                }

            }
        })*/

    }

}