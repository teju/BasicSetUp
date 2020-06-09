package com.maa.tiffens


import android.app.Application
import android.content.res.Configuration
import com.franmontiel.localechanger.LocaleChanger
import java.util.*

class commonApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        LocaleChanger.initialize(applicationContext, SUPPORTED_LOCALES)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        LocaleChanger.onConfigurationChanged()
    }

    companion object {
        val SUPPORTED_LOCALES = Arrays.asList(
            Locale("en", "US"),
            Locale("cn", "CN")
        )
    }
}