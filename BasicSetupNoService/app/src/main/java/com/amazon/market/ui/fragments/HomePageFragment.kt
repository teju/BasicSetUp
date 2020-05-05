package com.amazon.market.ui.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.amazon.market.R
import com.amazon.market.ui.adapters.BannerAdapter
import com.amazon.market.ui.adapters.PendingOrderAdapter
import kotlinx.android.synthetic.main.home_fragment.*
import androidx.recyclerview.widget.GridLayoutManager
import com.amazon.market.model.Products
import com.amazon.market.ui.adapters.HomeBannerAdapter
import com.amazon.market.ui.adapters.ProductsAdapter
import com.amazon.market.ui.views.AutoFitGridLayoutManager
import com.amazon.market.ui.views.BannerLayout
import com.amazon.market.ui.views.SpacesItemDecoration


class HomePageFragment : BaseFragment() {

    val banners: ArrayList<Int> = ArrayList()
    val productsarr: ArrayList<Products> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.home_fragment, container, false)
        return v
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initBanner()
        initPendingOrders()
        initProducts()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun initBanner() {
        banners.add(R.drawable.logo_big)
        val homeBannerAdapter = HomeBannerAdapter(activity!!.applicationContext, banners)
        homeBannerAdapter.isHome = true
        vp.setAdapter(homeBannerAdapter)
        vp.setSelectedColor(ContextCompat.getColor(activity!!.applicationContext, R.color.theme_blue))
        vp.setUnselectedColor(ContextCompat.getColor(activity!!.applicationContext, R.color.DarkGray))
        vp.setShowIndicator(true)
        vp.setAutoPlaying(true)
        BannerLayout.IndicatortopMargin = -10

    }
    fun initPendingOrders() {
        val sglm = LinearLayoutManager(context)
        pending_orders.layoutManager = sglm
        val pendingOrderAdapter = PendingOrderAdapter(activity!!.applicationContext)
        pending_orders.setAdapter(pendingOrderAdapter)

    }
    fun initProducts() {
        var productsModel = Products()
        productsModel.name = "Add Product"
        productsModel.image = R.drawable.plus_circle
        productsarr.add(productsModel)

        productsModel = Products()
        productsModel.name = "Fruits & Veggies"
        productsModel.image = R.drawable.fruits_veggies
        productsarr.add(productsModel)

        productsModel = Products()
        productsModel.name = "Houshole Care"
        productsModel.image = R.drawable.houshole_care
        productsarr.add(productsModel)

        productsModel = Products()
        productsModel.name = "Staples"
        productsModel.image = R.drawable.staples
        productsarr.add(productsModel)

        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.spacing_grid1)
        val manager = GridLayoutManager(activity!!, 2, GridLayoutManager.VERTICAL, false)

        products.setLayoutManager(manager)
        products.addItemDecoration(SpacesItemDecoration(2, spacingInPixels, true))
        products.setNestedScrollingEnabled(false)

        products.adapter = ProductsAdapter(activity!!,productsarr)
    }
}