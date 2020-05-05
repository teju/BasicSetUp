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
import com.etebarian.meowbottomnavigation.MeowBottomNavigation
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
        initBottomBar()
    }

    fun initBottomBar() {

        btmNavigation.apply {

            add(MeowBottomNavigation.Model(ID_STORE, R.drawable.manage_inventory))
            add(MeowBottomNavigation.Model(ID_ORDER_MANAGER, R.drawable.order_manager))
            add(MeowBottomNavigation.Model(ID_HOME, R.drawable.home))
            add(MeowBottomNavigation.Model(ID_ME, R.drawable.profile))
            add(MeowBottomNavigation.Model(ID_TUTORIALS, R.drawable.tutorials))
        }
        setCurrentItem(ID_HOME)
        btmNavigation.show(ID_HOME)
        btmNavigation.setOnClickMenuListener {
            Toast.makeText(context, "item ${it.id} is reselected.", Toast.LENGTH_LONG).show()
            setCurrentItem(it.id)

        }

    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)

    }


    fun setCurrentItem(which: Int) {
        Helper.showLog("setCurrentItem "+which)
        if (which == ID_STORE) {
            currentTab = ID_STORE
            home().setOrShowExistingFragmentByTag(
                R.id.mainLayoutFragment, "FIRST_TAB",
                "MAIN_TAB", StoreFragment(), Helper.listFragmentsMainTab
            )
        } else if (which == ID_ORDER_MANAGER) {
            currentTab = ID_ORDER_MANAGER
            home().setOrShowExistingFragmentByTag(
                R.id.mainLayoutFragment, "SECOND_TAB",
                "MAIN_TAB", OrderManagerFragment(), Helper.listFragmentsMainTab
            )
        } else if (which == ID_HOME) {
            currentTab = ID_HOME
            home().setOrShowExistingFragmentByTag(
                R.id.mainLayoutFragment, "THIRD_TAB",
                "MAIN_TAB", HomePageFragment(), Helper.listFragmentsMainTab
            )
        } else if (which == ID_ME) {
            currentTab = ID_ME
            home().setOrShowExistingFragmentByTag(
                R.id.mainLayoutFragment, "FOURTH_TAB",
                "MAIN_TAB", MeFragment(), Helper.listFragmentsMainTab
            )
//            home().setOrShowExistingFragmentByTag(
//                R.id.mainLayoutFragment, "FOURTH_TAB",
//                "MAIN_TAB", ShoppingCartListingFragment().apply {
//                    this.state.fromMainTab = true
//                }, Helper.listFragmentsMainTab
//            )
        } else if (which == ID_TUTORIALS) {
            currentTab = ID_TUTORIALS
            home().setOrShowExistingFragmentByTag(
                R.id.mainLayoutFragment, "FIFTH_TAB",
                "MAIN_TAB", TutorialFragment(), Helper.listFragmentsMainTab
            )
        }
    }

    companion object {
        private const val ID_STORE = 1
        private const val ID_ORDER_MANAGER = 2
        private const val ID_HOME = 3
        private const val ID_ME = 4
        private const val ID_TUTORIALS = 5
    }

}
