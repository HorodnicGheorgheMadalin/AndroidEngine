package com.hgm.androidengine;

import android.content.Context;
import android.opengl.GLES32;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import com.hgm.solarSystem.V3;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class OpenGLRenderer implements GLSurfaceView.Renderer {
    private final Context m_context;

    private Object3D Sun = null;
    private Object3D Earth = null;
    private Object3D Moon = null;
    private Object3D Ship = null;
    private Object3D[] m_vObjects = null;
    private ShaderProgram mShader;
    private ShaderProgram mLineShader;
    private float mAngleInDegrades; // General animation angle (for light, etc.)
    private float mEarthDayAngle;   // For Earth's daily rotation
    private float mSunVisualRotationAngle; // For Sun's smooth visual rotation
    private long mLastSunRotationUpdateTime;

    private OrbitPath earthOrbitPath, moonOrbitPath, shipOrbitPath;

    private final static int MATRIX_SIZE_PER_OBJECT = 16;
    private final static int NUMBER_OF_OBJECTS = 4;
    private final static int NUMBER_OF_LIGHTS = 1;
    private final static int OBJECTS_MATRIX_SIZE = MATRIX_SIZE_PER_OBJECT * NUMBER_OF_OBJECTS;
    private final static int LIGHTS_MATRIX_SIZE = MATRIX_SIZE_PER_OBJECT * NUMBER_OF_LIGHTS;
    private final static int POSITION_SIZE = 4;
    private static final float ZOOM_STEP = 5.0f; // Step for zooming in/out
    private static final float SUN_ROTATION_SPEED_DEGREES_PER_MILLISECOND = 0.009f;

    private final V3 mEyePosition = new V3(149.76, 174.72, 149.76);
    private final V3 mLookAtPoint = new V3(0, 0, 0);
    private final V3 mLookDirection = new V3(0, 1, 0);

    private final float[] mViewMatrix = new float[MATRIX_SIZE_PER_OBJECT];
    private final float[] mProjectionMatrix = new float[MATRIX_SIZE_PER_OBJECT];
    private final float[] mModelMatrix = new float[OBJECTS_MATRIX_SIZE];
    private final float[] mLightModelMatrix = new float[LIGHTS_MATRIX_SIZE];
    private final float[] mModelViewMatrix = new float[OBJECTS_MATRIX_SIZE];
    private final float[] mModelViewProjectionMatrix = new float[OBJECTS_MATRIX_SIZE];

    private final float[] mLightPosInModelSpace = new float[]{0.0f, 0.0f, 10.0f, 1.0f};
    private final float[] mLightPosInWorldSpace = new float[POSITION_SIZE];
    private final float[] mLightPosInEyeSpace = new float[POSITION_SIZE];

    float mDeltaX;
    float mDeltaY;

    OpenGLRenderer(Context context) {
        m_context = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES32.glClearColor(1.0f, 1.0f, 0.0f, 1.0f);
        GLES32.glEnable(GLES32.GL_CULL_FACE);
        GLES32.glEnable(GLES32.GL_DEPTH_TEST);
        GLES32.glDisable(GLES32.GL_BLEND);

        setViewMatrix(mEyePosition, mLookAtPoint, mLookDirection);
        mShader = new ShaderProgram(m_context);
        mLineShader = new ShaderProgram(m_context, R.raw.line_vertex_shader, R.raw.line_fragment_shader);

        getAngleInDegrades();
        mEarthDayAngle = 0;
        mSunVisualRotationAngle = 0.0f;
        mLastSunRotationUpdateTime = 0L;

        Sun = new Object3D(m_context, "Sun", R.drawable.orange);
        Sun.setPosition(new V3(0.0, 0.0, 0.0));
        Sun.setScale(new V3(15.0, 15.0, 15.0));
        Sun.setRotationAxis(new V3(0, 1, 0));
        Sun.setOrbitCenter(Sun.getPosition());
        Sun.setOrbitRadius(0.0f);
        Sun.setOrbitSpeed(0.0f);
        Sun.setOrbitAngle(0.0f);

        Earth = new Object3D(m_context, "Earth", R.drawable.blue);
        Earth.setPosition(new V3(50, 0, 0));
        Earth.setScale(new V3(3.67, 3.67, 3.67));
        double earthTiltAngleRad = Math.toRadians(23.5);
        Earth.setRotationAxis(new V3(Math.sin(earthTiltAngleRad), Math.cos(earthTiltAngleRad), 0));
        Earth.setOrbitCenter(Sun.getPosition());
        Earth.setOrbitRadius(50.0f);
        Earth.setOrbitSpeed(2.0f);
        Earth.setOrbitAngle(0.0f);


        Moon = new Object3D(m_context, "Moon", R.drawable.gray);
        float moonOrbitRadius = 25.0f;
        Moon.setPosition(new V3(Earth.getPosition().GetX() + moonOrbitRadius, Earth.getPosition().GetY(), Earth.getPosition().GetZ()));
        Moon.setScale(new V3(1.0, 1.0, 1.0));
        Moon.setRotationAxis(new V3(0, 1, 0));
        Moon.setOrbitCenter(Earth.getPosition());
        Moon.setOrbitRadius(moonOrbitRadius);
        Moon.setOrbitSpeed(5.0f);
        Moon.setOrbitAngle(90.0f);

        Ship = new Object3D(m_context, "Ship", R.drawable.rusty_iron_texture);
        float shipOrbitRadius = 30.0f;
        Ship.setPosition(new V3(Moon.getPosition().GetX() + shipOrbitRadius, Moon.getPosition().GetY(), Moon.getPosition().GetZ()));
        Ship.setScale(new V3(1.0, 1.0, 1.0));
        Ship.setRotation(0.0f);
        Ship.setRotationAxis(new V3(0, 1, 0));
        Ship.setOrbitCenter(Moon.getPosition());
        Ship.setOrbitRadius(shipOrbitRadius);
        Ship.setOrbitSpeed(10.0f);
        Ship.setOrbitAngle(0.0f);

        m_vObjects = new Object3D[4];
        m_vObjects[0] = Sun;
        m_vObjects[1] = Earth;
        m_vObjects[2] = Moon;
        m_vObjects[3] = Ship;

        earthOrbitPath = new OrbitPath(Earth.getOrbitCenter(), Earth.getOrbitRadius());
        moonOrbitPath = new OrbitPath(Moon.getOrbitCenter(), Moon.getOrbitRadius());
        shipOrbitPath = new OrbitPath(Ship.getOrbitCenter(), Ship.getOrbitRadius());
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES32.glClearColor(0, 1f, 0, 1);
        GLES32.glViewport(0, 0, width, height);
        setProjectionMatrix(width, height);
        GLES32.glEnable(GLES32.GL_CULL_FACE);
        GLES32.glEnable(GLES32.GL_DEPTH_TEST);
        GLES32.glDisable(GLES32.GL_BLEND);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES32.glClearColor(0, 1f, 0, 1);
        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT | GLES32.GL_DEPTH_BUFFER_BIT);

        long currentTime = SystemClock.uptimeMillis();
        if (mLastSunRotationUpdateTime == 0L) {
            mLastSunRotationUpdateTime = currentTime;
        }
        long deltaTimeMillis = currentTime - mLastSunRotationUpdateTime;

        mShader.useShader();
        int mModelViewProductMatrixHandle = mShader.getInputUniformParameter(mShader.m_programHandle, "u_MVPMatrix");
        int mModelViewMatrixHandle = mShader.getInputUniformParameter(mShader.m_programHandle, "u_MVMatrix");
        int mLightPosHandle = mShader.getInputUniformParameter(mShader.m_programHandle, "u_LightPos");
        int mPositionHandle = mShader.getInputAttributeParameter(mShader.m_programHandle, "a_Position");
        int mColorHandle = mShader.getInputAttributeParameter(mShader.m_programHandle, "a_Color");
        int mNormalHandle = mShader.getInputAttributeParameter(mShader.m_programHandle, "a_Normal");
        int mTextureUniformHandle = mShader.getInputUniformParameter(mShader.m_programHandle, "u_Texture");
        int mTextureCoordinateHandle = mShader.getInputAttributeParameter(mShader.m_programHandle, "a_TexCoordinate");

        GLES32.glActiveTexture(GLES32.GL_TEXTURE0);
        GLES32.glUniform1i(mTextureUniformHandle, 0);

        getAngleInDegrades();

        mEarthDayAngle += 1.0f;
        if (mEarthDayAngle >= 360.0f) {
            mEarthDayAngle -= 360.0f;
        }

        float sunRotationIncrement = deltaTimeMillis * SUN_ROTATION_SPEED_DEGREES_PER_MILLISECOND;
        mSunVisualRotationAngle = (mSunVisualRotationAngle + sunRotationIncrement) % 360.0f;
        mLastSunRotationUpdateTime = currentTime;

        setLightModelMatrix();

        Sun.setRotation(mSunVisualRotationAngle);
        Earth.setRotation(mEarthDayAngle);
        Moon.setRotation(Moon.getOrbitAngle());
        Ship.setRotation(Ship.getOrbitAngle());

        Sun.updateOrbit();

        Earth.setOrbitCenter(Sun.getPosition());
        Earth.updateOrbit();
        earthOrbitPath.updateOrbitPath(Sun.getPosition(), Earth.getOrbitRadius());

        Moon.setOrbitCenter(Earth.getPosition());
        Moon.updateOrbit();
        moonOrbitPath.updateOrbitPath(Earth.getPosition(), Moon.getOrbitRadius());

        Ship.setOrbitCenter(Moon.getPosition());
        Ship.updateOrbit();
        shipOrbitPath.updateOrbitPath(Moon.getPosition(), Ship.getOrbitRadius());


        if (Sun != null && Sun.getPosition() != null && Earth != null && Earth.getPosition() != null && Moon != null && Moon.getPosition() != null && Ship != null && Ship.getPosition() != null) {
            Log.d("OrbitDebug", "Sun: " + Sun.getPosition().print() + ", Earth: " + Earth.getPosition().print() + ", Moon: " + Moon.getPosition().print() + ", Ship: " + Ship.getPosition().print());
        }

        int nMatrixOffsetForObject = 0;
        for (int nObjectIndex = 0; nObjectIndex < m_vObjects.length; nObjectIndex++) {
            if (nObjectIndex >= NUMBER_OF_OBJECTS)
                break;

            Object3D currentObject = m_vObjects[nObjectIndex];

            Matrix.setIdentityM(mModelMatrix, nMatrixOffsetForObject);
            scaleObject(currentObject.getScale(), nMatrixOffsetForObject);
            rotateObject(currentObject.getRotation(), currentObject.getRotationAxis(), nMatrixOffsetForObject);
            setModelMatrix(currentObject.getPosition(), nMatrixOffsetForObject);

            drawTexture(currentObject.mTextureDataHandle);
            setModelViewMatrix(nMatrixOffsetForObject);
            setModelViewProductMatrix(nMatrixOffsetForObject);
            currentObject.draw(mPositionHandle, mColorHandle, mNormalHandle, mTextureCoordinateHandle);
            GLES32.glUniformMatrix4fv(mModelViewMatrixHandle, 1, false, mModelViewMatrix, nMatrixOffsetForObject);
            GLES32.glUniformMatrix4fv(mModelViewProductMatrixHandle, 1, false, mModelViewProjectionMatrix, nMatrixOffsetForObject);
            nMatrixOffsetForObject += MATRIX_SIZE_PER_OBJECT;
        }

        GLES32.glUniform3f(mLightPosHandle, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);

        // Draw orbit paths
        mLineShader.useShader();
        int colorHandle = GLES32.glGetUniformLocation(mLineShader.m_programHandle, "v_Color");

        GLES32.glUniform4f(colorHandle, 1.0f, 1.0f, 1.0f, 1.0f); // White color for orbit lines
        earthOrbitPath.setShader(mLineShader);
        earthOrbitPath.draw(mViewMatrix, mProjectionMatrix);

        GLES32.glUniform4f(colorHandle, 0.8f, 0.8f, 0.8f, 1.0f); // Light gray for moon orbit
        moonOrbitPath.setShader(mLineShader);
        moonOrbitPath.draw(mViewMatrix, mProjectionMatrix);

        GLES32.glUniform4f(colorHandle, 0.6f, 0.6f, 0.6f, 1.0f); // Darker gray for ship orbit
        shipOrbitPath.setShader(mLineShader);
        shipOrbitPath.draw(mViewMatrix, mProjectionMatrix);
    }

    public void setViewMatrix(V3 eyePosition, V3 lookAtPoint, V3 lookDirection) {
        mEyePosition.set(eyePosition.GetX(), eyePosition.GetY(), eyePosition.GetZ());
        mLookAtPoint.set(lookAtPoint.GetX(), lookAtPoint.GetY(), lookAtPoint.GetZ());
        mLookDirection.set(lookDirection.GetX(), lookDirection.GetY(), lookDirection.GetZ());
        Matrix.setLookAtM(mViewMatrix, 0, (float) mEyePosition.GetX(), (float) mEyePosition.GetY(), (float) mEyePosition.GetZ(),
                (float) mLookAtPoint.GetX(), (float) mLookAtPoint.GetY(), (float) mLookAtPoint.GetZ(),
                (float) mLookDirection.GetX(), (float) mLookDirection.GetY(), (float) mLookDirection.GetZ());
    }

    public void zoomIn() {
        Log.d("ZoomDebug", "Before zoomIn - Eye: " + mEyePosition.print());
        V3 viewDirection = new V3(
                mLookAtPoint.GetX() - mEyePosition.GetX(),
                mLookAtPoint.GetY() - mEyePosition.GetY(),
                mLookAtPoint.GetZ() - mEyePosition.GetZ()
        );
        viewDirection.normalize();
        viewDirection.scale(ZOOM_STEP);

        mEyePosition.add(viewDirection);
        Log.d("ZoomDebug", "After zoomIn - Eye: " + mEyePosition.print() + ", ViewDirectionStep: " + viewDirection.print());
        setViewMatrix(mEyePosition, mLookAtPoint, mLookDirection);
    }

    public void zoomOut() {
        Log.d("ZoomDebug", "Before zoomOut - Eye: " + mEyePosition.print());
        V3 viewDirection = new V3(
                mLookAtPoint.GetX() - mEyePosition.GetX(),
                mLookAtPoint.GetY() - mEyePosition.GetY(),
                mLookAtPoint.GetZ() - mEyePosition.GetZ()
        );
        viewDirection.normalize();
        viewDirection.scale(ZOOM_STEP);

        mEyePosition.sub(viewDirection);
        Log.d("ZoomDebug", "After zoomOut - Eye: " + mEyePosition.print() + ", ViewDirectionStep: " + viewDirection.print());
        setViewMatrix(mEyePosition, mLookAtPoint, mLookDirection);
    }

    private void setProjectionMatrix(int width, int height) {
        final float mRatio = (float) width / height;
        final float left = -mRatio;
        final float top = 1.0f;
        final float bottom = -1.0f;
        final float near = 0.8f;
        final float far = 1000.0f;
        Matrix.frustumM(mProjectionMatrix, 0, left, mRatio, bottom, top, near, far);
    }

    private void setModelViewProductMatrix(int objectIndex) {
        Matrix.multiplyMM(mModelViewProjectionMatrix, objectIndex, mProjectionMatrix, 0, mModelViewMatrix, objectIndex);
    }

    private void setModelMatrix(V3 position, int nIndex) {
        Matrix.translateM(mModelMatrix, nIndex, (float) position.GetX(), (float) position.GetY(), (float) position.GetZ());
    }

    private void scaleObject(V3 scale, int nIndex) {
        Matrix.scaleM(mModelMatrix, nIndex, (float) scale.GetX(), (float) scale.GetY(), (float) scale.GetZ());
    }

    private void rotateObject(float angle, V3 rotationAxis, int nIndex) {
        Matrix.rotateM(mModelMatrix, nIndex, angle, (float) rotationAxis.GetX(), (float) rotationAxis.GetY(), (float) rotationAxis.GetZ());
    }

    private void setLightModelMatrix() {
        Matrix.setIdentityM(mLightModelMatrix, 0);
        Matrix.rotateM(mLightModelMatrix, 0, mAngleInDegrades, 0.0f, 1.0f, 0.0f);
        Matrix.multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0);
        Matrix.multiplyMV(mLightPosInEyeSpace, 0, mViewMatrix, 0, mLightPosInWorldSpace, 0);
    }

    private void getAngleInDegrades() {
        long time = SystemClock.uptimeMillis() % 4000L;
        mAngleInDegrades = 0.090f * ((int) time);
    }

    private void drawTexture(int textureDataHandle) {
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, textureDataHandle);
    }

    private void setModelViewMatrix(int nObjectNo) {
        Matrix.multiplyMM(mModelViewMatrix, nObjectNo, mViewMatrix, 0, mModelMatrix, nObjectNo);
    }
}
