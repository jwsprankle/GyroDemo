package com.example.gyrodemo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import java.nio.FloatBuffer

class GlobeRender(var context: Context) {

    private var vPositionHandle: Int = 0
    private var cubeFaceTextureId: Int = 0
    private var vPMatrixHandle: Int = 0
    private var mProgram: Int
    private lateinit var meshBuffer: FloatBuffer

    fun initVertexShader() {

        val vertexShaderCode =
            """
        #version 100
        attribute vec3 vPosition;

        varying vec3 textureCoord;

        uniform mat4 uMVPMatrix;

        void main() {
            textureCoord = vec3(vPosition.x, vPosition.y, vPosition.z);
            gl_Position = uMVPMatrix * vec4(vPosition, 1.0);
        }
        """

        val vertexShader: Int = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)

        // add the vertex shader to program
        GLES20.glAttachShader(mProgram, vertexShader)

    }

    fun initFragmentShader() {

        val fragmentShaderCode =
            """
        #version 100
        precision mediump float;
        varying vec3 textureCoord;
        uniform samplerCube cubemap; // cubemap texture sampler

        void main()
        {
            gl_FragColor = textureCube(cubemap, textureCoord);
//            gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);
        }            
        """

        val fragmentShader: Int = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        // add the fragment shader to program
        GLES20.glAttachShader(mProgram, fragmentShader)
    }


    fun loadShader(type: Int, shaderCode: String): Int {

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        return GLES20.glCreateShader(type).also { shader ->

            // add the source code to the shader and compile it
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
        }
    }


    private fun loadTextureCubeFaces() {

        var textureId = intArrayOf(1)
        GLES20.glGenTextures(1, textureId, 0)
        cubeFaceTextureId = textureId[0]
        GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, cubeFaceTextureId)

        var bmRes = intArrayOf(
            R.drawable.posx,    // Right:   34069
            R.drawable.negx,    // Left:    34070
            R.drawable.posy,    // Top:     34071
            R.drawable.negy,    // Bottom:  34072
            R.drawable.negz,    // Back:    34073
            R.drawable.posz     // Front:   34074
        )

        var bitmap: Bitmap?
        val options = BitmapFactory.Options()
        options.inScaled = false
        for (i in 0 until bmRes.size) {
            bitmap = BitmapFactory.decodeResource(
                context.getResources(),
                bmRes[i], options
            )

            GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, bitmap, 0)
            bitmap.recycle()
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, cubeFaceTextureId)

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
    }


    fun loadMesh() {
        // First create sphere mesh
        meshBuffer = SphereMesh.createMesh()

        // Prepare the triangle coordinate data //mesh2
        GLES20.glVertexAttribPointer(
            vPositionHandle,
            SphereMesh.SPHERE_VERTEX_DIM,
            GLES20.GL_FLOAT,
            false,
            SphereMesh.VERTEX_STRIDE_BYTES,
            meshBuffer
        )
    }


    init {

        // create empty OpenGL ES Program
        mProgram = GLES20.glCreateProgram()

        // Setup vertex shader
        initVertexShader()

        // Setup fragment shader
        initFragmentShader()

        // creates OpenGL ES program executables
        GLES20.glLinkProgram(mProgram)

        // get handle to vertex shader's vPosition member
        vPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")

        // get handle to shape's transformation matrix
        vPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix")

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(vPositionHandle)

        // Load texture faces
        loadTextureCubeFaces()

        // Load sphere mesh
        loadMesh()

    }

    fun draw(mvpMatrix: FloatArray) { // pass in the calculated transformation matrix

        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram)

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(vPositionHandle)

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, mvpMatrix, 0)

        // Draw the triangles
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, SphereMesh.SPHERE_MESH_VERTEX_COUNT)

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(vPositionHandle)
    }
}

