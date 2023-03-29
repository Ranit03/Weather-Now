package com.example.weathernow

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import com.example.weathernow.Activities.MainActivity

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        val backgrounding : TextView = findViewById(R.id.textView3)
        val sideAnimation = AnimationUtils.loadAnimation(this,R.anim.slide)
        backgrounding.startAnimation(sideAnimation)

        Handler().postDelayed({
            startActivity(Intent(this,LoginActivity::class.java))
            finish()
        },3000)
    }
}