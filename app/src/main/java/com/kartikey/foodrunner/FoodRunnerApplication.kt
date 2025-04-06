package com.kartikey.foodrunner

import android.app.Application
import com.google.firebase.FirebaseApp

class FoodRunnerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
} 