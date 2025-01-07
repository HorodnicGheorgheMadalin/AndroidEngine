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

public class OpenGLRenderer implements GLSurfaceView.Renderer
{
    private final Context   m_context;

    //  TODO Change object to system
    //private SolarSystem     mSystem = null;

    private Object3D        Sun = null;
    private Object3D        Earth = null;
    private Object3D        Moon = null;
    private Object3D        Ship = null;
    private Object3D[]      m_vObjects = null;
    private ShaderProgram   mShader;
    private float           mAngleInDegrades;

    //  TODO - The maximum number of objects our engine can handle is currently 3
    private final static int MATRIX_SIZE_PER_OBJECT = 16;
    private final static int NUMBER_OF_OBJECTS = 4;
    private final static int NUMBER_OF_LIGHTS = 1;
    private final static int OBJECTS_MATRIX_SIZE = MATRIX_SIZE_PER_OBJECT * NUMBER_OF_OBJECTS;
    private final static int LIGHTS_MATRIX_SIZE = MATRIX_SIZE_PER_OBJECT * NUMBER_OF_LIGHTS;
    private final static int POSITION_SIZE = 4; //  3 positions and a alpha.

    //  Used to transform the world space into ou eys
    //  TODO(Madalin) - This should be made into a class
    private final V3 mEyePosition = new V3(80, 10, 0);
    private final V3 mLookAtPoint = new V3(0, 0, 0);
    private final V3 mLookDirection = new V3(0, 1, 0);
    private final float[] mViewMatrix                 = new float[MATRIX_SIZE_PER_OBJECT];
    //  Used to project the scene into a 2D view port
    private final float[] mProjectionMatrix           = new float[MATRIX_SIZE_PER_OBJECT];
    //  Used to move models in world space
    //  Here we have multiple matrices for each object in a array
    private final float[] mModelMatrix                = new float[OBJECTS_MATRIX_SIZE];
    private final float[] mLightModelMatrix           = new float[LIGHTS_MATRIX_SIZE];
    //  Here we only have one common matrix for all objects
    private final float[] mModelViewMatrix            = new float[OBJECTS_MATRIX_SIZE];
    private final float[] mModelViewProjectionMatrix  = new float[OBJECTS_MATRIX_SIZE];

    //  The light position with a fourth filler coordinate needed for multiplication with transformation marice
    private final float[] mLightPosInModelSpace   = new float[] { 0.0f, 0.0f, 10.0f, 1.0f};
    private final float[] mLightPosInWorldSpace   = new float[POSITION_SIZE];
    private final float[] mLightPosInEyeSpace     = new float[POSITION_SIZE];

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
      // Initially Clear the scene
      GLES32.glClearColor(1.0f, 1.0f, 0.0f, 1.0f);

      //  Cull back faces
      GLES32.glEnable(GLES32.GL_CULL_FACE);
      //  Enable depth testing
      GLES32.glEnable(GLES32.GL_DEPTH_TEST);
      //  Disable blending
      GLES32.glDisable(GLES32.GL_BLEND);

      //  Set Camera
      setViewMatrix( mEyePosition, mLookAtPoint, mLookDirection);

      //  Create Shader Program
      mShader = new ShaderProgram(m_context);

      //  Load Objects
      //  TODO( mHorodni ) Replace base type
      //  TODO(mHorodni) Fix texture
      //  TODO(mHorodni) Implement Orbits
      //mSystem = new SolarSystem(m_context);
      Sun = new Object3D(m_context, "Sun", R.drawable.rusty_iron_texture);
      Sun.setRotation(mAngleInDegrades);
      Sun.setPosition(new V3(0.0, 0.0, 0.0));
      Sun.setScale(new V3(10, 10, 10));
      Sun.setOrbitAngle(0.000f);
      Sun.setOrbitCenter( new V3(0, 0, 0));
      Sun.setOrbitSpeed(0.000f);
      Earth = new Object3D(m_context, "Earth", R.drawable.bumpy_bricks_public_domain);
      Earth.setRotation(mAngleInDegrades);
      Earth.setPosition(new V3(50, 0, 0));
      Earth.setScale(new V3(2,2,2  ));
      Earth.setOrbitAngle(45);
      Earth.setOrbitSpeed(0.5f);
      Earth.setOrbitCenter(Sun.getPosition());
      Earth.setOrbitRadius(50.0f);
      Earth.setPolarAngle(0.001f);
      Moon = new Object3D(m_context, "Moon", R.drawable.stone_wall_public_domain);
      Moon.setRotation(mAngleInDegrades/2);
      Moon.setPosition(new V3(60, 0, 0));
      //  NOTE: (Madalin) Orbit should fallow coordinates
      //  60, 0, 0
      //  50, 0, -10
      //  40, 0, 0
      //  50, 0, 10
      Moon.setScale(new V3(0.5,0.5,0.5));
      Moon.setOrbitCenter(Earth.getPosition());
      Moon.setOrbitAngle(45);
      Moon.setOrbitSpeed(0.5f);
      Moon.setOrbitRadius(20.0f);
      Moon.setPolarAngle(0.000f);
      Ship = new Object3D(m_context, "Ship", R.drawable.rusty_iron_texture);
      Ship.setPosition(new V3(65, 0, 18));
      Ship.setScale(new V3( 1, 0.1, 0.1));
      Ship.setOrbitCenter( new V3(0, 0, 0));
      Ship.setOrbitSpeed(0.000f);
      Ship.setOrbitAngle(0.000f);
      m_vObjects = new Object3D[4];
      m_vObjects[0] = Sun;
      m_vObjects[1] = Earth;
      //  TODO(Madalin) -  Add more objects as orbit begin to work
      m_vObjects[2] = Moon;
      m_vObjects[3] = Ship;
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
      //  We need to adjust the scene size and background on screen change
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
    {
      // NOTE Clearing the scene background to make sure we draw correctly
      GLES32.glClearColor(0, 1f, 0, 1 );

      GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT | GLES32.GL_DEPTH_BUFFER_BIT);

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
      setLightModelMatrix(0);

