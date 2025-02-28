package com.example.h2omanager

import android.animation.ObjectAnimator
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AdminConntactActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_admin_conntact)

        val textView = findViewById<TextView>(R.id.blinking_text)

        // Create an ObjectAnimator to blink the text
        val blinkAnimator = ObjectAnimator.ofFloat(textView, "alpha", 0f, 1f).apply {
            duration = 500 // Duration for one fade-in or fade-out
            repeatMode = ObjectAnimator.REVERSE
            repeatCount = ObjectAnimator.INFINITE // Repeat infinitely
        }

        // Start the animation
        blinkAnimator.start()

        // Set up touch-to-copy functionality
        textView.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copied Text", textView.text)
            clipboard.setPrimaryClip(clip)

            // Show a Toast to confirm the text is copied
            Toast.makeText(this, "Text Copied!", Toast.LENGTH_SHORT).show()
        }

    }
}