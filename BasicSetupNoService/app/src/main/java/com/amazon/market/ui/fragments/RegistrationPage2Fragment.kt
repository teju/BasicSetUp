package com.amazon.market.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amazon.market.R
import com.amazon.market.etc.BaseHelper
import com.amazon.market.etc.Helper
import com.amazon.market.etc.callback.BottomSheetMediaSelectionListener
import com.amazon.market.etc.callback.NotifyListener
import com.amazon.market.etc.callback.PermissionListener
import kotlinx.android.synthetic.main.registration2_fragment.*
import pl.aprilapps.easyphotopicker.DefaultCallback
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File
import android.graphics.BitmapFactory
import android.graphics.Bitmap


class RegistrationPage2Fragment : BaseFragment(),View.OnClickListener {


    private var myBitmap: Bitmap? = null
    private var userstorePhotoFileString: String? =""
    private var userAddressPhotoFileString: String? =""
    private var usersignaturePhotoFileString: String? =""

    var STORE_PHOTO = 1001
    var ADDRESS_PHOTO = 1002
    var SINATURE_PHOTO = 1003

    var address = ""
    var businessDetail = ""
    fun setFromAddress(fromAddress: String) {
        this.address = fromAddress
    }
    var isBackFromAddress = false
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.registration2_fragment, container, false)
        return v
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if(isBackFromAddress) {
            if(!BaseHelper.isEmpty(address)) {
                business_address.setText(address)
            }
            if(!BaseHelper.isEmpty(businessDetail)) {
                business_detail.setText(businessDetail)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        business_address.setOnClickListener(this)
        business_detail.setOnClickListener(this)
        register.setOnClickListener(this)
        store_photo.setOnClickListener(this)
        address_proof.setOnClickListener(this)
        signature.setOnClickListener(this)
        signature_cancel.setOnClickListener(this)
        address_cancel.setOnClickListener(this)
        store_cancel.setOnClickListener(this)

    }
    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.business_address -> {
                home().setFragment(AddressFragment().apply {
                    setAddRecipientFragment(this@RegistrationPage2Fragment)
                })
            }
            R.id.business_detail -> {
                home().setFragment(BusinessDetailFragment().apply {
                    setAddRecipientFragment(this@RegistrationPage2Fragment)
                })
            }
            R.id.register -> {
                home().setFragment(MainFragment())
            }

            R.id.store_photo -> {
                uploadImage(STORE_PHOTO)
            }
            R.id.address_proof -> {
                uploadImage(ADDRESS_PHOTO)
            }
            R.id.signature -> {
                uploadImage(SINATURE_PHOTO)
            }
            R.id.signature_cancel -> {
                signature.visibility = View.VISIBLE
                rlimg_signature.visibility = View.GONE
                img_signature.setImageResource(0)
            }
            R.id.address_cancel -> {
                address_proof.visibility = View.VISIBLE
                rlimg_address_proof.visibility = View.GONE
                img_address_proof.setImageResource(0)

            }
            R.id.store_cancel -> {
                store_photo.visibility = View.VISIBLE
                rlimg_store_photo.visibility = View.GONE
                img_store_photo.setImageResource(0)

            }
        }
    }
    fun uploadImage(type : Int) {
        BaseHelper.selectImageSource(this.activity, object : BottomSheetMediaSelectionListener {

            override fun onCameraSelection() {
                home().setEasyImageListener(object : DefaultCallback() {
                    override fun onImagePicked(imageFile: File, source: EasyImage.ImageSource, types: Int) {
                        try {

                            if (imageFile.exists()) {

                                myBitmap =
                                    BitmapFactory.decodeFile(imageFile.getAbsolutePath())

                            }
                            if(type == STORE_PHOTO) {
                                userstorePhotoFileString = imageFile.absolutePath
                                store_photo.visibility = View.GONE
                                rlimg_store_photo.visibility = View.VISIBLE
                                img_store_photo.setImageBitmap(myBitmap)

                            } else if(type == ADDRESS_PHOTO) {
                                address_proof.visibility = View.GONE
                                rlimg_address_proof.visibility = View.VISIBLE
                                img_address_proof.setImageBitmap(myBitmap)

                                userAddressPhotoFileString = imageFile.absolutePath
                            } else {
                                signature.visibility = View.GONE
                                rlimg_signature.visibility = View.VISIBLE
                                usersignaturePhotoFileString = imageFile.absolutePath
                                img_signature.setImageBitmap(myBitmap)

                            }
                           // val str = BaseHelper.saveSmallerImage(userPhotoFileString,200,200)
                            //userUploadPhotoViewModel.loadData(str)
                        } catch (e: Exception) {
                            Helper.logException(activity!!,"uploadImage "+e.toString())
                            e.printStackTrace()
                        }

                    }
                })

                val permissionListener: PermissionListener = object : PermissionListener {
                    override fun onCheckPermission(permission: String, isGranted: Boolean) {
                        if (isGranted) {
                            onPermissionAlreadyGranted()
                        } else {
                            onUserNotGrantedThePermission()
                        }
                    }

                    override fun onPermissionAlreadyGranted() {
                        openCamera()
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
                checkPermissions(permissions, permissionListener)

            }

            override fun onMediaStorageSelection() {
                home().setEasyImageListener(object : DefaultCallback() {
                    override fun onImagePicked(imageFile: File, source: EasyImage.ImageSource, types: Int) {
                        try {

                            if (imageFile.exists()) {

                                myBitmap =
                                    BitmapFactory.decodeFile(imageFile.getAbsolutePath())

                            }
                            if(type == STORE_PHOTO) {
                                userstorePhotoFileString = imageFile.absolutePath
                                store_photo.visibility = View.GONE
                                rlimg_store_photo.visibility = View.VISIBLE
                                img_store_photo.setImageBitmap(myBitmap)

                            } else if(type == ADDRESS_PHOTO) {
                                address_proof.visibility = View.GONE
                                rlimg_address_proof.visibility = View.VISIBLE
                                img_address_proof.setImageBitmap(myBitmap)

                                userAddressPhotoFileString = imageFile.absolutePath
                            } else {
                                signature.visibility = View.GONE
                                rlimg_signature.visibility = View.VISIBLE
                                usersignaturePhotoFileString = imageFile.absolutePath
                                img_signature.setImageBitmap(myBitmap)

                            }
                            // val str = BaseHelper.saveSmallerImage(userPhotoFileString,200,200)
                            //userUploadPhotoViewModel.loadData(str)
                        } catch (e: Exception) {
                            Helper.logException(activity!!,"uploadImage "+e.toString())
                            e.printStackTrace()
                        }
                    }
                })
                openGallery()
            }
        })
    }

    private fun openCamera() {
        EasyImage.openCamera(requireActivity(), 0)
    }

    fun openGallery() {
        EasyImage.openGallery(requireActivity(), 0)
    }
}