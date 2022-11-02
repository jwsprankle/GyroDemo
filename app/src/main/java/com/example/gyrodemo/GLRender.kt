package com.example.opengl1

import android.content.Context
import android.graphics.PointF
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.example.gyrodemo.GlobeRender

class GLRender(var context: Context) : GLSurfaceView.Renderer {
    @Volatile  // This needs to be Atomic, it ain't !
    var rotAngleDeg = PointF()


    // viewMtx is an abbreviation for "Model View Projection Matrix"
    private val projectionMatrix = FloatArray(16)
    private lateinit var mGlobeRender: GlobeRender


    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        mGlobeRender = GlobeRender(context)
    }

    override fun onDrawFrame(unused: GL10) {
        val viewMatrix = FloatArray(16)
        val modelMatrix = FloatArray(16)
        val mvpMatrix = FloatArray(16)

        // Set the model matrix
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.rotateM(modelMatrix, 0, rotAngleDeg.x, 1.0f, 0.0f ,0.0f) // Rotate around center
        Matrix.rotateM(modelMatrix, 0, rotAngleDeg.y, 0.0f, 1.0f ,0.0f) // Rotate around center
        Matrix.scaleM(modelMatrix, 0, 2.0f, 2.0f, 2.0f)

        // Set the camera position (View matrix)
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 26.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        Matrix.multiplyMM(viewMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        // Adjust for model
        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)

        // Clear frame to start with
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // Setup for back face culling
        GLES20.glEnable(GLES20.GL_CULL_FACE)
        GLES20.glCullFace(GLES20.GL_BACK)
        GLES20.glFrontFace(GLES20.GL_CCW)

        // Draw mesh
        mGlobeRender.draw(mvpMatrix)
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        val ratio: Float = width.toFloat() / height.toFloat()

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1.0f, 1.0f, 25.0f, 50.0f)
    }
}