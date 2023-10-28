package com.hgm.comunication;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.hgm.androidengine.SoL;

public class SolClientHandler extends Handler {
  private static final String TAG = "SolClientHandler";
  Bundle messageData;

  @Override
  public void handleMessage(Message msg){
    super.handleMessage(msg);
    messageData = msg.getData();
    int value = messageData.getInt(SolMessagesConstants.ACTION_KEY);
    Object clientObject = messageData.getSerializable(SolMessagesConstants.DATA_KEY);

    switch(value) {

      case SolMessagesConstants.MESSAGE_RECEIVED:{
        Log.d(TAG, "MESSAGE_RECEIVED");
        break;
      }

      //  PLAYER_LOGIN_OK message
      case SolMessagesConstants.PLAYER_LOGIN_OK: {
        Log.d(TAG, "PLAYER_LOGIN_OK");
        SoL.LoginConfirmed(true);
        break;
      }

      //  PLAYER_LOGIN_FAILED message
      case SolMessagesConstants.PLAYER_LOGIN_FAILED:{
        Log.d(TAG, "PLAYER_LOGIN_FAILED");
        SoL.LoginConfirmed(false);
        break;
      }
    }

    if(clientObject instanceof AuthenticatorToken){
      int returnedvalue = ((AuthenticatorToken) clientObject).getUserID();
    }
  }

  public static void sendToServer(Object object){
    SolClientSenderThread sendGameChange = new SolClientSenderThread(SolClientConnectionThread.socket, object);
    sendGameChange.start();
  }
}