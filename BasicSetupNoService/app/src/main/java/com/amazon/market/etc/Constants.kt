package com.amazon.market.etc


open class Constants  {

    companion object {

        // If true all the data load will ignore the location
        // As the emulator doesnt provide location based functions
        val IS_DEBUGGING = false

        // App's details
        val PLATFORM = "android"

        val REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 911
        val PAGE_DEFAULT = 1
        val PAGE_LIMIT = 10



        val DATE_MD_TIME = "dd MMM HH:mmaa"
        val DATE_MD_TIME2 = "dd MMM, HH:mmaa"
        val DATE_MD = "dd MMM"
        val DATE_Y = "yyyy"
        val DATE_MDY = "$DATE_MD yyyy"
        val DATE_YMD = "$DATE_Y MMM dd"
        val DATE_EMDY = "EEE, $DATE_MDY"
        val DATE_MDYhMA = "$DATE_MDY, kk:mm a"
        val DATE_MDYhMAA = "$DATE_MDY, h:mm aa"
        val DATE_YMDAAhM = "$DATE_YMD, aa h:mm"
        val DATE_JSON = "yyyy-MM-dd"
        val DATE_TIME_JSON = "yyyy-MM-dd HH:mm:ss"
        val DATE_TIME_JSON2 = "dd-MM-yyyy HH:mm:ss"
        val TIME_HHMA = "HH:mm a"
        val TIME_HHMAA = "HH:mm aa"
        val TIME_hMA = "h:mm a"
        val TIME_hA = "h a"
        val TIME_hM = "h:mm"
        val TIME_MS = "mm:ss"
        val TIME_JSON_HM = "HH:mm"
        val TIME_JSON_HMS = "HH:mm:ss"
        val DATE_MONTH_YEAR_ONLY = "MMMM yyyy"
        val DATE_MONTH = "dd MMM yyyy"
        val DATE_MONTH_FULL = "dd MMMM yyyy"
        val DATE_MONTH_HMA = "$DATE_MONTH $TIME_HHMA"
        val DATE_MONTH_HMAA = "$DATE_MONTH HH:mm aa"
        val TIME_JSON_HMS_SSS = "HH:mm:ss.SSS"
        val DATE_CUSTOM = "dd MMM yyyy | EEE"
        val DATE_YM = "yymm"
        val DATE_CREDITCARD = "MM/yy"
        val DATE_DMY_TIME = "dd-MM-yyyy HH:mm"


        // Regex
        // Alpha numeric input
        val REGEX_PASSWORD =
            "^[a-zA-Z0-9]+$"


        val REGEX_URL_ENCODE = "%[A-Z0-9]{2}"
        val RESTRICTED_CHAR = arrayOf("\'", "\"", ";", "<", ">", "=", "(", ")", "*")




    }
}
