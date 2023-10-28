package com.hgm.comunication;

import java.io.Serializable;

public class AuthenticatorToken implements Serializable {
  private static final long serialVersionUID = -9060733222164088571L;
  private int     userID;
  private String  userName;
  private String  userPassword;

  public void setUserName(String userName){ this.userName = userName; }
  public void setUserPassword(String userPassword){ this.userPassword = userPassword; }
  public void setUserID(int userID){ this.userID = userID; }
  public int getUserID(){ return this.userID; }
  public String getUserName(){ return this.userName; }
  public String getUserPassword(){ return this.userPassword; }

}