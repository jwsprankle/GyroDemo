package com.example.gyrodemo

import android.content.Context
import android.graphics.PointF
import android.opengl.GLSurfaceView
import android.os.Handler
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.minus
import com.example.opengl1.GLRender
import kotlin.math.asin

class GlobeSurfaceView : GLSurfaceView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    private var mGLRender: GLRender
    private var touchDownPos = PointF()
    private val screenCenter = PointF(context.getResources().getDisplayMetrics().widthPixels.toFloat() / 2.0f,
        context.getResources().getDisplayMetrics().heightPixels.toFloat() / 2.0f)


    init {
        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2)

        // Set the Renderer for drawing on the GLSurfaceView
        mGLRender = GLRender(context)

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(mGLRender)

        // Render the view only when there is a change in the drawing data
        renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY

        mGLRender.rotAngleDeg.x = 0.0f
        mGLRender.rotAngleDeg.y = 0.0f

    }


    override fun onTouchEvent(e: MotionEvent): Boolean {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                touchDownPos = cursorPos(e)
            }

            MotionEvent.ACTION_MOVE -> {
                var diffPos = cursorPos(e) - touchDownPos
                var theta = Math.toDegrees(asin(diffPos.x / 0.5)).toFloat()
                var phi = Math.toDegrees(asin(diffPos.y / 0.5)).toFloat()
                if (!theta.isNaN() && !phi.isNaN()) {
                    mGLRender.rotAngleDeg.y = (theta * 2.25f)
                    mGLRender.rotAngleDeg.x = (-phi * 2.25f)
                    requestRender()
                }
            }
        }

        return true
    }

    fun cursorPos(e: MotionEvent) : PointF {
        var radiusPixels = screenCenter.y
        var relToCenter = PointF(e.x - screenCenter.x, screenCenter.y - e.y)
        return PointF(relToCenter.x / radiusPixels, relToCenter.y /  radiusPixels)
    }
}

