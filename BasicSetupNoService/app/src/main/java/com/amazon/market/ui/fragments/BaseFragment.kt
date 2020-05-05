package com.amazon.market.ui.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.amazon.market.etc.Helper
import com.amazon.market.etc.UserInfoManager
import com.amazon.market.etc.callback.PermissionListener
import com.amazon.market.etc.generics.GenericFragment

import java.util.*
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext
import com.amazon.market.R
import com.amazon.market.etc.BaseHelper
import com.amazon.market.etc.Constants
import com.amazon.market.etc.callback.DatePickerListener
import com.amazon.market.etc.callback.NoInternetListener
import com.amazon.market.etc.callback.NotifyListener
import com.amazon.market.ui.ActivityMain
import com.amazon.market.ui.fragments.dialog.DatePickerDialogFragment
import com.amazon.market.ui.fragments.dialog.NoInternetDialogFragment
import com.amazon.market.ui.fragments.dialog.NotifyDialogFragment
import com.amazon.market.ui.views.LoadingCompound
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher

open class BaseFragment : GenericFragment() {

    lateinit var userInfo: UserInfoManager
        private set

    companion object {
        var permissionsThatNeedTobeCheck: List<String>? = null
            private set
        var permissionListener: PermissionListener? = null
            private set

    }

    var v: View? = null

    var obsOAuthExpired: Observer<Boolean> = Observer { isOauthExpired ->
        if (isOauthExpired!!) {
            showNotifyDialog(
                getString(R.string.session_expired), getString(R.string.you_have_been_loggedout),
                getString(R.string.ok),"",object : NotifyListener {
                    override fun onButtonClicked(which: Int) {
                        home().logout()
                    }
                })
        }
    }

    var obsNoInternet: Observer<Boolean> = Observer { isHaveInternet ->
        try {
            if (!isHaveInternet) {
                if (activity == null) return@Observer
                showNoInternetDialog(object : NoInternetListener {
                    override fun onReloadClicked() {
                        home().backToMainScreen()
                    }
                })
            }
        } catch (e: Exception) {
            Helper.logException(activity, e)
        }
    }

    class HttpLifecycleScope : CoroutineScope, LifecycleObserver {

        private lateinit var job: Job
        override val coroutineContext: CoroutineContext
            get() = job + Dispatchers.IO

        @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
        fun onCreate() {
            job = Job()
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        fun onPause() {
            job.cancel()
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            job.cancel()
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        fun onStop() {
            job.cancel()
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        fun onStart() {
            job = Job()
        }
    }

    val httpScope = HttpLifecycleScope()
    val singleThread = Executors.newFixedThreadPool(1).asCoroutineDispatcher()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.let {
            this.userInfo = UserInfoManager.getInstance(it)
            lifecycle.addObserver(httpScope)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        v?.let {
           // setBackButtonToolbarStyleOne(v!!)

        }

        v?.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                Helper.hideKeyboard(activity!!)
            }})

        val permissionListener: PermissionListener = object : PermissionListener {
            override fun onCheckPermission(permission: String, isGranted: Boolean) {
                if (isGranted) {
                    onPermissionAlreadyGranted()
                } else {
                    onUserNotGrantedThePermission()
                }
            }

            override fun onPermissionAlreadyGranted() {

            }

            override fun onUserNotGrantedThePermission() {
                showNotifyDialog(
                    "",getString(R.string.please_allow_deera_access_permission_camera),
                    getString(R.string.ok),"",object : NotifyListener {
                        override fun onButtonClicked(which: Int) {}
                    })
            }
        }
        val permissions = java.util.ArrayList<String>()
        permissions.add(android.Manifest.permission.CAMERA)
        permissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        permissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        checkPermissions(permissions, permissionListener)


    }

    open fun onBackTriggered(){
        home().proceedDoOnBackPressed()
    }

    fun home(): ActivityMain {
        return activity as ActivityMain
    }

    fun addChildFragment(frag: Fragment, id: Int, backstackTag: String) {
        childFragmentManager.beginTransaction().add(id, frag).addToBackStack(backstackTag).commit()
        Helper.hideKeyboard(activity!!)
    }

