package com.hgm.comunication;

import android.os.AsyncTask;
import android.util.Log;

import java.io.ObjectOutputStream;

public class ConnectTask extends AsyncTask<Object, Object, SolClient> {

  @Override
  protected SolClient doInBackground(Object... input){

    SolClient client = new SolClient(new SolClient.OnMessageReceived(){
      @Override
      public void messageReceived(String message){
        publishProgress(message);
      }
    });

    client.run();


    return client;
  }

  @Override
  protected void onProgressUpdate(Object... sol){
    super.onProgressUpdate(sol);
    //response received from server
    Log.d("test", "response " + sol[0]);
  }
}
