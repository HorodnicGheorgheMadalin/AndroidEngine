package com.hgm.comunication;

import static android.support.constraint.Constraints.TAG;

import android.util.Log;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SolClientSenderThread extends Thread {
  private Socket hostThreadSocket;
  Object message;
  private static boolean isSenderActive = true;

  public SolClientSenderThread(Socket socket, Object object){
    this.hostThreadSocket = socket;
    this.message = object;
  }

  @Override
  public void run(){
    OutputStream outputStream;
    ObjectOutputStream objectOutputStream;
    if(hostThreadSocket != null) {
      if (hostThreadSocket.isConnected()) {
        try {
          if (isSenderActive) {
            outputStream = hostThreadSocket.getOutputStream();
            objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(message);
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    Log.d(TAG, "NULL Host Thread Socket");
  }
}
