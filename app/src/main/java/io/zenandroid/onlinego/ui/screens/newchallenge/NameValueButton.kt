package io.zenandroid.onlinego.ui.screens.newchallenge

import android.app.AlertDialog
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doOnTextChanged
import io.zenandroid.onlinego.R

class NameValueButton : FrameLayout {

    var name: String? = null
        set(value) {
            field = value
            findViewById<TextView>(R.id.nameView).text = value
        }

    var value: String = ""
        set(value) {
            field = value
            findViewById<TextView>(R.id.valueView).apply {
                text = value
                setTextColor(ResourcesCompat.getColor(this.resources, R.color.colorActionableText, null))
            }
        }

    var valuesCallback: (() -> List<String?>)? = null
    var onChangeCallback: ((String) -> Unit)? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        val view = View.inflate(context, R.layout.view_name_value_button, this)
        setOnClickListener {
            valuesCallback?.invoke()?.let { list ->
                val array = list.toTypedArray()
                AlertDialog.Builder(context)
                        .setItems(array) { _, which ->
                            value = array[which] ?: ""
                        }
                        .setCancelable(true)
                        .create()
                        .show()
            }
        }
        findViewById<TextView>(R.id.valueView).doOnTextChanged { text, start, count, after -> 
            onChangeCallback?.invoke(text?.toString() ?: "")
        }
    }

}
