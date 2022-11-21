package com.example.test.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.Log
import com.example.test.R
import com.example.test.activity.ApplicationWrapper
import com.example.test.utils.dp

/**
 * @author: chengwen
 * @date: 2022/11/20
 */
class CircularVolumeDrawable : Drawable() {

    companion object {
        private const val TIME_NONE = 0
        private const val TIME_FAST = 280
        private const val TIME_MIDDLE = 320
        private const val TIME_SLOW = 360
        private const val TAG = "RippleVolumeDrawable"
    }

    var volume = 0f
        set(value) {
            field = value
            anim(value)
        }
    var animationListener: AnimatorListenerAdapter? = null
        set(value) {
            field = value
            animation.addListener(value)
        }
    private var currentTime = TIME_SLOW
    private var baseRatio = 0.62f
    private var leftBaseRatio = 1 - baseRatio
    private var currentRadius = baseRatio

    //渐变颜色
    private val mGradientColors = intArrayOf(
        ApplicationWrapper.instance.getColor(R.color.white_25),
        0x00ffffff,
        0x00ffffff,
        ApplicationWrapper.instance.getColor(R.color.white_25),
    )

    //渐变位置
    private val mGradientPosition = floatArrayOf(0f, 0.3f, 0.7f, 1f)
    private var shader: LinearGradient? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        strokeWidth = 11.dp().toFloat()
    }

    private val animation = ValueAnimator.ofInt(0, TIME_SLOW)

    init {
        animation.apply {
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {

                }
            })
            addUpdateListener {
                val time = it.animatedValue as Int
                val ratio = (time.toFloat() / currentTime)
                paint.alpha = ((1 - ratio) * 255).toInt()
                currentRadius = baseRatio + (ratio * leftBaseRatio)
                Log.d(TAG, "time = $time, alpha = ${paint.alpha}, radius = $currentRadius")
                invalidateSelf()
            }
        }
    }

    override fun onBoundsChange(bounds: Rect?) {
        super.onBoundsChange(bounds)
        bounds?.apply {
            shader = LinearGradient(0f, 0f, bounds.width().toFloat(), 0f, mGradientColors, mGradientPosition, Shader.TileMode.CLAMP)
        }
    }

    override fun draw(canvas: Canvas) {
        paint.shader = shader
        canvas.drawCircle(
            (bounds.width() / 2).toFloat(),
            (bounds.height() / 2).toFloat(),
            (bounds.width() / 2) * currentRadius,
            paint
        )
    }

    private fun anim(volume: Float) {
        if (animation.isRunning) {
            return
        }
        Log.d(TAG, "volume = $volume")
        if (volume < 0.01f) {
            animation.cancel()
        }
        currentTime = when {
            volume < 0.3 -> {
                TIME_SLOW
            }
            volume < 0.6 -> {
                TIME_MIDDLE
            }
            else -> {
                TIME_FAST
            }
        }
        animation.duration = currentTime.toLong()
        animation.setIntValues(0, currentTime)
        animation.start()
    }

    override fun setAlpha(alpha: Int) {
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
    }

    @Deprecated(
        "Deprecated in Java",
        ReplaceWith("PixelFormat.TRANSLUCENT", "android.graphics.PixelFormat")
    )
    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }
}