package com.yshikageone.calculator

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)

        //Delay for 1 second before transitioning to the second splash screen
        Handler(Looper.getMainLooper()).postDelayed({
            //Fade out the first splash screen
            val fadeOut = AlphaAnimation(1.0f, 0.0f)
            fadeOut.duration = 1000
            fadeOut.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}
                override fun onAnimationRepeat(animation: Animation) {}
                override fun onAnimationEnd(animation: Animation) {
                    //Set the second splash screen layout
                    setContentView(R.layout.splash_screen_white)

                    //Fade in the second splash screen
                    val fadeIn = AlphaAnimation(0.0f, 1.0f)
                    fadeIn.duration = 1000
                    findViewById<View>(R.id.logoImageView).startAnimation(fadeIn)
                    findViewById<View>(R.id.textView).startAnimation(fadeIn)

                    //Delay for another 1 second before transitioning to MainActivity
                    Handler(Looper.getMainLooper()).postDelayed({
                        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                        finish()
                    }, 1000)
                }
            })
            findViewById<View>(R.id.logoImageView).startAnimation(fadeOut)
        }, 1000)
    }
}