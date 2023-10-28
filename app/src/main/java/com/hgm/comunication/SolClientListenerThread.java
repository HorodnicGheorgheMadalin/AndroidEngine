package com.hgm.comunication;

import android.os.Bundle;
import android.os.Message;

import com.hgm.androidengine.SoL;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.Socket;

public class SolClientListenerThread extends Thread {
  private Socket socket;
  private boolean runListener;

  SolClientListenerThread(Socket socket){
    this.socket = socket;
    runListener = true;
  }

  public void KillListenerThread(boolean bKill){
    this.runListener = bKill;
  }

  @Override
  public void run(){
    try{
      while(runListener){
        ObjectInputStream objectInputStream;
        InputStream inputStream = null;
        inputStream = socket.getInputStream();
        objectInputStream = new ObjectInputStream(inputStream);
        Bundle data = new Bundle();

        Object serverObject = (Object) objectInputStream.readObject();

        if(objectInputStream != null){

          if(serverObject instanceof Integer){
            data.putInt(SolMessagesConstants.ACTION_KEY, (Integer) serverObject);
          }

          Message message = new Message();
          message.setData(data);
          SoL.clientHandler.sendMessage(message);
        }

      }
    }catch(IOException e){
      e.printStackTrace();
    }catch(ClassNotFoundException e){
      e.printStackTrace();
    }
  }
}
