package com.hgm.comunication;

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
    if(hostThreadSocket.isConnected()){
      try{
        if(isSenderActive){
          outputStream = hostThreadSocket.getOutputStream();
          objectOutputStream = new ObjectOutputStream(outputStream);
          objectOutputStream.writeObject(message);
        }
      }catch (IOException e){
        e.printStackTrace();
      }
    }
  }
}
