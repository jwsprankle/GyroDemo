package com.example.gyrodemo

import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class SphereMesh {
    companion object {

        const val SPHERE_DIAMETER = 0.5f
        const val SPHERE_GRID_RES_DEG = 5
        const val SPHERE_ROWS = (180 / SPHERE_GRID_RES_DEG)
        const val SPHERE_COLS = (360 / SPHERE_GRID_RES_DEG) + 1
        const val DEGENERATE_TRIANGLES = (SPHERE_ROWS -1 ) * 2 // Used to terminate each row of triangle strips
        const val SPHERE_VERTEX_DIM = 3
        const val SPHERE_MESH_VERTEX_COUNT = (SPHERE_ROWS * SPHERE_COLS * 2) + DEGENERATE_TRIANGLES
        const val SPHERE_MESH_SIZE = SPHERE_MESH_VERTEX_COUNT * SPHERE_VERTEX_DIM
        const val SPHERE_FLOAT_SIZE_BYTES = 4
        const val VERTEX_STRIDE_BYTES = SPHERE_VERTEX_DIM * SPHERE_FLOAT_SIZE_BYTES


        fun createMesh(): FloatBuffer {

            var meshFA = FloatArray(SPHERE_MESH_SIZE)
            var bufNdx = 0
            var sphereMtx = FloatArray(16)

            // Local function to create point for row/col
            fun createPoint(col:Int, row:Int) {
                var zRot = ((row.toFloat() * SPHERE_GRID_RES_DEG) - 90.0f)
                var yRot = ((col.toFloat() * SPHERE_GRID_RES_DEG) - 180.0f)

                Matrix.setIdentityM(sphereMtx, 0)
                Matrix.rotateM(sphereMtx, 0, yRot, 0.0f, 1.0f ,0.0f)
                Matrix.rotateM(sphereMtx, 0, zRot, 0.0f, 0.0f ,1.0f)

                var rotVect4  = floatArrayOf(SPHERE_DIAMETER, 0.0f, 0.0f, 1.0f)
                Matrix.multiplyMV(rotVect4, 0, sphereMtx, 0, rotVect4, 0)

                meshFA[bufNdx++] = rotVect4[0]
                meshFA[bufNdx++] = rotVect4[1]
                meshFA[bufNdx++] = rotVect4[2]
            }

            for (row in 0 until SPHERE_ROWS) {
                for (col in 0 until SPHERE_COLS)  {

                    // Top left point
                    createPoint(col, row + 1)

                    // Add degenerate triangle at start of each row, except row 0
                    if ( (col == 0) && (row != 0) ) {
                        createPoint(col, row + 1)
                    }

                    // Bottom left point
                    createPoint(col, row)

                    // Add degenerate triangle at end of each row, except last
                    if ( (col == SPHERE_COLS - 1) && (row != SPHERE_ROWS - 1)) {
                        createPoint(col, row)
                    }
                }
            }

            // Now convert to float buffer
            var meshFb: FloatBuffer =
                // (number of coordinate values * 4 bytes per float)
                ByteBuffer.allocateDirect(meshFA.size * 4).run {
                    // use the device hardware's native byte order
                    order(ByteOrder.nativeOrder())

                    // create a floating point buffer from the ByteBuffer
                    asFloatBuffer().apply {
                        // add the coordinates to the FloatBuffer
                        put(meshFA)
                        // set the buffer to read the first coordinate
                        position(0)
                    }
                }

            return meshFb
        }
    }
}