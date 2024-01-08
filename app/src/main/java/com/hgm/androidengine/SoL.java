package com.hgm.androidengine;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.hgm.comunication.AuthenticatorToken;
import com.hgm.comunication.SolClientConnectionThread;
import com.hgm.comunication.SolClientHandler;


//  TODO(ME)  : Make text message box next step for debugging

public class SoL extends AppCompatActivity implements View.OnClickListener
{
    //  Holds the game view.
    private int m_LoginAttempts = 0;

    private static final int MAX_LOGIN_ATTEMPTS = 3;
    private static final String TAG = "SoL";
    private static final String SHOWED_TOAST = "showed_toast";

    public static SolClientHandler clientHandler;
    public static SolClientConnectionThread clientConnection;
    //  TODO(Madalin)   - Remove skipp login.
    private static boolean areWeLogged = true;

    private EditText m_userName;
    private EditText m_userPassword;

    private EditText m_userEMail;

    String m_strUserName, m_strEMail, m_strPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Log.d(TAG, "OnCreate");

        Log.d(TAG, "SetUpControls");
        setUpControls();

        Log.d(TAG, "New Client Handler");
        clientHandler = new SolClientHandler();

        Log.d(TAG, "New Client connection thread");
        clientConnection = new SolClientConnectionThread(1);
        clientConnection.start();

        if(savedInstanceState == null ){
            Log.d(TAG, "NULL saved Instance State");
        }
    }

    //  Other utilities methods
    //  The onResume method called when the game starts
    @Override protected void onResume()
    {
        super.onResume();
    }

    //  The onPause method called when the game quites
    @Override protected void onPause()
    {
        super.onPause();
    }

    @Override protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SHOWED_TOAST, true);
    }

    public static void LoginConfirmed(boolean state){
      areWeLogged = state;
    }

    public void login(View view)
    {
        m_strUserName =  m_userName.getText().toString();
        m_strEMail = m_userEMail.getText().toString();
        m_strPassword = m_userPassword.getText().toString();
    }

    private void setUpControls() {

        Button m_loginButton = findViewById(R.id.login);
      m_loginButton.setOnClickListener(this);

      m_userName = findViewById(R.id.user_name);
      m_userName.setOnClickListener(this);

      m_userPassword = findViewById(R.id.user_password);
      m_userPassword.setOnClickListener(this);

      m_userEMail = findViewById(R.id.editTextTextEmailAddress);
      m_userEMail.setOnClickListener(this);
    }

  public void onClick(View v) {
    switch(v.getId())
    {
      case (R.id.login):
      {
        Log.d(TAG, "Clicked Login Button");
        m_LoginAttempts++;

        if(m_LoginAttempts >= MAX_LOGIN_ATTEMPTS)
        {
          Toast.makeText(getApplicationContext(), "Max attempts reached", Toast.LENGTH_SHORT).show();
          break;
        }

        //  TODO(Me) make return go to the next control.
        Log.d(TAG, "Try authentication");
        AuthenticatorToken auth = new AuthenticatorToken();
        auth.setUserName(m_userName.getText().toString());
        auth.setUserPassword(m_userPassword.getText().toString());

        //ConnectTask connection = new ConnectTask();

        try{
          //  client = connection.execute(auth);
          if(clientHandler == null)
            clientHandler = new SolClientHandler();

          SolClientHandler.sendToServer(auth);
        } catch (Exception e){
          e.printStackTrace();
        }

        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

        Log.d(TAG, "Authentication Result : " + areWeLogged);
        if( areWeLogged)
        {
          Toast.makeText(getApplicationContext(), "Login success", Toast.LENGTH_SHORT).show();
          //    TODO(Madalin)  : add update context at startup
          //Intent gameUpdate = new Intent (v.getContext(), GameUpdate.class)
          // startActivity(gameUpdate);
          Intent gameEngine = new Intent (getApplicationContext(), GameEngine.class);
          startActivity(gameEngine);
        }
        else
        {
          Toast.makeText(getApplicationContext(), "Invalid username or password", Toast.LENGTH_SHORT).show();
          Toast.makeText(getApplicationContext(), m_userName.getText().toString(), Toast.LENGTH_SHORT).show();
          Toast.makeText(getApplicationContext(), m_userPassword.getText().toString(), Toast.LENGTH_SHORT).show();
        }

        login(v);

        break;
      }

      case (R.id.button_set_mag_filter):
      {
        new AlertDialog.Builder(SoL.this)
          .setTitle("Mag Filter")
          .setMessage("MAG Filter Set")
          .setPositiveButton("OK", (dialog, id) -> {
            //setMinSetting(MAG_DIALOG);
            Toast.makeText(getApplicationContext(), "MAG button pressed", Toast.LENGTH_SHORT ).show();
            //dialog.cancel();
          }).show();
        break;
      }

      case (R.id.button_set_min_filter):
      {
        new AlertDialog.Builder(SoL.this)
          .setTitle("Min Filter")
          .setMessage("MIN Filter Set")
          .setPositiveButton("OK", (dialog, id) -> {
            //setMinSetting(MIN_DIALOG);
            Toast.makeText(getApplicationContext(), "MIN button pressed", Toast.LENGTH_SHORT ).show();
            dialog.cancel();
          }).show();
        break;
      }
    }
  }

}
