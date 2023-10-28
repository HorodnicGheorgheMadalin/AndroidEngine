package com.hgm.solarSystem;

public class V3
{
  private final double X;
  private final double Y;
  private final double Z;

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

  public String print()
  {
    String positionString;
    positionString = "X : " + this.X + ", Y:" + this.Y + ", Z: " + this.Z;
    return positionString;
  }


}
