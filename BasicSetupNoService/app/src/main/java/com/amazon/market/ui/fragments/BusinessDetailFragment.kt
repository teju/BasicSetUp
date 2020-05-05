package com.amazon.market.ui.fragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.amazon.market.R
import com.amazon.market.etc.BaseHelper
import com.amazon.market.etc.Helper
import com.amazon.market.etc.callback.BottomSheetMediaSelectionListener
import kotlinx.android.synthetic.main.business_fragment.*
import pl.aprilapps.easyphotopicker.DefaultCallback
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File


class BusinessDetailFragment : BaseFragment() ,View.OnClickListener{

    private var myBitmap: Bitmap? = null
    private var strbusiness_type: String = ""
    private var regPage2Fragment: RegistrationPage2Fragment? = null
    fun setAddRecipientFragment(addRecipientPage2Fragment: RegistrationPage2Fragment) {
        this.regPage2Fragment = addRecipientPage2Fragment
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.business_fragment, container, false)
        return v
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cancel.setOnClickListener(this)
        submit.setOnClickListener(this)
        tv_cert.setOnClickListener(this)
        initSpinner()
    }

    fun initSpinner() {
        val business_type = resources.getStringArray(R.array.business_type)
        if (spinner != null) {
            val adapter = ArrayAdapter(activity!!,
                R.layout.spinner_item, business_type)
            spinner.adapter = adapter
            strbusiness_type = business_type.get(0)
            spinner.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>,
                                            view: View, position: Int, id: Long) {
//                    Toast.makeText(this@MainActivity,
//                        getString(R.string.selected_item) + " " +
//                                "" + languages[position], Toast.LENGTH_SHORT).show()
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // write code to perform some action
                }
            }
        }
    }
    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.cancel -> {
                home().proceedDoOnBackPressed()
            }
            R.id.tv_cert -> {
                uploadImage()
            }
            R.id.submit -> {
                regPage2Fragment?.businessDetail = businessDetail()
                regPage2Fragment?.isBackFromAddress = true
                home().proceedDoOnBackPressed()
            }
        }
    }
    fun businessDetail() : String {
        val stringBuilder = StringBuilder()
        stringBuilder.append(strbusiness_type)
        if (!BaseHelper.isEmpty(reg_number.text.toString())) {
            stringBuilder.append(",")
            stringBuilder.append(reg_number.text.toString())
        }
        return  stringBuilder.toString()
    }

    fun uploadImage() {
        BaseHelper.selectImageSource(this.activity, object : BottomSheetMediaSelectionListener {

            override fun onCameraSelection() {
                home().setEasyImageListener(object : DefaultCallback() {
                    override fun onImagePicked(imageFile: File, source: EasyImage.ImageSource, types: Int) {
                        try {
                            Helper.logException(activity!!,"uploadImage ")

                            if (imageFile.exists()) {

                                myBitmap =
                                    BitmapFactory.decodeFile(imageFile.getAbsolutePath())

                            }
                            tv_cert.visibility = View.GONE
                            rlimg_cert.visibility = View.VISIBLE
                            img_cert.setImageBitmap(myBitmap)
                        } catch (e: Exception) {
                            Helper.logException(activity!!,"uploadImage "+e.toString())
                            e.printStackTrace()
                        }

                    }
                })
                openCamera()
            }

            override fun onMediaStorageSelection() {
                home().setEasyImageListener(object : DefaultCallback() {
                    override fun onImagePicked(imageFile: File, source: EasyImage.ImageSource, types: Int) {
                        try {
                            Helper.logException(activity!!,"uploadImage ")

                            if (imageFile.exists()) {

                                myBitmap =
                                    BitmapFactory.decodeFile(imageFile.getAbsolutePath())

                            }
                            tv_cert.visibility = View.GONE
                            rlimg_cert.visibility = View.VISIBLE
                            img_cert.setImageBitmap(myBitmap)
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