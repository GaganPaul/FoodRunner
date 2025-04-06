package com.kartikey.foodrunner.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.kartikey.foodrunner.R

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Using Handler with Looper.getMainLooper() for compatibility
        // Using anonymous Runnable instead of lambda for compatibility with older Android versions
        Handler(Looper.getMainLooper()).postDelayed(
            object : Runnable {
                override fun run() {
                    val intent = Intent(this@SplashActivity, LoginRegisterActivity::class.java)
                    finish()
                    startActivity(intent)
                }
            }, 1000
        )
    }
}