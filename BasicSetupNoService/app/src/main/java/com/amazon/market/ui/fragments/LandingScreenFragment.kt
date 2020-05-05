package com.amazon.market.ui.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.amazon.market.R
import com.amazon.market.ui.adapters.BannerAdapter
import kotlinx.android.synthetic.main.fragment_landing_screen.*

class LandingScreenFragment : BaseFragment() ,View.OnClickListener{
    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.get_started -> {
                home().setFragment(LoginFragment())
            }
        }
    }

    val banners: ArrayList<Int> = ArrayList()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.fragment_landing_screen, container, false)
        return v
    }

    override fun onBackTriggered() {
        super.onBackTriggered()
        home().exitApp()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initBanner()
        get_started.setOnClickListener(this)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun initBanner() {
        banners.add(R.drawable.logo_big)
        val homeBannerAdapter = BannerAdapter(activity!!.applicationContext, banners)
        vp.setAdapter(homeBannerAdapter)
        vp.setSelectedColor(ContextCompat.getColor(activity!!.applicationContext, R.color.theme_blue))
        vp.setUnselectedColor(ContextCompat.getColor(activity!!.applicationContext, R.color.DarkGray))
        vp.setShowIndicator(true)
        vp.setAutoPlaying(true)

        homeBannerAdapter.bannerAdapterListener = object  : BannerAdapter.BanneradapterListener{
            override fun onClick(position: Int) {}
        }
    }
}