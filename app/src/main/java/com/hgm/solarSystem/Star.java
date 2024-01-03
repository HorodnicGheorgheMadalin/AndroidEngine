package com.hgm.solarSystem;

import android.content.Context;

public class Star extends Body
{
  Star(Context context, int objectID, String name)
  {
    super(context, objectID, name);

    position = new V3( 0.0d, 0.0d, 0.0d );
  }

  public void getPosition()
  {
    super.getPosition();
  }
}