    fun clearChildFragmentBackstack(tag: String) {
        childFragmentManager.popBackStack(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    fun removeAllSpecificChildFragment(fragName: String){

        try {
            val f = childFragmentManager.beginTransaction()
            val list = childFragmentManager.getFragments()
            for(frag in list){
                if(frag.javaClass.name.equals(fragName)){
                    try {
                        childFragmentManager.beginTransaction().remove(frag)
                    } catch (e: Exception) {
                        childFragmentManager.beginTransaction().remove(frag).commitNowAllowingStateLoss()
                    }
                }
            }
        } catch (e: Exception) {
            Helper.logException(null, e)
        }

    }

    private fun addPermission(permissionsList: MutableList<String>, permission: String): Boolean {
        if (ContextCompat.checkSelfPermission(activity!!, permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission)
            // Check for Rationale Option
            if (!ActivityCompat.shouldShowRequestPermissionRationale(activity!!, permission))
                return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        try {
            if (requestCode == Constants.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS) {

                var isAllGranted = false
                val index = 0
                for (permission in permissionsThatNeedTobeCheck!!) {
                    if (permission.equals(Manifest.permission.CAMERA, ignoreCase = true)) {
                        if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                            isAllGranted = false
                            break
                        } else {
                            isAllGranted = true
                        }
                    } else if (permission.equals(Manifest.permission.READ_CONTACTS, ignoreCase = true)) {
                        if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                            isAllGranted = false
                            break
                        } else {
                            isAllGranted = true
                        }
                    } else if (permission.equals(Manifest.permission.WRITE_CONTACTS, ignoreCase = true)) {
                        if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                            isAllGranted = false
                            break
                        } else {
                            isAllGranted = true
                        }
                    } else if (permission.equals(Manifest.permission.READ_EXTERNAL_STORAGE, ignoreCase = true)) {
                        if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                            isAllGranted = false
                            break
                        } else {
                            isAllGranted = true
                        }
                    } else if (permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE, ignoreCase = true)) {
                        if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                            isAllGranted = false
                            break
                        } else {
                            isAllGranted = true
                        }
                    } else if (permission.equals(Manifest.permission.RECEIVE_SMS, ignoreCase = true)) {
                        if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                            isAllGranted = false
                            break
                        } else {
                            isAllGranted = true
                        }
                    } else if (permission.equals(Manifest.permission.READ_SMS, ignoreCase = true)) {
                        if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                            isAllGranted = false
                            break
                        } else {
                            isAllGranted = true
                        }
                    } else if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION, ignoreCase = true)) {
                        if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                            isAllGranted = false
                            break
                        } else {
                            isAllGranted = true
                        }
                    } else if (permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION, ignoreCase = true)) {
                        if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                            isAllGranted = false
                            break
                        } else {
                            isAllGranted = true
                        }
                    }
                    //                    index = index + 1;
                }

                //                index = index - 1;
                if (isAllGranted) {
                    permissionListener!!.onCheckPermission(permissions[index], true)
                } else {
                    permissionListener!!.onCheckPermission(permissions[index], false)
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

/*
    fun setBackButtonToolbarStyleOne(v: View) {
        try {
            val llBack = v.findViewById<LinearLayout>(R.id.llBack)
            llBack.setOnClickListener { home().onBackPressed() }
        } catch (e: Exception) {
            Helper.logException(activity, e)
        }

//        try {
//            val ivBack = v.findViewById<ImageView>(R.id.ivBack)
//            ivBack.setOnClickListener { home().onBackPressed() }
//        } catch (e: Exception) {
//            Helper.logException(activity, e)
//        }

       */
/* try {
            val toolbar = v.findViewById<Toolbar>(R.id.toolbar)
            toolbar.navigationIcon = null
            toolbar.title = ""
            toolbar.hideOverflowMenu()

            toolbar.setNavigationOnClickListener { home().onBackPressed() }
        } catch (e: Exception) {
            Helper.logException(activity, e)
        }*//*

    }
*/




    open fun showDatePicker(d: DatePickerListener, initDate: Date? = null){
        val f = DatePickerDialogFragment().apply {
            this.listener = d
            this.initDate = initDate
        }
        f.show(childFragmentManager, DatePickerDialogFragment.TAG)
    }

    open fun showNotifyDialog(
        tittle: String?,
        messsage: String?,
        button_positive:String?,
        button_negative: String?,
        n: NotifyListener){
        val f = NotifyDialogFragment().apply {
            this.listener = n
        }
        f.notify_tittle = tittle
        f.notify_messsage = messsage
        f.button_positive = button_positive
        f.button_negative = button_negative
        f.showTick = false
        f.isCancelable = false
        f.show(activity!!.supportFragmentManager, NotifyDialogFragment.TAG)
    }

    open fun showNoInternetDialog(n: NoInternetListener){
        val f = NoInternetDialogFragment().apply {
            this.listener = n
        }
        f.isCancelable = false

        f.show(activity!!.supportFragmentManager, NoInternetDialogFragment.TAG)
    }



    fun showLoadingLogicError(ld: LoadingCompound, errorLogicCode : String){
        ld.showError(getString(R.string.market__network_error),
            String.format("%s (%s)", getString(R.string.market__unknown_response), errorLogicCode))
    }


    fun setBoldSpannable(myText: String, start: Int, end: Int): SpannableString {
        try {
            val spannableContent = SpannableString(myText)
            // val typeface = Typeface.createFromAsset(context!!.assets, "font/poppins_bold")
            spannableContent.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE)

            return spannableContent
        } catch (e : java.lang.Exception) {
            val spannableContent = SpannableString(myText)
            // val typeface = Typeface.createFromAsset(context!!.assets, "font/poppins_bold")
            spannableContent.setSpan(StyleSpan(Typeface.BOLD), start, end - 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE)

            return spannableContent
        }
    }



    fun checkPermissions(permissionsThatNeedTobeCheck: List<String>, permissionListener: PermissionListener) {

        BaseFragment.permissionsThatNeedTobeCheck = permissionsThatNeedTobeCheck
        BaseFragment.permissionListener = permissionListener
        val permissionsNeeded = ArrayList<String>()
        val permissionsList = ArrayList<String>()

        for (s in permissionsThatNeedTobeCheck) {
            if (s.equals(Manifest.permission.CAMERA, ignoreCase = true)) {
                if (!addPermission(permissionsList, Manifest.permission.CAMERA))
                    permissionsNeeded.add("Camera")
            }else if (s.equals(Manifest.permission.READ_EXTERNAL_STORAGE, ignoreCase = true)) {
                if (!addPermission(permissionsList, Manifest.permission.READ_EXTERNAL_STORAGE))
                    permissionsNeeded.add("Read External Storage")
            }else if (s.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE, ignoreCase = true)) {
                if (!addPermission(permissionsList, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    permissionsNeeded.add("Write External Storage")
            }
        }

        if (permissionsList.size > 0) {
            if (permissionsNeeded.size > 0) {
                ActivityCompat.requestPermissions(
                    activity!!,
                    permissionsList.toTypedArray(),
                    Constants.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS
                )
                return
            }
            ActivityCompat.requestPermissions(
                activity!!, permissionsList.toTypedArray(),
                Constants.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS
            )
            return
        } else {
            permissionListener.onPermissionAlreadyGranted()
        }
    }


}