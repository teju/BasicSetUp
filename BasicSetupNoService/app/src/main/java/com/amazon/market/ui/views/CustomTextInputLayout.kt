package com.amazon.market.ui.views

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import com.amazon.market.R
import org.buffer.android.buffertextinputlayout.BufferTextInputLayout

/**
 * Contributor Pasca
 * <br></br><br></br>
 * Description : <br></br>
 * TextInputLayout that hint text size same as the floating size
 * (the original one the floating text size is smaller than the hint size)
 */
class CustomTextInputLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BufferTextInputLayout(context, attrs, defStyleAttr) {

    private var mainHintTextSize: Float = 0.toFloat()
    private var editTextSize: Float = 0.toFloat()

    init {

        val a = context.obtainStyledAttributes(
            attrs, R.styleable.CustomTextInputLayout
        )

        setUseDefault(true)
        orientation = LinearLayout.VERTICAL

        mainHintTextSize = a.getDimensionPixelSize(
            R.styleable.CustomTextInputLayout_mainHintTextSize, 0
        ).toFloat()

        a.recycle()
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        val b = child is EditText && mainHintTextSize > 0

        if (b) {
            val e = child as EditText
            editTextSize = e.textSize
            e.setTextSize(TypedValue.COMPLEX_UNIT_PX, mainHintTextSize)
        }

        super.addView(child, index, params)


        if (b) {
            editText!!.setTextSize(TypedValue.COMPLEX_UNIT_PX, editTextSize)
        }
    }

    // Units are pixels.

    fun getMainHintTextSize(): Float {
        return mainHintTextSize
    }

    // This optional method allows for dynamic instantiation of this class and
    // its EditText, but it cannot be used after the EditText has been added.
    // Units are scaled pixels.

    fun setMainHintTextSize(size: Float) {
        if (editText != null) {
            throw IllegalStateException(
                "Hint text size must be set before EditText is added"
            )
        }

        mainHintTextSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, size, resources.displayMetrics
        )
    }

}