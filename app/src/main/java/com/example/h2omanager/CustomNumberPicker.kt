package com.example.h2omanager



import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.widget.NumberPicker

class CustomNumberPicker(context: Context, attrs: AttributeSet?) : NumberPicker(context, attrs) {

    private val textColor = Color.BLACK // Set your desired color here
    private val textSize = 50f // Set your desired text size here
    private val paint = Paint().apply {
        color = textColor
        textSize = this@CustomNumberPicker.textSize
        isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Additional custom drawing can be added here if needed
    }
}