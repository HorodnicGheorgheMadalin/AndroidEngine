package com.hgm.solarSystem;

import android.content.Context;

import com.hgm.androidengine.Object3D;
import com.hgm.androidengine.R;

import java.util.Random;

import static com.hgm.solarSystem.SolarSystem.END_OF_SOLAR_SYSTEM;

public class Body
{
  private Context context;
  public V3 position;
  public String name;
  private Object3D object;
  public int id;

  Body(Context context, int objectID, String name )
  {
    Random rand = new Random();
    this.context = context;
    this.name = name;
    this.id = objectID;
    object = new Object3D(context, name, R.raw.mercury_8k);
    this.position = new V3( rand.nextDouble()*END_OF_SOLAR_SYSTEM, rand.nextDouble()*END_OF_SOLAR_SYSTEM, rand.nextDouble()*END_OF_SOLAR_SYSTEM );
  }

  public void getPosition()
  {
    System.out.println( name + " : " + position.print() );
  }

}
