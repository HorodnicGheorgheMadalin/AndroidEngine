package com.hgm.androidengine;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;

public class OpenGLGameView extends GLSurfaceView
{
    private OpenGLRenderer mRenderer;
    private float mPreviousX;
    private float mPreviousY;
    private float mDensity;

    public OpenGLGameView(Context context)
    {
        super(context);
        initView(context);
    }

    public OpenGLGameView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context)
    {
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mDensity = displayMetrics.density;

        setEGLContextClientVersion(3);
        setPreserveEGLContextOnPause(true);
        mRenderer = new OpenGLRenderer(context);
        setRenderer(mRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    public boolean onTouchEvent(MotionEvent event)
    {
        if( null != event )
        {
            float x = event.getX();
            float y = event.getY();

            if( MotionEvent.ACTION_MOVE == event.getAction())
            {
                if( null != mRenderer )
                {
                    float deltaX = (x - mPreviousX) /mDensity / 2.0f;
                    float deltaY = (y - mPreviousY) /mDensity / 2.0f;

                    mRenderer.mDeltaX += deltaX;
                    mRenderer.mDeltaY += deltaY;
                }
            }
            else if(MotionEvent.ACTION_DOWN == event.getAction())
            {
                if( null != mRenderer )
                {
                    //  We call the queueEvent bto call update on the OpenGL thread
                    queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            //  TODO( Add Click Handler)
                        }
                    });
                }
            }

            mPreviousX = x;
            mPreviousY = y;

            return true;
        }
        else
        {
            return super.onTouchEvent(null);
        }
    }

    //Hides superclass method
    public void setRenderer(OpenGLRenderer renderer, float density)
    {
        mRenderer = renderer;
        mDensity = density;
        super.setRenderer(renderer);
    }

    public void setMagFilter(final int filter){
    }

    public void setMinFilter(final int filter){
    }

}
