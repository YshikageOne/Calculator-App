package com.yshikageone.calculator

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import net.objecthunter.exp4j.ExpressionBuilder
import java.text.NumberFormat
import java.util.Locale


class MainActivity : AppCompatActivity() {

    private var animationTriggered = false

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Variables
        val inputText = findViewById<TextView>(R.id.inputText)
        val horizontalScrollView = findViewById<HorizontalScrollView>(R.id.horizontalScrollView)
        val outputText = findViewById<TextView>(R.id.outputText)
        val equalButton = findViewById<Button>(R.id.EqualButton)

        //Dynamically Adjusting the text size based on length
        fun adjustTextSize() {
            val length = inputText.text.length
            val newSize = when {
                length > 15 -> 24f
                length > 10 -> 32f
                length > 5 -> 48f
                else -> 64f
            }
            inputText.setTextSize(TypedValue.COMPLEX_UNIT_SP, newSize)
            horizontalScrollView.post {
                horizontalScrollView.fullScroll(View.FOCUS_RIGHT)
            }
        }

        //Output Text Animation
        fun animateOutputText(inputText: TextView, outputText: TextView){
            //Animate output text size and color
            val outputTextSizeAnimator = ValueAnimator.ofFloat(36f, 45f)
            outputTextSizeAnimator.addUpdateListener { animation ->
                val animatedValue = animation.animatedValue as Float
                outputText.setTextSize(TypedValue.COMPLEX_UNIT_SP, animatedValue)
            }

            val outputColorAnimator = ObjectAnimator.ofArgb(outputText, "textColor", Color.GRAY, Color.WHITE)

            // Animate input text size and color
            val inputTextSizeAnimator = ValueAnimator.ofFloat(64f, 30f) // Shrink input text
            inputTextSizeAnimator.addUpdateListener { animation ->
                val animatedValue = animation.animatedValue as Float
                inputText.setTextSize(TypedValue.COMPLEX_UNIT_SP, animatedValue)
            }

            val inputColorAnimator = ObjectAnimator.ofArgb(inputText, "textColor", Color.WHITE, Color.GRAY)

            // Play animations together
            outputTextSizeAnimator.duration = 300
            outputColorAnimator.duration = 300
            inputTextSizeAnimator.duration = 300
            inputColorAnimator.duration = 300

            outputTextSizeAnimator.start()
            outputColorAnimator.start()
            inputTextSizeAnimator.start()
            inputColorAnimator.start()
        }

        //Resting Text Properties
        fun resetTextProperties(inputText: TextView, outputText: TextView){
            inputText.text = ""
            inputText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 64f)
            inputText.setTextColor(Color.WHITE)

            outputText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 36f)
            outputText.setTextColor(Color.GRAY)

            animationTriggered = false
        }

        //List of button IDs
        val buttonIds = listOf(
            R.id.ACButton, R.id.PercentButton, R.id.BackspaceButton, R.id.DivideButton,
            R.id.SevenButton, R.id.EightButton, R.id.NineButton, R.id.MultiplyButton,
            R.id.FourButton, R.id.FiveButton, R.id.SixButton, R.id.SubtractButton,
            R.id.OneButton, R.id.TwoButton, R.id.ThreeButton, R.id.AddButton,
            R.id.DblZeroButton, R.id.ZeroButton, R.id.PointButton, R.id.EqualButton
        )

        //List of button IDs and their values
        val buttonValues = mapOf(
            R.id.SevenButton to "7", R.id.EightButton to "8", R.id.NineButton to "9",
            R.id.FourButton to "4", R.id.FiveButton to "5", R.id.SixButton to "6",
            R.id.OneButton to "1", R.id.TwoButton to "2", R.id.ThreeButton to "3",
            R.id.ZeroButton to "0", R.id.DblZeroButton to "00", R.id.PointButton to ".",
            R.id.AddButton to "+", R.id.SubtractButton to "-", R.id.MultiplyButton to "*",
            R.id.DivideButton to "/", R.id.PercentButton to "%"
        )

        //Button Animations
        buttonIds.forEach { id ->
            findViewById<View>(id).setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        //Scale down the button
                        v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).start()
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        //Scale back to original size
                        v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start()
                    }
                }
                false
            }
        }

        //Click Listeners to update the input Text
        // Set click listeners for each button

        buttonValues.forEach { (id, value) ->
            findViewById<View>(id).setOnClickListener {
                inputText.append(value)
                adjustTextSize()
                horizontalScrollView.post {
                    horizontalScrollView.fullScroll(View.FOCUS_RIGHT)
                }
                if(animationTriggered){
                    resetTextProperties(inputText, outputText)
                }
            }
        }

        //AC Button
        findViewById<View>(R.id.ACButton).setOnClickListener {
            inputText.text = ""
            inputText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 64f)
            horizontalScrollView.post {
                horizontalScrollView.fullScroll(View.FOCUS_RIGHT)
            }

            if(animationTriggered){
                resetTextProperties(inputText, outputText)
            }

        }

        //Backspace Button
        findViewById<View>(R.id.BackspaceButton).setOnClickListener {
            val currentText = inputText.text.toString()
            if (currentText.isNotEmpty()) {
                inputText.text = currentText.substring(0, currentText.length - 1)
                adjustTextSize()
                horizontalScrollView.post {
                    horizontalScrollView.fullScroll(View.FOCUS_RIGHT)
                }
                if(animationTriggered){
                    resetTextProperties(inputText, outputText)
                }
            }
        }

        equalButton.setOnClickListener {
            if (!animationTriggered){
                animateOutputText(inputText, outputText)
                animationTriggered = true
            }
        }


        //Functionalities

        fun evaluateExpression(expression: String): Double?{
            return try{
                //For percentages
                val modExpr = expression.replace(Regex("(\\d+)%")) {
                    matchResult -> val number = matchResult.groupValues[1]
                    "($number / 100)"
                }

                val expr = ExpressionBuilder(modExpr).build()
                expr.evaluate()
            }catch(e: Exception){
                null
            }
        }

        //Updating output text
        inputText.addTextChangedListener(object:TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            @SuppressLint("DefaultLocale")
            override fun afterTextChanged(s: Editable?) {
                val expression = s.toString()
                val result = evaluateExpression(expression)
                outputText.text = result?.let {
                    //one before 1 billion
                    if (it > 9999999999)  {
                        // Convert to scientific notation if necessary
                        String.format("%.6E", it)
                    } else {
                        // Format the result with commas and up to 6 decimal places
                        val formattedResult = if (it == it.toInt().toDouble()) {
                            NumberFormat.getNumberInstance(Locale.US).format(it.toInt())
                        } else {
                            String.format("%.6f", it).trimEnd('0').trimEnd('.')
                        }
                        formattedResult
                    }
                } ?: ""
            }
        })

    }

}