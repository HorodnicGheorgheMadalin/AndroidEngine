package com.hgm.androidengine;

import android.opengl.GLES32;

import com.hgm.solarSystem.V3;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class OrbitPath {
    private FloatBuffer vertexBuffer;
    private int numVertices;
    private ShaderProgram mShaderProgram;

    public OrbitPath(V3 orbitCenter, float orbitRadius) {
        mShaderProgram = null;
        updateOrbitPath(orbitCenter, orbitRadius);
    }

    public void updateOrbitPath(V3 orbitCenter, float orbitRadius) {
        if (orbitCenter == null) return;
        numVertices = 360;
        float[] orbitVertices = new float[numVertices * 3];
        for (int i = 0; i < numVertices; i++) {
            float angle = (float) Math.toRadians(i);
            orbitVertices[i * 3] = (float) (orbitCenter.GetX() + orbitRadius * Math.cos(angle));
            orbitVertices[i * 3 + 1] = (float) orbitCenter.GetY();
            orbitVertices[i * 3 + 2] = (float) (orbitCenter.GetZ() + orbitRadius * Math.sin(angle));
        }

        ByteBuffer bb = ByteBuffer.allocateDirect(orbitVertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(orbitVertices);
        vertexBuffer.position(0);
    }

    public void setShader(ShaderProgram shader) {
        mShaderProgram = shader;
    }

    public void draw(float[] viewMatrix, float[] projectionMatrix) {
        if (mShaderProgram == null || vertexBuffer == null) {
            return;
        }
        mShaderProgram.useShader();

        int mvpMatrixHandle = GLES32.glGetUniformLocation(mShaderProgram.m_programHandle, "u_MVPMatrix");
        int positionHandle = GLES32.glGetAttribLocation(mShaderProgram.m_programHandle, "a_Position");

        float[] modelMatrix = new float[16];
        android.opengl.Matrix.setIdentityM(modelMatrix, 0);

        float[] modelViewMatrix = new float[16];
        android.opengl.Matrix.multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0);

        float[] mvpMatrix = new float[16];
        android.opengl.Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0);

        GLES32.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);

        GLES32.glEnableVertexAttribArray(positionHandle);
        GLES32.glVertexAttribPointer(positionHandle, 3, GLES32.GL_FLOAT, false, 0, vertexBuffer);

        GLES32.glDrawArrays(GLES32.GL_LINE_LOOP, 0, numVertices);

        GLES32.glDisableVertexAttribArray(positionHandle);
    }
}
