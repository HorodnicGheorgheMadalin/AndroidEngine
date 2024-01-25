package com.hgm.solarSystem;

import android.content.Context;

public class SolarSystem
{
  private Context context = null;
  public static double LENGTH_UNIT           = 1.0d;
  public static double TEENS_OF              = 10.0d;
  public static double HUNDREDS_OF           = 100.0d;
  public static double THOUSANDS_OF          = 1000.0d;
  public static double TEENS_OF_CENTIMETERS  = LENGTH_UNIT;
  public static double METER                 = TEENS_OF * TEENS_OF_CENTIMETERS;
  public static double KILO_METER            = THOUSANDS_OF * METER;
  public static double KM                    = KILO_METER;
  public static double ASTRONOMICAL_UNIT     = 149598000.0d * KM;
  public static double AU                    = ASTRONOMICAL_UNIT;
  public static double END_OF_SOLAR_SYSTEM   = HUNDREDS_OF * AU;

  public static int STAR_SUN = 0;
  public static String STAR_SUN_NAME = "Sun";
  public static int PLANET_MERCURY = 1;
  public static String PLANET_MERCURY_NAME = "Mercury";
  public static int PLANET_VENUS = 2;
  public static String PLANET_VENUS_NAME = "Venus";
  public static int PLANET_EARTH = 3;
  public static String PLANET_EARTH_NAME = "Earth";

  public static String PLANET_CUBE_DEBUG = "Cube_Normal";

  public Star sun;

  public Planet mercury;
  public Planet venus;
  public Planet earth;
  // TODO(Madalin) : Expand the Solar System
  public Planet mars;
  public Planet jupiter;
  public Planet uranus;
  public Planet saturn;
  public Planet neptune;
  public DwarfPlanet pluto;

  public SolarSystem(Context context)
  {
    this.context = context;
    sun = new Star(context, STAR_SUN, STAR_SUN_NAME);
    //  TODO Init the rest of the sytem
    //mercury = new Planet(context, PLANET_MERCURY, PLANET_CUBE_DEBUG);
    //venus = new Planet(context, PLANET_VENUS, PLANET_CUBE_DEBUG);
    //earth = new Planet(context, PLANET_EARTH, PLANET_CUBE_DEBUG);
  }

  public void draw()
  {
    sun.draw();
    //  TODO Grow the rest of te system
    //mercury.getPosition();
    //venus.getPosition();
    //earth.getPosition();
  }

}
