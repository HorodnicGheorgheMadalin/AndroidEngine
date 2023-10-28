package com.hgm.androidengine;

import android.os.Handler;

public class ProgressThread extends Thread {

  private static final int PROGRESS_INITIAL_VALUE = 0;
  private static final int PROGRESS_MAX_VALUE = 10;
  private static final int PROGRESS_INCREMENTS_VALUE = 100/PROGRESS_MAX_VALUE*1000;
  private int progressStart = PROGRESS_INITIAL_VALUE;
  private int progressEnd = PROGRESS_MAX_VALUE;
  private int progressStep = PROGRESS_INCREMENTS_VALUE;
  private int progressStatus = PROGRESS_INITIAL_VALUE;
  private Handler handler;
  private Runnable runnableProgressWork;
  private Runnable runnableFinishWork;

  ProgressThread(Handler handler, Runnable runnableStep, Runnable runnableFinish, int startValue, int endValue, int stepInMs)
  {
    this.progressStart = startValue;
    this.progressEnd = endValue;
    this.progressStep = stepInMs;
    this.progressStatus = progressStart;
    this.handler = handler;
    this.runnableProgressWork = runnableStep;
    this.runnableFinishWork = runnableFinish;
  }

  @Override
  public void run(){
    while(progressStatus < progressEnd) {
      progressStatus++;
      try {
        Thread.sleep(progressStep);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      //  Do the work defined in the GameUpdate
      handler.post(runnableProgressWork);

    }

    handler.post(runnableFinishWork);
  }
}
