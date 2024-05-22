package com.hgm.androidengine;

import android.content.Context;
import android.opengl.GLES32;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

import com.hgm.solarSystem.V3;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class OpenGLRenderer implements GLSurfaceView.Renderer
{
    private final Context   m_context;

    //  TODO Change object to system
    //private SolarSystem     mSystem = null;

    private final int SOLAR_OBJECT_INDEX = 0;
    private final int SHIP_OBJECT_INDEX = 1;

    private Object3D Sun = null;
    private Object3D Earth = null;
    private Object3D Moon = null;
    private Object3D Ship = null;
    private Object3D[]      m_vObjects = null;
    private ShaderProgram   mShader;
    private float           mAngleInDegrades;

    /** These are handles to our texture data. */
    //private int mBrickDataHandle, mGrassDataHandle, mDirtDataHandle;
    private int mQueuedMagFilter, mQueuedMinFilter;

    //  Used to switch between different blending modes
    private boolean mBlending = true;

    //  TODO - The maximum number of objects our engine can handle is currently 3
    private final static int MATRIX_SIZE = 16*2;

    //  Used to transform the world space into ou eys
    private final float[] mViewMatrix                 = new float[MATRIX_SIZE];
    //  Used to project the scene into a 2D view port
    private final float[] mProjectionMatrix           = new float[MATRIX_SIZE];
    //  Used to move models in world space
    private final float[] mModelMatrix                = new float[MATRIX_SIZE];
    private final float[] mModelViewMatrix            = new float[MATRIX_SIZE];
    private final float[] mModelViewProjectionMatrix  = new float[MATRIX_SIZE];
    private final float[] mLightModelMatrix           = new float[MATRIX_SIZE];

    //  The light position with a fourth filler coordinate needed for multiplication with transformation marice
    private final float[] mLightPosInModelSpace   = new float[] { 0.0f, 0.0f, 0.0f, 1.0f};
    private final float[] mLightPosInWorldSpace   = new float[4];
    private final float[] mLightPosInEyeSpace     = new float[4];

    float mDeltaX;
    float mDeltaY;
    //  TODO(ME)  : USE Z
    //float mDeltaZ;

    OpenGLRenderer(Context context)
    {
        m_context = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
      GLES32.glClearColor(1.0f, 1.0f, 0.0f, 1.0f);

        //  Cull back faces
        GLES32.glEnable(GLES32.GL_CULL_FACE);
        //  Enable depth testing
        GLES32.glEnable(GLES32.GL_DEPTH_TEST);
        //  Disable blending
        GLES32.glDisable(GLES32.GL_BLEND);

      //  Set Camera
      setViewMatrix();

      //  Create Shader Program
      mShader = new ShaderProgram(m_context);

      //  Load Objects
      //  TODO( mHorodni ) Replace base type
        //  TODO(mHorodni) Fix texture
      //mSystem = new SolarSystem(m_context);
        Sun = new Object3D(m_context, "Sun", R.drawable.rusty_iron_texture);
        Sun.mRotation = mAngleInDegrades;
        Sun.mPosition = new V3(0, 0, 0);
      Earth = new Object3D(m_context, "Sun", R.drawable.bumpy_bricks_public_domain);
      Earth.mRotation = mAngleInDegrades;
      Earth.mPosition = new V3(50, 0, 10);
      Moon = new Object3D(m_context, "Sun", R.drawable.stone_wall_public_domain);
      Moon.mRotation = mAngleInDegrades/2;
      Moon.mPosition = new V3(60, 0, 15);
      Ship = new Object3D(m_context, "Ship", R.drawable.rusty_iron_texture);
      Ship.mPosition = new V3(65, 0, 18);
      m_vObjects = new Object3D[4];
      m_vObjects[0] = Sun;
      m_vObjects[1] = Earth;
      m_vObjects[2] = Moon;
      m_vObjects[3] = Ship;
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
      GLES32.glClearColor(0, 1f, 0, 1 );
      GLES32.glViewport(0, 0, width, height);
      setProjectionMatrix(width, height);
        //  Cull back faces
        GLES32.glEnable(GLES32.GL_CULL_FACE);
        //  Enable depth testing
        GLES32.glEnable(GLES32.GL_DEPTH_TEST);
        //  Disable blending
        GLES32.glDisable(GLES32.GL_BLEND);
    }

  //************************************************************************************************
  //
  //  Name        : onDrawFrame
  //
  //  Description : Method called each frame we display we want as less work as possible here and only to
  //                update the world state from frame to frame
  //
  //************************************************************************************************/*
    @Override
    public void onDrawFrame(GL10 gl)
//    {
//      // NOTE Clearing
//      GLES32.glClearColor(0, 1f, 0, 1 );
//
//      //  Check blending state from touch control
//      if(mBlending)
//      {
//        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT);
//      }
//      else
//      {
//        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT | GLES32.GL_DEPTH_BUFFER_BIT);
//      }
//
//      //  NOTE Use Shader Program
//      mShader.useShader();
//      //  NOTE Set up input
//      int mModelViewProductMatrixHandle = mShader.getInputUniformParameter(mShader.m_programHandle, "u_MVPMatrix");
//      int mModelViewMatrixHandle = mShader.getInputUniformParameter(mShader.m_programHandle, "u_MVMatrix");
//      int mLightPosHandle = mShader.getInputUniformParameter(mShader.m_programHandle, "u_LightPos");
//      int mPositionHandle = mShader.getInputAttributeParameter(mShader.m_programHandle, "a_Position");
//      int mColorHandle = mShader.getInputAttributeParameter(mShader.m_programHandle, "a_Color");
//      int mNormalHandle = mShader.getInputAttributeParameter(mShader.m_programHandle, "a_Normal");
//      int mTextureUniformHandle = mShader.getInputUniformParameter(mShader.m_programHandle, "u_Texture");
//      int mTextureCoordinateHandle = mShader.getInputAttributeParameter(mShader.m_programHandle, "a_TexCoordinate");
//
//      //  Set the active texture to 0
//      GLES32.glActiveTexture(GLES32.GL_TEXTURE0);
//      //  Bind the texture to this unit
//      GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, mSystem.mTextureDataHandle);
//      GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, mShip.mTextureDataHandle);
//      //  Tell the uniform sampler to use this texture
//      GLES32.glUniform1i(mTextureUniformHandle, 0);
//
//      //  Translate the objects on to the screen
//      //  Handle object rotation and lightning
//      getAngleInDegrades();
//      //  TODO this should be done for all lights not all objects
//      setLightModelMatrix(SOLAR_OBJECT_INDEX);
//
//      //  TODO(ME):Refactor the following code so that we only need to call one draw method to display an object
//      //  Set the first cube in world position
//      /*setModelMatrix( 0.0f, 0.0f, -7.0f );
//      setModelViewMatrix();
//      setModelViewProductMatrix();
//      */
//      //  Draw the system
//      //  TODO - Move the input parameters inside the draw function
//      //  TODO - Load 2 independent objects and display them
//      m_vObjects[0].mRotation = mAngleInDegrades;
//      m_vObjects[1].mRotation = mAngleInDegrades/2;
//
//      for(Object3D object : m_vObjects)
//      {
//          setModelMatrix( 0.0f, 0.0f, 0.0f, SOLAR_OBJECT_INDEX );
//          setModelViewMatrix(SOLAR_OBJECT_INDEX);
//          setModelViewProductMatrix(SOLAR_OBJECT_INDEX);
//          mSystem.draw(mPositionHandle, mColorHandle, mNormalHandle, mTextureCoordinateHandle);
//          GLES32.glUniformMatrix4fv(mModelViewMatrixHandle, 1, false, mModelViewMatrix, SOLAR_OBJECT_INDEX);
//          GLES32.glUniformMatrix4fv(mModelViewProductMatrixHandle, 1, false, mModelViewProjectionMatrix, SOLAR_OBJECT_INDEX);
//      }
//      //mSystem.setPosition(0, 0, 0);
//      //  Sets Object position and rotation
//      setModelMatrix( 0.0f, 0.0f, 0.0f, SOLAR_OBJECT_INDEX );
//      setModelViewMatrix(SOLAR_OBJECT_INDEX);
//      setModelViewProductMatrix(SOLAR_OBJECT_INDEX);
//      mSystem.draw(mPositionHandle, mColorHandle, mNormalHandle, mTextureCoordinateHandle);
//      GLES32.glUniformMatrix4fv(mModelViewMatrixHandle, 1, false, mModelViewMatrix, SOLAR_OBJECT_INDEX);
//      GLES32.glUniformMatrix4fv(mModelViewProductMatrixHandle, 1, false, mModelViewProjectionMatrix, SOLAR_OBJECT_INDEX);
//
//      setModelMatrix( 5.0f, 5.0f, 0.0f, SOLAR_OBJECT_INDEX );
//      setModelViewMatrix(SOLAR_OBJECT_INDEX);
//      setModelViewProductMatrix(SOLAR_OBJECT_INDEX);
//      mSystem.draw(mPositionHandle, mColorHandle, mNormalHandle, mTextureCoordinateHandle);
//
//      GLES32.glUniformMatrix4fv(mModelViewMatrixHandle, 1, false, mModelViewMatrix, SOLAR_OBJECT_INDEX);
//      GLES32.glUniformMatrix4fv(mModelViewProductMatrixHandle, 1, false, mModelViewProjectionMatrix, SOLAR_OBJECT_INDEX);
//
//      //  TODO Display The Second object
///*
//      mShip.mRotation = mAngleInDegrades;
//      //mShip.setPosition(2, 2, 0);
//      //  TODO use object position
//      setModelMatrix( 10.0f, 10.0f, 0.0f, SHIP_OBJECT_INDEX );
//      setModelViewMatrix(SHIP_OBJECT_INDEX);
//      setModelViewProductMatrix(SHIP_OBJECT_INDEX);
//      mShip.draw(mPositionHandle, mColorHandle, mNormalHandle, mTextureCoordinateHandle);
//      GLES32.glUniformMatrix4fv(mModelViewMatrixHandle, 1, false, mModelViewMatrix, SHIP_OBJECT_INDEX);
//      GLES32.glUniformMatrix4fv(mModelViewProductMatrixHandle, 1, false, mModelViewProjectionMatrix, SHIP_OBJECT_INDEX);
//
//      //  Draw the ship
//      /*
//      //  Set the second object in world position
//      setModelMatrix( 5.0f, 0.0f, -15.0f );
//      setModelViewMatrix();
//      setModelViewProductMatrix();
//      GLES32.glUniformMatrix4fv(mModelViewMatrixHandle, 1, false, mModelViewMatrix, 0);
//      GLES32.glUniformMatrix4fv(mModelViewProductMatrixHandle, 1, false, mModelViewProjectionMatrix, 0);*/
//
//
//      //  Pass in the light position
//      GLES32.glUniform3f(mLightPosHandle, mLightPosInEyeSpace[0],  mLightPosInEyeSpace[1],  mLightPosInEyeSpace[2]);
//
//      //  Draw light
//      //  TODO ( HANDLE Lightning)
//      renderLight();
//    }
    {
        // NOTE Clearing
        GLES32.glClearColor(0, 1f, 0, 1 );

        //  Check blending state from touch control
        if(mBlending)
        {
            GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT);
        }
        else
        {
            GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT | GLES32.GL_DEPTH_BUFFER_BIT);
        }

        //  NOTE Use Shader Program
        mShader.useShader();
        //  NOTE Set up input
        int mModelViewProductMatrixHandle = mShader.getInputUniformParameter(mShader.m_programHandle, "u_MVPMatrix");
        int mModelViewMatrixHandle = mShader.getInputUniformParameter(mShader.m_programHandle, "u_MVMatrix");
        int mLightPosHandle = mShader.getInputUniformParameter(mShader.m_programHandle, "u_LightPos");
        int mPositionHandle = mShader.getInputAttributeParameter(mShader.m_programHandle, "a_Position");
        int mColorHandle = mShader.getInputAttributeParameter(mShader.m_programHandle, "a_Color");
        int mNormalHandle = mShader.getInputAttributeParameter(mShader.m_programHandle, "a_Normal");
        int mTextureUniformHandle = mShader.getInputUniformParameter(mShader.m_programHandle, "u_Texture");
        int mTextureCoordinateHandle = mShader.getInputAttributeParameter(mShader.m_programHandle, "a_TexCoordinate");

        //  Set the active texture to 0
        GLES32.glActiveTexture(GLES32.GL_TEXTURE0);
        //  Bind the texture to this unit
        //  TODO(mHorodni) : Bind texture in object3d
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, Sun.mTextureDataHandle);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, Earth.mTextureDataHandle);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, Moon.mTextureDataHandle);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, Ship.mTextureDataHandle);
        //  Tell the uniform sampler to use this texture
        GLES32.glUniform1i(mTextureUniformHandle, 0);

        //  Translate the objects on to the screen
        //  Handle object rotation and lightning
        getAngleInDegrades();
        //  TODO this should be done for all lights not all objects
        setLightModelMatrix(SOLAR_OBJECT_INDEX);

        //  Draw the system
        //  TODO - Move the input parameters inside the draw function
        //  TODO - Load 3 independent objects and display them
        //  TODO - Make rotation part of object properties
        //  TODO - Understand OFFSET


        int nObjectIndex = 0;
        for(Object3D object : m_vObjects) {
            moveObject(object.mPosition, 0);
            rotateObject(mAngleInDegrades, object.mRotationAxis, 0);
            drawTexture(object.mTextureDataHandle);
            setModelViewMatrix(0);
            setModelViewProductMatrix(0);
            Earth.draw(mPositionHandle, mColorHandle, mNormalHandle, mTextureCoordinateHandle);
            GLES32.glUniformMatrix4fv(mModelViewMatrixHandle, 1, false, mModelViewMatrix, 0);
            GLES32.glUniformMatrix4fv(mModelViewProductMatrixHandle, 1, false, mModelViewProjectionMatrix, 0);
            nObjectIndex++;
        }

        //  Pass in the light position
        GLES32.glUniform3f(mLightPosHandle, mLightPosInEyeSpace[0],  mLightPosInEyeSpace[1],  mLightPosInEyeSpace[2]);

        //  Draw light
        //  TODO ( HANDLE Lightning)
        renderLight();
    }

  //************************************************************************************************
  //
  //  Name        : setViewMatrix
  //
  //  Description : Method used to ser the current view matrix for our scene. This is used by OpenGL
  //                  to determine how our scene is viewed. Also refereed as camera view matrix.
  //
  //************************************************************************************************
    private void setViewMatrix()
    {
        //  From where we are locking at the scene
        final float eyeX = 80.0f;
        final float eyeY = 10.0f;
        final float eyeZ = 25.0f;

        //  Where we are locking at
        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = 0.0f;

        //  In witch direction
        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;

        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);
    }

  //************************************************************************************************
  //
  //  Name        : setProjectionMatrix
  //
  //  Description : Method used to set projection matrix this is usually the size of the screen we
  //                  want to map to our screen device. It is usually called when the app is resumed
  //                  or the device resolution is changed.
  //
  //  Parameters  : int width - the width of our device screen
  //                int height - the height of our device
  //
  //************************************************************************************************
    private void setProjectionMatrix(int width, int height)
    {
        // Create the perspective matrix
      final float mRatio = (float) width / height;
      final float left = -mRatio;
      final float top = 1.0f;
      final float bottom = -1.0f;
      final float near = 0.8f;
      final float far = 200.0f;

      Matrix.frustumM(mProjectionMatrix, 0, left, mRatio, bottom, top, near, far);
    }

    private void setModelViewProductMatrix(int objectIndex)
    {
        Matrix.multiplyMM(mModelViewProjectionMatrix, objectIndex, mProjectionMatrix, 0, mModelViewMatrix, 0 );
    }

    private void setModelMatrix( float x, float y, float z, int objectIndex)
    {
        Matrix.setIdentityM(mModelMatrix, objectIndex);
        Matrix.translateM(mModelMatrix, objectIndex, x, y, z);

        //  TODO Handle only x axis rotation
        Matrix.rotateM(mModelMatrix, objectIndex, mAngleInDegrades + mDeltaX, 0.0f, 1.0f, 0.0f );
    }

    private void setModelMatrix(V3 position, int nIndex)
    {
        Matrix.setIdentityM(mModelMatrix, nIndex);
        Matrix.translateM(mModelMatrix, nIndex, (float)position.GetX(), (float)position.GetY(), (float)position.GetZ());

        //  TODO Handle only x axis rotation
        Matrix.rotateM(mModelMatrix, nIndex, mAngleInDegrades + mDeltaX, 0.0f, 1.0f, 0.0f );
    }

    private void moveObject(V3 position, int nIndex){
        Matrix.setIdentityM(mModelMatrix, nIndex);
        Matrix.translateM(mModelMatrix, nIndex, (float)position.GetX(), (float)position.GetY(), (float)position.GetZ());
    }

    private void rotateObject(float angle, V3 rotationAxis, int nIndex){
        //Matrix.setIdentityM(mModelMatrix, nIndex);
        Matrix.rotateM(mModelMatrix, nIndex, angle + mDeltaY, (float)rotationAxis.GetX(), (float)rotationAxis.GetY(), (float)rotationAxis.GetZ());
    }

    private void drawTexture(int mTextureHandle)
    {
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, mTextureHandle);
        //GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MIN_FILTER, filter);
    }

    private void setModelViewMatrix(int objectIndex)
    {
      Matrix.multiplyMM(mModelViewMatrix, objectIndex, mViewMatrix, 0, mModelMatrix, 0 );
    }

    private void setLightModelMatrix(int objectIndex)
    {
        Matrix.setIdentityM(mLightModelMatrix, objectIndex);
        //Matrix.translateM(mLightModelMatrix, objectIndex, 0.0f,3.0f, 5.0f);
        Matrix.rotateM(mLightModelMatrix, objectIndex, mAngleInDegrades + mDeltaY, 0.0f, 1.0f, 0.0f);
        //Matrix.translateM(mLightModelMatrix, objectIndex, 0.0f,0.0f, 2.0f);

        Matrix.multiplyMV(mLightPosInWorldSpace, objectIndex, mLightModelMatrix, 0, mLightPosInModelSpace, 0);
        Matrix.multiplyMV(mLightPosInEyeSpace, objectIndex, mViewMatrix, 0, mLightPosInWorldSpace, 0);
    }


    private void getAngleInDegrades()
    {
        long time = SystemClock.uptimeMillis() % 4000L;
        mAngleInDegrades = 0.090f * ((int) time);
    }

    private void renderLight()
    {

      int mPixelProgramHandle = mShader.usePixelShader();
      final int pointMVPMatrixHandle = GLES32.glGetUniformLocation( mPixelProgramHandle, "u_MVPMatrix");
      final int pointPositionHandle = GLES32.glGetAttribLocation( mPixelProgramHandle, "a_Position");

      //  Pass in the position
      GLES32.glVertexAttrib3f( pointPositionHandle, mLightPosInModelSpace[0],  mLightPosInModelSpace[1],  mLightPosInModelSpace[2]);

      //  Since we are not using a buffer object disable vertex arrays for this attribute
      GLES32.glDisableVertexAttribArray(pointPositionHandle);

      //  Pass in the transformation matrix
      Matrix.multiplyMM(mModelViewProjectionMatrix, 0, mViewMatrix, 0, mLightModelMatrix, 0);
      Matrix.multiplyMM(mModelViewProjectionMatrix, 0, mProjectionMatrix, 0, mModelViewProjectionMatrix, 0);
      GLES32.glUniformMatrix4fv(pointMVPMatrixHandle, 1, false, mModelViewProjectionMatrix, 0);

      //  Draw the point.
      GLES32.glDrawArrays(GLES32.GL_POINTS, 0, 1);
    }

    public void switchMode()
    {
      mBlending = !mBlending;

      if( mBlending)
      {
        //  Disable culling of faces
        GLES32.glDisable(GLES32.GL_CULL_FACE);
        //  Disable depth testing
        GLES32.glDisable(GLES32.GL_DEPTH_TEST);
        //  Enable blending
        GLES32.glEnable(GLES32.GL_BLEND);
        GLES32.glBlendFunc(GLES32.GL_ONE, GLES32.GL_ONE);
      }
      else
      {
        //  Cull back faces
        GLES32.glEnable(GLES32.GL_CULL_FACE);
        //  Enable depth testing
        GLES32.glEnable(GLES32.GL_DEPTH_TEST);
        //  Disable blending
        GLES32.glDisable(GLES32.GL_BLEND);
      }
    }

}
