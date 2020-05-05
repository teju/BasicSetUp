package com.amazon.market.ui.fragments.dialog

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.DatePicker
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.TextView
import com.amazon.market.R
import com.amazon.market.etc.callback.DatePickerListener
import kotlinx.android.synthetic.main.datepickerdialogfragment.*
import java.util.*


class DatePickerDialogFragment : BaseDialogFragment() {

    val DATEPICKERFRAGMENT_LAYOUT = R.layout.datepickerdialogfragment

    companion object {
        val TAG = "DatePickerDialogFragment"
    }

    lateinit var listener: DatePickerListener
    var initDate: Date? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Locale.setDefault(Locale("en", "US"))
        v = inflater.inflate(DATEPICKERFRAGMENT_LAYOUT, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        orderDate(dp, charArrayOf('d', 'm', 'y'))

        val today = Date()
        val c = Calendar.getInstance()
        c.time = today
        c.add(Calendar.YEAR, -100)

        dp.minDate = c.getTime().getTime()

        c.time = today
        c.add(Calendar.DAY_OF_MONTH, -1)

        dp.maxDate = c.getTime().getTime()

        if(initDate != null) {
            c.time = initDate
            dp.updateDate(
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
            )
        }

        btnConfirmDate.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                listener?.let {
                    listener.onDateSet(dp.dayOfMonth, dp.month, dp.year)
                }
                this@DatePickerDialogFragment.dismiss()
            }
        })

    }

    private val SPINNER_COUNT = 3

    private fun orderDate(dp: DatePicker, ymdOrder: CharArray) {

        val idYear = Resources.getSystem().getIdentifier("year", "id", "android")
        val idMonth = Resources.getSystem().getIdentifier("month", "id", "android")
        val idDay = Resources.getSystem().getIdentifier("day", "id", "android")
        val idLayout = Resources.getSystem().getIdentifier("pickers", "id", "android")

        val spinnerYear = dp.findViewById<View>(idYear) as NumberPicker
        val spinnerMonth = dp.findViewById<View>(idMonth) as NumberPicker
        val spinnerDay = dp.findViewById<View>(idDay) as NumberPicker
        val layout = dp.findViewById<View>(idLayout) as LinearLayout

        layout.removeAllViews()
        for (i in 0 until SPINNER_COUNT) {
            when (ymdOrder[i]) {
                'y' -> {
                    layout.addView(spinnerYear)
                    setImeOptions(spinnerYear, i)
                }
                'm' -> {
                    layout.addView(spinnerMonth)
                    setImeOptions(spinnerMonth, i)
                }
                'd' -> {
                    layout.addView(spinnerDay)
                    setImeOptions(spinnerDay, i)
                }
                else -> throw IllegalArgumentException("Invalid char[] ymdOrder")
            }
        }
    }

    private fun setImeOptions(spinner: NumberPicker, spinnerIndex: Int) {
        val imeOptions: Int
        if (spinnerIndex < SPINNER_COUNT - 1) {
            imeOptions = EditorInfo.IME_ACTION_NEXT
        } else {
            imeOptions = EditorInfo.IME_ACTION_DONE
        }
        val idPickerInput = getResources().getIdentifier("numberpicker_input", "id", "android")
        val input = spinner.findViewById<View>(idPickerInput) as TextView
        input.imeOptions = imeOptions
    }

}