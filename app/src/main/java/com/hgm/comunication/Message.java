package com.hgm.comunication;

import java.io.Serializable;

public class Message implements Serializable {

  private final String message = "";
  private final int messageID = 0;

  public Message(){
  }

  public String getText(){
    return this.messageID + this.message;
  }

}
