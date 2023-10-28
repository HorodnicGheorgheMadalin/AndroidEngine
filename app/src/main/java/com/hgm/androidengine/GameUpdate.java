package com.hgm.androidengine;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class GameUpdate extends Activity {

  private static final int PROGRESS_INITIAL_VALUE = 0;
  private static final int PROGRESS_MAX_VALUE = 10;
  private static final int PROGRESS_INCREMENTS_VALUE = 100/PROGRESS_MAX_VALUE;

  private ProgressBar     progressBar;
  private int             progressStatus = PROGRESS_INITIAL_VALUE;
  private Handler         progressHandler = new Handler();
  private TextView        progressText;
  private Intent          gameEngine;
  private Runnable        progressRunnableUpdateProgress;
  private Runnable        progressRunnableFinishProgress;
  private ProgressThread  progressThread;

  @Override
  protected void onCreate(@Nullable Bundle saveInstanceState){
    super.onCreate(saveInstanceState);
    setContentView(R.layout.activity_update);

    initializeProgress();
  }

  private void initializeProgress()
  {
    progressBar = findViewById(R.id.progress_bar);
    progressText = findViewById(R.id.progress_text);

    progressRunnableUpdateProgress = new Runnable() {
      @Override
      public void run() {
        progressStatus++;
        progressBar.setProgress(progressStatus*PROGRESS_INCREMENTS_VALUE);
        progressText.setText(progressStatus*PROGRESS_INCREMENTS_VALUE + "/" + progressBar.getMax());
      }
    };

    progressRunnableFinishProgress = new Runnable() {
      @Override
      public void run() {
        Toast.makeText(getApplicationContext(), "Progress complete", Toast.LENGTH_SHORT).show();
        Toast.makeText(getApplicationContext(), "Login success", Toast.LENGTH_SHORT).show();
        gameEngine = new Intent (getApplicationContext(), GameEngine.class);
        startActivity(gameEngine);
      }
    };

    progressThread =  new ProgressThread(progressHandler, progressRunnableUpdateProgress, progressRunnableFinishProgress,0, 10, 200);
    progressThread.start();

    /*
    new Thread(new Runnable() {
      @Override
      public void run() {
        while(m_progressStatus < PROGRESS_MAX_VALUE)
        {
          m_progressStatus++;
          //  update the progress bar display and text
          m_progressHandler.post(new Runnable() {
            @Override
            public void run() {
              m_progressBar.setProgress(m_progressStatus*PROGRESS_INCREMENTS_VALUE);
              m_progressText.setText(m_progressStatus*PROGRESS_INCREMENTS_VALUE + "/" + m_progressBar.getMax());
            }
          });
          try{
            //  Sleep for 200 milliseconds.
            Thread.sleep(200);
          }catch(InterruptedException e)
          {
            e.printStackTrace();
          }
        }
        //Toast.makeText(getApplicationContext(), "Progress complete", Toast.LENGTH_SHORT).show();
        //Toast.makeText(getApplicationContext(), "Login success", Toast.LENGTH_SHORT).show();
        gameEngine = new Intent (getApplicationContext(), GameEngine.class);
        startActivity(gameEngine);
      }
    }).start();
     */
  }
}
