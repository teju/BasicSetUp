package com.amazon.market.etc

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.InsetDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Process
import android.os.StrictMode
import android.provider.Settings
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.annotation.Nullable
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.fragment.app.Fragment
import com.amazon.market.R
import org.joda.time.DateTime
import org.joda.time.Seconds

import java.io.IOException
import java.io.InputStream
import java.io.PrintWriter
import java.io.StringWriter
import java.io.*
import java.math.BigDecimal
import java.security.InvalidKeyException
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.spec.InvalidKeySpecException
import java.security.spec.X509EncodedKeySpec
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException

open class Helper {




    companion object {
        val listFragmentsMainTab: ArrayList<String>
            get() {
                val list = ArrayList<String>()

                list.add("FIRST_TAB")
                list.add("SECOND_TAB")
                list.add("THIRD_TAB")
                list.add("FOURTH_TAB")
                list.add("FIFTH_TAB")

                return list
            }

        fun getMillis(curTime: DateTime, endTime: DateTime): Long {
            // curTime.withZone(endTime.getZone());
            // return new Interval(curTime, endTime).toDurationMillis();
            return Seconds.secondsBetween(curTime.toLocalTime(), endTime.toLocalTime()).seconds.toLong()
        }

        fun textDecorIU(text: String): SpannableString {
            val content = SpannableString(text)
            content.setSpan(UnderlineSpan(), 0, text.length, 0)
            content.setSpan(StyleSpan(Typeface.ITALIC), 0, text.length, 0)
            return content
        }

        fun intentMap(context: Context, name: String, subName: String, url: String) {
            var url = url
            url += "($3,$4)"
            if (!BaseHelper.isEmpty(name))
                url.replace("$3", name)
            else
                url.replace("$3", "")

            if (!BaseHelper.isEmpty(subName))
                url.replace("$4", subName)
            else
                url.replace("$4", "")

            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }


        fun intentCall(context: Context, number: String) {
            if (BaseHelper.isEmpty(number)) {
                return
            }

            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:" + number.replace(" ", ""))
            context.startActivity(intent)
        }

        fun intentEmail(context: Context, email: String, subject: String) {
            val emailIntent = Intent(Intent.ACTION_SENDTO)
            emailIntent.data = Uri.parse("mailto:")
            emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
            context.startActivity(Intent.createChooser(emailIntent, "Send mail.."))
        }

        fun intentWeb(context: Context?, url: String) {
            var url = url
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context!!.startActivity(browserIntent)
            } catch (e: Exception) {
                try {
                    url = "https://" + "url"
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context!!.startActivity(browserIntent)
                } catch (e1: Exception) {
                    e1.printStackTrace()
                }

            }

        }

        fun intentPlaystore(context: Context, url: String) {
            val marketUri = Uri.parse(url)
            val marketIntent = Intent(Intent.ACTION_VIEW, marketUri)
            context.startActivity(marketIntent)
        }

        fun intentPdf(frag: Fragment, url: String) {
            try {
                val intentUrl = Intent(Intent.ACTION_VIEW)
                intentUrl.setDataAndType(Uri.parse(url), "application/pdf")
                intentUrl.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                frag.startActivity(intentUrl)
            } catch (e: ActivityNotFoundException) {
                Helper.intentWeb(frag.activity, url)
            }

        }

        fun intentMarketSSC(context: Context) {
            val appPackageName = context.packageName
            try {
                (context as Activity).startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=$appPackageName")
                    )
                )
            } catch (anfe: ActivityNotFoundException) {
                (context as Activity).startActivity(
                    Intent(
                        Intent.ACTION_VIEW, Uri
                            .parse("http://play.google.com/store/apps/details?id=$appPackageName")
                    )
                )
            }

        }

        fun isValidName(edt: EditText): Boolean {
            val p = Pattern.compile(".*\\d+.*")
            val m = p.matcher(edt.text.toString().trim { it <= ' ' })

            return if (m.matches()) {
                false
            } else true
        }

        fun logException(ctx: Context?, e: Exception?) {
            try {
                if (Constants.IS_DEBUGGING) {
                    if (Constants.IS_DEBUGGING) {
                        if (ctx != null)
                            Log.v(ctx.getString(R.string.app_name), getStackTrace(e!!))
                        else
                            print(getStackTrace(e!!))
                    }
                }
            } catch (e1: Exception) {
            }

        }
        fun logException(ctx: Context?, e: String?) {
            try {
                if (Constants.IS_DEBUGGING) {
                    if (Constants.IS_DEBUGGING) {
                        if (ctx != null)
                            Log.v(ctx.getString(R.string.app_name), e)
                        else
                            print(e)
                    }
                }
            } catch (e1: Exception) {
            }

        }
        fun showLog(str : String) {
            try {
                if (Constants.IS_DEBUGGING) {
                    System.out.println()
                }
            } catch (e1: Exception) {
            }
        }
        fun getStackTrace(throwable: Throwable): String {
            val sw = StringWriter()
            val pw = PrintWriter(sw, true)
            throwable.printStackTrace(pw)
            return sw.buffer.toString()
        }
        fun hideKeyboard(activity: Activity) {
            val inputManager = activity
                .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (activity.currentFocus != null) {
                inputManager.hideSoftInputFromWindow(
                    activity.currentFocus!!
                        .windowToken, InputMethodManager.HIDE_NOT_ALWAYS
                )
            } else {
                //			inputManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
            }
        }
        @JvmStatic
        fun visibleView(v: View?) {
            if (v != null) {
                v.visibility = View.VISIBLE
            }
        }

        /**
         * Change the state of a view to View.INVISIBLE
         *
         * @param v , view to be updated
         */
        @JvmStatic
        fun invisibleView(v: View?) {
            if (v != null) {
                v.visibility = View.INVISIBLE
            }
        }
        @JvmStatic
        fun goneView(v: View?) {
            if (v != null) {
                v.visibility = View.GONE
            }

        }

    }

}