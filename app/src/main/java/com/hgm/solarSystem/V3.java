package com.hgm.solarSystem;

public class V3
{
  private double X = 0;
  private double Y = 0;
  private double Z = 0;

  public V3( double X, double Y, double Z)
  {
    this.X = X;
    this.Y = Y;
    this.Z = Z;
  }

  public V3 add(V3 addend)
  {
    return new V3(X + addend.X, Y + addend.Y, Z + addend.Z );
  }

  public void set(double X, double Y, double Z)
  {
    this.X = X;
    this.Y = Y;
    this.Z = Z;
  }

  public String print()
  {
    String positionString;
    positionString = "X : " + this.X + ", Y:" + this.Y + ", Z: " + this.Z;
    return positionString;
  }

  public double GetX()
  {
    return this.X;
  }

  public double GetY()
  {
    return this.Y;
  }

  public double GetZ()
  {
    return this.Z;
  }
}
