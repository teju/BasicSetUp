package com.amazon.market.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.amazon.market.R
import com.amazon.market.etc.BaseHelper
import com.amazon.market.etc.Helper
import com.amazon.market.etc.Keys
import com.amazon.market.etc.UserInfoManager
import com.amazon.market.ui.fragments.BaseFragment
import com.amazon.market.ui.fragments.LandingScreenFragment
import com.amazon.market.ui.fragments.LoginFragment
import com.amazon.market.ui.fragments.MainFragment
import pl.aprilapps.easyphotopicker.DefaultCallback
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File
import java.util.ArrayList

class ActivityMain : AppCompatActivity() {

    companion object {
        private var MAIN_FLOW_INDEX = 0
        private val MAIN_FLOW_TAG = "MainFlowFragment"
    }
    private var easyImageListener: DefaultCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        triggerMainProcess()
    }

    fun exitApp() {
        if(getSupportFragmentManager().getBackStackEntryCount()>1){
            getSupportFragmentManager().popBackStack();
        }else if(getSupportFragmentManager().getBackStackEntryCount()==1){
                this.finish();
            }
    }

    fun triggerMainProcess(){

        if(!BaseHelper.isEmpty(UserInfoManager.getInstance(this).authToken)) {
            setFragment(MainFragment())
        } else if (UserInfoManager.getInstance(this).getLoggedInStatus()) {
            setFragment(LoginFragment())
        } else {
            setFragment(LandingScreenFragment())
        }
    }

    fun setFragment(frag: Fragment) {
        try {
            val f = getSupportFragmentManager().beginTransaction()
            val list = getSupportFragmentManager().getFragments()
            for(frag in list){
                if(frag.isVisible){
                    f.hide(frag)
                }
            }

            MAIN_FLOW_INDEX = MAIN_FLOW_INDEX + 1
            f.add(R.id.layoutFragment, frag, MAIN_FLOW_TAG + MAIN_FLOW_INDEX).addToBackStack(
                MAIN_FLOW_TAG
            ).commitAllowingStateLoss()
            //BaseUIHelper.hideKeyboard(this)
        } catch (e: Exception) {
            Helper.logException(this@ActivityMain, e)
        }

    }

    fun jumpToPreviousFlowThenGoTo(fullFragPackageNameThatStillExistInStack: String, targetFrag: Fragment){
        jumpToPreviousFragment(fullFragPackageNameThatStillExistInStack)
        setFragment(targetFrag)
    }

    fun jumpToPreviousFragment(fullFragPackageNameThatStillExistInStack: String) {
        try {
            var indexTag: String? = null
            var fragmentName: String? = null

            val f = getSupportFragmentManager().beginTransaction()
            var list = getSupportFragmentManager().getFragments()
            for(i in  0..(list.size - 1)){

                if(list.get(i).javaClass.name.equals(fullFragPackageNameThatStillExistInStack, ignoreCase = false)){
                    indexTag = list.get(i).tag
                    fragmentName = list.get(i).javaClass.name
                }

                if(list.get(i).isVisible){
                    f.hide(list.get(i))
                }
            }

            if(indexTag == null){
                onBackPressed()
            }else{

                val currentIndex = getSupportFragmentManager().getFragments().size-1
                for(i in currentIndex downTo 0){
                    try {
                        if((MAIN_FLOW_TAG + i).equals(indexTag, ignoreCase = true)) break
                        try {
                            if(fragmentName.equals(currentFragment.javaClass.name, ignoreCase = true)) break
                        } catch (e: Exception) { }
                        if((indexTag).contains(MAIN_FLOW_TAG, ignoreCase = true)) {
                            getSupportFragmentManager().popBackStackImmediate()
                            MAIN_FLOW_INDEX = MAIN_FLOW_INDEX - 1
                            if(MAIN_FLOW_INDEX < 0) MAIN_FLOW_INDEX = 0
                        }
                    } catch (e: Exception) {
                        Helper.logException(this@ActivityMain, e)
                    }
                }

                list = getSupportFragmentManager().getFragments()
                for(i in  0..(list.size - 1)){
                    if(list.get(i).tag.equals(indexTag, ignoreCase = false)){
                        f.show(list.get(i))
                        break
                    }
                }

                Helper.hideKeyboard(this)
            }

        } catch (e: Exception) {
            Helper.logException(this@ActivityMain, e)
        }
    }

    fun setFragmentByReplace(frag: Fragment) {

        try {
            val f = getSupportFragmentManager().beginTransaction()
            MAIN_FLOW_INDEX = MAIN_FLOW_INDEX + 1
            f.replace(R.id.layoutFragment, frag, MAIN_FLOW_TAG + MAIN_FLOW_INDEX).addToBackStack(
                MAIN_FLOW_TAG
            ).commit()
            Helper.hideKeyboard(this)
        } catch (e: Exception) {
            Helper.logException(this@ActivityMain, e)
        }

    }

    fun clearFragmentBackstack(tag: String) {
        getSupportFragmentManager().popBackStack(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    fun clearFragment() {

        getSupportFragmentManager().popBackStack(MAIN_FLOW_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE)

        for (i in MAIN_FLOW_INDEX downTo 0) {
            try {
                val fragment = getSupportFragmentManager().findFragmentByTag(MAIN_FLOW_TAG + i)
                if (fragment != null)
                    getSupportFragmentManager().beginTransaction().remove(fragment).commitNowAllowingStateLoss()
            } catch (e: Exception) {
                Helper.logException(this@ActivityMain, e)
            }

        }

        getSupportFragmentManager().popBackStack("MAIN_TAB", FragmentManager.POP_BACK_STACK_INCLUSIVE)
        MAIN_FLOW_INDEX = 0
    }
    val currentFragment: Fragment
        get() = getSupportFragmentManager().findFragmentById(R.id.layoutFragment)!!

    fun proceedDoOnBackPressed(){
        Helper.hideKeyboard(this@ActivityMain)

        val f = getSupportFragmentManager().beginTransaction()
        val list = getSupportFragmentManager().getFragments()

        for(frag in list){
            try {
                if(frag.tag!!.contentEquals(MAIN_FLOW_TAG + (MAIN_FLOW_INDEX - 1))){
                    f.show(frag)
                }
            } catch (e: Exception) {}
        }

        if (getSupportFragmentManager().getBackStackEntryCount() <= 1 || (currentFragment is MainFragment)) {
            this@ActivityMain.finish()
        } else {
            try {
                (currentFragment as BaseFragment).httpScope.onPause()
            } catch (e: Exception) {
                Helper.logException(null, e)
            }
            super.onBackPressed()
        }

        MAIN_FLOW_INDEX = MAIN_FLOW_INDEX - 1
        if(MAIN_FLOW_INDEX < 0) MAIN_FLOW_INDEX = 0

    }

    fun logout() {
        UserInfoManager.getInstance(this).logout()
        val intent = Intent(this, ActivityMain::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.putExtra(Keys.STATUS, true)
        startActivity(intent)
        finish()
    }

    fun backToMainScreen(){
        clearFragment()
        triggerMainProcess()
    }

    fun setEasyImageListener(easyImageListener: DefaultCallback) {
        this.easyImageListener = easyImageListener
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        try {
            for (fragment in supportFragmentManager.fragments) {
                fragment.onActivityResult(requestCode, resultCode, data)
            }
        } catch (e: Exception) {
            Helper.logException(this, e)
        }

        Helper.logException(this!!,"onImagePicked resultCode")

        if (resultCode == RESULT_OK) {

            EasyImage.handleActivityResult(requestCode, resultCode, data, this, object : DefaultCallback() {

                override fun onImagePickerError(e: Exception, source: EasyImage.ImageSource, type: Int) {
                    if (easyImageListener != null) {
                        easyImageListener!!.onImagePickerError(e, source, type)
                    }
                }

                override fun onImagePicked(imageFile: File, source: EasyImage.ImageSource, type: Int) {
                    if (easyImageListener != null)
                        easyImageListener!!.onImagePicked(imageFile, source, type)
                }

                override fun onCanceled(source: EasyImage.ImageSource, type: Int) {
                    super.onCanceled(source, type)
                    if (easyImageListener != null)
                        easyImageListener!!.onCanceled(source, type)
                }

            })
        }
    }
    override fun onBackPressed() {
        val f = getSupportFragmentManager().beginTransaction()
        val list = getSupportFragmentManager().getFragments()
        var foundVisible = false
        for(i in  0..(list.size - 1)){
            if(list.get(i).isVisible){
                if(list.get(i) is BaseFragment) {
                    foundVisible = true
                    (list.get(i) as BaseFragment).onBackTriggered()
                }
            }
        }

        if(!foundVisible)
            proceedDoOnBackPressed()
    }
    fun setOrShowExistingFragmentByTag(
        layoutId: Int,
        fragTag: String,
        backstackTag: String,
        newFrag: Fragment,
        listFragmentTagThatNeedToHide: ArrayList<String>
    ) {

        var foundExistingFragment = false

        val fragment = supportFragmentManager.findFragmentByTag(fragTag)
        val transaction = supportFragmentManager.beginTransaction()
        if (fragment != null) {
            for (i in 0 until supportFragmentManager.fragments.size) {

                try {
                    val f = supportFragmentManager.fragments[i]

                    for (tag in listFragmentTagThatNeedToHide) {
                        try {
                            if (f.tag.toString().toLowerCase().equals(tag.toLowerCase())) {
                                transaction.hide(f)
                            }
                        } catch (e: Exception) {
                            Helper.logException(this@ActivityMain, e)
                        }

                    }

                } catch (e: Exception) {
                    Helper.logException(this@ActivityMain, e)
                }

            }

            try {
                transaction.show(fragment).commit()
            } catch (e: Exception) {
                try {
                    transaction.show(fragment).commitAllowingStateLoss()
                } catch (e1: Exception) {
                    Helper.logException(this@ActivityMain, e)
                }

            }

            foundExistingFragment = true

        }

        if (!foundExistingFragment) {
            setFragmentInFragment(layoutId, newFrag, fragTag, backstackTag)
        }

    }
    fun setFragmentInFragment(fragmentLayout: Int, frag: Fragment, tag: String, backstackTag: String) {
        try {
            supportFragmentManager.beginTransaction().add(fragmentLayout, frag, tag).addToBackStack(backstackTag)
                .commit()
            Helper.hideKeyboard(this)
        } catch (e: Exception) {
            try {
                supportFragmentManager.beginTransaction().add(fragmentLayout, frag, tag).addToBackStack(backstackTag)
                    .commitAllowingStateLoss()
                Helper.hideKeyboard(this)
            } catch (e1: Exception) {
                Helper.logException(this@ActivityMain, e)
            }

        }

    }

}
