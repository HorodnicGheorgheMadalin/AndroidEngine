package com.hgm.androidengine;

import android.content.Context;
import android.opengl.GLES32;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class OpenGLRenderer implements GLSurfaceView.Renderer
{
    private Context         m_context;
    private Object3D mShip = null;
    private ShaderProgram   mShader;
    private float           mAngleInDegrades;

    /** These are handles to our texture data. */
    private int mBrickDataHandle, mQueuedMagFilter;
    private int mGrassDataHandle, mQueuedMinFilter;

    //  Used to switch between different blending modes
    private boolean mBlending = true;

    private final static int MATRIX_SIZE = 16;

    //  Used to transform the world space into ou eys
    private float[] mViewMatrix                 = new float[MATRIX_SIZE];
    //  Used to project the scene into a 2D view port
    private float[] mProjectionMatrix           = new float[MATRIX_SIZE];
    //  Used to move models in world space
    private float[] mModelMatrix                = new float[MATRIX_SIZE];
    private float[] mModelViewMatrix            = new float[MATRIX_SIZE];
    private float[] mModelViewProjectionMatrix  = new float[MATRIX_SIZE];
    private float[] mLightModelMatrix           = new float[MATRIX_SIZE];

    //  The light position with a fourth filler coordinate needed for multiplication with transformation marice
    private float[] mLightPosInModelSpace   = new float[] { 0.0f, 0.0f, 0.0f, 1.0f};
    private float[] mLightPosInWorldSpace   = new float[4];
    private float[] mLightPosInEyeSpace     = new float[4];

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

      //  Disable culling of faces
      GLES32.glDisable(GLES32.GL_CULL_FACE);
      //  Disable depth testing
      GLES32.glDisable(GLES32.GL_DEPTH_TEST);
      //  Enable blending
      GLES32.glEnable(GLES32.GL_BLEND);
      GLES32.glBlendFunc(GLES32.GL_ONE, GLES32.GL_ONE);
      //GLES32.glBlendEquation(GLES32.GL_FUNC_ADD);

      setViewMatrix();

      //  Create Shader Program
      mShader = new ShaderProgram(m_context);

      // Load the texture
      mBrickDataHandle = mShip.loadTexture(m_context, R.drawable.stone_wall_public_domain);
      GLES32.glGenerateMipmap(GLES32.GL_TEXTURE_2D);

      mGrassDataHandle = mShip.loadTexture(m_context, R.drawable.noisy_grass_public_domain);
      GLES32.glGenerateMipmap(GLES32.GL_TEXTURE_2D);

      if (mQueuedMinFilter != 0)
      {
        setMinFilter(mQueuedMinFilter);
      }

      if (mQueuedMagFilter != 0)
      {
        setMagFilter(mQueuedMagFilter);
      }

      mShip = new Object3D(m_context, "Cube_3.obj");
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
      GLES32.glClearColor(0, 1f, 0, 1 );
      GLES32.glViewport(0, 0, width, height);
      setProjectionMatrix(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl)
    {
      // NOTE Clearing
      GLES32.glClearColor(0, 1f, 0, 1 );

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
      GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, mShip.mTextureDataHandle);
      //  Tell the uniform sampler to use this texture
      GLES32.glUniform1i(mTextureUniformHandle, 0);

      //  Translate the objects on to the screen
      getAngleInDegrades();
      setLightModelMatrix();

      //  TODO(ME):Refactor the following code so that we only need to call one draw method to display an object
      //  Set the first cube in world position
      setModelMatrix( 0.0f, 0.0f, -7.0f );
      setModelViewMatrix();
      setModelViewProductMatrix();
      //  Draw objects
      GLES32.glUniformMatrix4fv(mModelViewMatrixHandle, 1, false, mModelViewMatrix, 0);
      GLES32.glUniformMatrix4fv(mModelViewProductMatrixHandle, 1, false, mModelViewProjectionMatrix, 0);
      mShip.mRotation = mAngleInDegrades;
      mShip.draw(mPositionHandle, mColorHandle, mNormalHandle, mTextureCoordinateHandle);

      /*
      //  Set the second cube in world position
      setModelMatrix( 4.0f, 0.0f, -7.0f );
      setModelViewMatrix();
      setModelViewProductMatrix();
      GLES32.glUniformMatrix4fv(mModelViewMatrixHandle, 1, false, mModelViewMatrix, 0);
      GLES32.glUniformMatrix4fv(mModelViewProductMatrixHandle, 1, false, mModelViewProjectionMatrix, 0);
      mShip.draw(mPositionHandle, mColorHandle, mNormalHandle, mTextureCoordinateHandle);

      //  Set the third cube in world position
      setModelMatrix( 0.0f, 4.0f, -7.0f );
      setModelViewMatrix();
      setModelViewProductMatrix();
      GLES32.glUniformMatrix4fv(mModelViewMatrixHandle, 1, false, mModelViewMatrix, 0);
      GLES32.glUniformMatrix4fv(mModelViewProductMatrixHandle, 1, false, mModelViewProjectionMatrix, 0);
      mShip.draw(mPositionHandle, mColorHandle, mNormalHandle, mTextureCoordinateHandle);
      */

      //  Pass in the light position
      GLES32.glUniform3f(mLightPosHandle, mLightPosInEyeSpace[0],  mLightPosInEyeSpace[1],  mLightPosInEyeSpace[2]);

      //  Draw light
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
        final float eyeX = 0.0f;
        final float eyeY = 0.0f;
        final float eyeZ = 2.0f;

        //  Where we are locking at
        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = -10.0f;

        //  In which direction
        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;

        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);
    }

  //************************************************************************************************
  //
  //  Name        : setProjectionMatrix
  //
  //  Description : Method used to ser projection matrix this is usually the size of the screen we
  //                  want to map to our screen device. It is usually called when the app is resumed
  //                  or the device resolution si changed.
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
      final float right = mRatio;
      final float top = 1.0f;
      final float bottom = -1.0f;
      final float near = 0.8f;
      final float far = 20.0f;

      Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
    }

    private void setModelViewProductMatrix()
    {
        Matrix.multiplyMM(mModelViewProjectionMatrix, 0, mProjectionMatrix, 0, mModelViewMatrix, 0 );
    }

    private void setModelMatrix( float x, float y, float z)
    {
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, x, y, z);
        Matrix.rotateM(mModelMatrix, 0, mAngleInDegrades + mDeltaX, 0.0f, 1.0f, 0.0f );
    }

    private void setModelViewMatrix()
    {
      Matrix.multiplyMM(mModelViewMatrix, 0, mViewMatrix, 0, mModelMatrix, 0 );
    }


    private void setLightModelMatrix()
    {
        Matrix.setIdentityM(mLightModelMatrix, 0);
        Matrix.translateM(mLightModelMatrix, 0, 0.0f,0.0f, -5.0f);
        Matrix.rotateM(mLightModelMatrix, 0, mAngleInDegrades + mDeltaY, 0.0f, 1.0f, 0.0f);
        Matrix.translateM(mLightModelMatrix, 0, 0.0f,0.0f, 2.0f);

        Matrix.multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0);
        Matrix.multiplyMV(mLightPosInEyeSpace, 0, mViewMatrix, 0, mLightPosInWorldSpace, 0);
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

    public void setMinFilter(final int filter)
    {
      if (mBrickDataHandle != 0 && mGrassDataHandle != 0)
      {
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, mBrickDataHandle);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MIN_FILTER, filter);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, mGrassDataHandle);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MIN_FILTER, filter);
      }
      else
      {
        mQueuedMinFilter = filter;
      }
    }

    public void setMagFilter(final int filter)
    {
      if (mBrickDataHandle != 0 && mGrassDataHandle != 0)
      {
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, mBrickDataHandle);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MAG_FILTER, filter);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, mGrassDataHandle);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MAG_FILTER, filter);
      }
      else
      {
        mQueuedMagFilter = filter;
      }
    }

}
