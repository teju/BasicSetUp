package com.amazon.market.etc

import android.content.Context
import android.content.SharedPreferences

class UserInfoManager private constructor() {

    private val KEY_ACCESS_TOKEN = "F3ZT7"
    private val KEY_ACCOUNT_ID = "V8D85H"

    private var accessToken: String? = null
    private var accountId: String? = null
    private var userName: String? = null
    private var userProfileName: String? = ""
    private val USER_PROFILE_NAME = "user_profile_name"

    private var prefs: SharedPreferences? = null
    private var prefsnoclear: SharedPreferences? = null
    private val LOGGED_IN_BEFORE = "logged_in_before"


    private var otpresendduration: Long? = 0
    private var otpexpirtytimestam: Long? = 0

    private val OTPRESENDDURATIONSAVED = "otp_resend_period_saved"
    private val OTPEXPIRTYTIMESTAMP = "otp_expiry_timestamp"

    private var fromCountryCode: String? = null
    private var toCountryCode: String? = null

    private val KEY_USER_NAME = "key_user_name"


    private var userProfilePic: String? = ""
    private val USER_PROFILE_PIC = "user_profile_pic"

    val authToken: String?
        get() {
            if (accessToken == null) {
                this.accessToken = this.prefs!!.getString(KEY_ACCESS_TOKEN, null)
                if (this.accessToken != null)
                    if (this.accessToken!!.toLowerCase().contains("false")) {
                        this.accessToken = null
                    }
                if (accessToken == null)
                    return null
            }
            return accessToken
        }

    private fun openPrefsNoclear(c: Context) {
        this.prefsnoclear = c.getSharedPreferences(FILE_NAME_NOCLR, Context.MODE_PRIVATE)
    }

    private fun openPrefs(c: Context) {
        this.prefs = c.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
    }

    fun saveAuthToken(accessToken: String?) {
        this.accessToken = accessToken
        val editor = this.prefs!!.edit()
        editor.putString(KEY_ACCESS_TOKEN, this.accessToken)
        editor.commit()
    }

    fun getAccountId(): String {
        if (accountId == null) {
            this.accountId = this.prefs!!.getString(KEY_ACCOUNT_ID, null)
        }
        return accountId!!
    }




    fun saveAccountId(accountId: String) {
        this.accountId = accountId
        val editor = this.prefsnoclear!!.edit()
        editor.putString(KEY_ACCOUNT_ID, accountId)
        editor.commit()
    }


    fun logout() {
        saveAuthToken(null)
        prefs!!.edit().clear().commit()
        _userInfo = null
    }





    fun getUserProfilePic(): String {
        this.userProfilePic = this.prefs!!.getString(USER_PROFILE_PIC, "")
        return userProfilePic!!
    }

    fun saveUserProfilePic(c: String) {
        this.userProfilePic = c
        val editor = this.prefs!!.edit()
        editor.putString(USER_PROFILE_PIC, userProfilePic)
        editor.commit()
    }

    fun getUserProfileName(): String {
        this.userProfileName = this.prefs!!.getString(USER_PROFILE_NAME, "")
        return userProfileName!!
    }

    fun saveUserProfileName(c: String) {
        this.userProfileName = c
        val editor = this.prefs!!.edit()
        editor.putString(USER_PROFILE_NAME, userProfileName)
        editor.commit()
    }


    fun setOTPExpiryTimeStamp(timeStamp: Long) {
        try {
            this.otpexpirtytimestam = timeStamp
            val editor = this.prefs!!.edit()
            editor.putLong(OTPEXPIRTYTIMESTAMP, otpexpirtytimestam!!)
            editor.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun getOTPExpiryTimeStamp(): Long {
        try {
            this.otpexpirtytimestam = this.prefs!!.getLong(OTPEXPIRTYTIMESTAMP, 0)
            return otpexpirtytimestam!!
        } catch (e: Exception) {
            e.printStackTrace()
            return 0
        }

    }

    fun setOtpDuration(duration: Long) {
        try {
            if (duration != 0L) {
                this.otpresendduration = duration
                val editor = this.prefs!!.edit()
                editor.putLong(OTPRESENDDURATIONSAVED, otpresendduration!!)
                editor.commit()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun getOtpDuration(): Long {
        try {
            this.otpresendduration = this.prefs!!.getLong(OTPRESENDDURATIONSAVED, 0)
            return otpresendduration!!
        } catch (e: Exception) {
            e.printStackTrace()
            return 0
        }

    }

    fun getLoggedInStatus(): Boolean {
        return this.prefsnoclear!!.getBoolean(LOGGED_IN_BEFORE, false)
    }

    fun saveLoggedInStatus(b: Boolean) {
        val editor = this.prefsnoclear!!.edit()
        editor.putBoolean(LOGGED_IN_BEFORE, b)
        editor.commit()
    }



    companion object {

        private var _userInfo: UserInfoManager? = null
        private val FILE_NAME = "fast_market"
        private val FILE_NAME_NOCLR = "fast_market_sec_no_clr"

        fun getInstance(c: Context): UserInfoManager {
            if (_userInfo == null) {
                _userInfo = UserInfoManager()
                _userInfo!!.openPrefs(c.applicationContext)
                _userInfo!!.openPrefsNoclear(c.applicationContext)
            }

            return _userInfo!!
        }
    }

}