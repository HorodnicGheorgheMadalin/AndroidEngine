package com.hgm.androidengine;

import android.content.Context;
import android.opengl.GLES32;
import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import static android.content.ContentValues.TAG;

class ShaderProgram
{
  int m_programHandle;
  private final int m_pointProgramHandle;


  ShaderProgram(Context context)
    {
      InputStream vertexShaderStream = context.getResources().openRawResource(R.raw.vertex_shader_v4);
      InputStream fragmentShaderStream = context.getResources().openRawResource(R.raw.fragment_shader_v4);
      int m_vertexShaderHandle = setUpShader(GLES32.GL_VERTEX_SHADER, vertexShaderStream);
      int m_fragmentShaderHandle = setUpShader(GLES32.GL_FRAGMENT_SHADER, fragmentShaderStream);

      m_programHandle = createAndLinkProgram(m_vertexShaderHandle, m_fragmentShaderHandle, new String[]{"a_Position", "a_Color", "a_Normal", "a_TexCoordinate" } );

      InputStream pointVertexShaderStream = context.getResources().openRawResource(R.raw.point_vertex_shader_v1);
      InputStream pointFragmentShaderStream = context.getResources().openRawResource(R.raw.point_fragment_shader_v1);
      int m_pointVertexShaderHandle = setUpShader(GLES32.GL_VERTEX_SHADER, pointVertexShaderStream);
      int m_pointFragmentShaderHandle = setUpShader(GLES32.GL_FRAGMENT_SHADER, pointFragmentShaderStream);

      m_pointProgramHandle = createAndLinkProgram( m_pointVertexShaderHandle, m_pointFragmentShaderHandle, new String[]{ "a_Position" });
    }

    private int setUpShader( final int shaderType, InputStream shaderSource )
    {
        int shaderHandle = GLES32.glCreateShader(shaderType);

        if( 0 != shaderHandle)
        {
            String shaderCode = "";
            try
            {
                shaderCode = IOUtils.toString(shaderSource, Charset.defaultCharset());
            } catch (IOException e)
            {
                e.printStackTrace();
            }

            GLES32.glShaderSource(shaderHandle, shaderCode);
            GLES32.glCompileShader(shaderHandle);
            final int[] compileStatus = new int[1];
            GLES32.glGetShaderiv(shaderHandle, GLES32.GL_COMPILE_STATUS, compileStatus, 0);
            if(0 == compileStatus[0])
            {
                Log.e(TAG, "Error compiling shader: " + GLES32.glGetShaderInfoLog(shaderHandle));
                GLES32.glDeleteShader(shaderHandle);
                shaderHandle = 0;
            }
        }

        if(0 == shaderHandle)
        {
            throw new RuntimeException( "Error creating shader" );
        }

        return shaderHandle;

    }

    private int createAndLinkProgram(int vertexShaderHandle, int fragmentShaderHandle, final String[] attributes)
    {
        int programHandle = GLES32.glCreateProgram();

        if( 0 != programHandle )
        {
            //  Bind the vertex shader to the program
            GLES32.glAttachShader( programHandle, vertexShaderHandle);
            //  Bind the fragment shader to the program
            GLES32.glAttachShader( programHandle, fragmentShaderHandle);

            //  Bind attributes
            if( null != attributes )
            {
                final int size = attributes.length;
                for( int i = 0; i < size; i++ )
                {
                    GLES32.glBindAttribLocation( programHandle, i, attributes[i]);
                }
            }

            //  Link the two shader's together in a program
            GLES32.glLinkProgram(programHandle);

            //  Get the link status
            final int[] linkStatus = new int[1];
            GLES32.glGetProgramiv(programHandle, GLES32.GL_LINK_STATUS, linkStatus, 0);

            //  If the link status failed delete the program
            if(0 == linkStatus[0])
            {
                Log.e(TAG, "Error compiling shader program: " + GLES32.glGetProgramInfoLog(programHandle));
                GLES32.glDeleteProgram(programHandle);
                programHandle = 0;
            }
        }

        if( 0 == programHandle)
        {
            throw new RuntimeException("Error creating shader program");
        }

        return programHandle;
    }

    int getInputUniformParameter(int nProgramHandle, String variableName)
    {
      return GLES32.glGetUniformLocation(nProgramHandle, variableName);
    }

    int getInputAttributeParameter(int nProgramHandle, String variableName)
    {
      return GLES32.glGetAttribLocation(nProgramHandle, variableName);
    }

    void useShader()
    {
        GLES32.glUseProgram(m_programHandle);
    }

    int usePixelShader()
    {
        GLES32.glUseProgram(m_pointProgramHandle);
        return m_pointProgramHandle;
    }

}
