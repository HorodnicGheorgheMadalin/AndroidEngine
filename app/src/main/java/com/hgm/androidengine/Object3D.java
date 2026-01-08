package com.hgm.androidengine;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES32;
import android.opengl.GLUtils;
import android.util.Log;

import com.hgm.solarSystem.V3;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Object3D
{
    // TODO : Deal with non repeating NORMALS in code
    // TODO : BUG Find out why not all textures are mapped correctly

    //  Buffers used to store information about our 3d Model
    private FloatBuffer verticesBuffer;
    private ShortBuffer facesBuffer;
    private FloatBuffer colorBuffer;
    private FloatBuffer normalBuffer;
    private FloatBuffer cubeTexCoordinatesBuffer;

    private boolean isLoaded;
    private boolean drawPath;
    private int     mFacesNumber;
    public  int     mTextureID;
    public  String  mName;
    public  int     mTextureDataHandle;

    private float mRotation;

    private V3 mPosition = null;
    private V3 mMovement = null;
    private V3 mRotationAxis = null;
    private V3 mScale = null;
    private int mObjectCount;
    private float mOrbitRadius;
    private float mOrbitSpeed;
    private float mOrbitAngle;
    private float mPolarAngle; // Not used by current XZ planar orbit logic
    private V3 mOrbitCenter;   // Stores the center of the orbit

    private static final int SIZE_OF_FLOAT      = 4;
    private static final int SIZE_OF_SHORT      = 2;

    private static final int VALUES_PER_COLOR   = 4;
    private static final int VALUES_PER_VERTEX  = 3;
    private static final int VALUES_PER_NORMAL  = 3;
    private static final int VALUES_PER_FACE    = 4;
    private static final int VALUES_PER_TEXTURE = 2;

    private static final int VERTEX_ENTRY_SIZE  = VALUES_PER_VERTEX * SIZE_OF_FLOAT;
    private static final int NORMAL_ENTRY_SIZE  = VALUES_PER_NORMAL * SIZE_OF_FLOAT;
    private static final int FACE_ENTRY_SIZE    = VALUES_PER_FACE * SIZE_OF_SHORT;
    private static final int COLOR_ENTRY_SIZE   = VALUES_PER_COLOR * SIZE_OF_FLOAT;
    private static final int TEXTURE_ENTRY_SIZE = VALUES_PER_TEXTURE * SIZE_OF_FLOAT;

    private static final String VERTEX_LINE_START = "v ";
    private static final String NORMAL_LINE_START = "vn ";
    private static final String FACE_LINE_START   = "f ";
    private static final String COLOR_LINE_START  = "";
    private static final String TEXTURE_LINE_START = "vt ";


    private static final int X_INDEX            = 1;
    private static final int Y_INDEX            = 2;
    private static final int Z_INDEX            = 3;

    private static final int N_INDEX            = 4;

    private static final String FILE_EXTENSION_3D = ".obj";


    public Object3D(Context context, String strName, int textureID)
    {
        mTextureID = textureID;
        isLoaded = false;
        String strFileName = strName + FILE_EXTENSION_3D;
        initObject(context, strFileName);
        mName = strName;
        mRotation = 0.0f;
        mPosition = new V3(0, 0, 0);
        mMovement = new V3(0, 0, 0);
        mRotationAxis = new V3(0, 1, 0);
        mScale = new V3(1, 1, 1);
        mObjectCount = 0;
        mPolarAngle = 0.0f; 
        mOrbitCenter = new V3(0,0,0); // Initialize orbit center
    }

    private void initObject(Context context, String strFileName)
    {
        List<String> verticesList;
        List<String> facesList;
        List<String> normalList;
        List<String> textureList;
        verticesList = new ArrayList<>();
        facesList = new ArrayList<>();
        normalList = new ArrayList<>();
        textureList = new ArrayList<>();

        Scanner scanner = null;
        try {
            scanner = new Scanner(context.getAssets().open(strFileName));
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("SoL", "Exception opening: " + strFileName + " : " + e.toString() );
        }

        assert scanner != null;
        while(scanner.hasNext())
        {
            String line = scanner.nextLine();
            if( line.startsWith(VERTEX_LINE_START)) verticesList.add(line);
            else if( line.startsWith(FACE_LINE_START)) facesList.add(line);
            else if( line.startsWith(NORMAL_LINE_START)) normalList.add(line);
            else if( line.startsWith(TEXTURE_LINE_START)) textureList.add(line);
        }
        scanner.close();

        ByteBuffer buffer1 = ByteBuffer.allocateDirect(verticesList.size() * VERTEX_ENTRY_SIZE);
        buffer1.order(ByteOrder.nativeOrder());
        verticesBuffer = buffer1.asFloatBuffer();

        ByteBuffer buffer4 = ByteBuffer.allocateDirect(normalList.size() * NORMAL_ENTRY_SIZE);
        buffer4.order(ByteOrder.nativeOrder());
        normalBuffer = buffer4.asFloatBuffer();

        ByteBuffer buffer2 = ByteBuffer.allocateDirect(facesList.size() * FACE_ENTRY_SIZE );
        buffer2.order(ByteOrder.nativeOrder());
        facesBuffer = buffer2.asShortBuffer();
        
        ByteBuffer buffer3 = ByteBuffer.allocateDirect(verticesList.size() * COLOR_ENTRY_SIZE);
        buffer3.order(ByteOrder.nativeOrder());
        colorBuffer = buffer3.asFloatBuffer();

        ByteBuffer buffer5 = ByteBuffer.allocateDirect(textureList.size() * TEXTURE_ENTRY_SIZE );
        buffer5.order(ByteOrder.nativeOrder());
        cubeTexCoordinatesBuffer = buffer5.asFloatBuffer();

        float R = 1.0f, G = 1.0f, B = 1.0f, A = 1.0f;
        for(String vertex: verticesList)
        {
            String[] coordinates = vertex.split(" ");
            float x = Float.parseFloat(coordinates[X_INDEX]);
            float y = Float.parseFloat(coordinates[Y_INDEX]);
            float z = Float.parseFloat(coordinates[Z_INDEX]);
            verticesBuffer.put(x); verticesBuffer.put(y); verticesBuffer.put(z);
            colorBuffer.put(R); colorBuffer.put(G); colorBuffer.put(B); colorBuffer.put(A);
        }
        verticesBuffer.position(0); colorBuffer.position(0);

        for( String normal : normalList)
        {
          String[] normals = normal.split( " ");
          float normal1 = Float.parseFloat(normals[X_INDEX]);
          float normal2 = Float.parseFloat(normals[Y_INDEX]);
          float normal3 = Float.parseFloat(normals[Z_INDEX]);
          normalBuffer.put(normal1); normalBuffer.put(normal2); normalBuffer.put(normal3);
        }
        normalBuffer.position(0);

        for( String face: facesList)
        {
            String[] vertexIndexes = face.split( " " );
            short vertex1 = Short.parseShort(vertexIndexes[X_INDEX]);
            short vertex2 = Short.parseShort(vertexIndexes[Y_INDEX]);
            short vertex3 = Short.parseShort(vertexIndexes[Z_INDEX]);
            facesBuffer.put((short)(vertex1-1));
            facesBuffer.put((short)(vertex2-1));
            facesBuffer.put((short)(vertex3-1));
        }
        facesBuffer.position(0);

        mFacesNumber = facesList.size() * VALUES_PER_FACE;
        isLoaded = true;

        for( String textureCoordinate: textureList)
        {
            String[] textureIndexes = textureCoordinate.split( " " );
            float textureX = Float.parseFloat(textureIndexes[X_INDEX]);
            float textureY = Float.parseFloat(textureIndexes[Y_INDEX]);
            cubeTexCoordinatesBuffer.put(textureX); cubeTexCoordinatesBuffer.put(textureY);
        }
        cubeTexCoordinatesBuffer.position(0);
        mTextureDataHandle = loadTexture(context, mTextureID);
    }

    public void draw(int positionHandle, int colorHandle, int normalHandle, int textureHandle)
    {
        if(isLoaded)
        {
            GLES32.glEnableVertexAttribArray(positionHandle);
            GLES32.glVertexAttribPointer(positionHandle, VALUES_PER_VERTEX, GLES32.GL_FLOAT, false, VERTEX_ENTRY_SIZE, verticesBuffer);
            GLES32.glEnableVertexAttribArray(colorHandle);
            GLES32.glVertexAttribPointer(colorHandle, VALUES_PER_COLOR, GLES32.GL_FLOAT, false, COLOR_ENTRY_SIZE, colorBuffer );
            GLES32.glEnableVertexAttribArray(normalHandle);
            GLES32.glVertexAttribPointer(normalHandle, VALUES_PER_NORMAL, GLES32.GL_FLOAT, false, NORMAL_ENTRY_SIZE, normalBuffer );
            GLES32.glEnableVertexAttribArray(textureHandle);
            GLES32.glVertexAttribPointer(textureHandle, VALUES_PER_TEXTURE, GLES32.GL_FLOAT, false, TEXTURE_ENTRY_SIZE, cubeTexCoordinatesBuffer );
            GLES32.glDrawElements(GLES32.GL_TRIANGLES, mFacesNumber, GLES32.GL_UNSIGNED_SHORT, facesBuffer);
            GLES32.glDisableVertexAttribArray(colorHandle);
            GLES32.glDisableVertexAttribArray(positionHandle);
            GLES32.glDisableVertexAttribArray(normalHandle);
            GLES32.glDisableVertexAttribArray(textureHandle);
        }
    }

    public static int loadTexture( final Context context, final int resourceID)
    {
        final int[] textureHandle = new int[1];
        GLES32.glGenTextures( 1, textureHandle, 0);
        if( 0 != textureHandle[0] )
        {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;
            final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceID, options );
            GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, textureHandle[0]);
            GLES32.glTexParameteri( GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MIN_FILTER, GLES32.GL_NEAREST);
            GLES32.glTexParameteri( GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MAG_FILTER, GLES32.GL_NEAREST);
            GLUtils.texImage2D(GLES32.GL_TEXTURE_2D, 0, bitmap, 0);
            bitmap.recycle();
        }
        if( 0 == textureHandle[0]) throw new RuntimeException("ERROR Loading texture");
        return textureHandle[0];
    }

    public void updateOrbit()
    {
        if (mOrbitCenter == null) return; // Don't update if orbit center isn't set
        mOrbitAngle += mOrbitSpeed;
        if (mOrbitAngle > 360.0f) mOrbitAngle -= 360.0f;

        // Standard XZ planar orbit (assuming Y is up)
        float x = (float) (mOrbitCenter.GetX() + mOrbitRadius * Math.cos(Math.toRadians(mOrbitAngle)));
        float y = (float) (mOrbitCenter.GetY()); // Y stays the same as the orbit center's Y
        float z = (float) (mOrbitCenter.GetZ() + mOrbitRadius * Math.sin(Math.toRadians(mOrbitAngle)));

        Log.d("SoL", "Object " + mName + " New Pos: [" + x + "," + y + "," + z +"] calculated from OrbitCenter: [" + mOrbitCenter.GetX() + "," + mOrbitCenter.GetY() + "," + mOrbitCenter.GetZ() + "] OrbitAngle: " + mOrbitAngle);
        mPosition.set(x, y, z);
    }

    void setDrawPath() // Unused for now, but updated for XZ plane
    {
        if (mOrbitCenter == null) return;
        for( int i = 0; i < 360; i++) {
            float angleRad = (float) Math.toRadians(i);
            float x = (float) (mOrbitCenter.GetX() + mOrbitRadius * Math.cos(angleRad));
            float y = (float) (mOrbitCenter.GetY());
            float z = (float) (mOrbitCenter.GetZ() + mOrbitRadius * Math.sin(angleRad));
            //  TODO need to actually display the path.
        }
    }

    public void setOrbitRadius(float newOrbitRadius) { mOrbitRadius = newOrbitRadius; };
    public float getOrbitRadius() { return mOrbitRadius; };
    public void setOrbitSpeed(float newOrbitSpeed) { mOrbitSpeed = newOrbitSpeed; };
    public float getOrbitSpeed() { return mOrbitSpeed; };
    public void setOrbitAngle(float newOrbitAngle) { mOrbitAngle = newOrbitAngle; };
    public float getOrbitAngle() { return mOrbitAngle; };
    
    public void setOrbitCenter(V3 newOrbitCenter) { 
        this.mOrbitCenter = newOrbitCenter;
    };
    public V3 getOrbitCenter() { return mOrbitCenter; };

    public void setPosition( double X, double Y, double Z) { mPosition.set(X, Y, Z); }
    public void setPosition(V3 newPosition) { mPosition = newPosition; };
    public V3 getPosition() {  return mPosition; };
    public void setMovement(double X, double Y, double Z) { mMovement.set(X, Y, Z); };
    public void setMovement(V3 newMovement) { mMovement = newMovement; };
    public V3 getMovement() { return mMovement; };
    public void updatePosition() { mPosition.add(mMovement); };
    public void updateMovement(V3 newMovement) { mMovement.add(newMovement); updatePosition(); };
    public void setRotation(float newRotation) { mRotation = newRotation; };
    public float getRotation() { return mRotation; };
    public void setRotationAxis(V3 newRotationAxis) { mRotationAxis = newRotationAxis; };
    public V3 getRotationAxis() { return mRotationAxis; };
    public void setScale(double X, double Y, double Z) { mScale.set(X, Y, Z); };
    public void setScale(V3 newScale) { mScale = newScale; };
    public V3 getScale() { return mScale; };
    public void setPolarAngle( float angle) { mPolarAngle = angle; };
    public float getPolarAngle() { return mPolarAngle; };
}
