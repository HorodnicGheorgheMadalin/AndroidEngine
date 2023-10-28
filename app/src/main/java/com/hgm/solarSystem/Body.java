package com.hgm.solarSystem;

import java.util.Random;

import static com.hgm.solarSystem.SolarSystem.END_OF_SOLAR_SYSTEM;

public class Body
{
  public V3 position;
  public String name;

  Body( String name )
  {
    Random rand = new Random();

    this.name = name;
    this.position = new V3( rand.nextDouble()*END_OF_SOLAR_SYSTEM, rand.nextDouble()*END_OF_SOLAR_SYSTEM, rand.nextDouble()*END_OF_SOLAR_SYSTEM );
  }

  public void getPosition()
  {
    System.out.println( name + " : " + position.print() );
  }

}
