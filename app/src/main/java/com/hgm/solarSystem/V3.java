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

  public V3( V3 other)
  {
    this.X = other.X;
    this.Y = other.Y;
    this.Z = other.Z;
  }

  // Modifies this vector by adding the components of the 'added' vector
  public void add(V3 added)
  {
    this.X += added.GetX();
    this.Y += added.GetY();
    this.Z += added.GetZ();
  }

  // Modifies this vector by subtracting the components of the 'other' vector
  public void sub(V3 other)
  {
    this.X -= other.GetX();
    this.Y -= other.GetY();
    this.Z -= other.GetZ();
  }

  // Modifies this vector by scaling its components by 'factor'
  public void scale(double factor)
  {
    this.X *= factor;
    this.Y *= factor;
    this.Z *= factor;
  }

  // Modifies this vector to be a unit vector (length of 1)
  public void normalize()
  {
    double magnitude = Math.sqrt(X*X + Y*Y + Z*Z);
    if (magnitude != 0) {
      X /= magnitude;
      Y /= magnitude;
      Z /= magnitude;
    }
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
