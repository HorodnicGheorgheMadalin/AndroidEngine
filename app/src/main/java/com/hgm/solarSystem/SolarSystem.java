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
  public static int PLANET_VENUS = 1;
  public static String PLANET_VENUS_NAME = "Venus";
  public static int PLANET_EARTH = 1;
  public static String PLANET_EARTH_NAME = "Earth";

  public Star sun;
  public Planet mercury;
  public Planet venus;
  public Planet earth;
  public Planet mars;
  public Planet jupiter;
  public Planet uranus;
  public Planet saturn;
  public Planet neptune;
  public DwarfPlanet pluto;

  public SolarSystem(Context context)
  {
    this.context = context;
    sun = new Star(context, STAR_SUN, "Sun");
    //  TODO(madalin) : Create the rest of the world
    //mercury = new Planet(context, PLANET_MERCURY, "Mercury");
    //venus = new Planet(context, PLANET_MERCURY, "Venus");
    //earth = new Planet(context, PLANET_MERCURY, "Earth");
  }

  public void printObjects()
  {
    sun.getPosition();
    //  TODO(madalin) : Create the rest of the world
    //mercury.getPosition();
    //venus.getPosition();
    //earth.getPosition();
  }

}
