package com.hgm.comunication;

import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class SolClientConnectionThread extends Thread {
  private final int SERVER_PORT = 5056;
  //  TODO (Madalin) Take out debug server
  //  TODO (Madalin) The localhost cannot be used here since the localhost refers to the IP of the emulator and we need the actual
  //  IP of the host PC.
  private final String SERVER_HOSTNAME = "192.168.0.133";
  //private final String SERVER_HOSTNAME = "192.168.100.101";

  private int userID;
  private String userName;
  private String userPassword;

  public static Socket socket;

  public SolClientConnectionThread(int ID)
  {
    this.userID = ID;
  }

  @Override
  public void run(){
    if(socket == null){
      try{
        InetAddress serverAddress = InetAddress.getByName(SERVER_HOSTNAME);
        Log.d("SolClient", "C->Connecting...");

        socket = new Socket(serverAddress, SERVER_PORT);

        //  Start listening for responses
        SolClientListenerThread listenerThread = new SolClientListenerThread(socket);
        listenerThread.start();

        //  First thing we do is Login
        /*
        AuthenticatorToken auth = new AuthenticatorToken();
        auth.setUserName(userName);
        auth.setUserPassword(userPassword);
        SolClientSenderThread sendLogin = new SolClientSenderThread(socket, auth);
        sendLogin.start();
         */

      }catch(UnknownHostException e){
        e.printStackTrace();
      }catch(IOException e){
        e.printStackTrace();
      }

    }
  }
}