      //  Draw the system the order of operations below matter
      //  We first want to Scale > Rotate > Translate
      //  TODO - Move the input parameters inside the draw function
      //  TODO - Load 3 independent objects and display them
      //  TODO - Make rotation part of object properties
      //  TODO - Understand OFFSET
      //  TODO - Scale dose not work.
      int nMatrixOffsetForObject = 0;
      for (int nObjectIndex = 0; nObjectIndex < m_vObjects.length; nObjectIndex++) {
        if(nObjectIndex >= NUMBER_OF_OBJECTS)
          break;

        //  TODO(Madalin) - See why scale and rotation dose not work
        //scaleObject( m_vObjects[nObjectIndex].getScale(), nMatrixOffsetForObject);
        //rotateObject(mAngleInDegrades, m_vObjects[nObjectIndex].getRotationAxis(), nMatrixOffsetForObject);
        setModelMatrix(m_vObjects[nObjectIndex].getPosition(), nMatrixOffsetForObject);
        m_vObjects[nObjectIndex].updateOrbit();
        drawTexture(m_vObjects[nObjectIndex].mTextureDataHandle);
        setModelViewMatrix(nMatrixOffsetForObject);
        setModelViewProductMatrix(nMatrixOffsetForObject);
        m_vObjects[nObjectIndex].draw(mPositionHandle, mColorHandle, mNormalHandle, mTextureCoordinateHandle);
        GLES32.glUniformMatrix4fv(mModelViewMatrixHandle, 1, false, mModelViewMatrix, nMatrixOffsetForObject);
        GLES32.glUniformMatrix4fv(mModelViewProductMatrixHandle, 1, false, mModelViewProjectionMatrix, nMatrixOffsetForObject);
        nMatrixOffsetForObject += MATRIX_SIZE_PER_OBJECT;
      }

      //  Pass in the light position
      GLES32.glUniform3f(mLightPosHandle, mLightPosInEyeSpace[0],  mLightPosInEyeSpace[1],  mLightPosInEyeSpace[2]);

