package com.udacity

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.app.NotificationManager
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import com.google.android.material.snackbar.Snackbar
import com.udacity.utils.sendNotification
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

//This constructor is used when we create the View from the XML layout and use the theme attribute style.
class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    //width and height variables
    private var widthSize = 0
    private var heightSize = 0

    //custom attributes
    private var backGroundColor = 0
    private var textColor = 0
    private var defaultButtonColor = 0
    private var accentColor = 0

    private lateinit var rectF: RectF

    private var animatedWidth: Float = 0.0f
    private var computedAnimationValue: Float = 0.0f

    private val smallCircleAnimation = ValueAnimator.ofFloat(0f, 360f).apply {

        duration = 1000
        //set range of 0 to 360 degrees clockwise
        interpolator = LinearInterpolator()
        addUpdateListener {
            //set value of the new angle
            computedAnimationValue = it.animatedValue as Float

            if (computedAnimationValue == 360f) {
                buttonState = ButtonState.Completed
            }
            invalidate()
        }

    }

    private val buttonBackGroundAnimation = ValueAnimator.ofFloat(0f, widthSize.toFloat()).apply {
        duration = 1000
        interpolator = LinearInterpolator()
        //animate button width from 0 to the width size
        addUpdateListener {
            animatedWidth = it.animatedValue as Float
            invalidate()
        }

    }


    //takes the initial button value and a callback that is called after button state changes

    var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->
        when (new) {

            ButtonState.Loading -> {
                animateButtonBackGroundAndSmallCircleAsAnimatorSet()
            }
            ButtonState.Clicked -> {
                Toast.makeText(
                    context,
                    resources.getString(R.string.no_radio_button_clicked),
                    Toast.LENGTH_SHORT
                ).show()

            }

            ButtonState.Completed -> {
                Timber.i("Download is completed")
            }

        }
    }

    init {

        backGroundColor = Color.GREEN
        textColor = Color.WHITE
        accentColor = Color.YELLOW
        defaultButtonColor = R.color.blue

    }

    //PAINT OBJECT
    private val paint = Paint().apply {

        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 45f
    }

    //ONCLICK
    override fun performClick(): Boolean {
        buttonState = ButtonState.Loading
        return super.performClick()
    }

    //ON_DRAW
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawCommonButtonFeatures(canvas)
    }

    //ON_MEASURE
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        //Returns the left padding of this view
        val minWidth: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minWidth, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(View.MeasureSpec.getSize(w), heightMeasureSpec, 0)
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)

    }

    private fun drawCommonButtonFeatures(canvas: Canvas) {

        paint.color = defaultButtonColor
        canvas.drawRect(0f, 0f, widthSize.toFloat(), heightSize.toFloat(), paint)

        when (buttonState) {
            ButtonState.Completed -> {

                drawText(resources.getString(R.string.message_download), canvas)
            }
            ButtonState.Loading -> {

                drawAnimatedBackground(canvas)
                drawText(resources.getString(R.string.message_loading), canvas)
                drawSmallCircle(canvas)
            }
        }
    }


    //LOADING_BUTTON_BACKGROUND
    private fun drawAnimatedBackground(canvas: Canvas) {
        paint.color = backGroundColor
        canvas.drawRect(0f, 0f, computedAnimationValue * 3, heightSize.toFloat(), paint)
    }


    private fun drawSmallCircle(canvas: Canvas) {

        paint.color = accentColor

        //draw Arc
        //left
        //Top
        //right
        //bottom
        //Start Angle
        //Sweep Angle - dynamically generate
        //Use Center

        canvas.drawArc(
            (widthSize * 0.66).toFloat(),
            (heightSize * 0.2).toFloat(),
            (widthSize * 0.74).toFloat(),
            (heightSize * 0.8).toFloat(),
            0f,
            computedAnimationValue,
            true,
            paint
        )
    }

    //ANIMATE_LOADING_BUTTON_BACKGROUND
    private fun animateButtonBackGroundAndSmallCircleAsAnimatorSet() {
        AnimatorSet().apply {
            duration = TimeUnit.SECONDS.toMillis(2)
            play(buttonBackGroundAnimation).after(duration)
            play(smallCircleAnimation).before(buttonBackGroundAnimation)
        }.start()
    }


    //DRAW_TEXT_ON_BUTTONS
    private fun drawText(text: String, canvas: Canvas) {

        paint.color = textColor
        canvas.drawText(
            text,
            (widthSize / 2).toFloat(),
            (heightSize / 2) + (heightSize / 10).toFloat(),
            paint
        )
    }

}
