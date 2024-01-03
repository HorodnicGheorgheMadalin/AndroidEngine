package com.hgm.comunication;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class SolClient{

  private boolean RUN_CLIENT = true;
  private static final String TAG = SolClient.class.getSimpleName();
  private Socket socket = null;
  private AuthenticatorToken key = new AuthenticatorToken();

  private String mServerMessage;
  private OnMessageReceived  mMessageListener = null;
  private PrintWriter mBufferOut;
  private BufferedReader mBufferIn;

  private final int SERVER_PORT = 5056;
  private final String SERVER_HOSTNAME = "192.168.100.101";
  private final String SERVER_LOCALHOST = "127.0.0.1";

  public SolClient(OnMessageReceived messageListener){
    mMessageListener = messageListener;
  }

  public void sendMessage(final String message){
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        if(mBufferOut != null){
          Log.d(TAG, "Sending: " + message);
          mBufferOut.println(message);
          mBufferOut.flush();
        }
      }
    };
    Thread clientThread = new Thread(runnable);
    clientThread.start();
  }

  public void stopClient(){

    RUN_CLIENT = false;

    if(mBufferOut != null ){
      mBufferOut.flush();
      mBufferOut.close();
    }

    mMessageListener = null;
    mBufferIn = null;
    mBufferOut = null;
    mServerMessage = null;

  }

  public void run(){
    RUN_CLIENT = true;

    try{
      InetAddress serverAddress = InetAddress.getByName(SERVER_HOSTNAME);
      Log.d("SolClient", "C->Connecting...");

      Socket socket = new Socket(serverAddress, SERVER_PORT);

      try{
        mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
        mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        while(RUN_CLIENT){
          mServerMessage = mBufferIn.readLine();
          if(mServerMessage != null && mMessageListener != null){
            mMessageListener.messageReceived(mServerMessage);
          }
        }
        Log.d("SolClient", "C->'" + mServerMessage + "'");
      }catch (Exception e){
        Log.d("SolClient", "ERROR", e);
      } finally {
        socket.close();
      }

    }catch(Exception e){
      Log.d("SolClient", "ERROR", e);
    }
  }

  public interface OnMessageReceived{
    public void messageReceived(String message);
  }

  public int Authenticate(String userName, String password) throws UnknownHostException, IOException{

    try{
      Scanner scanner = new Scanner(System.in);

      //  Socket
      //socket = new Socket("192.168.100.101", 5056);
      // for localhost debugging
      //socket = new Socket("172.0.0.1", 5056);
      //  Create socket connection
      socket = new Socket(SERVER_HOSTNAME, SERVER_PORT);
      //  Get the data streams
      DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
      DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

      while(RUN_CLIENT){
        System.out.println(dataInputStream.readUTF());
        String sending = scanner.nextLine();
        sending = "Login";

        dataOutputStream.writeUTF(sending);
        dataOutputStream.flush();

        if(sending.equals("EXIT"))
        {
          System.out.println("Client " + this.socket + " sends exit...");
          System.out.println("Closing connection.");
          socket.close();
          System.out.println("Connection closed!");
          //	Exit the loop
          break;
        }

        dataOutputStream.writeUTF(userName);
        dataOutputStream.writeUTF(password);
        dataOutputStream.flush();

        String receiving = dataInputStream.readUTF();
        System.out.println(receiving);

        if(receiving.equals("Login=OK"))
        {
          return 0;
        }
        else if(receiving.equals("Login=FAILED"))
        {
          return -1;
        }
        else
        {
          return -2;
        }

      }

      scanner.close();
      dataInputStream.close();
      dataOutputStream.close();
    }catch(Exception e){
      e.printStackTrace();
    }

    return -3;
  }


}