      //  Draw light
      //  TODO ( HANDLE Lightning)
      // renderLight();
    }

  //************************************************************************************************
  //
  //  Name        : setViewMatrix
  //
  //  Description : Method used to set the current view matrix for our scene. This is used by OpenGL
  //                  to determine how our scene is viewed. Also refereed as camera view matrix.
  //
  //************************************************************************************************
  public void setViewMatrix(V3 eyePosition, V3 lookAtPoint, V3 lookDirection)
  {
      //  From where we are locking at the scene
      mEyePosition.set( mEyePosition.GetX() + eyePosition.GetX(), mEyePosition.GetY() + eyePosition.GetY(), mEyePosition.GetZ() + eyePosition.GetZ());

      //  Where we are locking at
      mLookAtPoint.set( mLookAtPoint.GetX() + lookAtPoint.GetX(), mLookAtPoint.GetY() + lookAtPoint.GetY(), mLookAtPoint.GetZ() + lookAtPoint.GetZ());

      //  In witch direction
      mLookDirection.set( mLookDirection.GetX() + lookDirection.GetX(), mLookDirection.GetY() + lookDirection.GetY(), mLookDirection.GetZ() + lookDirection.GetZ());

      Matrix.setLookAtM(mViewMatrix, 0, (float) mEyePosition.GetX(), (float) mEyePosition.GetY(), (float) mEyePosition.GetZ(),
         (float) mLookAtPoint.GetX(), (float) mLookAtPoint.GetY(), (float) mLookAtPoint.GetZ(),
         (float) mLookDirection.GetX(), (float) mLookDirection.GetY(), (float) mLookDirection.GetZ());
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
    final float far = 500.0f;

    Matrix.frustumM(mProjectionMatrix, 0, left, mRatio, bottom, top, near, far);
  }

  private void setModelViewProductMatrix(int objectIndex)
  {
      Matrix.multiplyMM(mModelViewProjectionMatrix, objectIndex, mProjectionMatrix, 0, mModelViewMatrix, objectIndex );
  }

  private void setModelMatrix( float x, float y, float z, int nIndex)
  {
      Matrix.setIdentityM(mModelMatrix, nIndex);
      Matrix.translateM(mModelMatrix, nIndex, x, y, z);
      printMatrix4x4at(nIndex);
      //  TODO Handle only x axis rotation
      //Matrix.rotateM(mModelMatrix, nIndex, mAngleInDegrades + mDeltaX, 0.0f, 1.0f, 0.0f );
  }

  private void setModelMatrix(V3 position, int nIndex)
  {
      Matrix.setIdentityM(mModelMatrix, nIndex);
      Matrix.translateM(mModelMatrix, nIndex, (float)position.GetX(), (float)position.GetY(), (float)position.GetZ());
      printMatrix4x4at(nIndex);
      //  TODO Handle only x axis rotation
      //Matrix.rotateM(mModelMatrix, nIndex, mAngleInDegrades + mDeltaX, 0.0f, 1.0f, 0.0f );
  }

  private void printMatrix4x4at(int nIndex)
  {
    Log.d("SoL", "ModelMatrixVector[" + nIndex/16 + "] : \n"
       + "[" + mModelMatrix[nIndex+ 0] + "," + mModelMatrix[nIndex+ 1] + "," + mModelMatrix[nIndex+ 2] + "," + mModelMatrix[nIndex+ 3] + "]\n"
       + "[" + mModelMatrix[nIndex+ 4] + "," + mModelMatrix[nIndex+ 5] + "," + mModelMatrix[nIndex+ 6] + "," + mModelMatrix[nIndex+ 7] + "]\n"
       + "[" + mModelMatrix[nIndex+ 8] + "," + mModelMatrix[nIndex+ 9] + "," + mModelMatrix[nIndex+10] + "," + mModelMatrix[nIndex+11] + "]\n"
       + "[" + mModelMatrix[nIndex+12] + "," + mModelMatrix[nIndex+13] + "," + mModelMatrix[nIndex+14] + "," + mModelMatrix[nIndex+15] + "]\n");
  }

  private void moveObject(V3 position, int nIndex)
  {
      Matrix.translateM(mModelMatrix, nIndex, (float)position.GetX(), (float)position.GetY(), (float)position.GetZ());
  }

  private void rotateObject(float angle, V3 rotationAxis, int nIndex)
  {
      Matrix.rotateM(mModelMatrix, nIndex, angle + mDeltaY, (float)rotationAxis.GetX(), (float)rotationAxis.GetY(), (float)rotationAxis.GetZ());
  }

  private void scaleObject( V3 scale, int nIndex )
  {
      Matrix.scaleM( mModelMatrix, nIndex, (float)scale.GetX(), (float)scale.GetY(), (float)scale.GetZ());
  }

  private void drawTexture(int mTextureHandle)
  {
      GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, mTextureHandle);
  }

  private void setModelViewMatrix(int objectIndex)
  {
    Matrix.multiplyMM(mModelViewMatrix, objectIndex, mViewMatrix, 0, mModelMatrix, objectIndex );
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

  private void renderLight(int nLightIndex)
  {

    int mPixelProgramHandle = mShader.usePixelShader();
    final int pointMVPMatrixHandle = GLES32.glGetUniformLocation( mPixelProgramHandle, "u_MVPMatrix");
    final int pointPositionHandle = GLES32.glGetAttribLocation( mPixelProgramHandle, "a_Position");

    //  Pass in the position
    GLES32.glVertexAttrib3f( pointPositionHandle, mLightPosInModelSpace[0],  mLightPosInModelSpace[1],  mLightPosInModelSpace[2]);

    //  Since we are not using a buffer object disable vertex arrays for this attribute
    GLES32.glDisableVertexAttribArray(pointPositionHandle);

    //  Pass in the transformation matrix
    Matrix.multiplyMM(mModelViewProjectionMatrix, nLightIndex, mViewMatrix, nLightIndex, mLightModelMatrix, nLightIndex);
    Matrix.multiplyMM(mModelViewProjectionMatrix, nLightIndex, mProjectionMatrix, nLightIndex, mModelViewProjectionMatrix, nLightIndex);
    GLES32.glUniformMatrix4fv(pointMVPMatrixHandle, 1, false, mModelViewProjectionMatrix, nLightIndex);

    //  Draw the point.
    GLES32.glDrawArrays(GLES32.GL_POINTS, 0, 1);
  }

}
