package com.rackaapps.colorviews

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.rackaapps.colorviews.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setClicks()
    }

    //Identify clicks on the text views and buttons then color them
    private fun setClicks() {
        val clickableViews: List<View> = listOf(binding.boxOneText,
                binding.boxTwoText,
                binding.boxThreeText,
                binding.boxFourText,
                binding.backgroundView,
                binding.boxFiveText,
                binding.redButton,
                binding.yellowButton,
                binding.greenButton)
        for (view in clickableViews) {
            view.setOnClickListener { makeItColored(it) }
        }
    }

    //Apply colors on the views
    private fun makeItColored(view: View) {
        binding.apply {
            when(view) {
                boxOneText -> view.setBackgroundColor(resources.getColor(R.color.my_light_maroon))
                boxTwoText -> view.setBackgroundColor(resources.getColor(R.color.my_blue))
                boxThreeText -> view.setBackgroundColor(resources.getColor(R.color.my_orange))
                boxFourText -> view.setBackgroundColor(resources.getColor(R.color.my_purple))
                boxFiveText -> view.setBackgroundColor(resources.getColor(R.color.teal_200))
                redButton -> {
                    view.setBackgroundColor(resources.getColor(R.color.button_red))
                    boxThreeText.setBackgroundColor(resources.getColor(R.color.button_red))
                }
                yellowButton -> {
                    view.setBackgroundColor(resources.getColor(R.color.button_yellow))
                    boxFourText.setBackgroundColor(resources.getColor(R.color.button_yellow))
                }
                greenButton -> {
                    view.setBackgroundColor(resources.getColor(R.color.button_green))
                    boxFiveText.setBackgroundColor(resources.getColor(R.color.button_green))
                }
                else -> view.setBackgroundColor(Color.LTGRAY)
            }
        }
    }
}