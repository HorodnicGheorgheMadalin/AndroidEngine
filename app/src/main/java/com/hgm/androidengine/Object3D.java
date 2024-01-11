package com.hgm.androidengine;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES32;
import android.opengl.GLUtils;
import android.util.Log;
import android.widget.Toast;

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
    private int     mFacesNumber;
    public  int     mTextureID;
    public  int     mTextureDataHandle;

    float mRotation;

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

    private static final int N_INDEX            =4;

    private static final String FILE_EXTENSION_3D = ".obj";


    public Object3D(Context context, String strName, int textureID)
    {
        mTextureID = textureID;
        isLoaded = false;
        String strFileName = strName + FILE_EXTENSION_3D;
        initObject(context, strFileName);
        mRotation = 0.0f;
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

        //  Open the ObjFile with a Scanner
        Scanner scanner = null;
        try {
            scanner = new Scanner(context.getAssets().open(strFileName));
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("SoL", "Exception opening: " + strFileName + " : " + e.toString() );
        }

        //  Loop trough the lines
        assert scanner != null;
        while(scanner.hasNext())
        {
            String line = scanner.nextLine();

            if( line.startsWith(VERTEX_LINE_START))
            {
                verticesList.add(line);
            }
            else if( line.startsWith(FACE_LINE_START))
            {
                facesList.add(line);
            }
            else if( line.startsWith(NORMAL_LINE_START))
            {
              normalList.add(line);
            }
            else if( line.startsWith(TEXTURE_LINE_START))
            {
                textureList.add(line);
            }
        }

        scanner.close();

        //  Create a buffer for vertices
        ByteBuffer buffer1 = ByteBuffer.allocateDirect(verticesList.size() * VERTEX_ENTRY_SIZE);
        buffer1.order(ByteOrder.nativeOrder());
        verticesBuffer = buffer1.asFloatBuffer();

        //  Create a buffer for normals
        ByteBuffer buffer4 = ByteBuffer.allocateDirect(normalList.size() * NORMAL_ENTRY_SIZE);
        buffer4.order(ByteOrder.nativeOrder());
        normalBuffer = buffer4.asFloatBuffer();

        //  Create a buffer for faces
        ByteBuffer buffer2 = ByteBuffer.allocateDirect(facesList.size() * FACE_ENTRY_SIZE );
        buffer2.order(ByteOrder.nativeOrder());
        facesBuffer = buffer2.asShortBuffer();
        
        //  Create a buffer for colors
        ByteBuffer buffer3 = ByteBuffer.allocateDirect(verticesList.size() * COLOR_ENTRY_SIZE);
        buffer3.order(ByteOrder.nativeOrder());
        colorBuffer = buffer3.asFloatBuffer();

        //  Create a buffer of texture coordinates
        ByteBuffer buffer5 = ByteBuffer.allocateDirect(textureList.size() * TEXTURE_ENTRY_SIZE );
        buffer5.order(ByteOrder.nativeOrder());
        cubeTexCoordinatesBuffer = buffer5.asFloatBuffer();

        //  Fill the information positions and color
        float R = 1.0f;
        float G = 1.0f;
        float B = 1.0f;
        float A = 1.0f;
        for(String vertex: verticesList)
        {
            //  Fill vertices positions
            String[] coordinates = vertex.split(" ");
            float x = Float.parseFloat(coordinates[X_INDEX]);
            float y = Float.parseFloat(coordinates[Y_INDEX]);
            float z = Float.parseFloat(coordinates[Z_INDEX]);
            verticesBuffer.put(x);
            verticesBuffer.put(y);
            verticesBuffer.put(z);
            //  Fill color positions per vertex with constant color
            colorBuffer.put( R );
            colorBuffer.put( G );
            colorBuffer.put( B );
            colorBuffer.put( A );
        }
        verticesBuffer.position(0);
        colorBuffer.position(0);

        for( String normal : normalList)
        {
          //  Fill normal position
          String[] normals = normal.split( " ");
          float normal1 = Float.parseFloat(normals[X_INDEX]);
          float normal2 = Float.parseFloat(normals[Y_INDEX]);
          float normal3 = Float.parseFloat(normals[Z_INDEX]);

          normalBuffer.put(normal1);
          normalBuffer.put(normal2);
          normalBuffer.put(normal3);
        }
        normalBuffer.position(0);

        //  Fill the faces
        //  TODO(Madalin) : deal with complex faces that have textures and normals mapped
        //      currently they are ignored. Ex: 1/2/3 2/3/4 4/5/6 6/5/4 per line
        for( String face: facesList)
        {
            String[] vertexIndexes = face.split( " " );
            short vertex1 = Short.parseShort(vertexIndexes[X_INDEX]);
            short vertex2 = Short.parseShort(vertexIndexes[Y_INDEX]);
            short vertex3 = Short.parseShort(vertexIndexes[Z_INDEX]);
            //short normal = Short.parseShort(vertexIndexes[N_INDEX]);
            facesBuffer.put((short)(vertex1-1));
            facesBuffer.put((short)(vertex2-1));
            facesBuffer.put((short)(vertex3-1));
        }
        facesBuffer.position(0);

        mFacesNumber = facesList.size() * VALUES_PER_FACE;
        isLoaded = true;

        //  Fill the texture coordinate information
        for( String textureCoordinate: textureList)
        {
            String[] textureIndexes = textureCoordinate.split( " " );
            float textureX = Float.parseFloat(textureIndexes[X_INDEX]);
            float textureY = Float.parseFloat(textureIndexes[Y_INDEX]);
            cubeTexCoordinatesBuffer.put(textureX);
            cubeTexCoordinatesBuffer.put(textureY);
        }
        cubeTexCoordinatesBuffer.position(0);

        mTextureDataHandle = loadTexture(context, mTextureID);
    }

    public void draw(int positionHandle, int colorHandle, int normalHandle, int textureHandle)
    {
        if(isLoaded)
        {
            //  Pass in the vertices position
            GLES32.glEnableVertexAttribArray(positionHandle);
            GLES32.glVertexAttribPointer(positionHandle, VALUES_PER_VERTEX, GLES32.GL_FLOAT, false, VERTEX_ENTRY_SIZE, verticesBuffer);

            //  Pass in the color information
            GLES32.glEnableVertexAttribArray(colorHandle);
            //  TODO(ME) : See if the following method needs GL_FLOAT or GL_SHORT
            GLES32.glVertexAttribPointer(colorHandle, VALUES_PER_COLOR, GLES32.GL_FLOAT, false, COLOR_ENTRY_SIZE, colorBuffer );

            //  Pass in the normal information
            //  TODO(ME) : Check that the normals are what we expect here
            GLES32.glEnableVertexAttribArray(normalHandle);
            GLES32.glVertexAttribPointer(normalHandle, VALUES_PER_NORMAL, GLES32.GL_FLOAT, false, NORMAL_ENTRY_SIZE, normalBuffer );

            //  Pass in the texture coordinates
            GLES32.glEnableVertexAttribArray(textureHandle);
            GLES32.glVertexAttribPointer(textureHandle, VALUES_PER_TEXTURE, GLES32.GL_FLOAT, false, TEXTURE_ENTRY_SIZE, cubeTexCoordinatesBuffer );

            //  Pass in the vertices indexes in order for GL to know how to assemble the triangles
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

            //  Bind the texture int OpenGL
            GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, textureHandle[0]);

            //  Set filtering
            GLES32.glTexParameteri( GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MIN_FILTER, GLES32.GL_NEAREST);
            GLES32.glTexParameteri( GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MAG_FILTER, GLES32.GL_NEAREST);

            //  Load the bitmap int the bound texture
            GLUtils.texImage2D(GLES32.GL_TEXTURE_2D, 0, bitmap, 0);

            //  Recycle the bitmap since the data has been loaded into OpenGL
            bitmap.recycle();
        }

        if( 0 == textureHandle[0])
        {
            throw new RuntimeException("ERROR Loading texture");
        }

        return textureHandle[0];
    }
}
