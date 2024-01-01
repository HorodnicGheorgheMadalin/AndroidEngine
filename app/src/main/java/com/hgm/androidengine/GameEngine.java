package com.hgm.androidengine;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLES32;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

//  TODO(Madalin) : - Fix Crash on Orientation Change.
//                  - Fix texture for cubes.

public class GameEngine extends Activity {
  private OpenGLRenderer mRenderer;
  public static OpenGLGameView mRendererView;
  private static int MIN_DIALOG = 1;
  private static int MAG_DIALOG = 2;
  private static final String SHOWED_TOAST = "showed_toast";
  private static final String MIN_SETTING = "min_setting";
  private static final String MAG_SETTING = "max_setting";

  private int m_MinSetting;
  private int m_MagSetting;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_engine);

    mRendererView = new OpenGLGameView(this);
    setContentView(mRendererView);

    //  Show some information to the user
    if(null == savedInstanceState)
    {
      Toast.makeText(this , R.string.HELP, Toast.LENGTH_SHORT).show();
    }

    Toast.makeText(this , R.string.HELP, Toast.LENGTH_SHORT).show();

    // Restore previous settings
    if (savedInstanceState != null)
    {
      m_MinSetting = savedInstanceState.getInt(MIN_SETTING, -1);
      m_MagSetting = savedInstanceState.getInt(MAG_SETTING, -1);

      if (m_MinSetting != -1) { setMinSetting(m_MinSetting); }
      if (m_MagSetting != -1) { setMagSetting(m_MagSetting); }
    }
  }

  //  Other utilities methods
  //  The onResume method called when the game starts
  @Override protected void onResume()
  {
    super.onResume();
    mRendererView.onResume();
  }

  //  The onPause method called when the game quites
  @Override protected void onPause()
  {
    super.onPause();
    mRendererView.onPause();
  }

  @Override protected void onSaveInstanceState(Bundle outState)
  {
    super.onSaveInstanceState(outState);
    outState.putBoolean(SHOWED_TOAST, true);
    outState.putInt(MIN_SETTING, m_MinSetting);
    outState.putInt(MAG_SETTING, m_MagSetting);
  }

  private boolean isOpenGL32Supported()
  {
    final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
    final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
    final boolean supportsES32 = configurationInfo.reqGlEsVersion >= 0x300000;

    //  TODO(Me)  The above code dose not work so we overwrite the output.
    if(true)
    {
      //  Request an OpenGL ES 3.2 compatible context
      mRendererView.setEGLContextClientVersion(3);
      //  Set the renderer to out renderer
      mRendererView.setRenderer(new OpenGLRenderer(this));
      return true;
    }
    else
    {
      //  No compatible device is found so we will just exit
      //  We have no intention to create renderer that are compatible with openGL 1 or 2
      return false;
    }
  }

  private void setMinSetting(final int item)
  {
    //m_MinSetting = item;

    mRendererView.queueEvent(new Runnable()
    {
      @Override
      public void run()
      {
        final int filter;

        if (item == 0)
        {
          filter = GLES32.GL_NEAREST;
        }
        else if (item == 1)
        {
          filter = GLES32.GL_LINEAR;
        }
        else if (item == 2)
        {
          filter = GLES32.GL_NEAREST_MIPMAP_NEAREST;
        }
        else if (item == 3)
        {
          filter = GLES32.GL_NEAREST_MIPMAP_LINEAR;
        }
        else if (item == 4)
        {
          filter = GLES32.GL_LINEAR_MIPMAP_NEAREST;
        }
        else // if (item == 5)
        {
          filter = GLES32.GL_LINEAR_MIPMAP_LINEAR;
        }

        mRendererView.setMinFilter(filter);
      }
    });
  }

  private void setMagSetting(final int item)
  {
    //m_MagSetting = item;

    mRendererView.queueEvent(new Runnable()
    {
      @Override
      public void run()
      {
        final int filter;

        if (item == 0)
        {
          filter = GLES32.GL_NEAREST;
        }
        else // if (item == 1)
        {
          filter = GLES32.GL_LINEAR;
        }

        mRendererView.setMagFilter(filter);
      }
    });
  }

}
