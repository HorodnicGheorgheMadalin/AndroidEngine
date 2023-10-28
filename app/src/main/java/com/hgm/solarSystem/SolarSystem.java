package com.hgm.solarSystem;

public class SolarSystem
{
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

  public Star sun;

  SolarSystem()
  {
    sun = new Star("Sun");
  }

  public void printObjects()
  {
    sun.getPosition();
  }

}
